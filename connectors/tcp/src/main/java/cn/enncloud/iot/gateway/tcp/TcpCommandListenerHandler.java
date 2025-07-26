package cn.enncloud.iot.gateway.tcp;

import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.entity.ConnectorType;
import cn.enncloud.iot.gateway.entity.Product;
import cn.enncloud.iot.gateway.message.*;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.tcp.session.LocalSession;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author hanyilong@enn.cn
 * kafka监听消息
 */
@Slf4j
@Component
public class TcpCommandListenerHandler {
    @Autowired
    DeviceContext deviceContext;
    @Autowired
    TcpSessionManger tcpSessionManger;
    /**
     * 监听kafka消息
     *
     * @param consumerRecord kafka的消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     * 使用autoStartup = "false"必须指定id
     */
//    @KafkaListener(topics = {"deviceCommandTopic"}, groupId = "deviceCommandListenerForTcp")
    public void listen(ConsumerRecord<Object, Object> consumerRecord) {
        try {
            log.info("grep deviceCommandListenerForTcp received command:{}", consumerRecord.value());
            Message message = JsonUtil.jsonToPojo(consumerRecord.value().toString(), Message.class);
            String deviceId = message.getDeviceId();
//            Product product = deviceContext.getProductByDeviceId(deviceId);
//            Protocol protocol = product.getDownProtocol();
//            if(protocol == null){
//                return;
//            }
//            if (product.getConnectorType() != ConnectorType.TCP){
//                return;
//            }

            LocalSession session = tcpSessionManger.getSession(deviceId);
            if(session == null){
                return;
            }
            Protocol protocol = session.getProtocol();
            session.writeAndFlush(protocol.encode(message));
        } catch (Exception e) {
            log.warn("消费失败 ", e);
        }
    }

}
