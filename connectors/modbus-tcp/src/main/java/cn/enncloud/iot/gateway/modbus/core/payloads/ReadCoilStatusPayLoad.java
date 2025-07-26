package cn.enncloud.iot.gateway.modbus.core.payloads;


import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;

public class ReadCoilStatusPayLoad extends BaseModbusPayLoad{
    public ReadCoilStatusPayLoad( int address, int amount) {
        super(ModbusFCode.READ_COIL_STATUS, address, amount);
    }


}
