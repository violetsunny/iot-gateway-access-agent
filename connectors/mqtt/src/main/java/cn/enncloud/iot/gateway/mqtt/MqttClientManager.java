package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import cn.enncloud.iot.gateway.mqtt.client.MqttClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MqttClientManager {
    Map<String, MqttClient> clients = new HashMap<>();

    public void addClient(String mqttServerConfigId, MqttClient client){
        clients.put(mqttServerConfigId, client);
    }
    public void removeClient(String mqttServerConfigId){
        clients.remove(mqttServerConfigId);
    }

    public MqttClient getClient(String mqttServerConfigId){
        return clients.get(mqttServerConfigId);
    }
}
