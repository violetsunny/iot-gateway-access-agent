package cn.enncloud.iot.gateway.tcp;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.config.connectors.TcpClientConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.tcp.handler.DecoderHandler;
import cn.enncloud.iot.gateway.tcp.handler.EncodeHandler;
import cn.enncloud.iot.gateway.tcp.handler.ExceptionHandler;
import cn.enncloud.iot.gateway.tcp.handler.MessageHandler;
import cn.enncloud.iot.gateway.tcp.handler.ReconnectHandler;
import cn.enncloud.iot.gateway.tcp.heartbeat.HeartbeatEventHandler;
import cn.enncloud.iot.gateway.tcp.heartbeat.HeartbeatHandlerProvider;
import cn.enncloud.iot.gateway.tcp.parser.ParserProvider;
import cn.enncloud.iot.gateway.tcp.process.LoginProcesser;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TcpConnectionClient implements Connector {


    private final TcpClientConfig config;

    private final ProtocolManager protocolManager;

    private final DeviceContext deviceContext;

    private final TcpSessionManger tcpSessionManger;

    private final LoginProcesser loginProcesser;

    private final ExceptionHandler exceptionHandler;

    private EventLoopGroup workerGroup;

    private Channel channel;

    private String protocolId;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean retry = new AtomicBoolean(false);

    public TcpConnectionClient(TcpClientConfig config,
                               ProtocolManager protocolManager,
                               DeviceContext deviceContext,
                               TcpSessionManger tcpSessionManger,
                               LoginProcesser loginProcesser,
                               ExceptionHandler exceptionHandler) {
        this.config = config;
        this.protocolManager = protocolManager;
        this.deviceContext = deviceContext;
        this.tcpSessionManger = tcpSessionManger;
        this.loginProcesser = loginProcesser;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void init() throws Exception {
        String remoteAddress = config.getRemoteAddress();
        int remotePort = config.getRemotePort();
        workerGroup = new NioEventLoopGroup(config.getWorkerGroupThread());
        protocolId = UUID.randomUUID().toString();
        Protocol protocol = protocolManager.register(config.getProtocol());
        channel = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                .option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LoggingHandler(config.getLogLevel()));
                        ParserProvider.newFrameDecoder(config.getParserType(), config.getParserParams())
                                .ifPresent(pipeline::addLast);
                        protocol.setDeviceContext(deviceContext);
                        HeartbeatHandlerProvider.newIdleStateHandler(config.getHeartbeatInterval(), IdleState.WRITER_IDLE)
                                .ifPresent(pipeline::addLast);
                        pipeline.addLast(new ReconnectHandler(config));
                        pipeline.addLast(new DecoderHandler(protocol));
                        pipeline.addLast(new EncodeHandler(protocol));
                        pipeline.addLast(new HeartbeatEventHandler());
                        pipeline.addLast(new MessageHandler(protocol, tcpSessionManger, loginProcesser));
                        pipeline.addLast(exceptionHandler);
                    }
                })
                .remoteAddress(remoteAddress, remotePort)
                .connect()
                .addListener(cf -> {
                    if (cf.isSuccess()) {
                        connected.set(true);
                        retry.set(false);
                        log.info("tcp client connect remote {}:{} success", remoteAddress, remotePort);
                    } else {
                        connected.set(false);
                        log.error("tcp client connect remote {}:{} failed", remoteAddress, remotePort, cf.cause());
                        // 重连
                        reconnect();
                    }
                })
                .channel();
    }

    @Override
    public void setupProtocol(Protocol protocol, Map<String, Object> params) {

    }

    @Override
    public Map<String, Object> getStatus() {
        return null;
    }

    @Override
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully()
                    .addListener(cf -> {
                        if (!cf.isSuccess()) {
                            log.error("tcp client {}:{} stop error", config.getRemoteAddress(), config.getRemotePort(), cf.cause());
                        }
                    });
        }
        log.info("tcp server {}:{} stopped", config.getRemoteAddress(), config.getRemotePort());
    }

    /**
     * 重连
     */
    private void reconnect() {
        long reconnectInterval = config.getReconnectInterval();
        if(reconnectInterval == 0L){
            return;
        }
//        if(retry.get()){
//            log.info("tcp server {}:{} reconnected now", config.getRemoteAddress(), config.getRemotePort());
//            return;
//        }
        retry.set(true);
        workerGroup.next().schedule(() -> {
            if(connected.get()){
                return;
            }
            try {
                log.info("tcp server {}:{} reconnected", config.getRemoteAddress(), config.getRemotePort());
                this.init();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, reconnectInterval, TimeUnit.SECONDS);
    }
}
