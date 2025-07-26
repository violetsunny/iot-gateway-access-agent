/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.request;

import cn.enncloud.iot.gateway.config.connectors.HttpRequestConfig;
import cn.enncloud.iot.gateway.config.connectors.HttpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.context.TrdPlatformCloudServer;
import cn.enncloud.iot.gateway.entity.cloud.*;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.service.RedisService;
import cn.enncloud.iot.gateway.timer.annotation.EnnIotXxlJob;
import cn.enncloud.iot.gateway.timer.handler.EnnIotXxlJobHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.kdla.framework.exception.BizException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author kanglele
 * @version $Id: CloudTaskManger, v 0.1 2024/3/21 14:55 kanglele Exp $
 */
@Slf4j
@Component
@EnnIotXxlJob("CloudJob")
public class TrdPlatformCloudJob extends EnnIotXxlJobHandler {

    @Resource
    private DeviceContext deviceContext;
    @Resource
    private TrdPlatformCloudServer trdPlatformCloudServer;
    @Resource
    private ProtocolManager protocolManager;
    @Resource
    private HttpRequestConfig httpRequestConfig;
    @Resource
    private RedisService redisService;

    @Value("${ennew.iot.switch.reslog:false}")
    private Boolean reslog;

    public void exeTask(String pCode, String taskCode) throws Exception {
        TrdPlatformReq trdPlatformReq = trdPlatformCloudServer.taskReqContext(pCode, taskCode);
        if (trdPlatformReq == null|| CollectionUtils.isEmpty(trdPlatformReq.getReqChildren())) {
            log.info("{} {} 没有可执行请求", pCode, taskCode);
            return;
        }
        for (TrdPlatformReqTask reqChild : trdPlatformReq.getReqChildren()) {
            double limit = reqChild.getLimit();
            if (limit == 0D) {
                limit = reqChild.getBodies().size();
            }
            RateLimiter rateLimiter = RateLimiter.create(limit);
            reqChild.getBodies().forEach(msg -> {
                // 获取一个令牌，如果没有令牌则阻塞
                rateLimiter.acquire();

                try {
                    if (reqChild.getApiType() != null && TrdPlatformEnum.ApiTypeEnum.AUTH.getCode() == reqChild.getApiType()) {
                        TrdPlatformAuthToken authToken = trdPlatformCloudServer.authRefreshToken(trdPlatformReq.getPCode(), reqChild.getAuthApi(), msg);
                        log.info("{} {} {} {} 刷新token:{}", trdPlatformReq.getPCode(), reqChild.getCode(), trdPlatformReq.getProductId(), reqChild.getAuthApi(), JSON.toJSONString(authToken));
                    } else {
                        dealCloudData(trdPlatformReq.getPCode(), reqChild.getCode(), trdPlatformReq.getProductId(), msg);
                    }

                } catch (Exception e) {
                    log.error("{} {} {} {} 请求失败，原因：", pCode, taskCode, trdPlatformReq.getProductId(), JSON.toJSONString(reqChild), e);
                }
            });
        }

    }

    private void dealCloudData(String pCode, String taskCode, String productId, TrdPlatformBody message) throws Exception {
        log.info("TrdPlatformCloudJob send  {} {} {} req:{}", pCode, taskCode, productId, JSON.toJSONString(message));
        JSONObject obj = trdPlatformCloudServer.sendRequest(message.getMethod(), message.getUrl(), message.getHeader(), message.getBody(), JSONObject.class);
        if (reslog) {
            log.info("TrdPlatformCloudJob res  {} {} {}  res:{}", pCode, taskCode, productId, JSON.toJSONString(obj));
        }
        //处理返回数据
        Protocol protocol = null;
        //转成产品找协议id 有三种加载协议的方式
        List<Object> protocolIds = redisService.getThirdCloudProductId(productId);
        if (CollectionUtils.isNotEmpty(protocolIds) && StringUtils.isNotBlank((String) protocolIds.get(0))) {
            //上行
            protocol = protocolManager.get((String) protocolIds.get(0));
        }
        if (protocol == null) {
            if (CollectionUtils.isNotEmpty(httpRequestConfig.getConfiguration())) {
                HttpServerConfig httpConfigProtocol = httpRequestConfig.getConfiguration().stream().filter(protocolConfig -> pCode.equals(protocolConfig.getAppid()) && taskCode.equalsIgnoreCase(protocolConfig.getServer())).findFirst().orElse(null);
                if (httpConfigProtocol != null && httpConfigProtocol.getProtocol() != null) {
                    protocol = protocolManager.get(httpConfigProtocol.getProtocol().getMainClass() + productId);
                }
            }
        }
        if (protocol == null) {
            String protocolId = trdPlatformCloudServer.getTrdPlatformProtocol(pCode, TrdPlatformEnum.FunctionTypeEnum.UP);
            if (StringUtils.isNotBlank(protocolId)) {
                protocol = protocolManager.get(protocolId);
            }
        }

        if (protocol == null) {
            log.warn("dealCloudData {} {} {} 没有找到协议", pCode, taskCode, productId);
            return;
        }

        protocol.setDeviceContext(deviceContext);
        //http server和 http client 应该统一入参：请求路径，请求体，返回体，映射关系
        List<? extends Message> cloudMessages = protocol.decodeMulti(JSONObject.toJSONBytes(obj), taskCode, productId, pCode, message);
        log.info("dealCloudData {} {} {} 解析后数据：{}", pCode, taskCode, productId, JSON.toJSONString(cloudMessages));
        if (CollectionUtils.isNotEmpty(cloudMessages)) {
            for (Message cloudMessage : cloudMessages) {
                protocol.getDeviceContext().storeMessage(cloudMessage);
            }
        }

    }

    @Override
    public boolean doExecute(String jobParam) throws Exception {
        try {
            String pCode = StringUtils.split(jobParam, ",")[0];
            String taskCode = StringUtils.split(jobParam, ",")[1];
            log.info("TrdPlatformCloudJob {} {} 调度过来啦！！！！", pCode, taskCode);
            exeTask(pCode, taskCode);
            return true;
        } catch (BizException e) {
            log.warn("TrdPlatformCloudJob 请求失败，原因：{}", ExceptionUtils.getMessage(e));
        } catch (Exception e) {
            log.error("TrdPlatformCloudJob 请求失败，原因：", e);
        }
        return false;
    }
}
