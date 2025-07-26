package cn.enncloud.iot.gateway.tcp.handler;

import cn.enncloud.iot.gateway.config.connectors.TcpClientConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    private final TcpClientConfig config;

    private ScheduledFuture<?> scheduledFuture;

    public ReconnectHandler(TcpClientConfig config) {
        this.config = config;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        reconnect(ctx);
        ctx.fireChannelInactive();
    }


    private void reconnect(ChannelHandlerContext ctx){
        long reconnectInterval = config.getReconnectInterval();
        if(reconnectInterval == 0L){
            return;
        }
        Channel channel = ctx.channel();
        scheduledFuture = ctx.channel()
                .eventLoop()
                .scheduleAtFixedRate(() -> {
                    InetSocketAddress remoteSocketAddress = new InetSocketAddress(config.getRemoteAddress(), config.getRemotePort());
                    channel.connect(remoteSocketAddress)
                            .addListener(cf -> {
                                if(cf.isSuccess() && scheduledFuture != null){
                                    scheduledFuture.cancel(false);
                                }
                            });
                }, reconnectInterval, reconnectInterval, TimeUnit.SECONDS);
    }
}
