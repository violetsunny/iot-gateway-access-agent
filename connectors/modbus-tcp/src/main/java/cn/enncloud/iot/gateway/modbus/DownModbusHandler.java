package cn.enncloud.iot.gateway.modbus;


import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import cn.enncloud.iot.gateway.exception.EncodeMessageException;
import cn.enncloud.iot.gateway.message.OperationRequest;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointWriteDTO;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClient;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 上行指令数据解析为modbus指令
 */
@Component
public class DownModbusHandler {


    @Autowired
    ModbusDeviceSessionManager deviceSessionManager;

    @Autowired
    ModbusClientManager modbusClientManager;

    public void handlerCmd(String cmd) {


        OperationRequest operationRequest = JsonUtil.jsonToPojo(cmd, OperationRequest.class);
        String deviceId = operationRequest.getDeviceId();

        ModbusServerConfig session = deviceSessionManager.getSession(deviceId);
        ModbusTcpClient client = modbusClientManager.getClient(session.getClientId());
        client.send(operationRequest);

    }
}
