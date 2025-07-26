//package cn.enncloud.iot.gateway.request;
//
//import cn.enncloud.iot.gateway.config.connectors.HttpServerConfig;
//import cn.enncloud.iot.gateway.context.CloudDockingServer;
//import cn.enncloud.iot.gateway.context.DeviceContext;
//import cn.enncloud.iot.gateway.message.Message;
//import cn.enncloud.iot.gateway.protocol.Protocol;
//import cn.enncloud.iot.gateway.entity.cloud.CloudDockingAuthTokenBo;
//import cn.enncloud.iot.gateway.entity.cloud.CloudDockingBodyBo;
//import cn.enncloud.iot.gateway.entity.cloud.CloudDockingTypeEnum;
//import cn.hutool.json.JSONObject;
//import com.alibaba.fastjson.JSON;
//import com.google.common.util.concurrent.RateLimiter;
//import io.vertx.core.http.HttpMethod;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import top.kdla.framework.dto.exception.ErrorCode;
//import top.kdla.framework.exception.BizException;
//
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.TimerTask;
//
//// 变量 {deviceId}, {sn}, {productKey}
//@Slf4j
//public class RequestTimerTask extends TimerTask {
//    Protocol protocol;
//    HttpServerConfig httpServer;
//    DeviceContext deviceContext;
//    CloudDockingServer cloudDockingServer;
//    String dataCode;
//
//    public RequestTimerTask(Protocol protocol, HttpServerConfig httpServer, DeviceContext deviceContext, CloudDockingServer cloudDockingServer, String dataCode) {
//        this.protocol = protocol;
//        this.httpServer = httpServer;
//        this.deviceContext = deviceContext;
//        this.cloudDockingServer = cloudDockingServer;
//        this.dataCode = dataCode;
//    }
//
//    @Override
//    public void run() {
//        log.info("RequestTimerTask {} {} 调度过来啦！！！！", httpServer.getAppid(), dataCode);
//        requestRemoteServer();
//    }
//
//    private void requestRemoteServer() {
//        try {
//            CloudDockingAuthTokenBo tokenBO = cloudDockingServer.createAuthToken(httpServer.getAppid());
//            Map<String, List<CloudDockingBodyBo>> messagesMap = cloudDockingServer.getCloudDockingBody(httpServer.getAppid(), tokenBO, dataCode, CloudDockingTypeEnum.Updown.UP.getCode());
//            if(MapUtils.isEmpty(messagesMap)){
//                throw new BizException(ErrorCode.BIZ_ERROR.getCode(),"没有可以执行的配置 %s %s",httpServer.getAppid(),dataCode);
//            }
//            for (Map.Entry<String, List<CloudDockingBodyBo>> v : messagesMap.entrySet()) {
//                //10个并发，有3个数据。那就应该发3次kafka
//                Integer limit = v.getValue().get(0).getLimit();
//                if (limit == null || limit <= 0) {
//                    limit = v.getValue().size();
//                }
//                RateLimiter rateLimiter = RateLimiter.create(limit);
//                v.getValue().forEach(msg -> {
//                    // 获取一个令牌，如果没有令牌则阻塞
//                    rateLimiter.acquire();
//
//                    try {
//                        dealCloudData(msg);
//                    } catch (Exception e) {
//                        log.error("请求失败，原因：", e);
//                    }
//                });
//            }
//        } catch (BizException e) {
//            log.warn("RequestTimerTask 请求失败，原因：{}", ExceptionUtils.getMessage(e));
//        } catch (Exception e) {
//            log.error("RequestTimerTask 请求失败，原因：", e);
//        }
//    }
//
//    public void dealCloudData(CloudDockingBodyBo message) throws Exception {
//        JSONObject obj = cloudDockingServer.sendRequest(HttpMethod.valueOf(message.getMethod().toUpperCase(Locale.ROOT)), message.getUrl(), message.getHeader(), message.getBody(), JSONObject.class);
//        //处理返回数据
//        protocol.setDeviceContext(deviceContext);
//        List<? extends Message> cloudMessages = protocol.decodeMulti(JSON.toJSONString(obj).getBytes());
//        log.info("RequestTimerTask dealCloudData 解析后数据：{}",JSON.toJSONString(cloudMessages));
//        for (Message cloudMessage : cloudMessages) {
//            protocol.getDeviceContext().storeMessage(cloudMessage);
//        }
//
//    }
//}
