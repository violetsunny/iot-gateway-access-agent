package cn.enncloud.iot.gateway.service;

import cn.enncloud.iot.gateway.cache.LocalCache;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.entity.Device;
import cn.enncloud.iot.gateway.entity.Product;
import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import cn.enncloud.iot.gateway.entity.tsl.ProductTsl;
import cn.enncloud.iot.gateway.integration.device.DeviceClient;
import cn.enncloud.iot.gateway.integration.device.model.*;
import cn.enncloud.iot.gateway.integration.other.TrdPlatformApiClient;
import cn.enncloud.iot.gateway.integration.other.model.*;
import cn.enncloud.iot.gateway.kafka.KafkaProducer;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.Metric;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.kdla.framework.dto.MultiResponse;
import top.kdla.framework.dto.SingleResponse;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service("deviceContext")
public class EnnewDeviceContext implements DeviceContext {

    @Resource
    private DeviceClient deviceClient;

    @Resource
    private RedisService redisService;

    @Resource
    private LocalCache localCache;

    @Resource
    private TrdPlatformApiClient trdPlatformApiClient;

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public ProductTsl getTslByDeviceId(String deviceId) {
        return null;
    }

    @Override
    public ProductTsl getTslByProductId(String productId) {
        return null;
    }

    @Override
    public ProductTsl getTslByCode(String tslCode) {
        return null;
    }

    @Override
    public String getDeviceIdBySn(String projectId, String sn) {
        try {
            // sn = projectId + sn;
            String deviceId = localCache.getDeviceId(sn);
            if (deviceId != null) {
                return deviceId;
            }
            SingleResponse<DeviceDataRes> response = deviceClient.getBySN(sn);
            log.info("EnnewDeviceContext deviceId {}", JSONObject.toJSONString(response));
            if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
                deviceId = response.getData().getId();
                String productId = response.getData().getProductId();
                localCache.putProductId(deviceId, productId);
                localCache.putDeviceId(sn, deviceId);
                localCache.putSnProductId(sn, productId);
                localCache.putSn(deviceId, sn);
                return deviceId;
            } else {
                // 可能外部用的就是恩牛id
                if (StringUtils.isNotBlank(getSnByDeviceId(sn))) {
                    return sn;
                }
            }

        } catch (Exception e) {
            log.warn("EnnewDeviceContext getDeviceIdBySn exception {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean authDevice(String deviceId, String password) {
        return false;
    }

    @Override
    public Metric getLastDevcieMetric(String deviceId, String metric) {
        return null;
    }

    @Override
    public void storeMessage(Message message) {
        //发送数据
        kafkaProducer.send("",message);
    }

    @Override
    public List<Device> getSnByProductId(String productId) {
        try {
            int index = 1;
            int size = 100;
            List<DeviceDataRes> deviceInfoList = null;
            List<Device> snList = new ArrayList<>();
            do {
                PageConditionReq req = new PageConditionReq();
                req.setCurrent(index);
                req.setSize(size);
                req.setProductId(productId);
                MultiResponse<DeviceDataRes> response = deviceClient.getUsedSnList(req);
                log.info("EnnewDeviceContext getSnByProductId productId:{} {}", productId, JSONObject.toJSONString(response));
                if (response != null) {
                    deviceInfoList = new ArrayList<>(response.getData());
                    if (CollectionUtil.isNotEmpty(response.getData())) {
                        snList.addAll(response.getData().stream().map(DeviceDataRes::transformDevice).collect(Collectors.toList()));
                    }
                } else {
                    deviceInfoList = null;
                }

                index++;
            } while (CollectionUtils.isNotEmpty(deviceInfoList));

            return snList;
        } catch (Exception e) {
            log.warn("EnnewDeviceContext getSnByProductId exception {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> getImeiByProductId(String productId) {
        try {
            MultiResponse<DeviceDataRes> response = deviceClient.getSnListByProductId(productId);
            log.info("EnnewDeviceContext getImeiByProductId productId:{} {}", productId, JSONObject.toJSONString(response));
            if (Objects.nonNull(response) && CollectionUtils.isNotEmpty(response.getData())) {
                return response.getData().stream().map(DeviceDataRes::getVehicleImei).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("EnnewDeviceContext getImeiByProductId exception {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getDeviceIdByProductId(String productId) {
        return null;
    }

    @Override
    public Product getProductByDeviceId(String deviceId) {
        String productId = localCache.getProductId(deviceId);
        if (StringUtils.isBlank(productId)) {
            getSnByDeviceId(deviceId);
            productId = localCache.getProductId(deviceId);
        }
        Product product = new Product();
        product.setProductId(productId);

        String upProtocolId = "";
        String downProtocolId = "";
        List<Object> protocolIds = redisService.getThirdCloudProductId(productId);
        if (CollectionUtils.isNotEmpty(protocolIds)) {
            upProtocolId = (String) protocolIds.get(0);
            if (protocolIds.size() > 1) {
                downProtocolId = (String) protocolIds.get(1);
            }
        }
        if (StringUtils.isBlank(upProtocolId)) {
            upProtocolId = localCache.getProtocolId(productId);
        }
        if (StringUtils.isNotBlank(upProtocolId)) {
            product.setUpProtocol(upProtocolId);
        }
        if (StringUtils.isNotBlank(downProtocolId)) {
            product.setDownProtocol(downProtocolId);
        }
        return product;
    }

    @Override
    public String getSnByDeviceId(String deviceId) {
        try {
            String sn = localCache.getSn(deviceId);
            if (sn != null) {
                return sn;
            }
            SingleResponse<DeviceDataRes> response = deviceClient.getDeviceId(deviceId);
            log.info("EnnewDeviceContext getSnByDeviceId deviceId {} {}", deviceId, JSONObject.toJSONString(response));
            if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
                String productId = response.getData().getProductId();
                localCache.putProductId(deviceId, productId);
                sn = response.getData().getSn();
                if (StringUtils.isNotBlank(sn)) {
                    localCache.putDeviceId(sn, deviceId);
                    localCache.putSn(deviceId, sn);
                    localCache.putSnProductId(sn, productId);
                    return sn;
                }
            }
        } catch (Exception e) {
            log.warn("EnnewDeviceContext getSnByDeviceId exception {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getDeviceIdByImei(String imei) {
        try {
            String deviceId = redisService.getDeviceIdByImei(imei);
            log.info("EnnewDeviceContext getDeviceIdByImei {} {}", imei, deviceId);
            return deviceId;
        } catch (Exception e) {
            log.warn("EnnewDeviceContext getDeviceIdByImei exception {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void updateImei(List<Device> sns) {
        try {
            List<DeviceSnReq> req = sns.stream().map(d -> {
                DeviceSnReq sn = new DeviceSnReq();
                sn.setSn(d.getSn());
                sn.setVehicleImei(d.getVehicleImei());
                return sn;
            }).collect(Collectors.toList());
            SingleResponse response = deviceClient.batchUpdate(req);
            log.info("EnnewDeviceContext updateImei {} {}", JSON.toJSONString(sns), JSONObject.toJSONString(response));
        } catch (Exception e) {
            log.warn("EnnewDeviceContext updateImei exception {}", e.getMessage());
        }
    }

    @Override
    public String getProductProtocolBySn(String sn) {
        String deviceId = getDeviceIdBySn(null, sn);
        if (deviceId == null) {
            log.debug("deviceId not found on enn platform, sn={}", sn);
            return null;
        }
        String productId = Optional.ofNullable(localCache.getProductId(deviceId))
                .orElseGet(() -> Optional.ofNullable(deviceClient.getBySN(sn))
                        .map(SingleResponse::getData)
                        .map(DeviceDataRes::getProductId)
                        .orElse(null));
        if (productId == null) {
            log.debug("productId not found on enn platform, deviceId={}", deviceId);
            return null;
        }
        String protocolId = localCache.getProtocolId(productId);
        if (protocolId != null) {
            return protocolId;
        }
        protocolId = Optional.ofNullable(deviceClient.getProductProtocol(new ProductAccessConfigReq(productId)))
                .map(SingleResponse::getData)
                .map(ProductAccessConfigRes::getMessageProtocolList)
                .flatMap(items -> items.stream()
                        .filter(item -> "biz_up_message_protocol_id".equals(item.getItemKey()))
                        .map(ProductAccessConfigRes.Item::getItemValue)
                        .findFirst())
                .orElse(null);
        if (protocolId != null) {
            localCache.putProtocolId(productId, protocolId);
        }
        return protocolId;
    }

    @Override
    public Map<String, String> modelRef(String pCode, String productId, Object modelRefObj) {
        Map<String, String> modelRef = null;
        if (modelRefObj != null) {
            JSONObject metricData = JSONObject.parseObject(JSONObject.toJSONString(modelRefObj));
            modelRef = metricData.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> String.valueOf(entry.getValue()),
                            (oldValue, newValue) -> oldValue
                    ));
        }
        if (StringUtils.isNotBlank(productId)) {
            try {
                SingleResponse<TrdPlatformModelRefDto> response = trdPlatformApiClient.modelRef(new TrdPlatformReqDto(null, pCode, null, productId, null));
                log.info("EnnewDeviceContext modelRef {} {}", productId, JSONObject.toJSONString(response));
                if (response != null && response.getData() != null && CollectionUtils.isNotEmpty(response.getData().getTrdPlatformMeasureRefList())) {
                    return response.getData().getTrdPlatformMeasureRefList().stream().collect(Collectors.toMap(TrdPlatformMeasureRefDto::getPlatformMeasureCode, TrdPlatformMeasureRefDto::getEnnMeasureCode, (o, n) -> o));
                } else {
                    return modelRef;
                }
            } catch (Exception e) {
                log.warn("EnnewDeviceContext modelRef exception {}", e.getMessage());
            }
        } else {
            return modelRef;
        }
        return null;
    }

    @Override
    public String modelRefMetric(String orgMetric, Map<String, String> modelRef) {
        if (MapUtils.isNotEmpty(modelRef)) {
            if (modelRef.containsKey(orgMetric)) {
                return modelRef.get(orgMetric);
            }
        }
        return orgMetric;
    }

    @Override
    public String registerDevice(Device sn) {
        try {
            DeviceAddReq addReq = new DeviceAddReq();
            addReq.setSn(sn.getSn());
            addReq.setName(sn.getName());
            addReq.setProductId(sn.getProductId());
            addReq.setTenantId(sn.getTenantId());
            addReq.setDeptId(sn.getDeptId());
            MultiResponse<String> response = deviceClient.batchCreate(Collections.singletonList(addReq));
            log.info("EnnewDeviceContext registerDevice-1 {} {}", JSON.toJSONString(sn), JSONObject.toJSONString(response));
            if (response != null && CollectionUtils.isNotEmpty(response.getData())) {
                String deviceId = response.getData().stream().map(d -> StringUtils.split(d, "|")[0]).findFirst().orElse(null);
                DevicePropReq propReq = new DevicePropReq();
                propReq.setId(deviceId);
                propReq.setExtend(sn.getExtend());
                SingleResponse response2 = deviceClient.updateById(propReq);
                log.info("EnnewDeviceContext registerDevice-2 {} {}", JSON.toJSONString(sn), JSONObject.toJSONString(response2));
                return deviceId;
            }
        } catch (Exception e) {
            log.warn("EnnewDeviceContext registerDevice exception {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void putDeviceProtocol(String device, String protocol) {
        try {
            redisService.putDeviceProtocol(device, protocol);
        } catch (Exception e) {
            log.warn("EnnewDeviceContext putDeviceProtocol Exception ", e);
        }
    }

    @Override
    public boolean validSnBelongProduct(String sn, String productId) {
        String productIdSn = localCache.getSnProductId(sn);
        if (StringUtils.isNotBlank(productIdSn) && StringUtils.isNotBlank(productId)) {
            return productIdSn.equals(productId);
        }
        return true;
    }

    @Override
    public List<ModbusPointMapping> getModbusPoint(String gatewayCodes) {
        if (StringUtils.isBlank(gatewayCodes)) {
            return null;
        }
        List<ModbusPointMapping> result = new ArrayList<>();
        String[] split = gatewayCodes.split(",");
        for (String gatewayCode : split) {
            Map<Long, ModbusPointMapping> res = new HashMap<>();
            MultiResponse<PointMappingDto> pointMappings = trdPlatformApiClient.getPointMapping(gatewayCode);
            if (pointMappings != null && CollectionUtils.isNotEmpty(pointMappings.getData())) {
                pointMappings.getData().forEach(pointMappingDto -> {
                    ModbusPointMapping modbusPointMapping = new ModbusPointMapping();
                    modbusPointMapping.setProductId(pointMappingDto.getProductId());
                    modbusPointMapping.setPointId(pointMappingDto.getPointId());
                    modbusPointMapping.setDeviceId(pointMappingDto.getDeviceId());
                    modbusPointMapping.setMetric(pointMappingDto.getMetric());
                    res.put(pointMappingDto.getPointId(), modbusPointMapping);
                });
            }
            MultiResponse<ModbusPointDto> modbusPoints = trdPlatformApiClient.getModbusPoints(gatewayCode);
            if (modbusPoints != null && CollectionUtils.isNotEmpty(modbusPoints.getData())) {
                modbusPoints.getData().forEach(modbusPointDto -> {
                    ModbusPointMapping modbusPointMapping = res.get(modbusPointDto.getPointId());
                    if (modbusPointMapping != null) {
                        modbusPointMapping.setRw(modbusPointDto.getRw());
                        modbusPointMapping.setSlaveAddr(modbusPointDto.getSlaveAddress());
                        modbusPointMapping.setRegisterAddress(modbusPointDto.getRegisterAddress());
                        modbusPointMapping.setByteOrder(modbusPointDto.getByteOrder());
                        modbusPointMapping.setDataType(modbusPointDto.getDataType());
                        modbusPointMapping.setFunctionCode(modbusPointDto.getFunctionCode());
                    }
                });
            }

            result.addAll(res.values());
        }

        return result;
    }
}
