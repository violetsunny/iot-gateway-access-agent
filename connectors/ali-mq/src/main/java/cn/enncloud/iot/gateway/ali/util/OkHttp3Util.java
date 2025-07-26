package cn.enncloud.iot.gateway.ali.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttp3Util {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    public static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    /**
     * GET
     *
     * @param url     请求的url
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return String
     */
    public static String get(String url, Map<String, String> queries) {
        return get(url, null, queries);
    }

    /**
     * GET
     *
     * @param url     请求的url
     * @param header  请求头
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return String
     */
    public static String get(String url, Map<String, String> header, Map<String, String> queries) {
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && !queries.keySet().isEmpty()) {
            sb.append("?clientId=blade");
            queries.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        }

        Request.Builder builder = new Request.Builder();

        if (header != null && !header.keySet().isEmpty()) {
            header.forEach(builder::addHeader);
        }

        Request request = builder.url(sb.toString()).build();
        return getBody(request);
    }

    /**
     * POST
     *
     * @param url    请求的url
     * @param params post form 提交的参数
     * @return String
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, null, params);
    }

    /**
     * POST
     *
     * @param url    请求的url
     * @param header 请求头
     * @param params post form 提交的参数
     * @return String
     */
    public static String post(String url, Map<String, String> header, Map<String, String> params) {
        FormBody.Builder formBuilder = new FormBody.Builder().add("clientId", "blade");
        //添加参数
        if (params != null && !params.keySet().isEmpty()) {
            params.forEach(formBuilder::add);
        }

        Request.Builder builder = new Request.Builder();

        if (header != null && !header.keySet().isEmpty()) {
            header.forEach(builder::addHeader);
        }

        Request request = builder.url(url).post(formBuilder.build()).build();
        return getBody(request);
    }

    /**
     * POST请求发送JSON数据
     *
     * @param url  请求的url
     * @param json 请求的json串
     * @return String
     */
    public static String postJson(String url, String json) {
        return postJson(url, null, json);
    }

    /**
     * POST请求发送JSON数据
     *
     * @param url    请求的url
     * @param header 请求头
     * @param json   请求的json串
     * @return String
     */
    public static String postJson(String url, Map<String, String> header, String json) {
        return postContent(url, header, json, JSON);
    }

    /**
     * POST请求发送xml数据
     *
     * @param url 请求的url
     * @param xml 请求的xml串
     * @return String
     */
    public static String postXml(String url, String xml) {
        return postXml(url, null, xml);
    }

    /**
     * POST请求发送xml数据
     *
     * @param url    请求的url
     * @param header 请求头
     * @param xml    请求的xml串
     * @return String
     */
    public static String postXml(String url, Map<String, String> header, String xml) {
        return postContent(url, header, xml, XML);
    }

    /**
     * 发送POST请求
     *
     * @param url       请求的url
     * @param header    请求头
     * @param content   请求内容
     * @param mediaType 请求类型
     * @return String
     */
    public static String postContent(String url, Map<String, String> header, String content, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(mediaType, content);
        Request.Builder builder = new Request.Builder();

        if (header != null && !header.keySet().isEmpty()) {
            header.forEach(builder::addHeader);
        }
        Request request = builder.url(url).post(requestBody).build();
        return getBody(request);
    }

    /**
     * 获取body
     *
     * @param request request
     * @return String
     */
    private static String getBody(Request request) {
        String responseBody = "";
        long startTime = System.currentTimeMillis();
        try (Response response = okHttpClient.newCall(request).execute()) {
            log.info("OkHttp3Util response {} 耗时 {} ms", response, System.currentTimeMillis() - startTime);
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("okhttp3 post error >> ex = {}", e.getMessage());
        }
        return responseBody;
    }

}
