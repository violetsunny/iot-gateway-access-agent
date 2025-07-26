package cn.enncloud.iot.gateway.ctwing;


import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.protocol.Protocol;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Service
public class CtwingConnectorStater implements Connector {

	@Autowired
	ConnectorManager connectorManager;

	@PostConstruct
	@SneakyThrows
	public void init() {
		connectorManager.addConnector(this);
		log.info("ctwing server started!!!!!!!!!!");
	}

	@Override
	public void setupProtocol(Protocol protocol, Map params) {

	}
	@Override
	public Map getStatus(){
		return null;
	}
}