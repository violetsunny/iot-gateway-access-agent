//package cn.enncloud.iot.gateway.config;
//
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
//import org.elasticsearch.action.ActionListener;
//import org.elasticsearch.action.bulk.BackoffPolicy;
//import org.elasticsearch.action.bulk.BulkItemResponse;
//import org.elasticsearch.action.bulk.BulkProcessor;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.bulk.BulkResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestClientBuilder;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.unit.ByteSizeUnit;
//import org.elasticsearch.common.unit.ByteSizeValue;
//import org.elasticsearch.core.TimeValue;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Duration;
//import java.util.Iterator;
//import java.util.function.BiConsumer;
//
//@Configuration
//@Slf4j
//public class EsConfig {
//
//    @Value("${spring.elasticsearch.high-level-client.ip}")
//    private String ip;
//    @Value("${spring.elasticsearch.high-level-client.port}")
//    private String port;
//    @Value("${spring.elasticsearch.high-level-client.username:-1}")
//    private String user;
//    @Value("${spring.elasticsearch.high-level-client.password:-1}")
//    private String pwd;
//
//    @Value("${spring.elasticsearch.high-level-client.actions:100}")
//    private Integer actions;
//    @Value("${spring.elasticsearch.high-level-client.size:5}")
//    private Integer size;
//    @Value("${spring.elasticsearch.high-level-client.wait-time:10}")
//    private Integer timeWait;
//    @Value("${spring.elasticsearch.high-level-client.retry-times:3}")
//    private Integer retryTimes;
//
//
//    /**
//     * @return
//     */
//    @Bean("bulkProcessor")
//    public BulkProcessor bulkProcessor() {
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        if (StringUtils.isNotBlank(user) && !user.equals("-1")) {
//            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
//        }
//
//
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(new HttpHost(ip, Integer.parseInt(port), "http")).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//                        httpClientBuilder.disableAuthCaching();
//                        httpClientBuilder.setKeepAliveStrategy((response, context) -> Duration.ofMinutes(5).toMillis());
//                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//                    }
//
//                })
//        );
//
//        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
//                (request, bulkListener) -> {
//                    request.timeout(TimeValue.timeValueSeconds(60));
//                    client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
//                };
//
//        return BulkProcessor.builder(bulkConsumer, new BulkProcessor.Listener() {
//            @Override
//            public void beforeBulk(long executionId, BulkRequest request) {
//                int i = request.numberOfActions();
//                log.info("ES bulkProcessor 同步数量{}", i);
//            }
//
//            @Override
//            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                Iterator<BulkItemResponse> iterator = response.iterator();
//                while (iterator.hasNext()) {
//                    BulkItemResponse bulkItemResponse = iterator.next();
//                    log.info("add device data:{}", JSON.toJSONString(bulkItemResponse));
//                    if (bulkItemResponse.isFailed()) {
//                        log.warn("add device data fail:{}", bulkItemResponse.getFailureMessage());
//                    } else {
//                        log.info("bulkProcessor add device data,sn:{},id:{}", bulkItemResponse.getIndex(), bulkItemResponse.getResponse().getId());
//                    }
//                }
//            }
//
//            @Override
//            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
//                log.error("bulkProcessor {} data bulk failed,reason :{}", executionId, failure);
//            }
//        })
//        //  达到刷新的条数
//        .setBulkActions(actions)
//        // 达到 刷新的大小
//        .setBulkSize(new ByteSizeValue(size, ByteSizeUnit.MB))
//        // 固定刷新的时间频率
//        .setFlushInterval(TimeValue.timeValueSeconds(timeWait))
//        //并发线程数
//        .setConcurrentRequests(1)
//        // 重试补偿策略
//        .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), retryTimes))
//        .build();
//
//    }
//}
