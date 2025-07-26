package cn.enncloud.iot.gateway.tcp;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.config.connectors.TcpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.tcp.handler.DecoderHandler;
import cn.enncloud.iot.gateway.tcp.handler.EncodeHandler;
import cn.enncloud.iot.gateway.tcp.handler.ExceptionHandler;
import cn.enncloud.iot.gateway.tcp.handler.MessageHandler;
import cn.enncloud.iot.gateway.tcp.heartbeat.HeartbeatEventHandler;
import cn.enncloud.iot.gateway.tcp.heartbeat.HeartbeatHandlerProvider;
import cn.enncloud.iot.gateway.tcp.parser.ParserProvider;
import cn.enncloud.iot.gateway.tcp.process.LoginProcesser;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TcpConnectorServer implements Connector {

    private static final long DEFAULT_ADD_PROTOCOL_TIMEOUT = 5000L;

    private final TcpServerConfig config;

    private final ProtocolManager protocolManager;

    private final DeviceContext deviceContext;

    private final TcpSessionManger tcpSessionManger;

    private final LoginProcesser loginProcesser;

    private final ExceptionHandler exceptionHandler;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel channel;


    public TcpConnectorServer(TcpServerConfig config,
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
        String host = config.getAddress();
        int port = config.getPort();
        assert port > 0 && port < 65535;
        bossGroup = new NioEventLoopGroup(config.getBossGroupThread());
        workerGroup = new NioEventLoopGroup(config.getWorkerGroupThread());
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        Protocol protocol = protocolManager.register(
                config.getProtocol(),
                workerGroup.next(),
                DEFAULT_ADD_PROTOCOL_TIMEOUT);
        if(protocol == null){
            log.error("protocol add failed");
            return;
        }
        channel = serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                // ByteBuf 分配器
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        Protocol protocol = protocolManager.get(config.getProtocol().getName());
                        if(protocol == null){
                            log.warn("protocol add failed, tcp server start failed");
                            return;
                        }
                        protocol.setDeviceContext(deviceContext);
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LoggingHandler(config.getLogLevel()));
                        ParserProvider.newFrameDecoder(config.getParserType(), config.getParserParams())
                                .ifPresent(pipeline::addLast);
                        HeartbeatHandlerProvider.newIdleStateHandler(config.getHeartbeatInterval(), IdleState.READER_IDLE)
                                .ifPresent(pipeline::addLast);
                        pipeline.addLast(new EncodeHandler(protocol));
                        pipeline.addLast(new DecoderHandler(protocol));
                        pipeline.addLast(new HeartbeatEventHandler());
                        pipeline.addLast(new MessageHandler(protocol, tcpSessionManger, loginProcesser));
                        pipeline.addLast(exceptionHandler);
                    }
                })
                .bind(host, port)
                .addListener((ChannelFutureListener) channelFuture -> {
                    if(channelFuture.isSuccess()){
                        log.info("tcp server start success, {}:{}", host, port);
                    }else{
                        log.error("tcp server " + host + ":" + port + " start error", channelFuture.cause());
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
    public void stop(){
        if(bossGroup != null){
            bossGroup.shutdownGracefully()
                    .addListener(cf -> {
                        if(!cf.isSuccess()){
                            log.error("tcp server " + config.getAddress() + ":" + config.getPort() + "stop error", cf.cause());
                        }
                    });
        }
        if(workerGroup != null){
            workerGroup.shutdownGracefully()
                    .addListener(cf -> {
                        if(!cf.isSuccess()){
                            log.error("tcp server " + config.getAddress() + ":" + config.getPort() + "stop error", cf.cause());
                        }
                    });
        }
        log.info("tcp server {}:{} stopped", config.getAddress(), config.getPort());
    }
}
