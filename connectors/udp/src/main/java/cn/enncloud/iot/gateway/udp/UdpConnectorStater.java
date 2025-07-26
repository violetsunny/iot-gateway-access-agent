package cn.enncloud.iot.gateway.udp;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.UdpConfig;
import cn.enncloud.iot.gateway.config.connectors.UdpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
@Slf4j
public class UdpConnectorStater implements Connector {

    @Autowired
    UdpConfig udpConfig;
    @Autowired
    DeviceContext deviceContext;
    @Autowired
    ProtocolManager protocolManager;
    @Autowired
    ConnectorManager connectorManager;

    public void start(){
        if(CollectionUtils.isEmpty(udpConfig.getConfiguration())){
            return;
        }
        for(UdpServerConfig config : udpConfig.getConfiguration()){
            EventLoopGroup group=new NioEventLoopGroup();
            try {
                Bootstrap bootstrap=new Bootstrap();
                Protocol protocol = protocolManager.register(config.getProtocol());
                protocol.setDeviceContext(deviceContext);
                protocol.setParams(config.getProtocol().getParams());
                bootstrap.group(group)
                        //UDP 采用 NioDatagramChannel
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST,true)
                        .handler(new LoggingHandler(config.getLogLevel()))
                        //业务处理类
                        .handler(new MessageHandler(deviceContext, protocol));
                bootstrap.bind(config.getPort()).addListener(cf -> {
                    if(cf.isSuccess()){
                        log.info("UDP bind success");
                    }else{
                        log.error("UDP bind failed");
                    }
                });
            } catch (Exception e){

            }
        }

    }

    @Override
    public void init() throws Exception {
        if (udpConfig != null){
            this.start();
        }
        connectorManager.addConnector(this);
    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus() {
        return null;
    }
}

