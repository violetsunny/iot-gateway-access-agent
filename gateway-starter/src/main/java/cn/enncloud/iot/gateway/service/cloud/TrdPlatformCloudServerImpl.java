/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.service.cloud;

import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.context.TrdPlatformCloudServer;
import cn.enncloud.iot.gateway.entity.Device;
import cn.enncloud.iot.gateway.entity.cloud.*;
import cn.enncloud.iot.gateway.integration.other.TrdPlatformApiClient;
import cn.enncloud.iot.gateway.integration.other.model.*;
import cn.enncloud.iot.gateway.service.RedisService;
import cn.enncloud.iot.gateway.service.converter.TrdPlatformConverter;
import cn.enncloud.iot.gateway.timer.manager.TimeJobManagerService;
import cn.enncloud.iot.gateway.timer.manager.impl.EnnIotXxlJobManager;
import cn.enncloud.iot.gateway.timer.manager.impl.LocalJobManager;
import cn.enncloud.iot.gateway.utils.SpringContextUtil;
import cn.enncloud.iot.gateway.utils.StringUtil;
import cn.hutool.cron.pattern.parser.PatternParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.kdla.framework.dto.MultiResponse;
import top.kdla.framework.dto.PageResponse;
import top.kdla.framework.dto.SingleResponse;
import top.kdla.framework.dto.exception.ErrorCode;
import top.kdla.framework.exception.BizException;
import top.kdla.framework.supplement.http.VertxHttpClient;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kanglele
 * @version $Id: TrdPlatformCloudServerImpl, v 0.1 2024/3/13 16:44 kanglele Exp $
 */
@Service
@Slf4j
public class TrdPlatformCloudServerImpl implements TrdPlatformCloudServer {

    @Resource
    private TrdPlatformApiClient trdPlatformApiClient;
    @Resource
    private TrdPlatformConverter trdPlatformConverter;
    @Resource
    private RedisService redisService;
    @Resource
    private VertxHttpClient vertxHttpClient;
    @Resource
    private DeviceContext deviceContext;

    @Value("${ennew.iot.switch.reslog:false}")
    private Boolean reslog;
    @Value("${ennew.iot.switch.datasize:100}")
    private Integer dataSize;

    private static final String TOKEN_PRE = "TrdPlatformCloudAuth:";
    private static final String TASK_JOB = "TrdPlatformCloudTask:";

    private static final String PAGE_PLACEHOLDER = "#page#";
    private static final String SN_LIST_PLACEHOLDER = "#deviceContext#";
    private static final String SN_PLACEHOLDER = "#deviceContextSn#";
    private static final String ENT_SN_PLACEHOLDER = "#deviceContextEntSn#";
    private static final String SN_ARRAY_PLACEHOLDER = "#deviceContextArraySn#";
    private static final String ENT_SN_ARRAY_PLACEHOLDER = "#deviceContextArrayEntSn#";
    private static final String ENT_PLACEHOLDER = "#deviceContextEnt#";

    @Override
    public JSONObject sendRequest(String method, String url, Map<String, String> headers, Object req, Class res) throws Exception {
        log.info("CloudDockingReqManage-send  url:{}  headers:{}  req:{}", url, JSON.toJSONString(headers), JSON.toJSONString(req));

        JSONObject jsonObject;
        try {
            CompletableFuture<HttpResponse<Buffer>> future = vertxHttpClient.sendRequest(method, url, headers, req);
            HttpResponse<Buffer> response = future.get();
            if (res.equals(String.class)) {
                String result = response.bodyAsString();
                if (result.startsWith("{") && result.endsWith("}")) {
                    jsonObject = JSONObject.parseObject(result);
                } else {
                    jsonObject = new JSONObject();
                    jsonObject.put("data", result);
                }
            } else {
                Object result = response.bodyAsJson(res);//默认json返回
                jsonObject = JSONObject.parseObject(JSONObject.toJSONString(result));
            }

            if (response.headers() != null) {
                JSONObject headerRes = JSONArray.parseArray(JSON.toJSONString(response.headers().entries())).stream()
                        .map(o -> (JSONObject) o)
                        .flatMap(json -> json.keySet().stream()
                                .collect(Collectors.toMap(Function.identity(), json::get, (value1, value2) -> value2)).entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value2, JSONObject::new));
                jsonObject.put("headers", headerRes);//合并对象
            }

            if (reslog) {
                log.info("CloudDockingReqManage-res {} {}", url, JSON.toJSONString(jsonObject));
            }
        } catch (Exception e) {
            throw new BizException(ErrorCode.FAIL.getCode(), "调用接口异常::%s", ExceptionUtils.getMessage(e));
        }

        return jsonObject;
    }

    @Override
    public List<TrdPlatformTask> taskWorkList(String pCode) {
        MultiResponse<TrdPlatformTaskDto> response = trdPlatformApiClient.taskWorkList(new TrdPlatformReqDto(null, pCode, null, null, null));
        return trdPlatformConverter.toTrdPlatformTasks(response.getData());
    }

    @Override
    public TrdPlatformTask taskWork(String pCode, String taskCode) {
        SingleResponse<TrdPlatformTaskDto> response = trdPlatformApiClient.taskWork(new TrdPlatformReqDto(null, pCode, taskCode, null, null));
        return trdPlatformConverter.toTrdPlatformTask(response.getData());
    }

    @Override
    public TrdPlatformReq downReqContext(String productId, String taskCode) throws Exception {
        SingleResponse<TrdPlatformTaskDto> response = trdPlatformApiClient.taskWork(new TrdPlatformReqDto(null, null, taskCode, productId, null));
        if (response == null || response.getData() == null) {
            return null;
        }
        return reqContext(response.getData());
    }

    @Override
    public TrdPlatformReq taskReqContext(String pCode, String taskCode) throws Exception {
        SingleResponse<TrdPlatformTaskDto> response = trdPlatformApiClient.taskWork(new TrdPlatformReqDto(null, pCode, taskCode, null, null));
        if (response == null || response.getData() == null) {
            return null;
        }
        return reqContext(response.getData());
    }

    public TrdPlatformReq reqContext(TrdPlatformTaskDto taskBo) throws Exception {
        SingleResponse<TrdPlatformInfoDto> infoRes = trdPlatformApiClient.trdInfo(new TrdPlatformReqDto(null, taskBo.getPCode(), null, null, null));
        if (infoRes == null || infoRes.getData() == null) {
            return null;
        }
        SingleResponse<TrdPlatformApiDto> apiRes = trdPlatformApiClient.apiInfo(new TrdPlatformReqDto(taskBo.getApiId(), null, null, null, null));
        if (apiRes == null || apiRes.getData() == null) {
            return null;
        }
        List<TrdPlatformReqTask> reqTasks = new ArrayList<>();
        TrdPlatformReqTask reqTask = new TrdPlatformReqTask();
        reqTask.setCode(taskBo.getTaskCode());
        reqTask.setLimit(apiRes.getData().getCallLimit());

        String url = "";
        if (StringUtils.isNotBlank(apiRes.getData().getFullUrl())) {
            url = String.format("%s%s", infoRes.getData().getConfigJson().get("baseUrl"), apiRes.getData().getFullUrl());
        }

        TrdPlatformAuthToken authToken = null;
        if (apiRes.getData().getAuthType() == TrdPlatformEnum.AuthWayEnum.TOKEN.getCode()) {
            authToken = this.authToken(taskBo.getPCode(), apiRes.getData().getAuthApi());
        }

        MultiResponse<TrdPlatformApiParamDto> paramRes = null;
        if (apiRes.getData().getHasParam()) {
            paramRes = trdPlatformApiClient.apiParam(new TrdPlatformReqDto(taskBo.getApiId(), null, null, null, null));
        }

        //TODO 特殊占位符替换
        List<List<TrdPlatformApiParam>> paramBoList = transformSpecial(paramRes, taskBo.getProductId());

        List<TrdPlatformBody> bodies = new ArrayList<>();

        if (CollectionUtils.isEmpty(paramBoList)) {
            bodies.addAll(createReqBody(apiRes.getData(), authToken, null, url));
        } else {
            for (List<TrdPlatformApiParam> paramBosTwo : paramBoList) {
                bodies.addAll(createReqBody(apiRes.getData(), authToken, paramBosTwo, url));
            }
        }

        reqTask.setApiId(taskBo.getApiId());
        reqTask.setAuthApi(apiRes.getData().getAuthApi());
        reqTask.setApiType(apiRes.getData().getApiType());
        reqTask.setBodies(bodies);
        reqTasks.add(reqTask);
        return TrdPlatformReq.builder()
                .pCode(taskBo.getPCode())
                .productId(taskBo.getProductId())
                .reqChildren(reqTasks)
                .build();
    }

    private List<List<TrdPlatformApiParam>> transformSpecial(MultiResponse<TrdPlatformApiParamDto> paramRes, String productId) throws Exception {
        List<List<TrdPlatformApiParam>> paramBoList = new ArrayList<>();
        if (paramRes != null && CollectionUtils.isNotEmpty(paramRes.getData())) {
            if (paramRes.getData().stream().anyMatch(t -> t.getParamValue().equalsIgnoreCase(SN_LIST_PLACEHOLDER))) {
                List<Device> sns = deviceContext.getSnByProductId(productId);
//                    sns = Arrays.asList("YBF2152F001B91008C","YBF2152F004B68008F","YBF2152F000BD8007F","YBF2152F006B19008E","YBF2152F0020D00087");
//                    sns = Arrays.asList("LZFF31T69ND002706","LZFF31W63KD035935","LGAX3BG56J1009717","LRDS6PEB8MR028582","LZZ1BYVF7MW809606");
                if (CollectionUtils.isNotEmpty(sns)) {
                    for (Device sn : sns) {
                        List<TrdPlatformApiParam> paramBosTwo = new ArrayList<>();
                        for (TrdPlatformApiParamDto paramBo : paramRes.getData()) {
                            TrdPlatformApiParam paramBoTwo = transformApiParam(paramBo);
                            if (paramBo.getParamValue().equalsIgnoreCase(SN_LIST_PLACEHOLDER)) {
                                paramBoTwo.setParamValue(sn.getSn());
                            }
                            if (paramBo.getParamValue().equalsIgnoreCase(ENT_PLACEHOLDER)) {
                                paramBoTwo.setParamValue(sn.getDeptId());
                            }
                            paramBosTwo.add(paramBoTwo);
                        }
                        paramBoList.add(paramBosTwo);
                    }
                } else {
                    paramBoList.add(paramRes.getData().stream().map(TrdPlatformCloudServerImpl::transformApiParam).collect(Collectors.toList()));
                }

            } else if (paramRes.getData().stream().anyMatch(t -> SN_PLACEHOLDER.equalsIgnoreCase(t.getParamValue()) || ENT_SN_PLACEHOLDER.equalsIgnoreCase(t.getParamValue()))) {
                List<Device> sns = deviceContext.getSnByProductId(productId);
                if (CollectionUtils.isNotEmpty(sns)) {
                    Map<String, List<Device>> snMap = sns.stream().collect(Collectors.groupingBy(dev -> StringUtils.isNotBlank(dev.getDeptId()) ? dev.getDeptId() : ""));
                    snMap.forEach((key, value) -> {
                        List<List<Device>> snList = Lists.partition(value, dataSize);
                        for (List<Device> snss : snList) {
                            List<TrdPlatformApiParam> paramBosTwo = new ArrayList<>();
                            for (TrdPlatformApiParamDto paramBo : paramRes.getData()) {
                                TrdPlatformApiParam paramBoTwo = transformApiParam(paramBo);
                                if (paramBo.getParamValue().equalsIgnoreCase(SN_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(StringUtils.join(snss.stream().map(Device::getSn).collect(Collectors.toList()), ","));
                                }
                                if (paramBo.getParamValue().equalsIgnoreCase(ENT_SN_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(StringUtils.join(snss.stream().map(device-> StringUtils.replace(device.getSn(),key+"_","")).collect(Collectors.toList()), ","));
                                }
                                if (paramBo.getParamValue().equalsIgnoreCase(ENT_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(key);
                                }
                                paramBosTwo.add(paramBoTwo);
                            }

                            paramBoList.add(paramBosTwo);
                        }
                    });
                } else {
                    paramBoList.add(paramRes.getData().stream().map(TrdPlatformCloudServerImpl::transformApiParam).collect(Collectors.toList()));
                }

            } else if (paramRes.getData().stream().anyMatch(t -> t.getParamValue().equalsIgnoreCase(SN_ARRAY_PLACEHOLDER) || ENT_SN_ARRAY_PLACEHOLDER.equalsIgnoreCase(t.getParamValue()))) {
                List<Device> sns = deviceContext.getSnByProductId(productId);
                if (CollectionUtils.isNotEmpty(sns)) {
                    Map<String, List<Device>> snMap = sns.stream().collect(Collectors.groupingBy(dev -> StringUtils.isNotBlank(dev.getDeptId()) ? dev.getDeptId() : ""));
                    snMap.forEach((key, value) -> {
                        List<List<Device>> snList = Lists.partition(value, dataSize);
                        for (List<Device> snss : snList) {
                            List<TrdPlatformApiParam> paramBosTwo = new ArrayList<>();
                            for (TrdPlatformApiParamDto paramBo : paramRes.getData()) {
                                TrdPlatformApiParam paramBoTwo = transformApiParam(paramBo);
                                if (paramBo.getParamValue().equalsIgnoreCase(SN_ARRAY_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(snss.stream().map(Device::getSn).collect(Collectors.toList()));
                                }
                                if (paramBo.getParamValue().equalsIgnoreCase(ENT_SN_ARRAY_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(snss.stream().map(device-> StringUtils.replace(device.getSn(),key+"_","")).collect(Collectors.toList()));
                                }
                                if (paramBo.getParamValue().equalsIgnoreCase(ENT_PLACEHOLDER)) {
                                    paramBoTwo.setParamValue(key);
                                }
                                paramBosTwo.add(paramBoTwo);
                            }

                            paramBoList.add(paramBosTwo);
                        }
                    });
                } else {
                    paramBoList.add(paramRes.getData().stream().map(TrdPlatformCloudServerImpl::transformApiParam).collect(Collectors.toList()));
                }

            } else {
                paramBoList.add(paramRes.getData().stream().map(TrdPlatformCloudServerImpl::transformApiParam).collect(Collectors.toList()));
            }
        }

        return paramBoList;
    }

    private List<TrdPlatformBody> createReqBody(TrdPlatformApiDto apiBo, TrdPlatformAuthToken authToken, List<TrdPlatformApiParam> paramBosTwo, String url) throws Exception {
        List<TrdPlatformBody> bodies = new ArrayList<>();
        if (apiBo.getHasPages()) {
            //参数中填写key和值，将key再存入标志中
            Integer page = apiBo.getPageStartNo();
            Integer pageSize = apiBo.getPageSize();
            String pageNumberKey = apiBo.getPageNumberKey();
            String pageSizeKey = apiBo.getPageSizeKey();
            Integer pagePosition = apiBo.getPagePosition();

            Integer finalPage = page;
            HashMap<String, Object> pageMap = new HashMap<String, Object>() {{
                put(pageNumberKey, finalPage);
                put(pageSizeKey, pageSize);
            }};

            Boolean pageFlag = false;
            if (CollectionUtils.isNotEmpty(paramBosTwo)) {
                pageFlag = paramBosTwo.stream().anyMatch(t -> String.valueOf(t.getParamValue()).equalsIgnoreCase(PAGE_PLACEHOLDER));
            }

            int count = 0;
            if (apiBo.getTotalNumberType() == TrdPlatformEnum.TotalDataGetWayEnum.FIXED.getCode()) {
                count = Integer.parseInt(apiBo.getTotalNumberConfig());
            }
            if (apiBo.getTotalNumberType() == TrdPlatformEnum.TotalDataGetWayEnum.ORIGINAL_API.getCode()) {

                TrdPlatformBody bodyre = createPageBody(authToken, paramBosTwo, url, apiBo.getMethod(), apiBo, pageMap, pagePosition, pageFlag);

                JSONObject obj = this.sendRequest(bodyre.getMethod(), bodyre.getUrl(), bodyre.getHeader(), bodyre.getBody(), JSONObject.class);
                if (apiBo.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.JSON.getCode()) {
                    Object v = JSONPath.read(obj.toJSONString(), apiBo.getBodyAnalysisCode());
                    count = Integer.parseInt(String.valueOf(v));
                }
                if (apiBo.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.GROOVY.getCode()) {
                    Object auth = groovyShellCal(apiBo.getBodyAnalysisCode(), obj);
                    count = Integer.parseInt(String.valueOf(auth));
                }
            }
            if (apiBo.getTotalNumberType() == TrdPlatformEnum.TotalDataGetWayEnum.NEW_API.getCode()) {
                Long countApi = Long.parseLong(apiBo.getTotalNumberConfig());
                TrdPlatformBody countbody = this.createApi(countApi);
                if (countbody != null) {
                    JSONObject countobj = this.sendRequest(countbody.getMethod(), countbody.getUrl(), countbody.getHeader(), countbody.getBody(), String.class);
                    log.info("获取count:{}", countobj);
                    if (countbody.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.JSON.getCode()) {
                        Object v = JSONPath.read(countobj.toJSONString(), countbody.getBodyAnalysisCode());
                        count = Integer.parseInt(String.valueOf(v));
                    } else if (countbody.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.GROOVY.getCode()) {
                        Object auth = groovyShellCal(countbody.getBodyAnalysisCode(), countobj);
                        count = Integer.parseInt(String.valueOf(auth));
                    } else {
                        count = Integer.parseInt(countobj.getString("data"));
                    }
                }

            }
            PageResponse pageResponse = PageResponse.of(null, count, pageSize, page);

            for (int i = 1; i <= pageResponse.getTotalPages(); i++) {
                pageMap.put(pageNumberKey, String.valueOf(page));
                bodies.add(createPageBody(authToken, paramBosTwo, url, apiBo.getMethod(), apiBo, pageMap, pagePosition, pageFlag));
                ++page;
            }

        } else {
            bodies.add(createBody(authToken, paramBosTwo, url, apiBo.getMethod(), apiBo));
        }

        return bodies;
    }

    private TrdPlatformBody createApi(Long api) throws Exception {
        SingleResponse<TrdPlatformApiDto> apiRes = trdPlatformApiClient.apiInfo(new TrdPlatformReqDto(api, null, null, null, null));
        if (apiRes == null || apiRes.getData() == null) {
            return null;
        }
        SingleResponse<TrdPlatformInfoDto> infoRes = trdPlatformApiClient.trdInfo(new TrdPlatformReqDto(null, apiRes.getData().getPCode(), null, null, null));
        if (infoRes == null || infoRes.getData() == null) {
            return null;
        }
        TrdPlatformAuthToken authToken = null;
        if (apiRes.getData().getAuthType() == TrdPlatformEnum.AuthWayEnum.TOKEN.getCode()) {
            authToken = this.authToken(apiRes.getData().getPCode(), apiRes.getData().getAuthApi());
        }
        List<TrdPlatformApiParam> params = null;
        if (apiRes.getData().getHasParam()) {
            MultiResponse<TrdPlatformApiParamDto> paramRes = trdPlatformApiClient.apiParam(new TrdPlatformReqDto(api, null, null, null, null));
            if (paramRes != null && CollectionUtils.isNotEmpty(paramRes.getData())) {
                params = paramRes.getData().stream().map(TrdPlatformCloudServerImpl::transformApiParam).collect(Collectors.toList());
            }
        }
        String url = "";
        if (StringUtils.isNotBlank(apiRes.getData().getFullUrl())) {
            url = String.format("%s%s", infoRes.getData().getConfigJson().get("baseUrl"), apiRes.getData().getFullUrl());
        }
        return createBody(authToken, params, url, apiRes.getData().getMethod(), apiRes.getData());
    }

    public synchronized TrdPlatformAuthToken authToken(String pCode, Long authApi) throws Exception {
        //从缓存获取token
        String key = String.format("%s%s_%s", TOKEN_PRE, pCode, authApi);
        Object tokenObject = redisService.getValue(key);
        TrdPlatformAuthToken authToken = null;
        if (Objects.nonNull(tokenObject)) {
            try {
                if (tokenObject instanceof String) {
                    authToken = JSONObject.parseObject((String) tokenObject, TrdPlatformAuthToken.class);
                } else {
                    authToken = JSONObject.parseObject(JSONObject.toJSONString(tokenObject), TrdPlatformAuthToken.class);
                }
                if (authToken != null) {
                    //在redis中直接返回
                    return authToken;
                }
            } catch (Exception e) {
                log.error("转换tokenBo error", e);
            }
        }
        TrdPlatformBody body = this.createApi(authApi);
        return this.authRefreshToken(pCode, authApi, body);
    }

    @Override
    public TrdPlatformAuthToken authRefreshToken(String pCode, Long authApi, TrdPlatformBody body) throws Exception {
        //从缓存获取token
        String key = String.format("%s%s_%s", TOKEN_PRE, pCode, authApi);
        TrdPlatformAuthToken authToken = createAuthToken(body);
        if (authToken == null) {
            return null;
        }
        //存入Redis-时间都要改成秒
        redisService.putValueDuration(key, JSONObject.toJSONString(authToken), Duration.ofSeconds(Long.parseLong(authToken.getExpirationTime())));
        return authToken;
    }

    private TrdPlatformAuthToken createAuthToken(TrdPlatformBody body) throws Exception {
        if (body == null) {
            return null;
        }
        JSONObject obj = this.sendRequest(body.getMethod(), body.getUrl(), body.getHeader(), body.getBody(), JSONObject.class);
        if (obj == null) {
            return null;
        }
        if (body.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.JSON.getCode()) {
            TrdPlatformAuthToken authToken = JSONObject.parseObject(body.getBodyAnalysisCode(), TrdPlatformAuthToken.class);
            Object v = JSONPath.read(obj.toJSONString(), authToken.getParamValue());
            authToken.setParamValue(String.valueOf(v));

            if (authToken.getExpirationTime().startsWith("$")) {
                try {
                    Object t = JSONPath.read(obj.toJSONString(), authToken.getExpirationTime());
                    if (t != null) {
                        authToken.setExpirationTime(String.valueOf(t));
                    }
                } catch (Exception e) {
                    log.warn("Exception", e);
                }
            }

            authToken.setCreateTime(System.currentTimeMillis());
            return authToken;
        }
        if (body.getBodyAnalysisType() == TrdPlatformEnum.BodyParsingMethodEnum.GROOVY.getCode()) {
            Object auth = groovyShellCal(body.getBodyAnalysisCode(), obj);
            TrdPlatformAuthToken authToken = JSONObject.parseObject(JSONObject.toJSONString(auth), TrdPlatformAuthToken.class);
            authToken.setCreateTime(System.currentTimeMillis());
            return authToken;
        }

        return null;
    }

    private TrdPlatformBody createPageBody(TrdPlatformAuthToken authToken, List<TrdPlatformApiParam> paramBos, String url, String method, TrdPlatformApiDto apiBo, HashMap<String, Object> pageMap, Integer pagePosition, Boolean pageFlag) throws Exception {
        List<TrdPlatformApiParam> paramBosTwo = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paramBos)) {
            for (TrdPlatformApiParam paramBo : paramBos) {
                TrdPlatformApiParam paramBoTwo = new TrdPlatformApiParam();
                BeanUtils.copyProperties(paramBoTwo, paramBo);
                if (String.valueOf(paramBoTwo.getParamValue()).equalsIgnoreCase(PAGE_PLACEHOLDER)) {
                    paramBoTwo.setParamValue(JSON.toJSONString(pageMap));
                }
                paramBosTwo.add(paramBoTwo);
            }
        }

        if (!pageFlag) {
            for (Map.Entry<String, Object> entry : pageMap.entrySet()) {
                TrdPlatformApiParam paramBo = new TrdPlatformApiParam();
                paramBo.setId(0L);
                paramBo.setParamKey(entry.getKey());
                paramBo.setParamType(TrdPlatformEnum.ParamTypeEnum.FIXED.getCode());
                paramBo.setParamPosition(pagePosition);
                paramBo.setParamValue(entry.getValue());
                paramBosTwo.add(paramBo);
            }
        }

        return createBody(authToken, paramBosTwo, url, method, apiBo);
    }

    private static TrdPlatformApiParam transformApiParam(TrdPlatformApiParamDto paramDto) {
        TrdPlatformApiParam paramBoTwo = new TrdPlatformApiParam();
        try {
            BeanUtils.copyProperties(paramBoTwo, paramDto);
        } catch (Exception e) {
            log.error("BeanUtils异常", e);
        }
        return paramBoTwo;
    }

    private TrdPlatformBody createBody(TrdPlatformAuthToken authToken, List<TrdPlatformApiParam> paramBos, String url, String method, TrdPlatformApiDto apiBo) {
        Map<String, Object> authMap = new LinkedHashMap<>();
        if (authToken != null) {
            authMap.put(authToken.getParamKey(), authToken.getParamValue());
        }

        if (CollectionUtils.isEmpty(paramBos)) {
            url = StringUtil.replaceUrl(url, authMap);

            return TrdPlatformBody.builder()
                    .url(url)
                    .header(null)
                    .method(method)
                    .body(null)
                    .bodyAnalysisType(apiBo.getBodyAnalysisType())
                    .bodyAnalysisCode(apiBo.getBodyAnalysisCode())
                    .build();
        }

        //对body的请求做再次加工
        Map<String, Object> finalBodyre = new LinkedHashMap<>(authMap);
        paramBos = paramBos.stream().sorted(Comparator.comparing(TrdPlatformApiParam::getId)) //排序问题
                .peek(param -> {
                    if (MapUtils.isNotEmpty(authMap) && authMap.containsKey(param.getParamKey())) {
                        Object auth = authMap.get(param.getParamKey());
                        param.setParamValue(auth);
                    }
                    if (param.getParamType() == TrdPlatformEnum.ParamTypeEnum.GROOVY.getCode()) {
                        Object obj = groovyShellCal(String.valueOf(param.getParamValue()), finalBodyre);
                        param.setParamValue(obj);
                        finalBodyre.put(param.getParamKey(), obj);
                    } else {
                        finalBodyre.put(param.getParamKey(), param.getParamValue());
                    }
                }).collect(Collectors.toList());

        Map<String, String> headerMap = paramBos.stream()
                .filter(res -> res.getParamPosition() == TrdPlatformEnum.ParamPositionEnum.HEAD.getCode())
                .collect(Collectors.toMap(TrdPlatformApiParam::getParamKey, param -> String.valueOf(param.getParamValue()), (oldValue, newValue) -> oldValue));

        Map<String, Object> bodyre = paramBos.stream()
                .filter(res -> res.getParamPosition() == TrdPlatformEnum.ParamPositionEnum.BODY.getCode() || res.getParamPosition() == TrdPlatformEnum.ParamPositionEnum.FORM.getCode())
                .collect(Collectors.toMap(TrdPlatformApiParam::getParamKey, TrdPlatformApiParam::getParamValue, (oldValue, newValue) -> oldValue));

        Map<String, Object> path = paramBos.stream()
                .filter(res -> res.getParamPosition() == TrdPlatformEnum.ParamPositionEnum.PATH.getCode() || res.getParamPosition() == TrdPlatformEnum.ParamPositionEnum.QUERY.getCode())
                .collect(Collectors.toMap(TrdPlatformApiParam::getParamKey, TrdPlatformApiParam::getParamValue, (oldValue, newValue) -> oldValue));

        url = StringUtil.replaceUrl(url, path);

        if (url.contains("%24%7B") || url.contains("${")) {
            url = StringUtil.replaceUrl(url, finalBodyre);
        }

        return TrdPlatformBody.builder()
                .url(url)
                .header(headerMap)
                .method(method)
                .body(bodyre)
                .bodyAnalysisType(apiBo.getBodyAnalysisType())
                .bodyAnalysisCode(apiBo.getBodyAnalysisCode())
                .build();
    }

    public static Object groovyShellCal(String groovyCode, Map<String, Object> bodyre) {
        // 创建一个绑定，用于存储变量
        Binding binding = new Binding();
        // 创建一个GroovyShell，用于执行Groovy代码
        GroovyShell shell = new GroovyShell(binding);

        // 设置变量
        if (bodyre != null) {
            bodyre.forEach(binding::setVariable);
        }

        // 执行代码
        Object res = shell.evaluate(groovyCode);
        return res;
    }

    @Override
    public void operateTaskWork(TrdPlatformTask task, Integer operate, String jobType) {
        TimeJobManagerService timeJobManagerService = null;
        if ("local".equalsIgnoreCase(jobType)) {
            timeJobManagerService = SpringContextUtil.getBean(LocalJobManager.class);
        } else {
            timeJobManagerService = SpringContextUtil.getBean(EnnIotXxlJobManager.class);
        }

        if (timeJobManagerService == null) {
            return;
        }

        if (StringUtils.isBlank(task.getFrequency()) || !isValidCronExpression(task.getFrequency())) {
            return;
        }

        String jobKey = TASK_JOB + task.getPCode() + "-" + task.getTaskCode();
        String taskId = (String) redisService.getValue(TASK_JOB + task.getPCode() + "-" + task.getTaskCode());

        if (TrdPlatformEnum.ADD.getCode() == operate) {
            if (StringUtils.isNotBlank(taskId)) {
                log.info("{} {} {} {} 任务已经存在", task.getPCode(), task.getTaskCode(), task.getFrequency(), taskId);
            } else {
                addTask(timeJobManagerService, task, jobKey);
            }
        }
        if (TrdPlatformEnum.UPDATE.getCode() == operate) {
            if (StringUtils.isNotBlank(taskId)) {
                removeTask(timeJobManagerService, task, jobKey, taskId);
            }

            addTask(timeJobManagerService, task, jobKey);
        }
        if (TrdPlatformEnum.REMOVE.getCode() == operate) {
            removeTask(timeJobManagerService, task, jobKey, taskId);
        }
    }

    private void updateTaskStatus(TrdPlatformTask task) {
        TrdPlatformReqDto reqDto = new TrdPlatformReqDto();
        reqDto.setCode(task.getPCode());
        reqDto.setTaskCode(task.getTaskCode());
        reqDto.setProductId(task.getProductId());
        reqDto.setStatus(TrdPlatformEnum.TaskStatusEnum.START.getCode());
        trdPlatformApiClient.updateTaskStatus(reqDto);
    }

    private synchronized void addTask(TimeJobManagerService timeJobManagerService, TrdPlatformTask task, String jobKey) {
        String taskIdRe = timeJobManagerService.register(task.getTaskName(), task.getFrequency(), "CloudJob", task.getPCode() + "," + task.getTaskCode());
        redisService.putValue(jobKey, taskIdRe);
        log.info("{} {} {} 启动成功", task.getPCode(), task.getTaskCode(), task.getFrequency());
        updateTaskStatus(task);
    }

    private synchronized void removeTask(TimeJobManagerService timeJobManagerService, TrdPlatformTask task, String jobKey, String taskId) {
        timeJobManagerService.unRegister(taskId);
        redisService.deleteKey(jobKey);
        log.info("{} {} 删除成功", task.getPCode(), task.getTaskCode());
    }

    public static boolean isValidCronExpression(String cronExpression) {
        try {
            PatternParser.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getEnum(String type, String name) {
        SingleResponse<Map<String, Map<String, Object>>> response = trdPlatformApiClient.getEnum("ALL");
        if (response != null && response.getData() != null) {
            return response.getData().get(type).get(name);
        }
        return null;
    }

    @Override
    public String getTrdPlatformProtocol(String pCode, TrdPlatformEnum.FunctionTypeEnum functionType) {
        SingleResponse<TrdPlatformInfoDto> infoRes = trdPlatformApiClient.trdInfo(new TrdPlatformReqDto(null, pCode, null, null, null));
        if (infoRes == null || infoRes.getData() == null) {
            return null;
        }
        if (StringUtils.isNotBlank(infoRes.getData().getProtocolId()) && "{".startsWith(infoRes.getData().getProtocolId())) {
            try {
                JSONObject jobj = JSONObject.parseObject(infoRes.getData().getProtocolId());
                String protocol = jobj.getString("jar");
                if (StringUtils.isNotBlank(protocol)) {
                    return protocol;
                } else {
                    String script = jobj.getString("script");
                    if (StringUtils.isNotBlank(script)) {
                        JSONObject jobjSc = JSONObject.parseObject(script);
                        String protocolUp = jobjSc.getString("up");
                        String protocolDown = jobjSc.getString("down");
                        if (TrdPlatformEnum.FunctionTypeEnum.UP.equals(functionType)) {
                            return protocolUp;
                        }
                        if (TrdPlatformEnum.FunctionTypeEnum.DOWN.equals(functionType)) {
                            return protocolDown;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        return "";
    }

}
