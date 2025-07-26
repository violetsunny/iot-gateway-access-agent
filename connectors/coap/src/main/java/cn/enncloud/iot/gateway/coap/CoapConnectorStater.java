package cn.enncloud.iot.gateway.coap;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.protocol.Protocol;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.MyIpResource;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.TcpConfig;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.elements.tcp.netty.TcpServerConnector;
import org.eclipse.californium.elements.util.NetworkInterfacesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service("coapserver")
public class CoapConnectorStater extends CoapServer implements Connector {
	@Autowired
	ConnectorManager connectorManager;

	static {
		CoapConfig.register();
		UdpConfig.register();
		TcpConfig.register();
	}


	@PostConstruct
	@SneakyThrows
	public void init() {
		try {
			// create server
			boolean udp = true;
			boolean tcp = false;
			int port = Configuration.getStandard().get(CoapConfig.COAP_PORT);

			CoapConnectorStater server = new CoapConnectorStater();
			// add endpoints on all IP addresses
			server.addEndpoints(udp, tcp, port);
			server.start();
			log.info("coap server started!!!!!!!!!!");

			connectorManager.addConnector(this);
		} catch (SocketException e) {
			System.err.println("Failed to initialize server: " + e.getMessage());
		}
	}

	@Override
	public void setupProtocol(Protocol protocol, Map params) {

	}

	/**
	 * Add individual endpoints listening on default CoAP port on all IPv4
	 * addresses of all network interfaces.
	 */
	private void addEndpoints(boolean udp, boolean tcp, int port) {
		Configuration config = Configuration.getStandard();
		for (InetAddress addr : NetworkInterfacesUtil.getNetworkInterfaces()) {
			InetSocketAddress bindToAddress = new InetSocketAddress(addr, port);
			if (udp) {
				CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
				builder.setInetSocketAddress(bindToAddress);
				builder.setConfiguration(config);
				addEndpoint(builder.build());
			}
			if (tcp) {
				TcpServerConnector connector = new TcpServerConnector(bindToAddress, config);
				CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
				builder.setConnector(connector);
				builder.setConfiguration(config);
				addEndpoint(builder.build());
			}

		}
	}

	/*
	 * Constructor for a new Hello-World server. Here, the resources of the
	 * server are initialized.
	 */
	public CoapConnectorStater() throws SocketException {

		// provide an instance of a Hello-World resource
		add(new HelloWorldResource());
		add(new PubSubResource());
		add(new MyIpResource(MyIpResource.RESOURCE_NAME, true));
	}

	/*
	 * Definition of the Hello-World Resource
	 */
	static class HelloWorldResource extends CoapResource {

		public HelloWorldResource() {

			// set resource identifier
			super("helloWorld");
			// set display name
			getAttributes().setTitle("Hello-World Resource");
		}

		@Override
		public void handleGET(CoapExchange exchange) {

			// respond to the request
			exchange.respond("Hello World!");
		}
	}
	/*
	 * Definition of the Hello-World Resource
	 */
	static class PubSubResource extends CoapResource {

		private volatile String resource = "";

		public PubSubResource() {

			// set resource identifier
			super("pub");
			setObservable(true);
			// set display name
			getAttributes().setTitle("pub-sub Resource");
		}

		@Override
		public void handleGET(CoapExchange exchange) {

			// respond to the request
			exchange.respond(resource);
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			resource = exchange.getRequestText();
			// respond to the request
			exchange.respond(ResponseCode.CHANGED);
			changed();
		}
	}
	@Override
	public Map getStatus(){
		return null;
	}
}