package cn.enncloud.iot.gateway.modbus.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据格式
 *
 * @author Mr.Jia
 * @date 2021/11/19 17:27
 */
@Getter
public enum DataTypeEnum {
    /**
     *
     */
    BIT0("bit0", 1),
    /**
     *
     */
    BIT1("bit1", 1),
    /**
     *
     */
    BIT2("bit2", 1),
    /**
     *
     */
    BIT3("bit3", 1),
    /**
     *
     */
    BIT4("bit4", 1),
    /**
     *
     */
    BIT5("bit5", 1),
    /**
     *
     */
    BIT6("bit6", 1),
    /**
     *
     */
    BIT7("bit7", 1),
    /**
     *
     */
    BIT8("bit8", 1),
    /**
     *
     */
    BIT9("bit9", 1),
    /**
     *
     */
    BIT10("bit10", 1),
    /**
     *
     */
    BIT11("bit11", 1),
    /**
     *
     */
    BIT12("bit12", 1),
    /**
     *
     */
    BIT13("bit13", 1),
    /**
     *
     */
    BIT14("bit14", 1),
    /**
     *
     */
    BIT15("bit15", 1),
    /**
     *
     */
    INT8("int8", 1),
    /**
     *
     */
    UINT8("uint8", 1),
    /**
     *
     */
    INT16("int16", 1),
    /**
     *
     */
    UINT16("uint16", 1),
    /**
     *
     */
    INT24("int24", 2),
    /**
     *
     */
    UINT24("uint24", 2),
    /**
     *
     */
    INT32("int32", 2),
    /**
     *
     */
    UINT32("uint32", 2),
    /**
     *
     */
    INT48("int48", 3),
    /**
     *
     */
    UINT48("uint48", 3),
    /**
     *
     */
    INT64("int64", 4),
    /**
     *
     */
    UINT64("uint64", 4),
    /**
     *
     */
    FLOAT32("float32", 2),
    /**
     *
     */
    FLOAT64("float64", 4),
    /**
     *
     */
    TIME48("time48", 3),
    /**
     *
     */
    TIME64("time64", 4);


    private final String value;
    private final Integer byteNum;

    DataTypeEnum(String value, Integer byteNum) {
        this.value = value;
        this.byteNum = byteNum;
    }

    public static DataTypeEnum getInstance(String dataType) {
        switch (dataType) {
            case "bit0":
                return BIT0;
            case "bit1":
                return BIT1;
            case "bit2":
                return BIT2;
            case "bit3":
                return BIT3;
            case "bit4":
                return BIT4;
            case "bit5":
                return BIT5;
            case "bit6":
                return BIT6;
            case "bit7":
                return BIT7;
            case "bit8":
                return BIT8;
            case "bit9":
                return BIT9;
            case "bit10":
                return BIT10;
            case "bit11":
                return BIT11;
            case "bit12":
                return BIT12;
            case "bit13":
                return BIT13;
            case "bit14":
                return BIT14;
            case "bit15":
                return BIT15;
            case "int8":
                return INT8;
            case "uint8":
                return UINT8;
            case "int16":
                return INT16;
            case "uint16":
                return UINT16;
            case "int32":
                return INT32;
            case "uint32":
                return UINT32;
            case "int48":
                return INT48;
            case "int64":
                return INT64;
            case "uint64":
                return UINT64;
            case "float32":
                return FLOAT32;
            case "float64":
                return FLOAT64;
            case "time48":
                return TIME48;
            case "time64":
                return TIME64;

            default:
                throw new IllegalArgumentException("未知的数据类型，dataType:" + dataType);
        }
    }

    public static boolean validate(String dataType) {
        if (StringUtils.isBlank(dataType)) {
            return false;
        }
        switch (dataType) {
            case "bit0":
            case "bit1":
            case "bit2":
            case "bit3":
            case "bit4":
            case "bit5":
            case "bit6":
            case "bit7":
            case "bit8":
            case "bit9":
            case "bit10":
            case "bit11":
            case "bit12":
            case "bit13":
            case "bit14":
            case "bit15":
            case "int8":
            case "uint8":
            case "int16":
            case "uint16":
            case "int32":
            case "uint32":
            case "int48":
            case "int64":
            case "uint64":
            case "float32":
            case "float64":
            case "time48":
            case "time64":
                return true;
            default:
                return false;
        }
    }

    /**
     * 默认数据类型UINT16
     *
     * @author Mr.Jia
     * @date 2021/11/26 11:05
     * @return com.enn.iot.dtu.protocol.modbus.constant.ByteOrderEnum
     */
    public static DataTypeEnum getDefault() {
        return UINT16;
    }
}
