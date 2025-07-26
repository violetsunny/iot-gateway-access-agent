package cn.enncloud.iot.gateway.snmp.snmp4j;

import org.slf4j.LoggerFactory;
import org.snmp4j.AbstractTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.smi.OID;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SnmpEngine {

	private static SnmpEngine instance = new SnmpEngine();
	// slf4j  log
	private static org.slf4j.Logger log =  LoggerFactory.getLogger(SnmpEngine.class);
	private Snmp snmp;
	// key:udp address value:snmp
	private Map<String, Snmp> snmpV3Sessions = new ConcurrentHashMap<>();
	private DefaultUdpTransportMapping transport;
	private static Map<String, Map<String, Boolean>> oidExsitFlagCache = new ConcurrentHashMap<>();

	private SnmpEngine() {
		try {
			transport = new DefaultUdpTransportMapping();
			// Start the snmp engine.
			snmp = new Snmp(transport);
			transport.listen();
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("SnmpEngine init error", e);
			}
		}
	}

	public static SnmpEngine getInstance() {
		return instance;
	}

	public synchronized Snmp getV3Snmp(final String targetAddress) {
		if (targetAddress == null) {
			return null;
		}

		Snmp v3snmp = snmpV3Sessions.get(targetAddress);
		if (v3snmp == null) {
			try {
				MessageDispatcher dispatcher = new MessageDispatcherImpl();
				DefaultUdpTransportMapping tm = new DefaultUdpTransportMapping();
				tm.setSocketTimeout(5000);
				v3snmp = new Snmp(dispatcher, tm);
				v3snmp.listen();
				snmpV3Sessions.put(targetAddress, v3snmp);
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("error",e);
				}
			}
		}
		return v3snmp;
	}

	public Snmp getSnmp() {
		return snmp;
	}

	public DefaultUdpTransportMapping getTransport() {
		return transport;
	}

	public static boolean isOidExist(AbstractTarget target,
			TableUtils tableUtils, String oid) {
		Map<String, Boolean> deviceFlagCache = null;
		synchronized (oidExsitFlagCache) {
			deviceFlagCache = oidExsitFlagCache.get(target.getAddress()
					.toString());
			if (deviceFlagCache == null) {
				deviceFlagCache = new HashMap<String, Boolean>();
				oidExsitFlagCache.put(target.getAddress().toString(),
						deviceFlagCache);
			}
		}
		Boolean flag = null;
		synchronized (deviceFlagCache) {
			flag = deviceFlagCache.get(oid);
			if (flag == null) {
				List eventList = tableUtils.getTable(target,
						new OID[] { new OID(oid) }, null, null);
				if (eventList == null || eventList.isEmpty()) {
					flag = new Boolean(false);
				} else {
					flag = new Boolean(true);
				}
				deviceFlagCache.put(oid, flag);
			}
		}
		return flag.booleanValue();
	}
}
