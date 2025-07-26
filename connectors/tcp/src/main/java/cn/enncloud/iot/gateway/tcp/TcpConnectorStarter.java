package cn.enncloud.iot.gateway.tcp;

import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.TcpClientConfig;
import cn.enncloud.iot.gateway.config.connectors.TcpConfig;
import cn.enncloud.iot.gateway.config.connectors.TcpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.tcp.handler.ExceptionHandler;
import cn.enncloud.iot.gateway.tcp.process.LoginProcesser;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Netty 网关
 *
 * @author hanyilong@enn.cn
 */
@Slf4j
@Service("nettyTcpConnector")
public class TcpConnectorStarter {

    @Autowired
    TcpConfig tcpConfig;

    @Autowired
    DeviceContext deviceContext;
    @Autowired
    ExceptionHandler exceptionHandler;

    @Autowired
    ProtocolManager protocolManager;

    @Autowired
    LoginProcesser loginProcesser;

    @Autowired
    TcpSessionManger tcpSessionManger;

    @Autowired
    ConnectorManager connectorManager;






    @PostConstruct
    @SneakyThrows
    public void init() {
        if (tcpConfig == null){
           return;
        }
        if(tcpConfig.getServer() != null){
            initServer();
        }
        if(tcpConfig.getClient() != null){
            initClient();
        }
    }



    private void initServer() throws Exception {
        TcpConfig.TcpServerConnectorConfig server = tcpConfig.getServer();
        if(server == null || server.getConfiguration() == null || server.getConfiguration().isEmpty()){
            return;
        }
        for(TcpServerConfig tcpServerConfig: server.getConfiguration()){
            log.info("starting tcp server {} {}", server.getName(), server.getType());
            TcpConnectorServer tcpConnectorServer = new TcpConnectorServer(
                    tcpServerConfig,
                    protocolManager,
                    deviceContext,
                    tcpSessionManger,
                    loginProcesser,
                    exceptionHandler
            );
            connectorManager.addConnector(tcpConnectorServer);
            tcpConnectorServer.init();
        }
    }


    private void initClient() throws Exception {
        TcpConfig.TcpClientConnectorConfig client = tcpConfig.getClient();
        if(client == null || client.getConfiguration() == null || client.getConfiguration().isEmpty()){
            return;
        }
        for(TcpClientConfig tcpClientConfig: client.getConfiguration()){
            log.info("starting tcp client {} {}", client.getName(), client.getType());
            TcpConnectionClient tcpConnectionClient = new TcpConnectionClient(
                    tcpClientConfig,
                    protocolManager,
                    deviceContext,
                    tcpSessionManger,
                    loginProcesser,
                    exceptionHandler
            );
            connectorManager.addConnector(tcpConnectionClient);
            tcpConnectionClient.init();
        }
    }



}
