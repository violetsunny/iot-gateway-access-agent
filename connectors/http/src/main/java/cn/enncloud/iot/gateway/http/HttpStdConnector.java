/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.http;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.entity.gateway.HttpEventDataCmd;
import cn.enncloud.iot.gateway.entity.gateway.HttpGatewayRtgCmd;
import cn.enncloud.iot.gateway.message.EventReportRequest;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.message.Metric;
import cn.enncloud.iot.gateway.message.MetricReportRequest;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.protocol.std.EnnStandardHttpProtocol;
import cn.enncloud.iot.gateway.utils.CommonUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.kdla.framework.common.aspect.watch.StopWatchWrapper;
import top.kdla.framework.dto.SingleResponse;
import top.kdla.framework.dto.exception.ErrorCode;
import top.kdla.framework.log.catchlog.CatchAndLog;
import top.kdla.framework.validator.BaseAssert;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kanglele
 * @version $Id: HttpStdConnector, v 0.1 2024/4/3 13:24 kanglele Exp $
 */
@Tag(name = "标准http接入")
@RestController
@RequestMapping("/access")
@Slf4j
@CatchAndLog
public class HttpStdConnector implements Connector {

    @Autowired
    ConnectorManager connectorManager;
    @Autowired
    DeviceContext deviceContext;

    @PostMapping("/std/rtg")
    @Operation(summary = "标准协议上报")
    @StopWatchWrapper(logHead = "rtg", msg = "标准协议上报")
    public SingleResponse<String> rtg(HttpServletRequest request, @Validated @RequestBody HttpGatewayRtgCmd httpGatewayRtgCmd) throws Exception {
        log.info("HttpStdConnector rtg  body: {}", httpGatewayRtgCmd);
        //标准报文
        EnnStandardHttpProtocol httpProtocol = new EnnStandardHttpProtocol();
        httpProtocol.setDeviceContext(deviceContext);
        List<? extends Message> messages = httpProtocol.decodeMulti(JSONObject.toJSONBytes(httpGatewayRtgCmd), request.getRequestURI());
        log.info("HttpStdConnector rtg 解析后数据：{}", JSON.toJSONString(messages));
        if (CollectionUtils.isNotEmpty(messages)) {
            for (Message message : messages) {
                deviceContext.storeMessage(message);
            }
        }
        return SingleResponse.buildSuccess("OK");
    }

    @PostMapping("/std/history")
    @Operation(summary = "历史数据上报")
    public SingleResponse<String> history(HttpServletRequest request, @Validated @RequestBody HttpGatewayRtgCmd httpGatewayRtgCmd) throws Exception {
        log.info("HttpStdConnector history  body: {}", httpGatewayRtgCmd);
        //标准报文
        EnnStandardHttpProtocol httpProtocol = new EnnStandardHttpProtocol();
        httpProtocol.setDeviceContext(deviceContext);
        List<? extends Message> messages = httpProtocol.decodeMulti(JSONObject.toJSONBytes(httpGatewayRtgCmd), request.getRequestURI());
        log.info("HttpStdConnector history 解析后数据：{}", JSON.toJSONString(messages));
        if (CollectionUtils.isNotEmpty(messages)) {
            for (Message message : messages) {
                deviceContext.storeMessage(message);
            }
        }
        return SingleResponse.buildSuccess("OK");
    }

    @PostMapping("/std/event")
    @Operation(summary = "事件上报")
    @StopWatchWrapper(logHead = "event", msg = "事件上报")
    public SingleResponse<String> event(HttpServletRequest request,@Validated @RequestBody HttpEventDataCmd httpGatewayRtgCmd) throws Exception {
        log.info("HttpStdConnector event  body: {}", httpGatewayRtgCmd);
        //标准报文
        EnnStandardHttpProtocol httpProtocol = new EnnStandardHttpProtocol();
        httpProtocol.setDeviceContext(deviceContext);
        //http server和 http client 应该统一入参：请求路径，请求体，返回体，映射关系
        List<? extends Message> messages = httpProtocol.decodeMulti(JSONObject.toJSONBytes(httpGatewayRtgCmd), request.getRequestURI());
        log.info("HttpStdConnector event 解析后数据：{}", JSON.toJSONString(messages));
        if (CollectionUtils.isNotEmpty(messages)) {
            for (Message message : messages) {
                deviceContext.storeMessage(message);
            }
        }
        return SingleResponse.buildSuccess("OK");
    }

    /**
     * 设备信息上报
     **/
    @PostMapping("/info")
    @Operation(summary = "设备信息上报")
    public SingleResponse<Boolean> infoStd(HttpServletRequest request,@RequestBody JSONObject httpGatewayInfoData) {
        //info上报设备信息，仅对原始报文进行转发 原始报文格式不固定
        log.info("http gateway info data body: {}", httpGatewayInfoData);
        BaseAssert.isBlank(httpGatewayInfoData.getString("pKey"), ErrorCode.PARAMETER_ERROR, "pKey不能为空");
        BaseAssert.isBlank(httpGatewayInfoData.getString("sn"), ErrorCode.PARAMETER_ERROR, "sn不能为空");
        return SingleResponse.buildSuccess(true);
    }


    @SneakyThrows
    @Override
    public void init() throws Exception {
        connectorManager.addConnector(this);
    }

    @Override
    public void setupProtocol(Protocol protocol, Map<String, Object> params) {

    }

    @Override
    public Map<String, Object> getStatus() {
        return null;
    }

    @Override
    public void stop() {

    }
}
