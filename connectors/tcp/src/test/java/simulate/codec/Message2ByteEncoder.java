package simulate.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author hanyilong@enn.cn
 * @description: 编码
 * @date 2018-12-27 15:09
 *
 */
public class Message2ByteEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes((byte[]) msg);
        ctx.writeAndFlush(byteBuf);
        byteBuf.release();
    }
}
