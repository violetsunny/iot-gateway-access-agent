package cn.enncloud.iot.gateway.mqtt.extend;

import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;

public class ProtocolChooser {


    private ProtocolManager protocolManager;

    private DeviceContext deviceContext;

    public ProtocolChooser(ProtocolManager protocolManager, DeviceContext deviceContext) {
        this.protocolManager = protocolManager;
        this.deviceContext = deviceContext;
    }

    public Protocol choose(String topic, byte[] msg){
        TopicParams topicParams = new TopicParams(topic);
        String protocolId = deviceContext.getProductProtocolBySn(topicParams.sn);
        if(protocolId == null){
            return null;
        }
        return protocolManager.get(protocolId);
    }


    static class TopicParams {
        public TopicParams(String topic) {
            this.topic = topic;
            String[] strings = topic.split("/");
            if(strings.length >= 5){
                this.pKey = strings[3];
                this.sn = strings[4];
            }
        }
        String topic;
        String pKey;
        String sn;
    }
}
