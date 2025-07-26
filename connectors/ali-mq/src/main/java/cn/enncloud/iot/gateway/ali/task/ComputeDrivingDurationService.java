//package cn.enncloud.iot.gateway.ali.task;
//
//import cn.enncloud.iot.gateway.ali.util.OkHttp3Util;
//import cn.enncloud.iot.gateway.context.DeviceContext;
//import cn.enncloud.iot.gateway.message.Metric;
//import cn.enncloud.iot.gateway.message.MetricReportRequest;
//import cn.enncloud.iot.gateway.message.enums.DataTypeEnum;
//import cn.enncloud.iot.gateway.message.enums.MessageType;
//import cn.enncloud.iot.gateway.parser.JsonMessagePayloadParser;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.Resource;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//@Component
//public class ComputeDrivingDurationService {
//
//    @Resource
//    DeviceContext deviceContext;
//
//    @Value("${zy.getTraffiCanalysisResInterfaceUrl:http://gatewayapi.lunztech.cn/api/ApiPlat/GetTraffiCanalysisResInterface}")
//    String url;
//
//    @Value("${zy.appKey:2C6074D0-C8E4-4BA4-A50A-E3A46EAD394A}")
//    String appKey;
//
//    @Value("${zy.productId:1754033461550440449}")
//    String productId;
//
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @Scheduled(cron = "${zy.task.computeDrivingDuration.cron:0 0/2 * * * ?}")
//    public void computeCurrentDayDrivingDuration() {
//        log.info("start 根据产品ID {} 查询sn(车架号)和imei号列表", productId);
//        // 根据产品ID(服务商)查询sn(车架号)和imei号列表
//        List<String> imeiByProductId = deviceContext.getImeiByProductId(productId);
//        log.info("获取到 imei {}", imeiByProductId.size());
//        if (!CollectionUtils.isEmpty(imeiByProductId)) {
//            imeiByProductId.forEach(imei -> {
//                // 循环调用zy接口
//                String result = getTraffiCanalysisResInterface(imei);
//                // 计算当日行驶时长
//                Long currentDayDrivingDuration = computer(result);
//                log.info("computeCurrentDayDrivingDuration imei {} DriveTimeYesterday {}", imei, currentDayDrivingDuration);
//                // 发送kafka
//                MetricReportRequest reportRequest = createMetricReportRequest(imei, currentDayDrivingDuration);
//                if (reportRequest != null) {
//                    log.info("computeCurrentDayDrivingDuration 发送kafka {} {}", reportRequest.getDeviceId(), reportRequest);
//                    deviceContext.storeMessage(reportRequest);
//                }
//            });
//        }
//        log.info("end task computeCurrentDayDrivingDuration");
//    }
//
//    private String getTraffiCanalysisResInterface(String imei) {
//        try {
//            Map<String, String> header = Collections.singletonMap("appKey", appKey);
//
//            Map<String, String> requestBody = new HashMap<>();
//            requestBody.put("DeviceNumber", imei);
//            requestBody.put("StartTime", getYesterdayDate());
//            requestBody.put("EndTime", getCurrentDate());
//            String requestJson = JSON.toJSONString(requestBody);
//
//            String result = OkHttp3Util.postJson(url, header, requestJson);
//
//            if (StringUtils.isNotEmpty(result)) {
//                log.info("getTraffiCanalysisResInterface: imei {} result {}", imei, result);
//                return result;
//            } else {
//                log.warn("查询 {} 行程 请求结果为空", imei);
//                return "";
//            }
//        } catch (Exception e) {
//            log.warn("查询 {} 行程 请求异常 ", imei, e);
//            return null;
//        }
//    }
//
//    private Long computer(String result) {
//        long currentDayDrivingDuration = 0L;
//        if (StringUtils.isNotEmpty(result)) {
//            JSONObject jsonObject = JSONObject.parseObject(result);
//            if (Boolean.TRUE.equals(jsonObject.getBoolean("Success"))) {
//                JSONArray jsonArray = jsonObject.getJSONArray("Data");
//                currentDayDrivingDuration = jsonArray.stream().map(obj -> ((JSONObject) obj).getLongValue("continue_t")).reduce(0L, Long::sum);
//            }
//        }
//        return currentDayDrivingDuration;
//    }
//
//    private MetricReportRequest createMetricReportRequest(String imei, Long currentDayDrivingDuration) {
//        MetricReportRequest reportRequest = new MetricReportRequest();
//        String deviceId = deviceContext.getDeviceIdByImei(imei);
//        if (StringUtils.isBlank(deviceId)) {
//            //没有对应的恩牛设备
//            log.warn("imei {} 没有对应的恩牛设备", imei);
//            return null;
//        }
//        reportRequest.setDeviceId(deviceId);
//        reportRequest.setMessageId(String.format("%s%s", deviceId, UUID.randomUUID().toString().replace("-", "")));
//        reportRequest.setMessageType(MessageType.DEVICE_REPORT_REQ);
//        long currentTimeMillis = System.currentTimeMillis();
//        reportRequest.setTimeStamp(currentTimeMillis);
//        reportRequest.setIngestionTime(currentTimeMillis);
//
//        Metric metric = new Metric((Long) JsonMessagePayloadParser.typeOf(currentTimeMillis, DataTypeEnum.LONG), "DriveTimeYesterday", currentDayDrivingDuration);
//        reportRequest.setMetrics(Collections.singletonList(metric));
//
//        return reportRequest;
//    }
//
//    private String getCurrentDate() {
//        return LocalDate.now().format(DATE_FORMATTER);
//    }
//
//    private String getNextDate() {
//        return LocalDate.now().plusDays(1).format(DATE_FORMATTER);
//    }
//
//    private String getYesterdayDate() {
//        return LocalDate.now().plusDays(-1).format(DATE_FORMATTER);
//    }
//
//}
