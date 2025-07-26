package cn.enncloud.iot.gateway.modbus;


import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.ModbusConfig;
import cn.enncloud.iot.gateway.config.connectors.ModbusServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.modbus.dto.DevicePointMapDTO;
import cn.enncloud.iot.gateway.modbus.dto.ModbusPointDTO;
import cn.enncloud.iot.gateway.modbus.poll.ModbusDataHandler;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClient;
import cn.enncloud.iot.gateway.modbus.poll.ModbusTcpClientConfig;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class ModbusConnectorStater implements Connector {

    @Autowired
    ConnectorManager connectorManager;

    @Autowired
    ModbusDataHandler modbusDataHandler;
    @Autowired
    ModbusConfig config;

    @Autowired
    ProtocolManager protocolManager;

    @Autowired
    DeviceContext deviceContext;

    @Autowired
    ModbusClientManager modbusClientManager;

    @Autowired
    ModbusDeviceSessionManager deviceSessionManager;

    private static ExecutorService executorService = new ThreadPoolExecutor(
            1,
            5,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new CustomizableThreadFactory("Modbus-"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    @PostConstruct
    @Override
    public void init() throws Exception {
        log.info("初始化Modbus");
        if (CollectionUtils.isEmpty(config.getConfiguration())) {
            return;
        }
        for (ModbusServerConfig server : config.getConfiguration()) {

            ModbusTcpClientConfig modbusTcpClientConfig = new ModbusTcpClientConfig();
            modbusTcpClientConfig.setConfigId(server.getClientId());
            modbusTcpClientConfig.setGatewayCode(server.getGatewayCode());
            modbusTcpClientConfig.setHost(server.getHost());
            modbusTcpClientConfig.setPort(server.getPort());
            modbusTcpClientConfig.setReadDiff(Optional.ofNullable(server.getReadDiff()).orElse(10));
            modbusTcpClientConfig.setMaxRetries(Optional.ofNullable(server.getMaxRetries()).orElse(100));
            modbusTcpClientConfig.setRetryDelay(Optional.ofNullable(server.getRetryDelay()).orElse(60));
            modbusTcpClientConfig.setConnectTimeout(Optional.ofNullable(server.getConnectTimeout()).orElse(5000));

//            // 点表路径
//            String pointDTOSPath = server.getPointDTOSPath();
//
////            List<ModbusPointDTO> pointDTOS = new ArrayList<>();
////            pointDTOS.add(new ModbusPointDTO(1L, 1, ModbusFCode.READ_HOLDING_REGISTER, 100, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
////            pointDTOS.add(new ModbusPointDTO(1L, 1, ModbusFCode.READ_INPUT_REGISTER, 103, DataTypeEnum.getInstance("int32").getValue(), ByteOrderEnum.getInstance("1234").getValue()));
//
//            List<ModbusPointDTO> modbusPoints = new ArrayList<>();
//            if (pointDTOSPath.contains("BatchConfig:")) {
//                modbusPoints = batchGetModbusPoints(pointDTOSPath);
//            } else {
//                modbusPoints = getModbusPoints(pointDTOSPath);
//            }
//            if (CollectionUtils.isEmpty(modbusPoints)) {
//                log.warn("modbus client init warning,modbus point 获取异常{}", pointDTOSPath);
//                continue;
//            }
//            modbusTcpClientConfig.setPointDTOS(modbusPoints);
//
//            // 点位设备映射信息路径
//            String pointMapDTOSPath = server.getPointMapDTOSPath();
////            modbusTcpClientConfig.setPointMapDTOS();
//
////            DevicePointMapDTO devicePointMapDTO = new DevicePointMapDTO();
////            devicePointMapDTO.setDeviceId("11233");
////            devicePointMapDTO.setMetric("power");
////            devicePointMapDTO.setPointId(1L);
////            devicePointMapDTO.setProductCode("12333");
////            devicePointMapDTO.setRw("r");
////            List<DevicePointMapDTO> devicePointMapDTOS = Collections.singletonList(devicePointMapDTO);
//            List<DevicePointMapDTO> devicePointMapDTOS = new ArrayList<>();
//            if (pointMapDTOSPath.contains("BatchConfig:")) {
//                devicePointMapDTOS = batchGetPointMap(pointMapDTOSPath);
//            } else {
//                devicePointMapDTOS = getPointMap(pointMapDTOSPath);
//            }
//
//            if (CollectionUtils.isEmpty(devicePointMapDTOS)) {
//                log.warn("modbus client init warning,device modbus pointMap 获取异常{}", pointMapDTOSPath);
//                continue;
//            }

//            HashMap<Long, DevicePointMapDTO> pointMap = buildPointMap(devicePointMapDTOS);

            ModbusDeviceProtocol protocol = new ModbusDeviceProtocol();
//            protocol.setParams(pointMap);
            protocol.setDeviceContext(deviceContext);

//            UpModbusHandler upModbusHandler = new UpModbusHandler(protocol, server, deviceSessionManager);
            ModbusTcpClient modbusTcpClient = new ModbusTcpClient(modbusDataHandler, modbusTcpClientConfig, protocol, server);
            modbusTcpClient.run();

            // 添加连接管理
            modbusClientManager.addClient(server.getClientId(), modbusTcpClient);

            // 添加设备连接对应关系
            modbusTcpClientConfig.getPointDTOS().stream().map(ModbusPointMapping::getDeviceId).distinct().forEach(dev -> deviceSessionManager.addSession(dev, server));

        }
        Map<String, String> clientStatus = modbusClientManager.getClientStatus();
        connectorManager.addConnector(this);
    }

    private List<DevicePointMapDTO> batchGetPointMap(String pointMapDTOSPath) {

        List<DevicePointMapDTO> result = new ArrayList<>();

        //BatchConfig:http://192.168.26.253:8870/access/cloud-gateway/{gatewayId}/modbus/points,MAQT01,MAQT02

        String[] split = pointMapDTOSPath.split(",");

        String url = split[0].replace("BatchConfig:", "");

        for (int i = 1; i < split.length; i++) {

            String gatewayUrl = url.replace("{gatewayId}", split[i]);
            List<DevicePointMapDTO> pointMap = getPointMap(gatewayUrl);

            if (CollectionUtils.isNotEmpty(pointMap)) {
                result.addAll(pointMap);
            } else {
                log.warn("modbus 批量获取点表设备映射关系为空，gatewayId={}", split[i]);
            }
        }


        return result;
    }

    private List<ModbusPointDTO> batchGetModbusPoints(String pointDTOSPath) {
        List<ModbusPointDTO> result = new ArrayList<>();

        //BatchConfig:http://192.168.26.253:8870/access/cloud-gateway/{gatewayId}/point/mapping,MAQT01,MAQT02
        String[] split = pointDTOSPath.split(",");

        String url = split[0].replace("BatchConfig:", "");

        for (int i = 1; i < split.length; i++) {

            String gatewayUrl = url.replace("{gatewayId}", split[i]);
            List<ModbusPointDTO> pointDTOS = getModbusPoints(gatewayUrl);

            if (CollectionUtils.isNotEmpty(pointDTOS)) {
                result.addAll(pointDTOS);
            } else {
                log.warn("modbus 批量获取点表配置为空，gatewayId={}", split[i]);
            }
        }
        return result;
    }


//    public static void main(String[] args) {
//        ModbusConnectorStater modbusConnectorStater = new ModbusConnectorStater();
//////        List<DevicePointMapDTO> pointMap = modbusConnectorStater.getPointMap("http://localhost:8870/access/cloud-gateway/zidonghua_test/point/mapping");
//////        List<ModbusPointDTO> modbusPoints = modbusConnectorStater.getModbusPoints("http://localhost:8870/access/cloud-gateway/zidonghua_test/modbus/points");
////
//        List<DevicePointMapDTO> pointMap = modbusConnectorStater.batchGetPointMap("BatchConfig:https://iot-gateway-other.fat.ennew.com/access/cloud-gateway/{gatewayId}/point/mapping,zidonghua_test,aaa");
//        List<ModbusPointDTO> modbusPoints = modbusConnectorStater.batchGetModbusPoints("BatchConfig:https://iot-gateway-other.fat.ennew.com/access/cloud-gateway/{gatewayId}/modbus/points,zidonghua_test,aaa");
////        https://iot-gateway-other.fat.ennew.com/access/cloud-gateway/zidonghua_test/modbus/points
//        System.out.println(pointMap);
//        System.out.println(modbusPoints);
//    }

    private List<DevicePointMapDTO> getPointMap(String pointMapDTOSPath) {

        List<DevicePointMapDTO> devicePointMapDTOS = null;
        try {
            String res = HttpUtil.get(pointMapDTOSPath);
            if (pointMapDTOSPath.contains("cloud-gateway")) {
                JSONObject jsonObject = JSONUtil.parseObj(res);
                res = jsonObject.getStr("data");
            }
            devicePointMapDTOS = JSONUtil.toList(res, DevicePointMapDTO.class);
        } catch (Exception e) {
            log.warn("设备点表映射信息获取异常：{}", e.getMessage());
        }

        if (CollectionUtils.isNotEmpty(devicePointMapDTOS) && pointMapDTOSPath.contains("cloud-gateway")) {
            devicePointMapDTOS.forEach(devicePointMapDTO -> {
                if (devicePointMapDTO.getRw() == null) {
                    devicePointMapDTO.setRw("r");
                }
                devicePointMapDTO.setProductCode(devicePointMapDTO.getProductId());
            });
        }
        return devicePointMapDTOS;
    }

    private List<ModbusPointDTO> getModbusPoints(String pointDTOSPath) {
        List<ModbusPointDTO> modbusPointDTOS = new ArrayList<>();
        try {
            String res = HttpUtil.get(pointDTOSPath);
            if (pointDTOSPath.contains("cloud-gateway")) {
                JSONObject jsonObject = JSONUtil.parseObj(res);
                res = jsonObject.getStr("data");
            }
            JSONArray jsonArray = JSONUtil.parseArray(res);

            jsonArray.forEach(o -> {

                String toString = o.toString();
                ModbusPointDTO modbusPointDTO = JSONUtil.toBean(toString, ModbusPointDTO.class);
                JSONObject jsonObject = JSONUtil.parseObj(toString);
                modbusPointDTO.setSlaveAddr(jsonObject.getInt("slaveAddress"));

                modbusPointDTOS.add(modbusPointDTO);


            });
        } catch (Exception e) {
            log.warn("设备modbus点表信息获取异常：{}", e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(modbusPointDTOS) && pointDTOSPath.contains("cloud-gateway")) {
            modbusPointDTOS.forEach(modbusPointDTO -> {
                if (modbusPointDTO.getSlaveAddr() == null) {
                    modbusPointDTO.setSlaveAddr(1);
                }
            });
        }
        return modbusPointDTOS;
    }

    public static HashMap<Long, DevicePointMapDTO> buildPointMap(List<DevicePointMapDTO> devicePointMapDTOS) {

        HashMap<Long, DevicePointMapDTO> pointMap = new HashMap<>();
        for (DevicePointMapDTO pointMapDTO : devicePointMapDTOS) {
            pointMap.put(pointMapDTO.getPointId(), pointMapDTO);
        }
        return pointMap;
    }

    @Override
    public void setupProtocol(Protocol protocol, Map<String, Object> params) {

    }

    @Override
    public Map<String, Object> getStatus() {
        return null;
    }
}
