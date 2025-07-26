/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Random;

/**
 * @author kanglele
 * @version $Id: PhysicalModelCache, v 0.1 2023/8/4 15:52 kanglele Exp $
 */
@Component
@Slf4j
public class VModelMetricCache extends CafCache<String, Map<String, Object>> {

    /**
     * 缓存过期时间，分钟
     */
    @Value("${ennew.caffeine.vmetric.duration:10}")
    private Long duration;
    /**
     * 缓存初始容量
     */
    @Value("${ennew.caffeine.vmetric.initialCapacity:1000}")
    private Integer initialCapacity;
    /**
     * 缓存最大容量
     */
    @Value("${ennew.caffeine.vmetric.maximumSize:10000}")
    private Integer maximumSize;

    /**
     * 初始化参数
     */
    @PostConstruct
    public void initPhysicalModelCache() {
        super.setDuration(duration << 1);
        super.setInitialCapacity(initialCapacity);
        super.setMaximumSize(maximumSize);
        super.init();
    }

    public void putLocal(String key, Map<String, Object> value) {
        super.put(key, value, duration + new Random().nextInt(duration.intValue()));
    }

    public void putNullLocal(String key, Map<String, Object> value) {
        super.put(key, value, (duration + new Random().nextInt(duration.intValue()))>>2);
    }

    public Map<String, Object> getFromLocal(String key) {
        return super.get(key);
    }

    /**
     * 删除
     *
     * @param key
     */
    public void delete(String key) {
        super.del(key);
    }
}
