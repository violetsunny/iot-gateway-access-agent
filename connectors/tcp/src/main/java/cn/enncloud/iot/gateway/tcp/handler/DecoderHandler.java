package cn.enncloud.iot.gateway.tcp.handler;

import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.MetricCloudCallResponse;
import cn.enncloud.iot.gateway.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 解码器
 *
 * @author hanyilong@enn.cn
 */
@Slf4j
@Data
public class DecoderHandler extends ByteToMessageDecoder {
    Protocol protocol;


    public DecoderHandler(Protocol protocol) {
        this.protocol = protocol;

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 标记一些当前的readIndex
        in.markReaderIndex();
        int length = in.readableBytes();
        if(log.isDebugEnabled()){
            log.debug("receive msg <<< {}", ByteBufUtil.hexDump(in));
        }
        byte[] messageBytes;
        if (in.hasArray()) {
            ByteBuf slice = in.slice();
            messageBytes = slice.array();
            int readLength = slice.readableBytes();
            in.readerIndex(in.readerIndex()+readLength);
        } else {
            messageBytes = new byte[length];
            in.readBytes(messageBytes, 0, length);
        }
        List<? extends Message> messages = protocol.decodeMulti(messageBytes);
        if(messages == null || messages.isEmpty()){
            return;
        }
        messages.forEach(msg -> {
            try {
                sendResponse(ctx.channel(), msg.getResponse());
            } catch (InterruptedException e) {
                log.error("send response was interrupted", e);
                Thread.currentThread().interrupt();
            }
            out.add(msg);
        });
    }



    private void sendResponse(Channel channel, String hex) throws InterruptedException {
        if(hex != null){
            Message res = new MetricCloudCallResponse();
            res.setResponse(hex);
            if(channel == null){
                return;
            }
            if(!channel.isActive()){
                return;
            }
            if(channel.isWritable()){
                channel.writeAndFlush(res)
                        .addListener(cf -> {
                            if(cf.isSuccess()){
                                log.info("send response success, {}",  hex);
                            }else {
                                log.error("send response failed, " + hex + ",", cf.cause());
                            }
                        });
            }else{
                channel.writeAndFlush(res)
                        .sync()
                        .addListener(cf -> {
                            if(cf.isSuccess()){
                                log.info("send response success, {}",  hex);
                            }else {
                                log.error("send response failed, " + hex + ",", cf.cause());
                            }
                        });
            }
        }
    }
}
