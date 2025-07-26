package cn.enncloud.iot.gateway.modbus;

import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import cn.enncloud.iot.gateway.exception.DecodeMessageException;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.modbus.core.responses.ModbusResponse;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointDTO;
import cn.enncloud.iot.gateway.protocol.Protocol;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 上行点表数据解析为设备数据
 */
@Slf4j
public class UpModbusHandler {


    Protocol protocol;

    ModbusServerConfig modbusServerConfig;

    ModbusDeviceSessionManager modbusDeviceSessionManager;

    public Protocol getProtocol() {
        return protocol;
    }

    public UpModbusHandler(Protocol protocol, ModbusServerConfig modbusServerConfig, ModbusDeviceSessionManager modbusDeviceSessionManager) {
        this.protocol = protocol;
        this.modbusServerConfig = modbusServerConfig;
        this.modbusDeviceSessionManager = modbusDeviceSessionManager;
    }


    public void handler(ModbusResponse value, ModbusPointDTO modbusPointDTO) {

        // modbus数据解析为设备物模型数据
        List<? extends Message> messages = null;
        try {
            messages = protocol.decodeMulti(JSONObject.toJSONBytes(value), modbusPointDTO);
            log.info("{} 数据上报，{}", modbusServerConfig.getClientId(),JSONObject.toJSONString(messages));
        } catch (DecodeMessageException e) {
            log.warn("{} 设备数据decode异常，error",modbusServerConfig.getClientId(), e);
        }
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        // 数据存储
        messages.forEach(message -> protocol.getDeviceContext().storeMessage(message));

    }
}
