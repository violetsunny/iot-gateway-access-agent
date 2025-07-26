package cn.enncloud.iot.gateway.modbus;

import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理modbus client与设备关系
 */
@Component
public class ModbusDeviceSessionManager {




    Map<String, ModbusServerConfig> session = new HashMap<>();

    public void addSession(String deviceId, ModbusServerConfig client){
        session.put(deviceId, client);
    }
    public void removeSession(String deviceId){
        session.remove(deviceId);
    }

    public ModbusServerConfig getSession(String deviceId){
        return session.get(deviceId);
    }
}
