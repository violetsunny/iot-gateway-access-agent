///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.service.cloud;
//
//import cn.enncloud.iot.gateway.config.connectors.HttpRequestConfig;
//import cn.enncloud.iot.gateway.config.connectors.HttpServerConfig;
//import cn.enncloud.iot.gateway.context.CloudDockingServer;
//import cn.enncloud.iot.gateway.context.DeviceContext;
//import cn.enncloud.iot.gateway.entity.cloud.*;
//import cn.enncloud.iot.gateway.repository.*;
//import cn.enncloud.iot.gateway.repository.bo.*;
//import cn.enncloud.iot.gateway.utils.StringUtil;
//import cn.hutool.json.JSONObject;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONPath;
//import io.vertx.core.http.HttpMethod;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.beanutils.BeanUtils;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import top.kdla.framework.dto.PageResponse;
//import top.kdla.framework.dto.exception.ErrorCode;
//import top.kdla.framework.exception.BizException;
//
//import javax.annotation.Resource;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @author kanglele
// * @version $Id: CloudDockingService, v 0.1 2024/1/25 17:05 kanglele Exp $
// */
//@Service
//@Slf4j
//public class CloudDockingServerImpl implements CloudDockingServer {
//    @Resource
//    private CloudDockingRepository cloudDockingRepository;
//    @Resource
//    private CloudDockingAuthRepository cloudDockingAuthRepository;
//    @Resource
//    private CloudDockingRespRepository cloudDockingRespRepository;
//    @Resource
//    private CloudDockingParamsRepository cloudDockingParamsRepository;
//    @Resource
//    private CloudDockingDataRepository cloudDockingDataRepository;
//    @Resource
//    private CloudDockingReqManage cloudDockingReqManage;
//    @Autowired
//    private DeviceContext deviceContext;
//    @Autowired
//    private HttpRequestConfig httpRequestConfig;
//
//    public CloudDockingAuthTokenBo createAuthToken(String authCode) {
//        log.info("认证器{} 生成认证参数", authCode);
//        CloudDockingResBo cloudDockingResBO = cloudDockingRepository.getByCode(authCode);
//        CloudDockingAuthBo cloudDockingAuthBO = cloudDockingAuthRepository.searchOneByHostId(authCode);
//        CloudDockingAuthResBo authRes = cloudDockingRespRepository.getCloudDockingAuthResBO(authCode);
//        List<CloudDockingDataParamsBo> dockingAuthParamsList = cloudDockingParamsRepository.searchByCode(authCode, null, CloudDockingTypeEnum.AUTH.getCode(), null);
//        log.info("CloudDockingAuthTokenBO auth {} - {}", cloudDockingAuthBO, dockingAuthParamsList);
//
//        Map<String, String> tokenMap = new HashMap<>();
//        if (cloudDockingAuthBO == null || StringUtils.isBlank(cloudDockingAuthBO.getRequestUrl())) {
//            //没有请求方式，就说明是写死的token值或者根本不验证
//            if (authRes == null) {
//                //不验证
//                return null;
//            }
//
//            Map<String, Object> bodyre = null;
//            if (CollectionUtils.isNotEmpty(dockingAuthParamsList)) {
//                bodyre = dockingAuthParamsList.stream()
//                        .collect(Collectors.toMap(CloudDockingDataParamsBo::getParamKey, CloudDockingDataParamsBo::getParamValue,
//                                (oldValue, newValue) -> oldValue,
//                                LinkedHashMap::new));
//            }
//
//            //##Basic=username:password
//            tokenMap.put(authRes.getAccessKey(), (String) CloudDockingReqManage.groovyShellCal(authRes.getAccessPrefix(), bodyre));
//            CloudDockingAuthTokenBo authToken = CloudDockingAuthTokenBo.builder()
//                    .createTime(System.currentTimeMillis())
//                    .tokenMap(tokenMap)
//                    .paramsType(authRes.getParamsType())
//                    .expirationTime(authRes.getExpireTime())
//                    .build();
//
//            log.info("response {}", authToken);
//            return authToken;
//
//        } else {
//            JSONObject response = null;
//            if (cloudDockingAuthBO.getRequestMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
//                response = cloudDockingReqManage.sendGetRequest(cloudDockingResBO, cloudDockingAuthBO, dockingAuthParamsList);
//            }
//            if (cloudDockingAuthBO.getRequestMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
//                response = cloudDockingReqManage.sendPostRequest(cloudDockingResBO, cloudDockingAuthBO, dockingAuthParamsList);
//            }
//            if (Objects.isNull(response)) {
//                throw new BizException(ErrorCode.BIZ_ERROR);
//            }
//
//            // 返回值中的key=authRes.getAccessRef() 走到这里就说明是接口中获取，就是ref
//            String accessToken = response.getStr(authRes.getAccessRef());
//            if (StringUtils.isBlank(accessToken)) {
//                accessToken = authRes.getAccessRef();
//            }
//
//            if (StringUtils.isNotBlank(response.getStr(authRes.getAccessPrefix())) && StringUtils.isNotBlank(accessToken)) {
//                tokenMap.put(authRes.getAccessKey(), response.getStr(authRes.getAccessPrefix()) + " " + accessToken);
//            } else if (StringUtils.isNotBlank(authRes.getAccessPrefix()) && StringUtils.isNotBlank(accessToken)) {
//                tokenMap.put(authRes.getAccessKey(), authRes.getAccessPrefix() + " " + accessToken);
//            } else if (StringUtils.isNotBlank(accessToken)) {
//                tokenMap.put(authRes.getAccessKey(), accessToken);
//            } else if (StringUtils.isNotBlank(authRes.getAccessPrefix())) {
//                tokenMap.put(authRes.getAccessKey(), authRes.getAccessPrefix());
//            }
//
//            CloudDockingAuthTokenBo authToken = CloudDockingAuthTokenBo.builder()
//                    .createTime(System.currentTimeMillis())
//                    .tokenMap(tokenMap)
//                    .paramsType(authRes.getParamsType())
//                    .build();
//            // 这个expirationTime必须要是秒，不然存redis会有错误
//            if (StringUtils.isNotBlank(authRes.getExpireKey()) && response.get(authRes.getExpireKey()) != null) {
//                Integer expirationTime = response.getInt(authRes.getExpireKey());
//                Long expirationTime2 = expirationTime == null ? response.getLong(authRes.getExpireKey()) : expirationTime.longValue();
//                authToken.setExpirationTime(expirationTime2);
//            } else {
//                authToken.setExpirationTime(authRes.getExpireTime() != null ? authRes.getExpireTime() : 60);
//            }
//            log.info("response {}", authToken);
//            return authToken;
//        }
//
//    }
//
//    public Map<String, List<CloudDockingBodyBo>> getCloudDockingBody(String code, CloudDockingAuthTokenBo authToken, String dataCode, String updown) throws Exception {
//        List<CloudDockingBodyBo> boList = new ArrayList<>();
//        CloudDockingResBo cloudDockingResBO = cloudDockingRepository.getByCode(code);
//        List<CloudDockingDataBo> cloudDockingDataBos = cloudDockingDataRepository.getByHostId(code, dataCode, updown);
//        //如果分页的需要把对应的count也要带上
//        if (StringUtils.isNotBlank(dataCode) && dataCode.startsWith("page-")) {
//            String dataCode2 = StringUtils.replace(dataCode, "page-", "count-");
//            cloudDockingDataBos.addAll(cloudDockingDataRepository.getByHostId(code, dataCode2, updown));
//        }
//        cloudDockingDataBos.forEach(cloudDockingDataBO -> {
//            String url = "";
//            if (StringUtils.isNotBlank(cloudDockingDataBO.getRequestUrl())) {
//                url = String.format("%s%s", cloudDockingResBO.getBaseUrl(), cloudDockingDataBO.getRequestUrl());
//            }
//            //List<CloudDockingMetadataBo> metadatas = cloudDockingMetadataRepository.getMetadata(code, cloudDockingDataBO.getDataCode());
//            List<CloudDockingDataParamsBo> dockingAuthParamsList = cloudDockingParamsRepository.searchByCode(code, cloudDockingDataBO.getDataCode(), CloudDockingTypeEnum.PULL_DATA.getCode(), null);
//
//            //处理无参数调用
//            if (CollectionUtils.isEmpty(dockingAuthParamsList)) {
//                Map<String, String> headerMap = new HashMap<>();
//                String urlFinal = transformBodyData(authToken, url, headerMap, new HashMap<>());
//
//                CloudDockingBodyBo bodyBO = CloudDockingBodyBo.builder()
//                        .code(code)
//                        .url(urlFinal)
//                        .dataCode(cloudDockingDataBO.getDataCode())
//                        .method(cloudDockingDataBO.getRequestMethod())
//                        .requestType(cloudDockingDataBO.getRequestType())
//                        .header(headerMap)
//                        .body(null)
//                        .limit(Optional.ofNullable(cloudDockingDataBO.getReqLimit()).orElse(-1))
//                        .rootPath(cloudDockingDataBO.getRootPath())
////                        .devicePath(cloudDockingDataBO.getDevicePath())
//                        .group(cloudDockingDataBO.getDataCode())
//                        .build();
//
//                boList.add(bodyBO);
//            } else {
//                Map<String, String> headerMap = dockingAuthParamsList.stream()
//                        .filter(res -> res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.HEADER.getCode())).collect(Collectors.toMap(CloudDockingDataParamsBo::getParamKey, CloudDockingDataParamsBo::getParamValue));
//
//                if (cloudDockingDataBO.getRequestType().equalsIgnoreCase(CloudDockingTypeEnum.RequestType.FORM.getCode())) {
//                    headerMap.put("content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
//                } else {
//                    headerMap.put("content-type", MediaType.APPLICATION_JSON_VALUE);
//                }
//
//                String finalUrl = url;
//                Map<String, List<CloudDockingDataParamsBo>> cloudDockingAuthParamsGroup = dockingAuthParamsList.stream()
//                        .filter(res -> !res.getParamType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.HEADER.getCode()))
//                        .sorted(Comparator.comparing(CloudDockingDataParamsBo::getId))
//                        .collect(Collectors.groupingBy(CloudDockingDataParamsBo::getReqGroup, LinkedHashMap::new, Collectors.toList()));
//
//                cloudDockingAuthParamsGroup.forEach((key, value) -> {
//
//                    Map<String, Object> bodyre = CloudDockingReqManage.buildBody(value);
//
//                    String urlFinal = transformBodyData(authToken, finalUrl, headerMap, bodyre);
//
//                    CloudDockingBodyBo bodyBO = CloudDockingBodyBo.builder()
//                            .code(code)
//                            .url(urlFinal)
//                            .dataCode(cloudDockingDataBO.getDataCode())
//                            .method(cloudDockingDataBO.getRequestMethod())
//                            .requestType(cloudDockingDataBO.getRequestType())
//                            .header(headerMap)
//                            .body(bodyre)
//                            .limit(Optional.ofNullable(cloudDockingDataBO.getReqLimit()).orElse(-1))
//                            .rootPath(cloudDockingDataBO.getRootPath())
////                            .devicePath(cloudDockingDataBO.getDevicePath())
//                            .group(cloudDockingDataBO.getDataCode() + key)
//                            .build();
//
//                    boList.add(bodyBO);
//                });
//            }
//
//        });
//
//        //需要处理下分页的问题
//        Map<String, List<CloudDockingBodyBo>> bodyBoMap = boList.stream().collect(Collectors.groupingBy(CloudDockingBodyBo::getDataCode));
//        List<Map<String, CloudDockingBodyBo>> bodyBoList = new ArrayList<>();
//        //先找出来
//        for (Map.Entry<String, List<CloudDockingBodyBo>> v : bodyBoMap.entrySet()) {
//            if (v.getKey().startsWith("page-")) {
//                String countKey = StringUtils.replace(v.getKey(), "page-", "count-");
//                if (bodyBoMap.containsKey(countKey)) {
//                    Map<String, CloudDockingBodyBo> bodyMap = new LinkedHashMap<>();
//                    //默认只会有写一个
//                    bodyMap.put(countKey, bodyBoMap.get(countKey).get(0));
//                    bodyMap.put(v.getKey(), bodyBoMap.get(v.getKey()).get(0));
//                    bodyBoList.add(bodyMap);
//                }
//            }
//        }
//        //要将page对应的请求按照count分，替换，最后将count接口剔除
//        for (Map<String, CloudDockingBodyBo> body : bodyBoList) {
//            int count = 1;
//            for (Map.Entry<String, CloudDockingBodyBo> v : body.entrySet()) {
//                if (v.getKey().startsWith("count-")) {
//                    CloudDockingBodyBo message = v.getValue();
//                    if (StringUtils.isNotBlank(message.getUrl())) {
//                        String obj = cloudDockingReqManage.sendRequest(HttpMethod.valueOf(message.getMethod().toUpperCase(Locale.ROOT)), message.getUrl(), message.getHeader(), message.getBody(), String.class);
//                        log.info("获取count:{}",obj);
//                        if (StringUtils.isNotBlank(message.getRootPath())) {
//                            Object objRes = JSONPath.read(obj, message.getRootPath());
//                            count = Integer.parseInt(String.valueOf(objRes));
//                        } else {
//                            count = Integer.parseInt(obj);
//                        }
//                    } else if (StringUtils.isNotBlank(message.getRootPath())) {
//                        count = Integer.parseInt(message.getRootPath());
//                    }
//
//                    bodyBoMap.remove(v.getKey());
//                }
//                if (v.getKey().startsWith("page-")) {
//                    List<CloudDockingBodyBo> bos = new ArrayList<>();
//                    CloudDockingBodyBo message = v.getValue();
//                    //$.pageSize 有新的也要加
//                    Object pageSize2 = JSONPath.read(JSON.toJSONString(message.getBody()), "$.pageSize");
//                    Long pageSize = Long.parseLong(String.valueOf(pageSize2));
////                    count = 20;//TODO test
//                    PageResponse pageResponse = PageResponse.of(null, count, pageSize, 1);
//                    for (int i = 1; i <= pageResponse.getTotalPages(); i++) {
//                        CloudDockingBodyBo value = new CloudDockingBodyBo();
//                        BeanUtils.copyProperties(value, message);
//
//                        //替换当前页
//                        value = transformPage(value, i);
//
//                        bos.add(value);
//                    }
//
//                    bodyBoMap.remove(v.getKey());
//                    bodyBoMap.put(v.getKey(), bos);
//                }
//
//            }
//        }
//
//        //需要处理从设备管理获取设备列表-refer(##deviceContext标注设备) 或者本地读取指定json中的设备-local(##deviceId标注设备)，代替param中写大量的指定参数。
//        //先找出来
//        for (Map.Entry<String, List<CloudDockingBodyBo>> v : bodyBoMap.entrySet()) {
//            if (v.getKey().startsWith("refer-") || v.getKey().startsWith("local-")) {
//                Map<String, CloudDockingBodyBo> bodyMap = new LinkedHashMap<>();
//                //默认只会有写一个
//                bodyMap.put(v.getKey(), v.getValue().get(0));
//                bodyBoList.add(bodyMap);
//            }
//        }
//        //替换设备字段
//        for (Map<String, CloudDockingBodyBo> body : bodyBoList) {
//            for (Map.Entry<String, CloudDockingBodyBo> v : body.entrySet()) {
//                if (v.getKey().startsWith("refer-")) {
//
//                    CloudDockingBodyBo message = v.getValue();
//                    //设备管理获取全量设备
//                    String productId = null;
//                    String[] split = StringUtils.split(v.getKey(), "-");
//                    if (split.length >= 3) {
//                        productId = split[2];
//                    } else if (CollectionUtils.isNotEmpty(httpRequestConfig.getConfiguration())) {
//                        productId = httpRequestConfig.getConfiguration().stream().filter(t -> t.getAppid().equalsIgnoreCase(message.getCode())).map(HttpServerConfig::getProductId).findFirst().orElse(null);
//                    }
//
//                    List<String> sns = deviceContext.getSnByProductId(productId);
//                    //TODO test
////                    sns = Arrays.asList("LZFF31T69ND002706","LZFF31W63KD035935","LGAX3BG56J1009717","LRDS6PEB8MR028582","LZZ1BYVF7MW809606");
//                    if(CollectionUtils.isEmpty(sns)){
//                        //设备是空的，就不要继续调用了。
//                        log.info("{} 获取转换设备为空，剔除不再调用",productId);
//                        bodyBoMap.remove(v.getKey());
//                    } else {
//                        List<CloudDockingBodyBo> bos = new ArrayList<>();
//                        for (String sn : sns) {
//                            CloudDockingBodyBo value = new CloudDockingBodyBo();
//                            BeanUtils.copyProperties(value, message);
//
//                            //替换设备号
//                            String bojson = StringUtils.replace(JSON.toJSONString(value), "!device!", sn);
//                            CloudDockingBodyBo bo = JSON.parseObject(bojson, CloudDockingBodyBo.class);
//
//                            bos.add(bo);
//                        }
//
//                        bodyBoMap.remove(v.getKey());
//                        bodyBoMap.put(v.getKey(), bos);
//                    }
//
//                }
//                if (v.getKey().startsWith("local-")) {
//                    //以后再说
//                    //sn-deviceId 配置或者redis
//                }
//            }
//        }
//        return bodyBoMap;
//    }
//
//    private String transformBodyData(CloudDockingAuthTokenBo authToken, String urlFinal, Map<String, String> headerMap, Map<String, Object> bodyre) {
//        if (StringUtils.isBlank(urlFinal)) {
//            return urlFinal;
//        }
//        if (authToken != null) {
//            if (authToken.getParamsType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.HEADER.getCode())) {
//                headerMap.putAll(authToken.getTokenMap());
//            }
//            Map<String, Object> urlReplace = new HashMap<>(bodyre);
//            if (authToken.getParamsType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.PATH.getCode()) ||
//                    authToken.getParamsType().equalsIgnoreCase(CloudDockingTypeEnum.CloudDockingParamsType.PARAMS.getCode())) {
//                urlReplace.putAll(authToken.getTokenMap());
//            }
//            urlFinal = StringUtil.replaceUrl(urlFinal, urlReplace);
//        }
//        return urlFinal;
//    }
//
//    private CloudDockingBodyBo transformPage(CloudDockingBodyBo value, int page) {
////        //怎么调整当前页  page/current/currentPage/pageNumber  body/Params
////        Map<String, Object> bodyre = (Map) value.getBody();
////        if (bodyre.containsKey("current")) {
////            bodyre.put("current", page);
////            if (value.getUrl().indexOf("current") > 0) {
////                value.setUrl(StringUtils.replace(value.getUrl(), "current=##1", "current=" + page));
////            }
////        }
////        if (bodyre.containsKey("currentPage")) {
////            bodyre.put("currentPage", page);
////            if (value.getUrl().indexOf("currentPage") > 0) {
////                value.setUrl(StringUtils.replace(value.getUrl(), "currentPage=##1", "currentPage=" + page));
////            }
////        }
////        if (bodyre.containsKey("pageNumber")) {
////            bodyre.put("pageNumber", page);
////            if (value.getUrl().indexOf("pageNumber") > 0) {
////                value.setUrl(StringUtils.replace(value.getUrl(), "pageNumber=##1", "pageNumber=" + page));
////            }
////        }
////        //需要处理Object中是分页的场景
////        if (bodyre.containsKey("Paging")) {
////            com.alibaba.fastjson.JSONObject jobj = JSON.parseObject(StringUtils.replace(JSON.toJSONString(bodyre.get("Paging")), "##1", "" + page));
////            bodyre.put("Paging", jobj);
////        }
//        //直接
//        return JSON.parseObject(StringUtils.replace(JSON.toJSONString(value), "!page!", "" + page), CloudDockingBodyBo.class);
//    }
//
//    public <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, Object req, Class<T> res) throws Exception {
//        return cloudDockingReqManage.sendRequest(method, url, headers, req, res);
//    }
//
//}
