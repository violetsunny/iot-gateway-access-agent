package cn.enncloud.iot.gateway.tcp.parser;

import java.util.Arrays;

public enum ParserType {

    DIRECT,

    /**
     * 固定长度解码器
     */
    FIXED_LENGTH,


    /**
     * 长度字段解码器
     */
    LENGTH_FIELD,

    /**
     * 基于分隔符的解码器
     */
    DELIMITER,

    /**
     * json对象
     */
    JSON_OBJECT,

    /**
     * xml
     */
    XML,

    /**
     * line
     */
    LINE,

    /**
     * zlib解码器
     */
    ZLIB;


    public static ParserType parse(String val) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(val))
                .findFirst()
                .orElse(null);
    }
}
