package cn.enncloud.iot.gateway.modbus.core.value;

public class MultipleValue extends BaseModbusValue{
    public MultipleValue(short... values){
        this.len = values.length;
        this.value = values;
    }
}
