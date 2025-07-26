//package cn.enncloud.iot.gateway.ali.task;
//
//import cn.enncloud.iot.gateway.ali.util.OkHttp3Util;
//import cn.enncloud.iot.gateway.context.DeviceContext;
//import cn.enncloud.iot.gateway.entity.Device;
//import cn.hutool.core.collection.CollectionUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.Resource;
//import java.io.Serializable;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//public class SupplyImeiService {
//
//    @Resource
//    DeviceContext ennewDeviceContext;
//
//    @Value("${zy.getCarUserInfoYouLisUrl:http://gatewayapi.lunztech.cn/api/ApiPlat/GetCarUserInfoYouLis}")
//    String url;
//
//    @Value("${zy.appKey:2C6074D0-C8E4-4BA4-A50A-E3A46EAD394A}")
//    String appKey;
//
//    @Scheduled(cron = "${zy.task.supplyImei.cron:0 0/2 * * * ?}")
//    public void supplyImei() {
//        log.info("start task supplyImei");
//        // 循环分页查询车架号、imei号-查中瑞
//        int index = 1;
//        int size = 100;
//        List<DeviceInfo> deviceInfoList;
//        do {
//            deviceInfoList = getCarUserInfoYouList(Integer.toString(index), Integer.toString(size));
//            if (CollectionUtil.isNotEmpty(deviceInfoList)) {
//                log.info("getCarUserInfoYouList index: {} VinNumbers: {}", index, deviceInfoList.stream().map(DeviceInfo::getVinNumber).collect(Collectors.toList()));
//                // 反写imei号
//                ennewDeviceContext.updateImei(deviceInfoList.stream().filter(deviceInfo -> deviceInfo.getIsWireLess() == 0).map(DeviceInfo::toDevice).collect(Collectors.toList()));
//            }
//            index++;
//        } while (!CollectionUtils.isEmpty(deviceInfoList));
//        log.info("end task supplyImei");
//    }
//
//    private List<DeviceInfo> getCarUserInfoYouList(String index, String size) {
//        List<DeviceInfo> deviceInfoList = new ArrayList<>();
//        try {
//            Map<String, String> header = Collections.singletonMap("appKey", appKey);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            Map<String, String> paging = new HashMap<>();
//            paging.put("pageIndex", index);
//            paging.put("pageSize", size);
//            requestBody.put("Paging", paging);
//            String requestJson = JSON.toJSONString(requestBody);
//
//            String result = OkHttp3Util.postJson(url, header, requestJson);
//
//            if (StringUtils.isNotEmpty(result)) {
//                JSONObject jsonObject = JSONObject.parseObject(result);
//                if (Boolean.TRUE.equals(jsonObject.getBoolean("Success"))) {
//                    deviceInfoList = Arrays.stream(jsonObject.getJSONArray("Data").toArray())
//                            .map(object -> JSON.parseObject(object.toString(), DeviceInfo.class))
//                            .collect(Collectors.toList());
//                } else {
//                    log.warn("查询车架号信息 请求结果异常 {}", result);
//                }
//            } else {
//                log.warn("查询车架号信息 请求结果为空");
//            }
//        } catch (Exception e) {
//            log.warn("查询车架号信息 请求异常 ", e);
//        }
//        return deviceInfoList;
//    }
//
//    @Data
//    public static class DeviceInfo implements Serializable {
//
//        private String vinNumber;
//        private String deviceNumber;
//        private String deviceTypeName;
//        private String installPerson;
//        private String installTime;
//        private String carBrandName;
//        private String carModelName;
//        private String remark;
//        private String appNo;
//        private int isWireLess;
//        private String plateNumber;
//
//        private Device toDevice() {
//            Device device = new Device();
//            device.setVehicleImei(deviceNumber);
//            device.setSn(vinNumber);
//            return device;
//        }
//
//    }
//
//}
