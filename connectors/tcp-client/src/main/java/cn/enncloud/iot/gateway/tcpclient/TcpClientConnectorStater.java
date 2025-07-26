package cn.enncloud.iot.gateway.tcpclient;

import java.util.Map;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.protocol.Protocol;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service("tcpClientConnect")
public class TcpClientConnectorStater implements Connector {
	@Autowired
	ConnectorManager connectorManager;



	@PostConstruct
	@SneakyThrows
	public void init() {

	}

	@Override
	public void setupProtocol(Protocol protocol, Map params) {

	}

	@Override
	public Map getStatus() {
		return null;
	}


}