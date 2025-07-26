package cn.enncloud.iot.gateway.modbus.core.value;

public interface ModbusValue<T> {
    T value();

    int len();

}
