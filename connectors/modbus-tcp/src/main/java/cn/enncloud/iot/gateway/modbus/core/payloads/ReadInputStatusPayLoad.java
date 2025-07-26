package cn.enncloud.iot.gateway.modbus.core.payloads;


import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;

public class ReadInputStatusPayLoad extends BaseModbusPayLoad{
    public ReadInputStatusPayLoad(int address, int amount) {
        super( ModbusFCode.READ_INPUT_STATUS, address, amount);
    }
}
