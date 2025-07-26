package cn.enncloud.iot.gateway.modbus.constant;

import lombok.Getter;

/**
 * 控制下行针对下发的值做上下限数值校验
 * 
 * @author Mr.Jia
 * @date 2022/9/20 10:31 AM
 */
@Getter
public enum NumberLimitEnum {

    /**
     * int16-数值范围：-32768 到 32767
     */
    INT16_LIMIT(-32768, 32767),

    UINT16_LIMIT(0, 65535),

    /**
     * int32-数值范围：-2,147,483,648 到 2,147,483,647
     */
    INT32_LIMIT(-2147483648, 2147483647),

    /**
     * uint32: 0 ~ 4294967295
     */
    UINT32_LIMIT(0, 4294967295L),

    /**
     * int64-数值范围：-9223372036854775808 到 9223372036854775807
     */
    INT64_LIMIT(-9223372036854775808L, 9223372036854775807L),

    /**
     * uint64: 0 ~ 18446744073709551615
     */
    UINT64_LIMIT(0, (long)Math.pow(2, 64));

    private final long minValue;
    private final long maxValue;

    NumberLimitEnum(long minValue, long maxValue) {
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
