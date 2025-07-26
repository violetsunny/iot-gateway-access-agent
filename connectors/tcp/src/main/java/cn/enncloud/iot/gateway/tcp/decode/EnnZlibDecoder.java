package cn.enncloud.iot.gateway.tcp.decode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.zip.Inflater;


@Slf4j
public class EnnZlibDecoder extends ByteToMessageDecoder {


    final JdkZlibDecoder decoder = new JdkZlibDecoder();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 创建 Inflater 对象
        Field inflaterField = JdkZlibDecoder.class.getDeclaredField("inflater");
        inflaterField.setAccessible(true);
        Inflater inflater = (Inflater) inflaterField.get(decoder);

        Field finished = JdkZlibDecoder.class.getDeclaredField("finished");
        finished.setAccessible(true);

        Method method = JdkZlibDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
        method.setAccessible(true);

        int i = byteBuf.readerIndex();
        log.info("接收报文：" + ByteBufUtil.hexDump(byteBuf));

        try {
            method.invoke(decoder, channelHandlerContext, byteBuf, list);
        } catch (IllegalAccessException e) {
            log.warn("zlib解析IllegalAccessException异常", e);
            byteBuf.skipBytes(byteBuf.readableBytes());
            inflater.reset();
            return;
        } catch (IllegalArgumentException e) {
            log.warn("zlib解析IllegalArgumentException异常", e);
            byteBuf.skipBytes(byteBuf.readableBytes());
            inflater.reset();
            return;
        } catch (InvocationTargetException e) {
            log.warn("zlib解析InvocationTargetException异常", e);
            byteBuf.skipBytes(byteBuf.readableBytes());
            inflater.reset();
            return;
        }

        log.info("解析后报文：" + ByteBufUtil.hexDump(byteBuf));

        // 重置 finished 标记和解压缩缓存区
        inflater.reset();
        if (!decoder.isClosed()) {
            byteBuf.readerIndex(i);
            list.clear();
        }
        finished.set(decoder, false);
        log.info("最终报文：" + ByteBufUtil.hexDump(byteBuf));
    }
}
