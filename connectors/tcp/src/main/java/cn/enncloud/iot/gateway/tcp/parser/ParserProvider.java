package cn.enncloud.iot.gateway.tcp.parser;

import cn.enncloud.iot.gateway.tcp.decode.EnnZlibDecoder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.xml.XmlFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class ParserProvider {

    /**
     * 创建TCP编解码组件
     *
     * @param parserType   解码器类型
     * @param parserConfig 解码器配置
     * @return 粘包拆包器
     */
    public static Optional<ChannelHandler> newFrameDecoder(String parserType, Map<String, Object> parserConfig) {
        ParserType type = ParserType.parse(parserType);
        JSONObject config = (JSONObject) JSON.toJSON(parserConfig);
        ChannelHandler parser = null;
        switch (type) {
            case DIRECT:
                break;
            case DELIMITER:
                int maxLength = config.getInteger("maxLength");
                // 分隔符为十六进制字符串
                String delimiter = config.getString("delimiter");
                parser = newDelimiterBasedFrameDecoder(maxLength, delimiter);
                break;
            case LENGTH_FIELD:
                int lengthFieldMaxLen = config.getIntValue("maxLength");
                int lengthFieldOffset = config.getIntValue("lengthFieldOffset");
                int lengthFiledLength = config.getIntValue("lengthFieldLength");
                int lengthAdjustment = config.getIntValue("lengthAdjustment");
                int initialBytesToStrip = config.getIntValue("initialBytesToStrip");
                String byteOrderParam = (String) config.getOrDefault("byteOrder", "BIG_ENDIAN");
                ByteOrder byteOrder = byteOrderParam.equals(ByteOrder.BIG_ENDIAN.toString()) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                parser = newLengthFieldBasedFrameDecoder(byteOrder, lengthFieldMaxLen, lengthFieldOffset, lengthFiledLength, lengthAdjustment, initialBytesToStrip);
                break;
            case JSON_OBJECT:
                parser = newJsonObjectDecoder();
                break;
            case FIXED_LENGTH:
                int frameLength = config.getIntValue("frameLength");
                parser = newFixedLengthFrameDecoder(frameLength);
                break;
            case XML:
                int xmlMaxLen = config.getIntValue("maxLength");
                parser = new XmlFrameDecoder(xmlMaxLen);
                break;
            case LINE:
                int lineMaxLen = config.getIntValue("maxLength");
                parser = new LineBasedFrameDecoder(lineMaxLen);
                break;
            case ZLIB:

                parser = new EnnZlibDecoder();

                break;
            default:
                log.error("unsupported tcp parse type: " + parserType);
        }
        return Optional.ofNullable(parser);
    }


    /**
     * 创建分隔符解码器
     *
     * @param maxLength 包最大长度
     * @param delimiter 分隔符，十六进制字符串
     * @return DelimiterBasedFrameDecoder
     */
    public static DelimiterBasedFrameDecoder newDelimiterBasedFrameDecoder(int maxLength, String delimiter) {
        ByteBuf delimiterByteBuf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(delimiter));
        return new DelimiterBasedFrameDecoder(maxLength, delimiterByteBuf);
    }


    /**
     * 创建长度字段解码器
     *
     * @param maxLength         最大长度
     * @param lengthFieldOffset 长度字段起始位置
     * @param lengthFiledLength 长度字段长度
     * @return LengthFieldBasedFrameDecoder
     */
    public static LengthFieldBasedFrameDecoder newLengthFieldBasedFrameDecoder(ByteOrder byteOrder,
                                                                               int maxLength,
                                                                               int lengthFieldOffset,
                                                                               int lengthFiledLength,
                                                                               int lengthAdjustment,
                                                                               int initialBytesToStrip) {
        return new LengthFieldBasedFrameDecoder(
                byteOrder,
                maxLength,
                lengthFieldOffset,
                lengthFiledLength,
                lengthAdjustment,
                initialBytesToStrip,
                true);
    }


    /**
     * 创建json对象解码器
     *
     * @return JsonObjectDecoder
     */
    public static JsonObjectDecoder newJsonObjectDecoder() {
        return new JsonObjectDecoder();
    }


    /**
     * 创建固定长度解码器
     *
     * @param frameLength 包长度
     * @return FixedLengthFrameDecoder
     */
    public static FixedLengthFrameDecoder newFixedLengthFrameDecoder(int frameLength) {
        return new FixedLengthFrameDecoder(frameLength);
    }
}
