package cn.enncloud.iot.gateway.modbus.core.payloads;


import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;

public class ReadHoldingRegisterPayLoad extends BaseModbusPayLoad{
    public ReadHoldingRegisterPayLoad( int address, int amount) {
        super(ModbusFCode.READ_HOLDING_REGISTER, address, amount);
    }
}
