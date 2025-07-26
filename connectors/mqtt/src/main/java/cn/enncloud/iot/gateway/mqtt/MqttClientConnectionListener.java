package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import cn.enncloud.iot.gateway.mqtt.client.MqttClientCallback;
import cn.enncloud.iot.gateway.mqtt.client.MqttClientConfig;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MqttClientConnectionListener implements MqttClientCallback {

    private MqttClientConfig config;


    public MqttClientConnectionListener(MqttClientConfig config) {
        this.config = config;
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("{} connection lost", config.getClientId());
    }

    @Override
    public void onSuccessfulReconnect() {
        log.info("{} reconnect success", config.getClientId());
    }
}
