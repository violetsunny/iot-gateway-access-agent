package cn.enncloud.iot.gateway.snmp;

import cn.enncloud.iot.gateway.config.connectors.SnmpServerConfig;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

// 变量 {deviceId}, {sn}, {productKey}
@Slf4j
public class SnmpTimerTask extends TimerTask {
    Protocol protocol;
    SnmpServerConfig snmpServerConfig;

    public SnmpTimerTask(Protocol protocol, SnmpServerConfig snmpServerConfig) {
        this.protocol = protocol;
        this.snmpServerConfig = snmpServerConfig;
    }

    @Override
    public void run() {
        try {
            //设定CommunityTarget
            CommunityTarget communityTarget = new CommunityTarget();
            //机器的地址
            Address address = GenericAddress.parse("udp:" + snmpServerConfig.getAddress() + "/" + snmpServerConfig.getPort());
            //设定地址
            communityTarget.setAddress(address);
            //设置snmp共同体
            communityTarget.setCommunity(new OctetString("huawei123"));
            //设置超时重试次数
            communityTarget.setRetries(snmpServerConfig.getRetry());
            //设置超时的时间
            communityTarget.setTimeout(snmpServerConfig.getTimeout());
            //设置使用的snmp版本
            communityTarget.setVersion(SnmpConstants.version2c);
            //设定采取的协议
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            //调用TransportMapping中的listen()方法，启动监听进程，接收消息，由于该监听进程是守护进程，最后应调用close()方法来释放该进程
            transport.listen();
            //创建SNMP对象，用于发送请求PDU
            Snmp snmp = new Snmp(transport);

            String[] oids = snmpServerConfig.getProtocol().getParams().get("OIDS").toString().split(",");
            PDU request = new PDU();
            for (String oid : oids) {
                //创建请求pdu,获取mib
                request.add(new VariableBinding(new OID(oid)));
            }
            //调用setType()方法来确定该pdu的类型
            request.setType(PDU.GET);
            //调用 send(PDU pdu,Target target)发送pdu，返回一个ResponseEvent对象
            ResponseEvent responseEvent = snmp.send(request, communityTarget);

            //通过ResponseEvent对象来获得SNMP请求的应答pdu，方法：public PDU getResponse()
            PDU response = responseEvent.getResponse();
            List<? extends VariableBinding> variableBindings = response.getVariableBindings();
            Map<String, Object> map = Maps.newHashMap();
            for (VariableBinding variableBinding : variableBindings) {
                String oid = variableBinding.getOid().toString();
                String value = variableBinding.getVariable().toString();
                // do something with oid and value
                map.put(oid, value);
            }
            List<? extends Message> messages = protocol.decodeMulti(JSON.toJSONBytes(map));
            if (CollectionUtil.isNotEmpty(messages)) {
                messages.forEach(message -> {
                    log.info("snmp测点数据保存{}", message);
                    protocol.getDeviceContext().storeMessage(message);
                    log.info("snmp测点数据保存成功");
                });
            }
            transport.close();
            snmp.close();
        } catch (DecodeMessageException e) {
            log.info("snmp协议解析数据失败{}", e.getMessage());
        } catch (IOException e) {
            log.info("snmp 连接异常{}", e.getMessage());
        }
    }


}
