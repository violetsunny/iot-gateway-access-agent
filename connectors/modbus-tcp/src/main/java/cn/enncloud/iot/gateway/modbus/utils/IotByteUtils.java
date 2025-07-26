package cn.enncloud.iot.gateway.modbus.utils;

import cn.enncloud.iot.gateway.modbus.constant.ByteOrderEnum;
import cn.enncloud.iot.gateway.modbus.constant.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.math.BigInteger;

/**
 * @author lixiang
 * @date 2021/11/24
 **/
public class IotByteUtils {

    public static int calculateBytesNumberForStatusRegion(int booleanNumber) {
        return (booleanNumber + 7) / 8;
    }

    /**
     * 解析01、02 功能码
     *
     * @param byteBuf    报文内容
     * @param startIndex 本次想解析的线圈在报文中的字节下标
     * @param bitIndex   所在字节的第几位
     * @return java.lang.Number
     * @author Mr.Jia
     * @date 2022/1/20 19:00
     */
    public static Number getBooleanFromStatusRegion(ByteBuf byteBuf, int startIndex, int bitIndex) {
        return byteBuf.getByte(startIndex) >> bitIndex & 0x01;
    }

    /**
     * 在03、04 功能码场景中，根据数据类型获取寄存器个数。
     * <p>
     * 一个寄存器等于2个字节
     *
     * @param dataType
     * @return
     */
    public static int calculateRegisterNumberForRegisterRegion(DataTypeEnum dataType) {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType 不能为空");
        }
        switch (dataType) {
            case BIT0:
            case BIT1:
            case BIT2:
            case BIT3:
            case BIT4:
            case BIT5:
            case BIT6:
            case BIT7:
            case BIT8:
            case BIT9:
            case BIT10:
            case BIT11:
            case BIT12:
            case BIT13:
            case BIT14:
            case BIT15:
            case INT8:
            case UINT8:
            case INT16:
            case UINT16:
                return 1;
            case INT24:
            case UINT24:
            case INT32:
            case UINT32:
            case FLOAT32:
                return 2;
            case INT48:
            case UINT48:
            case TIME48:
            case TIME64:
                return 3;
            case INT64:
            case UINT64:
            case FLOAT64:
                return 4;
            default:
                throw new IllegalArgumentException("不支持的数据类型，dataType:" + dataType.getValue());
        }
    }

    /**
     * 写指令支持的数据类型转换成报文
     *
     * @param byteBuf   缓冲区
     * @param data      下发的值
     * @param dataType  数据类型
     * @param byteOrder 字节序
     * @author Mr.Jia
     * @date 2022/7/22 14:35
     */
    public static ByteBuf setNumberFromRegisterRegion(ByteBuf byteBuf, Number data, DataTypeEnum dataType,
                                                      ByteOrderEnum byteOrder) {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType 不能为空");
        }
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder 不能为空");
        }
        ByteBuf newByteBuf = setByteBufByData(data, dataType);
        swap2BigEndian(newByteBuf, byteOrder);
        return Unpooled.wrappedBuffer(byteBuf, newByteBuf);
    }

    /**
     * 创建一个新的缓冲区,并给这个缓存区写个下发的值
     *
     * @param data     下发的值
     * @param dataType 数据类型枚举
     * @return io.netty.buffer.ByteBuf
     * @author Mr.Jia
     * @date 2022/7/22 3:43 PM
     */
    private static ByteBuf setByteBufByData(Number data, DataTypeEnum dataType) {
        ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.directBuffer(64);
        switch (dataType) {
            case INT16:
            case UINT16:
                byteBuf.writeShort(data.intValue());
                return byteBuf;
            case INT32:
                byteBuf.writeInt(data.intValue());
                return byteBuf;
            case UINT32:
                byteBuf.writeInt((int) data.longValue());
                return byteBuf;
            case INT64:
            case UINT64:
                // FIXME 针对UINT64类型，目前不支持大于Long.MAX_VALUE的值。
                byteBuf.writeLong(data.longValue());
                return byteBuf;
            case FLOAT32:
                byteBuf.writeFloat(data.floatValue());
                return byteBuf;
            case FLOAT64:
                byteBuf.writeDouble(data.doubleValue());
                return byteBuf;
            default:
                throw new IllegalArgumentException("写指令不支持的数据类型，dataType:" + dataType.getValue());
        }
    }

    /**
     * 根据字节序、数据类型提取数值。
     *
     * @param byteBuf
     * @param startIndex
     * @param byteOrder
     * @param dataType
     * @return
     */
    public static Number getNumberFromRegisterRegion(ByteBuf byteBuf, int startIndex, ByteOrderEnum byteOrder,
                                                     DataTypeEnum dataType) {
        int length = byteBuf.readableBytes() - startIndex;
        if (length < 1) {
            throw new IllegalArgumentException("length 不能小于1");
        }
        if (byteBuf.readableBytes() - startIndex < length) {
            throw new IllegalArgumentException(
                    "可读字节数不能小于length，readableBytes:" + byteBuf.readableBytes() + ", length:" + length);
        }
        int bytesNum = 2 * calculateRegisterNumberForRegisterRegion(dataType);
        if (length < bytesNum) {
            throw new IllegalArgumentException("数组长度不够, readableBytes:" + byteBuf.readableBytes() + ", startIndex:"
                    + startIndex + ", dataType:" + dataType + ", bytesNum:" + bytesNum);
        }
        ByteBuf data = byteBuf.copy(startIndex, bytesNum);
        swap2BigEndian(data, byteOrder);
        try {
            switch (dataType) {
                case INT8:
                    // 由于寄存器长度是2个字节，字节序是大端时，int8读取低字节。
                    data.readByte();
                    return data.readByte();
                case UINT8:
                    // 由于寄存器长度是2个字节，字节序是大端时，int8读取低字节。
                    data.readByte();
                    return data.readUnsignedByte();
                case INT16:
                    return data.readShort();
                case UINT16:
                    return data.readUnsignedShort();
                case INT24:
                    data.readByte();
                    return data.readMedium();
                case UINT24:
                    data.readByte();
                    return data.readUnsignedMedium();
                case INT32:
                    return data.readInt();
                case UINT32:
                    return data.readUnsignedInt();
                case INT48: {
                    long hInt = data.readInt();
                    int lShort = data.readUnsignedShort();
                    return hInt << 16 | lShort;
                }
                case UINT48: {
                    long hInt = data.readUnsignedInt();
                    int lShort = data.readUnsignedShort();
                    return hInt << 16 | lShort;
                }
                case INT64:
                    return data.readLong();
                case UINT64: {
                    long hInt = data.readUnsignedInt();
                    long lInt = data.readUnsignedInt();
                    BigInteger value = BigInteger.valueOf(hInt);
                    // var << 32 位 等同于 var * 2^32 等同于 var * 0x1_00_00_00_00L
                    value = value.multiply(BigInteger.valueOf(0x1_00_00_00_00L));
                    value = value.add(BigInteger.valueOf(lInt));
                    return value;
                }

                case TIME48:
                    return getTime48(data);
                case TIME64:
                    return getTime64(data);
                case FLOAT32:
                    return data.readFloat();
                case FLOAT64:
                    return data.readDouble();
                case BIT0:
                    return getBooleanFromRegister(data, 0);
                case BIT1:
                    return getBooleanFromRegister(data, 1);
                case BIT2:
                    return getBooleanFromRegister(data, 2);
                case BIT3:
                    return getBooleanFromRegister(data, 3);
                case BIT4:
                    return getBooleanFromRegister(data, 4);
                case BIT5:
                    return getBooleanFromRegister(data, 5);
                case BIT6:
                    return getBooleanFromRegister(data, 6);
                case BIT7:
                    return getBooleanFromRegister(data, 7);
                case BIT8:
                    return getBooleanFromRegister(data, 8);
                case BIT9:
                    return getBooleanFromRegister(data, 9);
                case BIT10:
                    return getBooleanFromRegister(data, 10);
                case BIT11:
                    return getBooleanFromRegister(data, 11);
                case BIT12:
                    return getBooleanFromRegister(data, 12);
                case BIT13:
                    return getBooleanFromRegister(data, 13);
                case BIT14:
                    return getBooleanFromRegister(data, 14);
                case BIT15:
                    return getBooleanFromRegister(data, 15);
                default:
                    throw new IllegalArgumentException("不支持的数据类型，dataType:" + dataType.getValue());
            }
        } finally {
            data.release();
        }
    }

    /**
     * 从 2 个字节中,获取 bit 位对应的值
     *
     * @param twoBytes 缓冲区
     * @param bitIndex bit 位
     * @return java.lang.Number
     * @author Mr.Jia
     * @date 2022/7/22 5:14 PM
     */
    private static Number getBooleanFromRegister(ByteBuf twoBytes, int bitIndex) {
        return (byte) (twoBytes.readShort() >> bitIndex & 0x01);
    }

    /**
     * 调整字节序
     *
     * @param byteBuf   缓冲区
     * @param byteOrder 字节序
     * @author Mr.Jia
     * @date 2022/7/22 5:15 PM
     */
    private static void swap2BigEndian(ByteBuf byteBuf, ByteOrderEnum byteOrder) {
        if (byteOrder.isByteSwap()) {
            swapByte(byteBuf);
        }
        if (byteOrder.isWordSwap()) {
            swapWord(byteBuf);
            swapDword(byteBuf);
        }
    }

    /**
     * 字节交换
     *
     * @param byteBuf 缓冲区
     * @author Mr.Jia
     * @date 2021/11/29 15:24
     */
    private static void swapByte(ByteBuf byteBuf) {
        int wordBytes = 2;
        int wordLength = byteBuf.readableBytes() / wordBytes;
        for (int i = 0; i < wordLength; i++) {
            int hIndex = i * wordBytes, lIndex = hIndex + 1;
            byte oldH = byteBuf.getByte(hIndex);
            byte oldL = byteBuf.getByte(lIndex);
            byteBuf.setByte(hIndex, oldL);
            byteBuf.setByte(lIndex, oldH);
        }
    }

    /**
     * 字交换（双字节交换）
     *
     * @param byteBuf 缓冲区
     * @author Mr.Jia
     * @date 2021/11/29 15:24
     */
    private static void swapWord(ByteBuf byteBuf) {
        int dwordBytes = 4;
        int dwordLength = byteBuf.readableBytes() / dwordBytes;
        for (int i = 0; i < dwordLength; i++) {
            int hIndex = i * dwordBytes, lIndex = hIndex + 2;
            short oldH = byteBuf.getShort(hIndex);
            short oldL = byteBuf.getShort(lIndex);
            byteBuf.setShort(hIndex, oldL);
            byteBuf.setShort(lIndex, oldH);
        }
    }

    /**
     * 双字交换
     *
     * @param byteBuf 缓冲区
     * @author Mr.Jia
     * @date 2021/11/29 15:24
     */
    private static void swapDword(ByteBuf byteBuf) {
        int ddWordBytes = 8;
        int ddWordLength = byteBuf.readableBytes() / ddWordBytes;
        for (int i = 0; i < ddWordLength; i++) {
            int hIndex = i * ddWordBytes, lIndex = hIndex + 4;
            int oldH = byteBuf.getInt(hIndex);
            int oldL = byteBuf.getInt(lIndex);
            byteBuf.setInt(hIndex, oldL);
            byteBuf.setInt(lIndex, oldH);
        }
    }

    /**
     * 时间48 <br/>
     * 原始数据： 0x07D1,06,07,03,03 <br/>
     * 解析后数据： 2001,06,07,03,03 等于 2001年6月7日3时03分 <br/>
     * <code>
     * 格式：yyyyMMddHHmm
     * short: yyyy
     * byte3: MM
     * byte2: dd
     * byte1: HH
     * byte0: mm
     * </code>
     */
    public static Number getTime48(ByteBuf data) {
        data.markReaderIndex();
        short byte5 = data.readUnsignedByte();
        short byte4 = data.readUnsignedByte();
        short byte3 = data.readUnsignedByte();
        short byte2 = data.readUnsignedByte();
        short byte1 = data.readUnsignedByte();
        short byte0 = data.readUnsignedByte();
        data.resetReaderIndex();
        return 1_0000_0000L * ((byte5 << 8) | byte4) + 100_0000L * byte3 + 1_0000 * byte2 + 100 * byte1 + byte0;
    }

    /**
     * 时间64 <br/>
     * <p>
     * 原始数据： 0x01,0A,06,07,03,13 <br/>
     * 解析后数据：2001,10,06,07,03,19 等于 2001年10月06日07时03分19秒 <br/>
     *
     * <code>
     * 格式：20yyMMddHHmmss
     * byte5: yy
     * byte4: MM
     * byte3: dd
     * byte2: HH
     * byte1: mm
     * byte0: ss
     * </code>
     */
    public static Number getTime64(ByteBuf data) {
        data.markReaderIndex();
        short byte5 = data.readUnsignedByte();
        short byte4 = data.readUnsignedByte();
        short byte3 = data.readUnsignedByte();
        short byte2 = data.readUnsignedByte();
        short byte1 = data.readUnsignedByte();
        short byte0 = data.readUnsignedByte();
        data.resetReaderIndex();
        return 100_0000_0000L * (byte5 + 2000) + 1_0000_0000L * byte4 + 100_0000L * byte3 + 10000 * byte2 + 100 * byte1
                + byte0;
    }


    public static void main(String[] args) {
        Double a = 256.0;


        byte[] str = new byte[]{0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x68, 0x66, 0x64, 0x67, 0x68, 0x66, 0x67, 0x68, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String s = new String(str);
        String m = " ";
        System.out.println(s);


//        byte[] BADCFEHG = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//        byte[] BADCFEHG = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64, 0x00};
//        byte[] BADCFEHG = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x64, 0x00};

        // 小小
        byte[] HGFEDCBA = new byte[]{0x38, (byte) 0xa2, (byte) 0x87, 0x00, 0x00, 0x00, 0x00, 0x00};
        // 大大
        byte[] ABCDEFGH = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87, (byte) 0xa2, 0x38};
        // 大字节序小字序
        byte[] GHEFCDAB = new byte[]{(byte) 0xa2, 0x38, 0x00, (byte) 0x87, 0x00, 0x00, 0x00, 0x00};
        // 小字节序大字序
        byte[] BADCFEHG = new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x87, 0x00, 0x38, (byte) 0xa2,};
        ByteBuf byteBuf = Unpooled.wrappedBuffer(BADCFEHG);
        swap2BigEndian(byteBuf, ByteOrderEnum.BADCFEHG);

        System.out.println(ByteBufUtil.hexDump(byteBuf));
        double v = byteBuf.readLong();
        System.out.println(v);


//        BigDecimal bigDecimal = new BigDecimal(v);
//        Double v1 = bigDecimal.setScale(5, RoundingMode.HALF_UP).doubleValue();
//        System.out.println(v1);
    }
}
