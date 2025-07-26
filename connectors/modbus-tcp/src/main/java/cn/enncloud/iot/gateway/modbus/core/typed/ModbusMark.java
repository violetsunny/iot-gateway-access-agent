package cn.enncloud.iot.gateway.modbus.core.typed;

import java.util.concurrent.atomic.AtomicInteger;

public class ModbusMark {
    public final static short MODBUS_FLAG = 0x0001;
    public final static short MODBUS_POOL = 0x0000;

    public static int flag(AtomicInteger flag) {
        if (flag.get() >= Short.MAX_VALUE) {
            flag.set(0x0000);
        }
        return flag.incrementAndGet();
    }


}
