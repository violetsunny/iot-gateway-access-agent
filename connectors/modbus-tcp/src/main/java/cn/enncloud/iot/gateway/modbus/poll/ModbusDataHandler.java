package cn.enncloud.iot.gateway.modbus.poll;


import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.modbus.constant.ByteOrderEnum;
import cn.enncloud.iot.gateway.modbus.constant.DataTypeEnum;
import cn.enncloud.iot.gateway.modbus.core.payloads.*;
import cn.enncloud.iot.gateway.modbus.core.requests.ModBusTcpRequest;
import cn.enncloud.iot.gateway.modbus.core.requests.ModbusRequest;
import cn.enncloud.iot.gateway.modbus.core.responses.ModbusResponse;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.core.value.BinaryValue;
import cn.enncloud.iot.gateway.modbus.core.value.MultipleValue;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointDTO;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointWriteDTO;
import cn.enncloud.iot.gateway.modbus.utils.IotByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class ModbusDataHandler {


    public Number respHandler(ModbusResponse resp, ModbusPointDTO modbusPointDTO) {


        byte[] bts = (byte[]) resp.data();
        Number value = null;
        log.info("功能码：{}，接收数据：{}", modbusPointDTO.getFunctionCode(), resp);
        if (resp.code() == 1) {
            value = getBooleanValue(modbusPointDTO, bts);
        } else if (resp.code() == 2) {
            value = getBooleanValue(modbusPointDTO, bts);
        } else if (resp.code() == 3) {
            value = getValue(modbusPointDTO, bts);

        } else if (resp.code() == 4) {
            value = getValue(modbusPointDTO, bts);
        } else {
            log.warn("功能码暂不支持，code：{}，info：{}", resp.code(), resp.toString());
        }

        if (Objects.nonNull(value)) {
            log.info("功能码：{}，解析数据：{}", modbusPointDTO.getFunctionCode(), value.longValue());

            return value;
        }
        return null;
    }
    public static Number getBooleanValue(ModbusPointDTO modbusPointDTO, byte[] bts) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bts);

        DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());

        String name = dataType.getValue();
        Integer bit = Integer.valueOf(name.replace("bit", ""));
        Number value = IotByteUtils.getBooleanFromStatusRegion(buffer, 0, bit);
        return value;
    }

    private static Number getValue(ModbusPointDTO modbusPointDTO, byte[] bts) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(bts);

        ByteOrderEnum byteOrder = ByteOrderEnum.getInstance(modbusPointDTO.getByteOrder());
        DataTypeEnum dataType = DataTypeEnum.getInstance(modbusPointDTO.getDataType());
        Number value = IotByteUtils.getNumberFromRegisterRegion(buffer, 0,
                byteOrder, dataType);
        return value;
    }


    /**
     * 构建
     *
     * @param pointDTOS
     * @return
     */
    public ModbusRequest buildReadReq(ModbusPointMapping pointDTOS) {

        BaseModbusPayLoad baseModbusPayLoad = null;
        Integer amount = transAmount(pointDTOS.getDataType());
        if (Objects.isNull(amount)) {
            return null;
        }
        if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_HOLDING_REGISTER)) {
            baseModbusPayLoad = new ReadHoldingRegisterPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_INPUT_REGISTER)) {
            baseModbusPayLoad = new ReadInputRegisterPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_COIL_STATUS)) {
            baseModbusPayLoad = new ReadCoilStatusPayLoad(pointDTOS.getRegisterAddress(), amount);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.READ_INPUT_STATUS)) {
            baseModbusPayLoad = new ReadInputStatusPayLoad(pointDTOS.getRegisterAddress(), amount);
        }
        ModBusTcpRequest modBusTcpRequest = new ModBusTcpRequest(pointDTOS.getSlaveAddr().shortValue(), baseModbusPayLoad);
        return modBusTcpRequest;


    }

    public static void main(String[] args) {
        Boolean a = true;
        System.out.println();
    }

    public ModbusRequest buildWriteReq(ModbusPointWriteDTO modbusPointWriteDTO, ModbusPointDTO pointDTOS) {

        Object value = modbusPointWriteDTO.getValue();
        //TODO: 后续完善数据写入逻辑
        ByteBuf buffer = Unpooled.buffer();
        ByteBuf byteBuf = IotByteUtils.setNumberFromRegisterRegion(buffer, (Number) value, DataTypeEnum.getInstance(pointDTOS.getDataType()), ByteOrderEnum.getInstance(pointDTOS.getByteOrder()));

        BaseModbusPayLoad baseModbusPayLoad = null;
        if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_SINGLE_COIL)) {
            byte b = byteBuf.readByte();
            baseModbusPayLoad = new WriteSingleCoilPayLoad(0, true);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_SINGLE_REGISTER)) {
            baseModbusPayLoad = new WriteSingleRegisterPayLoad(1, (short) 199);
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_MULTIPLE_COIL)) {
            baseModbusPayLoad = new WriteMultipleCoilPayLoad(0, new BinaryValue(1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1));
        } else if (pointDTOS.getFunctionCode().equals(ModbusFCode.WRITE_MULTIPLE_REGISTER)) {
            baseModbusPayLoad = new WriteMultipleRegisterPayLoad(0, new MultipleValue((short) 199, (short) 299, (short) 399, (short) 499, (short) 599, (short) 699, (short) 799, (short) 899));
        }
        ModBusTcpRequest modBusTcpRequest = new ModBusTcpRequest(baseModbusPayLoad);
        return modBusTcpRequest;
//        //            写入单个线圈 三种方式
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, true));
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, 1));
//        new ModBusTcpRequest(new WriteSingleCoilPayLoad(0, (short) 1));
////           =============================================================================
////            写入多个线圈
//        new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0, new BinaryValue(1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1)));
//        new ModBusTcpRequest(new WriteMultipleCoilPayLoad(0, new BinaryValue((short) 1, (short) 1, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) 1)));
////           =============================================================================
////            写入单个寄存器
//        new ModBusTcpRequest(new WriteSingleRegisterPayLoad(1, (short) 199));
////           =============================================================================
////            写入多个寄存器
//        new ModBusTcpRequest(new WriteMultipleRegisterPayLoad(0, new MultipleValue((short) 199, (short) 299, (short) 399, (short) 499, (short) 599, (short) 699, (short) 799, (short) 899)));
    }

    /**
     * 数据类型转换byte数量
     *
     * @param dataType
     * @return
     */
    private Integer transAmount(String dataType) {

        DataTypeEnum instance = DataTypeEnum.getInstance(dataType);

        if (Objects.isNull(instance)) {
            log.warn("点表数据类型异常，{}", dataType);
            return null;
        }
        return instance.getByteNum();
    }
}
