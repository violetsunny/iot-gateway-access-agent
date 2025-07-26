package cn.enncloud.iot.gateway.modbus.core.requests;


import cn.enncloud.iot.gateway.modbus.core.payloads.ModbusPayLoad;

public abstract class BaseModbusRequest implements ModbusRequest{
    int flag =  0x7FFF;
    short pool =  0x0000;

    // 站点地址 默认1
    short uid = 1;

    ModbusPayLoad payLoad;
    public BaseModbusRequest(ModbusPayLoad payLoad) {
        this.payLoad = payLoad;
    }

    public BaseModbusRequest(short uid,ModbusPayLoad payLoad) {
       this(payLoad);
       this.uid = uid;
    }

    public ModbusPayLoad getPayLoad() {
        return payLoad;
    }

    public int getFlag() {
        return flag;
    }

    public short getPool() {
        return pool;
    }

    @Override
    public void setFlag(int flag) {
        this.flag = (short) flag;
    }


    @Override
    public short getUid() {
        return uid;
    }
}
