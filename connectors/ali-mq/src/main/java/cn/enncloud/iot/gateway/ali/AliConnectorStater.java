package cn.enncloud.iot.gateway.ali;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.ali.consumer.ConsumerClient;
import cn.enncloud.iot.gateway.config.connectors.AliMqConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AliConnectorStater implements Connector {

    @Resource
    AliMqConfig aliMqConfig;

    @Resource
    ProtocolManager protocolManager;

    @Resource
    ConnectorManager connectorManager;

    @Resource
    DeviceContext deviceContext;

    List<ConsumerClient> consumerClientList = new ArrayList<>();

    @PostConstruct
    @SneakyThrows
    public void init() {

        aliMqConfig.getConfiguration().forEach(aliMQConsumerConfig -> {
            aliMQConsumerConfig.getTopics().forEach(topicConfiguration ->
                    protocolManager.register(topicConfiguration.getProtocol().getMainClass() + topicConfiguration.getTopic(),
                            topicConfiguration.getProtocol().getMainClass(),
                            topicConfiguration.getProtocol().getPath(),
                            topicConfiguration.getProtocol().getParams(),
                            false));
            // 创建消费者
            ConsumerClient consumerClient = new ConsumerClient(aliMQConsumerConfig);
            // 订阅topic
            consumerClient.subscribe(deviceContext, protocolManager);
            consumerClientList.add(consumerClient);
        });

        connectorManager.addConnector(this);
        log.info("ali server started!!!!!!!!!!");

    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {
        // 实现协议的设置逻辑
    }

    @Override
    public Map<String, Object> getStatus() {
        return consumerClientList.stream()
                .collect(Collectors.toMap(ConsumerClient::getClientName, ConsumerClient::getStatus));
    }

    @PreDestroy
    public void shutdown() {
        consumerClientList.forEach(ConsumerClient::shutdown);
    }

}