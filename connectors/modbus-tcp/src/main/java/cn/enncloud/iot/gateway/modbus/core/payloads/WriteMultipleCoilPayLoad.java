package cn.enncloud.iot.gateway.modbus.core.payloads;


import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.core.value.ModbusValue;

public class WriteMultipleCoilPayLoad extends BaseModbusPayLoad{
    public WriteMultipleCoilPayLoad(int address, ModbusValue<Short> value) {
        super(ModbusFCode.WRITE_MULTIPLE_COIL, address, value.len(), value.value());
    }
}
