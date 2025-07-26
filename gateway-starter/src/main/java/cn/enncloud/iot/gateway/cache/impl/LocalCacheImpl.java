package cn.enncloud.iot.gateway.cache.impl;

import cn.enncloud.iot.gateway.cache.LocalCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LocalCacheImpl implements LocalCache {


    /**
     * 本地缓存初始化容量
     */
    private static final int DEFAULT_LOCAL_CACHE_CAPACITY = 5000;

    /**
     * 本地缓存最大容量
     */
    private static final int DEFAULT_LOCAL_CACHE_MAXIMUM = 50000;

    /**
     * 本地缓存失效时间，分钟，不可靠
     */
    private static final int DEFAULT_LOCAL_CACHE_EXPIRE = 3;

    private static final String DEVICE_PREFIX = "dev_";

    private static final String PRODUCT_PREFIX = "pro_";

    private static final String PROTOCOL_PREFIX = "proto_";

    private static final String SN_PREFIX = "sn_";

    private static final String SN_PRODUCT_PREFIX = "sn_pro_";


    private static final LoadingCache<String, String> LOCAL_ID_CACHE;



    static {
        LOCAL_ID_CACHE = CacheBuilder.newBuilder()
                .initialCapacity(DEFAULT_LOCAL_CACHE_CAPACITY)
                .maximumSize(DEFAULT_LOCAL_CACHE_MAXIMUM)
                .expireAfterWrite(DEFAULT_LOCAL_CACHE_EXPIRE, TimeUnit.MINUTES)
                .removalListener(notification -> {
                    if(log.isTraceEnabled()){
                        log.trace("local cache removed, key:{}, cause:{}", notification.getKey(), notification.getCause());
                    }
                })
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return null;
                    }
                });
    }

    @Override
    public String getDeviceId(String sn) {
        return LOCAL_ID_CACHE.getIfPresent(DEVICE_PREFIX + sn);
    }

    @Override
    public String getProductId(String deviceId) {
        return LOCAL_ID_CACHE.getIfPresent(PRODUCT_PREFIX + deviceId);
    }

    @Override
    public String getProtocolId(String productId) {
        return LOCAL_ID_CACHE.getIfPresent(PROTOCOL_PREFIX + productId);
    }

    @Override
    public void putDeviceId(String sn, String deviceId) {
        LOCAL_ID_CACHE.put(DEVICE_PREFIX + sn, deviceId);
    }

    @Override
    public void putProductId(String deviceId, String productId) {
        LOCAL_ID_CACHE.put(PRODUCT_PREFIX + deviceId, productId);
    }

    @Override
    public void putProtocolId(String productId, String protocolId) {
        LOCAL_ID_CACHE.put(PROTOCOL_PREFIX + productId, protocolId);
    }

    @Override
    public void putSn(String deviceId, String sn) {
        LOCAL_ID_CACHE.put(SN_PREFIX + deviceId, sn);
    }

    @Override
    public String getSn(String deviceId) {
        return LOCAL_ID_CACHE.getIfPresent(SN_PREFIX + deviceId);
    }

    @Override
    public void putSnProductId(String sn, String productId) {
        LOCAL_ID_CACHE.put(SN_PRODUCT_PREFIX + sn, productId);
    }

    @Override
    public String getSnProductId(String sn) {
        return LOCAL_ID_CACHE.getIfPresent(SN_PRODUCT_PREFIX + sn);
    }
}
