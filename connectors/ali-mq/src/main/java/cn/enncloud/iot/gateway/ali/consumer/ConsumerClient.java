package cn.enncloud.iot.gateway.ali.consumer;

import cn.enncloud.iot.gateway.config.connectors.AliMQConsumerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import com.aliyun.openservices.ons.api.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@Slf4j
@Data
public class ConsumerClient {

    private final String clientName;

    private final AliMQConsumerConfig aliMQConsumerConfiguration;

    private final Consumer consumer;

    public ConsumerClient(AliMQConsumerConfig aliMQConsumerConfiguration) {
        this.aliMQConsumerConfiguration = aliMQConsumerConfiguration;
        this.clientName = aliMQConsumerConfiguration.getGroupId();
        Properties properties = new Properties();
        // 您在消息队列RocketMQ版控制台创建的Group ID。
        properties.put(PropertyKeyConst.GROUP_ID, aliMQConsumerConfiguration.getGroupId());
        // AccessKey ID，阿里云身份验证标识。
        properties.put(PropertyKeyConst.AccessKey, aliMQConsumerConfiguration.getAccessKey());
        // AccessKey Secret，阿里云身份验证密钥。
        properties.put(PropertyKeyConst.SecretKey, aliMQConsumerConfiguration.getSecretKey());
        // 设置TCP接入域名，进入消息队列RocketMQ版控制台实例详情页面的接入点区域查看。
        properties.put(PropertyKeyConst.NAMESRV_ADDR, aliMQConsumerConfiguration.getNameSevAddr());

        // 在订阅消息前，必须调用start方法来启动Consumer，只需调用一次即可。
        this.consumer = ONSFactory.createConsumer(properties);
        log.info("create ali mq consumer config {}", properties);
    }

    public void subscribe(DeviceContext deviceContext, ProtocolManager protocolManager) {
        aliMQConsumerConfiguration.getTopics().forEach(topic -> consumer.subscribe(
                // Message所属的Topic。
                topic.getTopic(),
                // 订阅指定Topic下的Tags: (1. * 表示订阅所有消息。2. TagA || TagB 表示订阅TagA或TagB的消息。)
                topic.getTag(),
                // MessageListener
                (message, consumeContext) -> {
                    if(log.isInfoEnabled()){
                        log.info("Receive message topic {} tag {} message {}", topic.getTopic(), topic.getTag(), new String(message.getBody(), StandardCharsets.UTF_8));
                    }
                    return messageHandle(deviceContext, protocolManager, message, topic);
                }));
        consumer.start();
    }

    private Action messageHandle(DeviceContext deviceContext, ProtocolManager protocolManager, Message message, AliMQConsumerConfig.TopicConfiguration topic) {
        //业务处理
        try {
            Protocol protocol = protocolManager.get(topic.getProtocol().getMainClass() + topic.getTopic());
            if (protocol == null) {
                throw new RuntimeException("protocol is missing");
            }
            protocol.setDeviceContext(deviceContext);
            List<? extends cn.enncloud.iot.gateway.message.Message> messages = protocol.decodeMulti(message.getBody());
            if (!CollectionUtils.isEmpty(messages)) {
                messages.forEach(deviceContext::storeMessage);
            }
            return Action.CommitMessage;
        } catch (DecodeMessageException e) {
            log.warn("Failed to decode message ", e);
            return Action.ReconsumeLater;
        } catch (Exception e) {
            log.warn("Failed to consume message ", e);
            return Action.ReconsumeLater;
        }
    }

    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    public Boolean getStatus() {
        if (consumer != null) {
            return consumer.isStarted();
        }
        return false;
    }

}
