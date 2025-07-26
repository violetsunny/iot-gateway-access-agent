package cn.enncloud.iot.gateway.modbus.core.payloads;


import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.core.value.ModbusValue;

public class WriteMultipleRegisterPayLoad extends BaseModbusPayLoad{
    public WriteMultipleRegisterPayLoad( int address, ModbusValue<short[]> value) {
        super(ModbusFCode.WRITE_MULTIPLE_REGISTER, address, value.len(), value.value());
    }
}
