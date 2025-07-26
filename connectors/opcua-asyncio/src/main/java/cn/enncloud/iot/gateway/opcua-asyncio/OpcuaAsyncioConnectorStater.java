//package cn.enncloud.iot.gateway.ftp;
//
//import cn.enncloud.iot.gateway.Connector;
//import cn.enncloud.iot.gateway.ConnectorManager;
//import cn.enncloud.iot.gateway.config.connectors.UdpConfig;
//import cn.enncloud.iot.gateway.context.DeviceContext;
//import cn.enncloud.iot.gateway.protocol.Protocol;
//import cn.enncloud.iot.gateway.protocol.ProtocolManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * @author hanyilong@enn.cn
// */
//@Component
//public class OpcuaAsyncioConnectorStater implements Connector {
//    @Autowired
//    UdpConfig udpConfig;
//    @Autowired
//    DeviceContext deviceContext;
//    @Autowired
//    ProtocolManager protocolManager;
//    @Autowired
//    ConnectorManager connectorManager;
//    @Override
//    public void init() throws Exception {
//        connectorManager.addConnector(this);
//    }
//
//    @Override
//    public void setupProtocol(Protocol protocol, Map params) {
//
//    }
//
//    @Override
//    public Map getStatus() {
//        return null;
//    }
//}
