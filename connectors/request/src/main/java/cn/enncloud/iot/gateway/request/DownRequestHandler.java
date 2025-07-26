package cn.enncloud.iot.gateway.request;

import cn.enncloud.iot.gateway.config.connectors.HttpRequestConfig;
import cn.enncloud.iot.gateway.config.connectors.HttpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.context.TrdPlatformCloudServer;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformBody;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformReq;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformReqTask;
import cn.enncloud.iot.gateway.message.OperationRequest;
import cn.enncloud.iot.gateway.message.enums.MessageType;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.service.RedisService;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import cn.enncloud.iot.gateway.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import top.kdla.framework.dto.exception.ErrorCode;
import top.kdla.framework.exception.BizException;
import top.kdla.framework.supplement.http.VertxHttpClient;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下控
 *
 * @author hanyilong@enn.cn
 * kafka监听消息
 */
@Component
@Slf4j
public class DownRequestHandler {
    @Autowired
    DeviceContext deviceContext;
    @Autowired
    ProtocolManager protocolManager;
    @Autowired
    HttpRequestConfig httpRequestConfig;
    @Autowired
    RedisService redisService;
    @Resource
    private TrdPlatformCloudServer trdPlatformCloudServer;

    /**
     * 监听kafka消息
     *
     * @param consumerRecord kafka的消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     *                       使用autoStartup = "false"必须指定id
     */
    @KafkaListener(topics = {"${ennew.iot.topics.commandTopic:device_command_topic}"}, groupId = "deviceCommandListenerForRequest")
    public void listen(ConsumerRecord<Object, Object> consumerRecord) {
        try {
            log.info("grop deviceCommandListenerForRequest received command {}", consumerRecord.value());
            OperationRequest request = JsonUtil.jsonToPojo(consumerRecord.value().toString(), OperationRequest.class);
            String server = request.getFunction();
            String deviceId = request.getDeviceId();
//            Product product = deviceContext.getProductByDeviceId(deviceId);
//            Protocol protocol = product.getProtocol();
//            if (product.getConnectorType() != ConnectorType.REQUEST) {
//                return;
//            }
            String productId = redisService.getProductIdIdFromRedis(deviceId);
            if (StringUtils.isBlank(productId)) {
                throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "设备缓存不存在 %s", deviceId);
            }
            Protocol protocol = null;
            List<Object> protocolIds = redisService.getThirdCloudProductId(productId);
            //下行
            if (CollectionUtils.isNotEmpty(protocolIds) && StringUtils.isNotBlank((String) protocolIds.get(1))) {
                protocol = protocolManager.get((String) protocolIds.get(1));
            }
            if (protocol == null) {
                if (CollectionUtils.isNotEmpty(httpRequestConfig.getConfiguration())) {
                    HttpServerConfig httpConfigProtocol = httpRequestConfig.getConfiguration().stream().filter(protocolConfig -> productId.equals(protocolConfig.getProductId()) && server.equalsIgnoreCase(protocolConfig.getServer())).findFirst().orElse(null);
                    if (httpConfigProtocol != null && httpConfigProtocol.getProtocol() != null) {
                        protocol = protocolManager.get(httpConfigProtocol.getProtocol().getMainClass() + httpConfigProtocol.getProductId());
                    }
                }
            }
            if (protocol == null) {
                log.warn("deviceCommandListenerForRequest 没有找到协议");
                return;
            }

            if (request.getMessageType() == MessageType.CLOUD_OPERATION_REQ) {

                OperationRequest operationRequest = JsonUtil.jsonToPojo(consumerRecord.value().toString(), OperationRequest.class);
                protocol.setDeviceContext(deviceContext);
                byte[] encode = protocol.encode(operationRequest,productId,server);
                operationRequest = JsonUtil.jsonBytes2Object(encode, OperationRequest.class);

                //下行
                TrdPlatformReq trdPlatformReq = trdPlatformCloudServer.downReqContext(productId, server);
                if (trdPlatformReq == null) {
                    return;
                }
                for (TrdPlatformReqTask reqChild : trdPlatformReq.getReqChildren()) {
                    //10个并发，有3个数据。那就应该发3次kafka
                    double limit = reqChild.getLimit();
                    if (limit == 0D) {
                        limit = reqChild.getBodies().size();
                    }
                    OperationRequest finalOperationRequest = operationRequest;
                    RateLimiter rateLimiter = RateLimiter.create(limit);
                    reqChild.getBodies().forEach(msg -> {
                        // 获取一个令牌，如果没有令牌则阻塞
                        rateLimiter.acquire();

                        try {
                            dealCloudData(msg, finalOperationRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }

            }


        } catch (BizException e) {
            log.warn("deviceCommandListenerForRequest 失败，原因：{}", ExceptionUtils.getMessage(e));
        } catch (Exception e) {
            log.error("deviceCommandListenerForRequest error", e);
        }
    }

    private void dealCloudData(TrdPlatformBody message, OperationRequest operationRequest) throws Exception {
        JSONObject req = JsonUtil.jsonToPojo(JSONObject.toJSONString(operationRequest), JSONObject.class);
        String url = StringUtil.replaceUrl(message.getUrl(), req);
        JSONObject obj = trdPlatformCloudServer.sendRequest(message.getMethod(), url, message.getHeader(), operationRequest.getParam(), JSONObject.class);
        log.info("deviceCommandListenerForRequest 下控成功 {}", obj.toString());
    }

}
