//package cn.enncloud.iot.gateway.service.cloud;
//
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingAuthBo;
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingDataParamsBo;
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingResBo;
//import cn.enncloud.iot.gateway.repository.bo.RequestParams;
//import cn.enncloud.iot.gateway.utils.StringUtil;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSON;
//import groovy.lang.Binding;
//import groovy.lang.GroovyShell;
//import io.vertx.core.http.HttpMethod;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import top.kdla.framework.dto.exception.ErrorCode;
//import top.kdla.framework.exception.BizException;
//import top.kdla.framework.supplement.http.VertxHttpClient;
//import cn.enncloud.iot.gateway.entity.cloud.*;
//
//import javax.annotation.Resource;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
///**
// * @Author: alec
// * Description: 查询token
// * @date: 下午2:43 2023/5/25
// */
//@Component
//@Slf4j
//public class CloudDockingReqManage {
//
//    @Resource
//    private VertxHttpClient vertxHttpClient;
//
//    private static String groovyCode =
//            "import java.time.LocalDateTime;" +
//            "import java.time.format.DateTimeFormatter;" +
//            "import top.kdla.framework.common.utils.Md5Util;" +
//            "import java.util.Base64;" +
//            "if (v.equalsIgnoreCase('#System.currentTimeMillis()#')) { res = System.currentTimeMillis(); }" +
//            "else if (v.equalsIgnoreCase('#System.currentTimeMillis()/1000#')) { res = (int)(System.currentTimeMillis() / 1000); }" +
//            "else if (v.equalsIgnoreCase('#Md5(Md5(${key})+${time})#')) { res = Md5Util.md5(Md5Util.md5(key) + time); }" +
//            "else if (v.equalsIgnoreCase('#Basic#')) { res = Base64.getEncoder().encodeToString(username+':'+password); }" +
//            "else if (v.equalsIgnoreCase('#Md5#')) { res = Md5Util.md5(key); }" +
//            "else if (v.equalsIgnoreCase('#today(yyyy-MM-dd HH:00:00)#')) { res = LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd HH:00:00')); }" +
//            "else if (v.equalsIgnoreCase('#today(yyyy-MM-dd HH:59:59)#')) { res = LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd HH:59:59')); }" +
//            "else if (v.equalsIgnoreCase('#today(yyyy-MM-dd)#')) { res = LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd')); }" +
//            "else if (v.equalsIgnoreCase('#today(yyyy-MM-dd 00:00:00)#')) { res = LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd 00:00:00')); }" +
//            "else if (v.equalsIgnoreCase('#today(yyyy-MM-dd 23:59:59)#')) { res = LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd 23:59:59')); }" +
//            "else if (v.equalsIgnoreCase('#yesterday(yyyy-MM-dd 00:00:00)#')) { res = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern('yyyy-MM-dd 00:00:00')); }" +
//            "else if (v.equalsIgnoreCase('#yesterday(yyyy-MM-dd 23:59:59)#')) { res = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern('yyyy-MM-dd 23:59:59')); }" +
//            "";
//
//    @SneakyThrows
//    public JSONObject sendPostRequest(CloudDockingResBo cloudDockingResBO, CloudDockingAuthBo cloudDockingAuthBO, List<CloudDockingDataParamsBo> dockingAuthParamsList) {
//        if (CollectionUtils.isEmpty(dockingAuthParamsList)) {
//            dockingAuthParamsList = new ArrayList<>();
//        }
//        /*请求参数*/
//        RequestParams<Map<String, Object>> requestParams = buildRequestEntity(cloudDockingResBO, cloudDockingAuthBO, dockingAuthParamsList);
//        /*请求头*/
//        Map<String, String> header = getHeader(dockingAuthParamsList);
//
//        String response;
//        if (cloudDockingAuthBO.getRequestType().equalsIgnoreCase(CloudDockingTypeEnum.RequestType.FORM.getCode())) {
//            HttpEntity<MultiValueMap<String, Object>> httpEntity = buildFormPostRequest(requestParams, header);
//            response = sendRequest(HttpMethod.POST, requestParams.getRequestUrl(), httpEntity.getHeaders().toSingleValueMap(), httpEntity.getBody(), String.class);
//            //response = restTemplateManage.sendPostForForm(requestParams,String.class, header);
//        } else {
//            response = sendRequest(HttpMethod.POST, requestParams.getRequestUrl(), header, requestParams.getParams(), String.class);
//            //response = restTemplateManage.sendPostForJson(requestParams,String.class, header);
//        }
//        return getResponseJson(cloudDockingAuthBO, response);
//    }
//
//    public HttpEntity<MultiValueMap<String, Object>> buildFormPostRequest(RequestParams<Map<String, Object>> requestParam, Map<String, String> header) {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        for (Map.Entry<String, String> entry : header.entrySet()) {
//            httpHeaders.set(entry.getKey(), entry.getValue());
//        }
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.setAll(requestParam.getParams());
//        return new HttpEntity<>(body, httpHeaders);
//    }
//
//
//    @SneakyThrows
//    public JSONObject sendGetRequest(CloudDockingResBo cloudDockingResBO, CloudDockingAuthBo cloudDockingAuthBO, List<CloudDockingDataParamsBo> dockingAuthParamsList) {
//        if (CollectionUtils.isEmpty(dockingAuthParamsList)) {
//            dockingAuthParamsList = new ArrayList<>();
//        }
//        /*请求参数*/
//        RequestParams<Map<String, Object>> requestParams = buildRequestEntity(cloudDockingResBO, cloudDockingAuthBO, dockingAuthParamsList);
//        /*请求头*/
//        Map<String, String> header = getHeader(dockingAuthParamsList);
//        /*根据请求类型请求数据*/
//        Map<String, Object> params = requestParams.getParams();
//        String response = sendRequest(HttpMethod.GET, requestParams.getRequestUrl(), header, params, String.class);
//        //String response = restTemplateManage.sendGet(requestParams,String.class,header);
//        if (StringUtils.isEmpty(response)) {
//            log.error("response is null");
//            throw new BizException(ErrorCode.BIZ_ERROR);
//        }
//        return getResponseJson(cloudDockingAuthBO, response);
//    }
//
//    public <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, Object req, Class<T> res) throws Exception {
//        log.info("CloudDockingReqManage-send url:{} headers:{} req:{}", url,JSON.toJSONString(headers), JSON.toJSONString(req));
//        CompletableFuture<T> future = vertxHttpClient.sendRequest(method, url, headers, req, res);
//        return future.get();
//    }
//
//
//    private JSONObject getResponseJson(CloudDockingAuthBo cloudDockingAuthBO, String response) {
//        try {
//            JSONObject jsonObject = JSONUtil.parseObj(response);
//            if (StringUtils.isEmpty(cloudDockingAuthBO.getRootPath())) {
//                return jsonObject;
//            }
//            return jsonObject.getJSONObject(cloudDockingAuthBO.getRootPath());
//        } catch (Exception e) {
//            log.error("response is error", e);
//            throw new BizException(ErrorCode.BIZ_ERROR);
//        }
//    }
//
//    private RequestParams<Map<String, Object>> buildRequestEntity(CloudDockingResBo cloudDockingResBO, CloudDockingAuthBo cloudDockingAuthBO, List<CloudDockingDataParamsBo> dockingAuthParamsList) throws Exception {
//        RequestParams<Map<String, Object>> requestParams = new RequestParams<>();
//
//        String url = String.format("%s%s", cloudDockingResBO.getBaseUrl(), cloudDockingAuthBO.getRequestUrl());
//
//        List<CloudDockingDataParamsBo> cloudDockingAuthParams = dockingAuthParamsList.stream()
//                .filter(res -> !res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.HEADER.getCode()))
//                .sorted(Comparator.comparing(CloudDockingDataParamsBo::getId))
//                .collect(Collectors.toList());
//
//        Map<String, Object> bodyre = buildBody(cloudDockingAuthParams);
//        String urlFinal = url;
//        urlFinal = StringUtil.replaceUrl(urlFinal, bodyre);
//
//        requestParams.setRequestUrl(urlFinal);
//
//        if (MapUtils.isNotEmpty(bodyre)) {
//            requestParams.setParams(bodyre);
//        }
//        return requestParams;
//    }
//
//    private Map<String, String> getHeader(List<CloudDockingDataParamsBo> dockingAuthParamsList) {
//
//        List<CloudDockingDataParamsBo> paramsList = dockingAuthParamsList.stream().filter(res -> res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.HEADER.getCode())).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(paramsList)) {
//            return new HashMap<>();
//        }
//        return paramsList.stream().collect(Collectors.toMap(CloudDockingDataParamsBo::getParamKey, CloudDockingDataParamsBo::getParamValue));
//    }
//
//
//    private JSONObject getChildrenJson(List<String> key, JSONObject jsonObject) {
//
//        if (Objects.isNull(jsonObject.getJSONObject(key.get(0)))) {
//            return jsonObject;
//        }
//        JSONObject object = jsonObject.getJSONObject(key.get(0));
//        if (key.size() == 1) {
//            return object;
//        }
//        return getChildrenJson(key.subList(1, key.size()), object);
//    }
//
//
//    public static Map<String, Object> buildBody(List<CloudDockingDataParamsBo> cloudDockingAuthParams) {
//        Map<String, Object> body = cloudDockingAuthParams.stream()
//                .collect(Collectors.toMap(CloudDockingDataParamsBo::getParamKey, res -> {
//                            log.info("CloudDockingAuthParams {}", res);
//                            if (res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.BODY.getCode())) {
//                                if (res.getParamValue().startsWith("{")) {
//                                    return JSONUtil.parse(res.getParamValue());
//                                } else if (res.getParamValue().startsWith("[")) {
//                                    return JSONUtil.parseArray(res.getParamValue());
//                                }
//                            }
//
//                            if (res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.PATH.getCode()) ||
//                                    res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.PARAMS.getCode())) {
//                                return res.getParamValue();
//                            }
//
//                            return res.getParamValue();
//                        },
//                        (oldValue, newValue) -> oldValue,
//                        LinkedHashMap::new));
//
//        //对body的请求做再次加工
//        Map<String, Object> bodyre = new LinkedHashMap<>();
//        body.forEach((k, v) -> {
//            bodyre.put(k, groovyShellCal(v, bodyre));
//        });
//
//        return bodyre;
//    }
//
//    public static Object groovyShellCal(Object value, Map<String, Object> bodyre) {
//        if(!(value instanceof String)){
//            return value;
//        }
//        // 创建一个绑定，用于存储变量
//        Binding binding = new Binding();
//        // 创建一个GroovyShell，用于执行Groovy代码
//        GroovyShell shell = new GroovyShell(binding);
//
//        // 设置变量
//        binding.setVariable("v", value);
//        if (bodyre != null) {
//            bodyre.forEach(binding::setVariable);
//        }
//
//        // 执行代码
//        Object res = shell.evaluate(groovyCode);
//        return res == null ? value : res;
//    }
//
//}
