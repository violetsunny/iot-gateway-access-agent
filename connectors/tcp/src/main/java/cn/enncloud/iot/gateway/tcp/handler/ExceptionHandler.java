package cn.enncloud.iot.gateway.tcp.handler;

import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.PlatformDependent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    TcpSessionManger tcpSessionManger;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{} 异常处理 ", this.getClass().getName(), cause);
        Channel channel = ctx.channel();
        if(channel.isActive()) {
            ctx.close();
        }
        long usedDirectMemory = PlatformDependent.usedDirectMemory();
        log.info("直接内存占用：{}", usedDirectMemory);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        try {

            tcpSessionManger.closeSession(ctx);
        } catch (Exception e) {
            log.warn("ctx remove warning", e);
        }
    }
}