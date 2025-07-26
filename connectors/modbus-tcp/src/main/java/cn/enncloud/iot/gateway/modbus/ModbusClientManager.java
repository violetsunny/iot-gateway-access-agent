package cn.enncloud.iot.gateway.modbus;

import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.modbus.constant.ByteOrderEnum;
import cn.enncloud.iot.gateway.modbus.constant.DataTypeEnum;
import cn.enncloud.iot.gateway.modbus.core.typed.ModbusFCode;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClient;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClientConfig;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理加载 modbusTcpClient
 */
@Component
public class ModbusClientManager {


    static ConcurrentHashMap<String, ModbusTcpClient> ModbusTcpClientMap = new ConcurrentHashMap<>();

    HashMap<String, ModbusTcpClient> clients = new HashMap<>();

    public void addClient(String ModbusTcpClientConfigId, ModbusTcpClient client) {
        clients.put(ModbusTcpClientConfigId, client);
    }

    public void removeClient(String ModbusTcpClientConfigId) {
        clients.remove(ModbusTcpClientConfigId);
    }

    public ModbusTcpClient getClient(String ModbusTcpClientConfigId) {
        return clients.get(ModbusTcpClientConfigId);
    }

    public Map<String, String> getClientStatus() {
        Map<String, String> statusMap = new HashMap<>();
        Set<Map.Entry<String, ModbusTcpClient>> uniqueValues = new HashSet<>();
        for (Map.Entry<String, ModbusTcpClient> entry : clients.entrySet()) {
            if (uniqueValues.add(entry)) {
                if (statusMap.put(entry.getKey(), entry.getValue().getStatus()) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
        }
        return statusMap;
    }


    @SneakyThrows
    public static void main(String[] args) {
//        float a = (float) 10.4;
//        ByteBuf buffer = Unpooled.buffer();
//        ByteBuf byteBuf = buffer.writeFloat(a);
//        String s = ByteBufUtil.hexDump(byteBuf);
//        System.out.println(s);
//        System.out.println(s+"1");
//        float v = byteBuf.readFloat();
//        run();

//        10.20.37.69", 9999

        List<ModbusPointMapping> pointDTOS = new ArrayList<>();
        pointDTOS.add(new ModbusPointMapping(1L, 1, ModbusFCode.READ_HOLDING_REGISTER, 100, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
        pointDTOS.add(new ModbusPointMapping(1L, 1, ModbusFCode.READ_INPUT_REGISTER, 103, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));

        ModbusTcpClientConfig modbusTcpClientConfig = new ModbusTcpClientConfig();
        modbusTcpClientConfig.setHost("10.20.37.69");
        modbusTcpClientConfig.setPort(9999);
        modbusTcpClientConfig.setPointDTOS(pointDTOS);
        modbusTcpClientConfig.setReadDiff(10);
        modbusTcpClientConfig.setMaxRetries(100);
        modbusTcpClientConfig.setRetryDelay(60);
        modbusTcpClientConfig.setConnectTimeout(5000);
        // TODO:修改 new ModbusDataHandler()
//        ModbusTcpClient modbusTcpClient = new ModbusTcpClient(new ModbusDataHandler(), modbusTcpClientConfig);
//        modbusTcpClient.run();
    }
//        Thread.sleep(40000);
//        modbusTcpClient.closeModbusClient();

}
