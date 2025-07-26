package cn.enncloud.iot.gateway.mqtt;

import cn.enncloud.iot.gateway.config.connectors.MqttConfigServer;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.message.*;
import cn.enncloud.iot.gateway.mqtt.client.MqttClient;
import cn.enncloud.iot.gateway.mqtt.client.MqttHandler;
import cn.enncloud.iot.gateway.mqtt.extend.ProtocolChooser;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UpMqttHander implements MqttHandler {
    Protocol protocol;
    MqttClient client;

    MqttConfigServer server;

    ProtocolChooser chooser;

    MqttDeviceSessionManager deviceSessionManager;

    public UpMqttHander(Protocol protocol, MqttClient client, MqttConfigServer server, MqttDeviceSessionManager deviceSessionManager, ProtocolChooser chooser) {
        this.protocol = protocol;
        this.server = server;
        this.deviceSessionManager = deviceSessionManager;
        this.chooser = chooser;
        this.client = client;
    }

    @Override
    public void onMessage(String topic, ByteBuf payload) {
        try {

            int readableBytes = payload.readableBytes();
            byte[] bytes = new byte[readableBytes];
            payload.readBytes(bytes);

            if(log.isDebugEnabled()){
                log.debug("receive MQTT msg, topic: {}, payload: {}", topic, new String(bytes, StandardCharsets.UTF_8));
            }
            Protocol choose = chooser.choose(topic, bytes);
            List<? extends Message> messages = new ArrayList<>();
            if(choose != null){
                messages = choose.decodeMulti(bytes, topic);
            }else if(protocol!=null){
                messages = protocol.decodeMulti(bytes, topic);
            }
            log.info("UpMqttHander onMessage 解析后数据：{}", JSON.toJSONString(messages));
            for (Message message : messages) {
                deviceSessionManager.addSession(message.getDeviceId(), server);
                DeviceContext deviceContext = protocol.getDeviceContext();
                deviceContext.storeMessage(message);
                if(StringUtils.isNotBlank(message.getResponse())){
                    ByteBuf buf = Unpooled.buffer(200);
                    buf.setBytes(0, JsonUtil.object2JsonBytes(message.getResponse()));
                    client.publish(replaceTopic(topic), buf, MqttQoS.AT_LEAST_ONCE);
                }
//                if (message instanceof MetricReportRequest) {
//
//                } else if (message instanceof DeviceNtpRequest) {
//                    // do some process
//                } else if (message instanceof CloudNtpResponse) {
//                    // do some process
//                } else if (message instanceof HistoryMetricReportRequest) {
//                    // do some process
//                } else if (message instanceof InfoReportRequest) {
//                    // do some process
//                } else if (message instanceof StatusReportRequest) {
//                    // do some process
//                } else if (message instanceof OperationResponse) {
//                    // do some process
//                } else if (message instanceof MetricCloudCallResponse) {
//                    // do some process
//                }
            }


        } catch (Throwable e) {
            log.error("decode error", e);
        }

    }

    private static String replaceTopic(String topic) {
        return topic.replace("edge", "cloud");
    }
}
