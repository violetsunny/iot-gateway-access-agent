package cn.enncloud.iot.gateway.modbus.core.requests;


import cn.enncloud.iot.gateway.modbus.core.payloads.ModbusPayLoad;

public interface ModbusRequest{
    ModbusPayLoad getPayLoad();

    int getFlag();

    void setFlag(int i);

    short getPool();

    short getUid();

}
