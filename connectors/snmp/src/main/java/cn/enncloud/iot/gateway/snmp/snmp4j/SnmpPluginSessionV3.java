package cn.enncloud.iot.gateway.snmp.snmp4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author willhu
 * @Date 2022/5/30 11:00
 */

public final class SnmpPluginSessionV3 {

    private static final long serialVersionUID = -4744070264035857273L;
    // slf4j  log
    private org.slf4j.Logger log = LoggerFactory.getLogger(SnmpPluginSessionV3.class);

    private Target target;

    private TableUtils tableUtils;
    /*   private PDU pdu;
*    private ScopedPDU scopedpdu;
      private USM usm;
      private Snmp snmp;
      private static final String OID_HOSTNAME = "1.3.6.1.2.1.1.2";*/
    private static final int MIN_RETRYS = 1;


    public boolean initSession(final Map<String, String> paramInfo) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("paramInfo: {} ", paramInfo.toString());
        }
        try {
            String ip = paramInfo.get("IP");
            String udpPort = paramInfo.get("UdpPort");
            String community = paramInfo.get("Community");
            String version = paramInfo.get("Version");
            String snmpTimeout = paramInfo.get("SnmpTimeout");
            String snmpRetry = paramInfo.get("SnmpRetry");

            if (StringUtils.isEmpty(udpPort)) {
                udpPort = "161";
            }
            UdpAddress add = new UdpAddress(ip + "/" + udpPort);
            if (version.equalsIgnoreCase("2c")) {
                initV2c(community, add, Long.parseLong(snmpTimeout), Integer.parseInt(snmpRetry));
            } else {
                throw new Exception("SnmpPluginSession Failed to Init. Wrong SNMP Version " + version);
            }

        } catch (Exception e) {
            throw new Exception("snmpPluginSession Failed to Init.", e);
        }
        return true;
    }

    private void initV2c(String community, Address address, long timeOut, int retry) {
 /*       if (tableUtils == null) {
            tableUtils = new TableUtils(SnmpEngine.getInstance().getSnmp(), new DefaultPDUFactory(PDU.GETBULK));
        }*/
        if (target == null) {
            target = new CommunityTarget(address, new OctetString(community));
        }
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(timeOut);
        if (retry < MIN_RETRYS) {
            target.setRetries(MIN_RETRYS);
        } else {
            target.setRetries(retry);
        }
    }

    public List<String[]> execute(final List<String> oidValues) throws Exception {
        List<String[]> result = null;
        PDU pdu = new PDU();

        if (oidValues == null || oidValues.isEmpty()) {
            throw new IllegalArgumentException("queryParamter is null.");
        }
        if (log.isDebugEnabled()) {
            log.debug("oidValues: {}", oidValues.toString());
            log.debug("target: {}", target.toString());
        }

        OID[] snmpoids = new OID[oidValues.size()];
        for (int i = 0; i < oidValues.size(); i++) {
            snmpoids[i] = new OID(oidValues.get(i));
            pdu.add(new VariableBinding(snmpoids[i]));
        }


        TransportMapping transportMapping = new DefaultUdpTransportMapping();
        transportMapping.listen();
        Snmp snmp = new Snmp(transportMapping);
        //snmp.listen();

        TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETNEXT));
        VariableBinding[] variableArr = null;
        TableEvent tableEvent = null;
        List eventList = tableUtils.getTable(target, snmpoids, null, null);

        if (eventList != null && eventList.size() > 0) {
            log.info(" get by tableuitls gettable method. eventList is not null, size is: {}", eventList.size());

            try {

                if (log.isDebugEnabled()) {
                    log.debug("snmp get oid result eventList size: {}", eventList == null ? "null" : eventList.size());
                }

                if (eventList != null && eventList.size() > 0) {
                    result = new ArrayList<>();
                    for (int i = 0; i < eventList.size(); i++) {
                        Object obj = eventList.get(i);
                        if (log.isDebugEnabled()) {
                            log.debug("i:{}, name is: {}, type is:{}", i, obj.getClass().getName(), obj.getClass().getTypeName());
                        }
                        tableEvent = (TableEvent) obj;
                        variableArr = tableEvent.getColumns();

                        OID oidIndex = tableEvent.getIndex();
                        PDU reportPDU = tableEvent.getReportPDU();

                        if (log.isDebugEnabled()) {
                            log.debug("i: {}, snmp get oid  status:{}", i, tableEvent.getStatus());
                            log.debug("i:{},is error:{}", i, tableEvent.isError());
                            log.debug("tableEvent: {}", tableEvent.toString());
                            if (tableEvent.isError()) {
                                log.debug("tableEvent.getErrorMessage(): {}", tableEvent.getErrorMessage());
                            }
                            if (null != oidIndex) {
                                log.debug("oidIndex: {}", oidIndex.toString());
                            }
                            if (null != reportPDU) {
                                log.debug("reportPDU.size:{}, content:{}", reportPDU.size(), reportPDU.toString());
                            }
                            if (null != tableEvent.getException()) {
                                log.error("tableEvent.getException(): ", tableEvent.getException());
                            }
                        }
                        if (null == variableArr) {
                            log.debug("i:{}, variableArr is null", i);
                            continue;
                        }
                        if (variableArr.length == 0) {
                            log.debug("i:{}, variableArr length is 0", i);
                            continue;
                        }
                        //  if (tableEvent.getStatus() == TableEvent.STATUS_OK && variableArr.length == oidValues.size()) {
                        String[] colArr = new String[variableArr.length];
                        for (int k = 0; k < variableArr.length; k++) {
                            if (variableArr[k] != null) {
                                colArr[k] = variableArr[k].getVariable().toString();
                            }
                        }
                        result.add(colArr);
                        // }
                        if (tableEvent.getException() != null) {
                            log.error(target.getAddress() + " status: " + tableEvent.getStatus() + " errorMessage:" + tableEvent.getErrorMessage());
                            log.error("snmp4j.getException: ", tableEvent.getException());
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    if (null != result) {
                        log.debug("result size: {}", result.size());
                    } else {
                        log.debug("result size: 0");
                    }
                    log.debug("result: {}", null == result ? "null" : result.toString());

                }
            } catch (Exception e) {
                log.error("SnmpPluginSession execute : ", e);
            }

        } else {
            log.info("get by snmp send method.");
            pdu.setType(PDU.GET);
            ResponseEvent responseEvent = snmp.send(pdu, target, transportMapping);
            PDU repose = responseEvent.getResponse();
            int num = 0;
            if (null != repose) {
                while (num < repose.size()) {
                    log.debug("index:{}  respose:{}", num, repose.get(num).toString());
                    num++;
                }
            } else {
                log.debug(" response is null.");
            }
        }


        return result;
    }


}
