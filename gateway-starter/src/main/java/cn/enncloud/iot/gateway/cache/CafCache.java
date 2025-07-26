/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * caffeine
 *
 * @author kanglele
 * @version $Id: KdlaCafCache, v 0.1 2023/8/3 9:55 kanglele Exp $
 */
@Slf4j
public class CafCache<T, E> {

    /**
     * 缓存过期时间，分
     */
    @Setter
    private Long duration;
    /**
     * 缓存初始容量
     */
    @Setter
    private Integer initialCapacity;
    /**
     * 缓存最大容量  50000是8m
     */
    @Setter
    private Integer maximumSize;

    private Cache<T, E> loadingCache;

    @Setter
    private Function<T, E> loaderFunction;

    protected void init() {
        // 创建一个Caffeine缓存实例
        this.loadingCache = Caffeine.newBuilder()
                // 设置缓存容器的初始容量
                .initialCapacity(this.initialCapacity)
                // 设置最大缓存数量
                .maximumSize(this.maximumSize)
                // 使用自定义的过期策略--没办法按照key设置不同的超时时间
                //.expireAfter(new CustomExpiry())
                // 设置缓存过期时间-公共过期
                .expireAfterWrite(this.duration, TimeUnit.MINUTES)
                // 设置要统计缓存的命中率
                //.recordStats()
                // 设置缓存的移除通知
                .removalListener((RemovalListener<T, E>) (t, e, removalCause) -> log.warn("CafCache key:{} value不建议打印 was removed, cause is {}", t, removalCause))
                .build();
    }

    /**
     * 获取Cache
     *
     * @return
     */
    public Cache<T, E> getCafCache() {
        return this.loadingCache;
    }

    /**
     * 放入缓存
     *
     * @param key
     * @param duration 分
     */
    public synchronized void put(T key, long duration) {
        E value = this.loaderFunction == null ? null : this.loaderFunction.apply(key);
        // 使用Cache.policy().expireVariably()设置单个键的过期时间
        loadingCache.policy().expireVariably().ifPresent(expiry -> expiry.setExpiresAfter(key, duration, TimeUnit.MINUTES));
        // 将值放入缓存
        loadingCache.put(key, value);
    }

    /**
     * 放入缓存
     *
     * @param key
     * @param value
     * @param duration 分
     */
    public synchronized void put(T key, E value, long duration) {
        // 使用Cache.policy().expireVariably()设置单个键的过期时间
        loadingCache.policy().expireVariably().ifPresent(expiry -> expiry.setExpiresAfter(key, duration, TimeUnit.MINUTES));
        // 将值放入缓存
        loadingCache.put(key, value);
    }

    /**
     * 获取
     *
     * @param t
     * @return
     */
    public E get(T t) {
        return this.loadingCache.getIfPresent(t);
    }

    /**
     * 删除
     *
     * @param t
     * @throws Exception
     */
    public void del(T t) {
        this.loadingCache.invalidate(t);
    }

    /**
     * 清除
     */
    public void clear() {
        this.loadingCache.invalidateAll();
    }

    /**
     * 缓存大小
     *
     * @return
     */
    public long size() {
        return this.loadingCache.estimatedSize();
    }

    /**
     * 统计信息
     *
     * @return
     */
    public CacheStats getStats() {
        return this.loadingCache.stats();
    }

}
