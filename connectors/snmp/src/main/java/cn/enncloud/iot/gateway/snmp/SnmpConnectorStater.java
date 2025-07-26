package cn.enncloud.iot.gateway.snmp;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.SnmpConfig;
import cn.enncloud.iot.gateway.config.connectors.SnmpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * @author hanyilong@enn.cn
 */
@Component
@Slf4j
public class SnmpConnectorStater implements Connector {
    @Autowired
    SnmpConfig snmpConfig;

    @Autowired
    ProtocolManager protocolManager;

    @Autowired
    DeviceContext deviceContext;

    @Autowired
    ConnectorManager connectorManager;

    SnmpTimerTask snmpTimerTask;
    Map<String, SnmpTimerTask> tasks = new HashMap<>();

    @Override
    @PostConstruct
//    @SneakyThrows
    public void init() throws Exception {
        log.info("snmp连接器初始化============");
        connectorManager.addConnector(this);
        List<SnmpServerConfig> protocols = snmpConfig.getConfiguration();
        if(CollectionUtils.isEmpty(protocols)){
            return;
        }
        for (SnmpServerConfig server : protocols) {
            Protocol protocol = protocolManager.register(server.getProtocol());
            protocol.setDeviceContext(deviceContext);
            log.info("snmp连接器初始化成功===========");
            log.info("snmp定时任务初始化=============");
            snmpTimerTask = new SnmpTimerTask(protocol, server);
            Timer timer = new Timer();
            timer.schedule(snmpTimerTask, new Date(), server.getIntervalMinute() * 1000 * 60);
            tasks.put(server.getAddress(), snmpTimerTask);
            log.info("snmp定时任务初始化成功=============");
        }
    }

    @Override
    @SneakyThrows
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus() {
        log.info("snmp getStatus===========");
        return null;
    }

}
