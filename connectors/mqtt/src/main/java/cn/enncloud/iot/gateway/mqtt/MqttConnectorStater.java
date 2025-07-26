
package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.MqttConfig;
import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import cn.enncloud.iot.gateway.config.connectors.ProtocolConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.mqtt.client.MqttClient;
import cn.enncloud.iot.gateway.mqtt.client.MqttClientCallback;
import cn.enncloud.iot.gateway.mqtt.client.MqttClientConfig;
import cn.enncloud.iot.gateway.mqtt.client.MqttConnectResult;
import cn.enncloud.iot.gateway.mqtt.extend.ProtocolChooser;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Map;

@Component
@Slf4j
public class MqttConnectorStater implements Connector {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Autowired
    ConnectorManager connectorManager;


    @Autowired
    MqttConfig config;

    @Autowired
    ProtocolManager protocolManager;

    @Autowired
    DeviceContext deviceContext;

    @Autowired
    MqttClientManager mqttClientManager;

    @Autowired
    MqttDeviceSessionManager deviceSessionManager;

    @PostConstruct
    @SneakyThrows
    public void init() throws Exception {
        if(CollectionUtils.isEmpty(config.getConfiguration())){
            return;
        }
        log.info("初始化mqtt");
        for (MqttConfigServer server : config.getConfiguration()) {
            Protocol protocol = null;
            if(server.getProtocol()!=null){
                ProtocolConfig protocolConfig = server.getProtocol();
                try {
                    protocol = protocolManager.register(protocolConfig);
                    if(protocol!=null){
                        protocol.setDeviceContext(deviceContext);
                        log.info("{} {} 协议加载成功",protocolConfig.getMainClass(),protocolConfig.getPath());
                    }
                } catch (Exception e) {
                    log.warn("{} {} 协议加载失败:{}",protocolConfig.getMainClass(),protocolConfig.getPath(), ExceptionUtils.getStackTrace(e));
                }
            }

            String host = server.getHost();
            int port = server.getPort();
            MqttClientConfig config = new MqttClientConfig();
            config.setReconnect(server.isReconnectEnabled());
            config.setReconnectDelay(server.getReconnectDelay());
            config.setPassword(server.getPassword());
            config.setUsername(server.getUsername());
            config.setMaxBytesInMessage(server.getMessageMaxBytes());
//            config.setClientId(server.getClientId());
            MqttClient client = MqttClient.create(config, null);
            mqttClientManager.addClient(server.uuid(), client);

            Protocol finalProtocol = protocol;
            client.connect(host, port)
                    .addListener(cf -> {
                        MqttConnectResult result = (MqttConnectResult) cf.get();
                        if(result.isSuccess()){
                            log.info("mqtt broker {}:{} connected, client: {}",  host, port, config.getClientId());
                            for(String topic : server.getTopics()){
                                ProtocolChooser chooser = new ProtocolChooser(protocolManager, deviceContext);
                                UpMqttHander upMqttHander = new UpMqttHander(finalProtocol, client,server,deviceSessionManager, chooser);
                                client.on(topic, upMqttHander);
                            }

                        }else{
                            log.error("mqtt broker {}:{} connect failed, client: {}, reason: {}", host, port, config.getClientId(), result.getReturnCode());
                        }
                    });

        }

        connectorManager.addConnector(this);
    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus(){
        return null;
    }
}
