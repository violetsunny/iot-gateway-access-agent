package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.OperationRequest;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.mqtt.client.MqttClient;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author hanyilong@enn.cn
 * kafka监听消息
 */
@Component
@Slf4j
public class DownMqttHandler {
    @Autowired
    DeviceContext deviceContext;
    @Autowired
    MqttDeviceSessionManager mqttDeviceSessionManager;
    @Autowired
    MqttClientManager mqttClientManager;


    @Autowired
    ProtocolManager protocolManager;

    /**
     * 监听kafka消息
     *
     * @param consumerRecord kafka的消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     *                       使用autoStartup = "false"必须指定id
     */
    @KafkaListener(topics = {"${ennew.iot.topics.commandTopic:device_command_topic}"}, groupId = "deviceCommandListenerForMqtt")
    public void listen(ConsumerRecord<Object, Object> consumerRecord) {
        try {
            log.info("group deviceCommandListenerForMqtt received command:{}", consumerRecord.value());
            Message request = JsonUtil.jsonToPojo(consumerRecord.value().toString(), OperationRequest.class);
            String deviceId = request.getDeviceId();
//            Product product = deviceContext.getProductByDeviceId(deviceId);
//            Protocol protocol = product.getProtocol();
//            if (product.getConnectorType() != ConnectorType.MQTT) {
//                return;
//            }
            MqttConfigServer mqttConfigServer = mqttDeviceSessionManager.getSession(deviceId);
            if (mqttConfigServer == null) {
                return;
            }
            MqttClient client = mqttClientManager.getClient(mqttConfigServer.uuid());
            if (client == null) {
                return;
            }
            Protocol protocol = protocolManager.get(mqttConfigServer.getProtocol().getName());
            if (request.getMessageType() == MessageType.CLOUD_HISTORY_REQ) {
                //
            } else if (request.getMessageType() == MessageType.CLOUD_OPERATION_REQ) {

                byte[] encode = protocol.encode(request);

                OperationRequest operationRequest = JsonUtil.jsonBytes2Object(encode, OperationRequest.class);
                if (Objects.nonNull(operationRequest) && Objects.nonNull(operationRequest.getParam())) {
                    ByteBuf buf = Unpooled.buffer(200);
                    buf.setBytes(0, JsonUtil.object2JsonBytes(operationRequest.getParam()));
                    client.publish(operationRequest.getTopic(), buf, MqttQoS.AT_LEAST_ONCE);
                } else {
                    log.warn("指令下发取消，协议解析后无下发参数信息，info：{}", JSONUtil.toJsonStr(operationRequest));
                }
            } else if (request.getMessageType() == MessageType.CLOUD_NTP_REQ) {
                //
            }


        } catch (Exception e) {
            log.error("deviceCommandListenerForMqtt error", e);
        }
    }

}
