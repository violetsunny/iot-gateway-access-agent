package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MqttDeviceSessionManager {
    Map<String, MqttConfigServer> session = new HashMap<>();

    public void addSession(String deviceId, MqttConfigServer server){
        session.put(deviceId, server);
    }
    public void removeSession(String deviceId){
        session.remove(deviceId);
    }

    public MqttConfigServer getSession(String deviceId){
        return session.get(deviceId);
    }
}
