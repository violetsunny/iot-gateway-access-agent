package cn.enncloud.iot.gateway.modbus.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数据格式
 *
 * @author Mr.Jia
 * @date 2021/11/19 17:27
 */
@Getter
public enum ByteOrderEnum {
    /**
     * Big-endian <br/>
     * Swap Mode: N/A
     */
    ABCD("1234", false, false),
    /**
     * Big-endian byte swap <br/>
     * Swap Mode: byte swap
     */
    BADC("2143", true, false),
    /**
     * Little-endian byte swap <br/>
     * Swap Mode: word swap
     */
    CDAB("3412", false, true),
    /**
     * Little-endian <br/>
     * Swap Mode: byte and word swap
     */
    DCBA("4321", true, true),

    ABCDEFGH("12345678", false, false),
    BADCFEHG("21436587", true, false),
    GHEFCDAB("78563412", false, true),
    HGFEDCBA("87654321", true, true);
    private final String value;
    /**
     * 是否是字节交换
     */
    private final boolean byteSwap;
    /**
     * 是否是字交换
     */
    private final boolean wordSwap;

    ByteOrderEnum(String value, boolean byteSwap, boolean wordSwap) {
        this.value = value;
        this.byteSwap = byteSwap;
        this.wordSwap = wordSwap;
    }

    /**
     * 以测点的第一个非空字节序作为该设备的字节序，都为空时默认3412
     *
     * @return com.enn.iot.dtu.protocol.modbus.constant.ByteOrderEnum
     * @author Mr.Jia
     * @date 2021/11/26 11:05
     */
    public static ByteOrderEnum getDefault() {
        return CDAB;
    }

    public static ByteOrderEnum getInstance(String byteOrder) {

        ByteOrderEnum byteOrderEnum = Arrays.stream(ByteOrderEnum.values())
                .filter(orderEnum -> orderEnum.value.equals(byteOrder))
                .findAny().orElse(null);
        if (Objects.isNull(byteOrderEnum)) {
            throw new IllegalArgumentException("未知的字节序，byteOrder:" + byteOrder);
        }
        return byteOrderEnum;
//        switch (byteOrder) {
//            case "1234":
//                return ABCD;
//            case "2143":
//                return BADC;
//            case "3412":
//                return CDAB;
//            case "4321":
//                return DCBA;
//            default:
//                throw new IllegalArgumentException("未知的字节序，byteOrder:" + byteOrder);
//        }
    }

    public static boolean validate(String byteOrder) {
        if (StringUtils.isBlank(byteOrder)) {
            return false;
        }
        ByteOrderEnum byteOrderEnum = Arrays.stream(ByteOrderEnum.values())
                .filter(orderEnum -> orderEnum.value.equals(byteOrder))
                .findAny().orElse(null);

        return Objects.nonNull(byteOrderEnum);

//        switch (byteOrder) {
//            case "1234":
//            case "2143":
//            case "3412":
//            case "4321":
//                return true;
//            default:
//                return false;
//        }
    }
}
