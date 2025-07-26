package cn.enncloud.iot.gateway.tcp.handler;


import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hanyilong@enn.cn
 * @since 2022-02-09 10:57:16
 */
@Slf4j
@Data
public class EncodeHandler extends MessageToByteEncoder {
    Protocol protocol;

    public EncodeHandler(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 发送字节码消息
        if(!(msg instanceof Message)){
            return;
        }
        byte[] bytes = protocol.encode((Message) msg);
        if(bytes == null){
            return;
        }
        out.writeBytes(bytes);
    }


}
