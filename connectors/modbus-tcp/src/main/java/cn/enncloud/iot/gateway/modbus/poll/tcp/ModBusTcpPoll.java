package cn.enncloud.iot.gateway.modbus.poll.tcp;

import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.core.responses.ModbusResponse;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusMark;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusSwitch;
import cn.enncloud.iot.gateway.modbus.poll.tcp.codec.MessageCodec;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class ModBusTcpPoll implements Runnable {
    private final AtomicInteger flag = new AtomicInteger(ModbusMark.MODBUS_FLAG);
    private CompletableFuture<Channel> future = new CompletableFuture<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Consumer<ModbusResponse> resp;
    private final Consumer<ChannelFuture> close;
    private final Consumer<ChannelFuture> readTask;
    private final ModbusTcpConfig config;

    //重试次数
    AtomicInteger retries = new AtomicInteger(0);
    //连接状态
    private final AtomicBoolean connected = new AtomicBoolean(false);
    //是否有重试
    private final AtomicBoolean retry = new AtomicBoolean(false);

    AtomicReference<String> ModbusStatus = new AtomicReference<>(ModbusSwitch.OFF.name());

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public ModBusTcpPoll(ModbusTcpConfig config, Consumer<ModbusResponse> consumer, Consumer<ChannelFuture> close, Consumer<ChannelFuture> readTask) {
        this.close = close;
        this.resp = consumer;
        this.readTask = readTask;
        this.config = config;
        this.init();
    }

    private void init() {
        this.config.getBootstrap().group(config.getGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        ch.pipeline().addLast(new MessageCodec());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof ModbusResponse) {
                                    resp.accept((ModbusResponse<byte[]>) msg);
                                }
                            }
                        });
                        ch.pipeline().addLast(new ExceptionHandler());
                    }
                });
    }

    @Override
    public void run() {
        config.getBootstrap()
                .connect(config.getAddress(), config.getPort())
                .addListener((ChannelFuture channelFuture) -> {
                    if (channelFuture.isSuccess()) {
                        log.info("TCP连接连接成功,id:{},host:{},port:{}",config.getConfigId(), config.getAddress(), config.getPort());
                        channelFuture.channel().closeFuture().addListener((ChannelFuture channel) -> {
                            log.info("TCP连接连接断开,id:{},host:{},port:{}", config.getConfigId(),config.getAddress(), config.getPort());
                            disConnect();
                            close.accept(channel);
                            connected.set(false);
                            scheduleReconnect();
                        });
                        future = new CompletableFuture<>();
                        future.complete(channelFuture.channel());
                        ModbusStatus.set(ModbusSwitch.ON.name());
                        connected.set(true);
                        retry.set(false);
                        // 点表读取任务
                        readTask.accept(channelFuture);
                        retries = new AtomicInteger(0);
                    } else {
                        log.warn("TCP连接连接失败,id:{},host:{},port:{}", config.getConfigId(),config.getAddress(), config.getPort(),channelFuture.cause());
                        future.completeExceptionally(channelFuture.cause());
                        connected.set(false);
                        scheduleReconnect();
                    }
                });
    }

    public void connect() {
        config.getExecutor().execute(this);

    }

    private void disConnect() throws ExecutionException, InterruptedException {
        future.get().close();
    }

    public void shutdownClient() throws InterruptedException, ExecutionException {
        // 关闭连接
        future.get().close();
        // 关闭eventLoop
        config.getGroup().shutdownGracefully();
        // 关闭重连线程池
        executorService.shutdown();
        // FullGc
        System.gc();
    }

    public Boolean isOpen() throws ExecutionException, InterruptedException {
        return future.get().isOpen();
    }

    public Integer send(ModbusRequest req) throws ExecutionException, InterruptedException {
        if (!isOpen()) {
            log.warn("modbus client is reconnecting ,id:{},host:{},port:{}", config.getConfigId(), config.getAddress(), config.getPort());
            return null;
        }
        if(req == null){
            log.warn("req is null ,id:{},host:{},port:{}",config.getConfigId(), config.getAddress(), config.getPort());
            return null;
        }
        req.setFlag(ModbusMark.flag(flag));
        future.get().writeAndFlush(req);
        log.info("id:{},host:{},port:{},下发指令事务id：{}",config.getConfigId(), config.getAddress(), config.getPort(),req.getFlag());
        return req.getFlag();
    }

    public String getStatus() {
        return ModbusStatus.get();
    }


    private void scheduleReconnect() {
//        if (retry.get()) {
//            log.warn("TCP连接正在重连,id:{},host:{},port:{}",config.getConfigId(), config.getAddress(), config.getPort());
//            return;
//        }
        retry.set(true);
        ModbusStatus.set(ModbusSwitch.RETRY.name());
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if(connected.get()){
                    return;
                }
                if (retries.getAndIncrement() >= config.getMaxRetries()) {
                    log.warn("TCP连接重连失败,id:{},host:{},port:{}",config.getConfigId(), config.getAddress(), config.getPort());
                    ModbusStatus.set(ModbusSwitch.OFF.name());
                    try {
                        shutdownClient();
                    } catch (InterruptedException | ExecutionException e) {
                        log.warn("TCP连接重连失败,id:{},host:{},port:{}", config.getConfigId(),config.getAddress(), config.getPort(),e);
                    }
                } else {
                    log.warn("id:{},host:{},port:{} 正在进行第{}次TCP连接重连",config.getConfigId(), config.getAddress(), config.getPort(), retries.get());
                    connect(); // 重新连接服务器
                }
            }
        }, config.getRetryDelay(), TimeUnit.SECONDS);
    }
}
