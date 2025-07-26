package cn.enncloud.iot.gateway.modbus.core.value;

public class BinaryValue extends BaseModbusValue{

    public BinaryValue(Short... values){
        dispose(values);
    }
    public BinaryValue(Integer... values){
        dispose(values);
    }

    private <T> void dispose(T[] vals){
        if(vals.length == 0){
            throw new RuntimeException("values is Empty!");
        }
        StringBuffer sb = new StringBuffer();
        if(vals instanceof Short[] || vals instanceof Integer[]){
            for (int i = 0; i < vals.length; i++) {
                sb.append(vals[vals.length - i - 1]);
            }
        }else{
            sb.append(0x0000);
        }
        this.len = vals.length;
        this.value = Integer.parseInt(sb.toString(),2);
    }

}
