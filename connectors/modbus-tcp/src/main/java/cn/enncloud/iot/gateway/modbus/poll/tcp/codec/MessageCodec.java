package cn.enncloud.iot.gateway.modbus.poll.tcp.codec;

import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.modbus.core.payloads.ModbusPayLoad;
import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.core.responses.ReadCoilStatusResponse;
import cn.enncloud.iot.gateway.modbus.core.responses.ReadHoldingRegisterResponse;
import cn.enncloud.iot.gateway.modbus.core.responses.ReadInputRegisterResponse;
import cn.enncloud.iot.gateway.modbus.core.responses.ReadInputStatusResponse;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<ModbusRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ModbusRequest request, ByteBuf buf) throws Exception {
        int sIndex = buf.writerIndex();
        buf.writeZero(7);
        int rIndex = buf.writerIndex();
        payload(request, buf);
        header(sIndex, rIndex, request.getFlag(), request.getPool(), request.getUid(), buf);

        log.info("下发指令:{},remoteAddress:{}", ByteBufUtil.hexDump(buf), channelHandlerContext.channel().remoteAddress());

        //buf.writeBytes(protocol.encode(null,ModbusPointDTO));
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int bytesLen = byteBuf.readableBytes();
        if (bytesLen < 10) {
            byteBuf.skipBytes(bytesLen);
            return;
        }

        log.info("接收数据:{},remoteAddress:{}", ByteBufUtil.hexDump(byteBuf), channelHandlerContext.channel().remoteAddress());

        byteBuf.markReaderIndex();
        int flag = byteBuf.readUnsignedShort();
        int pool = byteBuf.readUnsignedShort();
        int dtLen = byteBuf.readUnsignedShort();

        // 数据域长度判断
        int lastSize = byteBuf.readableBytes();
        if (lastSize < dtLen) {
            byteBuf.resetReaderIndex();
            return;
        }

        int uid = byteBuf.readUnsignedByte();
        if (pool != 0) {
            int i = dtLen - 1;
            if (i > 0) {
                byteBuf.skipBytes(i);
            }
            return;
        }
        int code = byteBuf.readByte();
        int btLen = byteBuf.readUnsignedByte();
        byte[] data = new byte[btLen];
        int index = byteBuf.readerIndex();
        byteBuf.getBytes(index, data, 0, btLen);
        if (code == ModbusFCode.READ_COIL_STATUS) {
            list.add(new ReadCoilStatusResponse(flag, uid, code, btLen, data));
        }
        if (code == ModbusFCode.READ_INPUT_STATUS) {
            list.add(new ReadInputStatusResponse(flag, uid, code, btLen, data));
        }
        if (code == ModbusFCode.READ_HOLDING_REGISTER) {

            list.add(new ReadHoldingRegisterResponse(flag, uid, code, btLen, data));
        }
        if (code == ModbusFCode.READ_INPUT_REGISTER) {
            list.add(new ReadInputRegisterResponse(flag, uid, code, btLen, data));
        }
        byteBuf.readBytes(btLen);

        // 无写入返回
//        byteBuf.resetReaderIndex();
//        byteBuf.readBytes(bytesLen);

        // list.add(protocol.decodeMulti(byteBuf,REQUEST_CACHE,POINT_CACHE));
    }

    private void header(int sIndex, int rIndex, int flag, short pool, short uid, ByteBuf buf) {
        int layLen = buf.writerIndex() - rIndex;
        int nIndex = buf.writerIndex();
        buf.writerIndex(sIndex);
        // 事务号
        buf.writeShort(flag);
        // 协议
        buf.writeShort(pool);
        // 数据长度
        buf.writeShort(layLen + 1);
        // 站点地址
        buf.writeByte(uid);
        buf.writerIndex(nIndex);
    }

    private void payload(ModbusRequest request, ByteBuf buf) {
        int code = request.getPayLoad().getCode();
        buf.writeByte(code);
        switch (code) {
            case ModbusFCode.READ_COIL_STATUS:
            case ModbusFCode.READ_INPUT_STATUS:
            case ModbusFCode.READ_HOLDING_REGISTER:
            case ModbusFCode.READ_INPUT_REGISTER:
                currency(request.getPayLoad(), buf);
                break;
            case ModbusFCode.WRITE_SINGLE_COIL:
                writeSingleCoil(request.getPayLoad(), buf);
                break;
            case ModbusFCode.WRITE_SINGLE_REGISTER:
                writeSingleRegister(request.getPayLoad(), buf);
                break;
            case ModbusFCode.WRITE_MULTIPLE_COIL:
                writeMultipleCoil(request.getPayLoad(), buf);
                break;
            case ModbusFCode.WRITE_MULTIPLE_REGISTER:
                writeMultipleRegister(request.getPayLoad(), buf);
                break;
            default:
                throw new RuntimeException(String.format("%s is an unsupported method", code));

        }
    }

    private void currency(ModbusPayLoad payload, ByteBuf buf) {
        buf.writeShort(payload.getAddress());
        buf.writeShort(payload.getAmount());
    }

    private void writeSingleCoil(ModbusPayLoad<Integer> payload, ByteBuf buf) {
        buf.writeShort(payload.getAddress());
        buf.writeShortLE(payload.val());
        //  buf.writeByte(payload.value());
    }

    private void writeSingleRegister(ModbusPayLoad<Short> payload, ByteBuf buf) {
        buf.writeShort(payload.getAddress());
        buf.writeShort(payload.val());
    }

    private void writeMultipleCoil(ModbusPayLoad<Integer> payload, ByteBuf buf) {
        currency(payload, buf);
        int count = (payload.getAmount() + 7) / 8;
        buf.writeByte(count);
        ByteBuf temp = Unpooled.buffer();
        temp.writeShortLE(payload.val());
        buf.writeBytes(temp, count);
    }

    private void writeMultipleRegister(ModbusPayLoad<short[]> payload, ByteBuf buf) {
        currency(payload, buf);
        int count = payload.getAmount() * 2;
        buf.writeByte(count);
        ByteBuf temp = Unpooled.buffer();
        for (int i = 0; i < payload.val().length; i++) {
            temp.writeShort(payload.val()[i]);
        }
        buf.writeBytes(temp, count);
    }

//
//    public static void main(String[] args) {
//
//        String aa = "33 d1 00 00 00 05 7f 03 02 00 6f 33 d2 00 00 00 05 83 03 02 00 98 33 d3 00 00 00 05 67 03 02 01 bd 33 d4 00 00 00 05 67 03 02 00 85";
//
//        ByteBuf byteBuf = Unpooled.buffer();
//
//        byte[] bytes = ByteBufUtil.decodeHexDump(aa.replace(" ", ""));
//        byteBuf.writeBytes(bytes);
//
//        while (byteBuf.readableBytes()>=10) {
//            int bytesLen = byteBuf.readableBytes();
//            if (bytesLen < 10) {
//                byteBuf.skipBytes(bytesLen);
//                return;
//            }
//
//
//            byteBuf.markReaderIndex();
//            int flag = byteBuf.readUnsignedShort();
//            int pool = byteBuf.readUnsignedShort();
//            int dtLen = byteBuf.readUnsignedShort();
//            int uid = byteBuf.readUnsignedByte();
//            if (pool != 0) {
//                int i = dtLen - 1;
//                if (i > 0) {
//                    byteBuf.skipBytes(i);
//                } else {
//                    byteBuf.skipBytes(byteBuf.readableBytes());
//                }
//                return;
//            }
//            int code = byteBuf.readByte();
//            int btLen = byteBuf.readUnsignedByte();
//            byte[] data = new byte[btLen];
//            int index = byteBuf.readerIndex();
//            byteBuf.getBytes(index, data, 0, btLen);
//
//            List<Object> list = new ArrayList<>();
//            if (code == ModbusFCode.READ_COIL_STATUS) {
//                list.add(new ReadCoilStatusResponse(flag, uid, code, btLen, data));
//            }
//            if (code == ModbusFCode.READ_INPUT_STATUS) {
//                list.add(new ReadInputStatusResponse(flag, uid, code, btLen, data));
//            }
//            if (code == ModbusFCode.READ_HOLDING_REGISTER) {
//
//                list.add(new ReadHoldingRegisterResponse(flag, uid, code, btLen, data));
//            }
//            if (code == ModbusFCode.READ_INPUT_REGISTER) {
//                list.add(new ReadInputRegisterResponse(flag, uid, code, btLen, data));
//            }
//            byteBuf.readBytes(btLen);
//
//            System.out.println(list.get(0).toString());
//        }
//    }
}
