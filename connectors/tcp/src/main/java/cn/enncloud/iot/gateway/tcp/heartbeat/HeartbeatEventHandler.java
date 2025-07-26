package cn.enncloud.iot.gateway.tcp.heartbeat;

import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatEventHandler extends ChannelInboundHandlerAdapter {



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("new tcp connect, remote: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            handleIdleStateEvent(ctx, (IdleStateEvent) evt);
        }
        ctx.fireUserEventTriggered(evt);
    }


    /**
     * 处理读写事件
     * 服务端关注读操作，用于检测客户端心跳
     * 客户端关注写操作，触发发送心跳动作
     *
     * @param ctx channel上下文
     * @param idleStateEvent idle事件
     * @throws Exception 异常
     */
    private void handleIdleStateEvent(ChannelHandlerContext ctx, IdleStateEvent idleStateEvent) throws Exception {
        switch (idleStateEvent.state()){
            case READER_IDLE:
                log.warn("heartbeat timeout, close remote connection, {}", ctx.channel().remoteAddress());
                ctx.channel().close();
                break;
            case WRITER_IDLE:
                Message message = new Message();
                message.setMessageType(MessageType.HEARTBEAT);
                sendHeartbeat(ctx.channel(), message);
                break;
            case ALL_IDLE:
            default:
                log.warn("unsupported all idle");
        }
    }



    private void sendHeartbeat(Channel channel, Message message) throws InterruptedException {
        if(channel.isActive()){
            if(channel.isWritable()){
                channel.writeAndFlush(message)
                        .addListener(cf -> {
                            if(cf.isSuccess()){
                                log.debug("heartbeat send success, {}", message);
                            }else {
                                log.error("heartbeat send error", cf.cause());
                            }
                        });
            }else{
                channel.writeAndFlush(message)
                        .sync()
                        .addListener(cf -> {
                            if(cf.isSuccess()){
                                log.debug("heartbeat send success, {}", message);
                            }else {
                                log.error("heartbeat send error", cf.cause());
                            }
                        });
            }
        }else{
            log.warn("channel inactive, heartbeat send failed");
        }
    }
}
