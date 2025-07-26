package cn.enncloud.iot.gateway.modbus.poll.tcp;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.PlatformDependent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {


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


}