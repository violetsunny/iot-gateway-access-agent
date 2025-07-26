/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author kanglele
 * @version $Id: KafkaNewMessage, v 0.1 2023/10/17 11:15 kanglele Exp $
 */
@NoArgsConstructor
@Data
public class KafkaNewMessage implements Serializable {

    /**
     * 版本
     */
    @JsonProperty("version")
    private String version;
    /**
     * 产品id
     */
    @JsonProperty("productId")
    private String productId;
    /**
     * 租户id
     */
    @JsonProperty("tenantId")
    private String tenantId;
    /**
     * 跳变
     */
    @JsonProperty("changeRpt")
    private Boolean changeRpt;
    /**
     * debug
     */
    @JsonProperty("debug")
    private Integer debug;
    /**
     * resume
     */
    @JsonProperty("resume")
    private String resume;
    /**
     * 场站id，取自redis中device:{deviceId}.projectCode
     */
    @JsonProperty("staId")
    private String staId;
    /**
     * 业务域，取自redis中device:{deviceId}.domain
     */
    @JsonProperty("domain")
    private String domain;
    /**
     * 第三方标识，取自redis中device:{deviceId}.thirdCode
     */
    @JsonProperty("deviceCode")
    private String deviceCode;
    /**
     * 频率
     */
    @JsonProperty("period")
    private String period;
    /**
     * 设备名称
     */
    @JsonProperty("deviceName")
    private String deviceName;
    /**
     * 设备标识
     */
    @JsonProperty("sn")
    private String sn;
    /**
     * 企业id
     */
    @JsonProperty("deptId")
    private String deptId;
    /**
     * 上游为kafka消息时，取kafka record中的timestamp，非kafka消息时为当前时间--新增
     */
    @JsonProperty("ingestionTime")
    private Long ingestionTime;
    /**
     * 物模型编码，对应的取值改为redis中device:{deviceId}.entityTypeCode
     */
    @JsonProperty("devType")
    private String devType;
    /**
     * 设备id
     */
    @JsonProperty("devId")
    private String devId;
    /**
     * 数据来源
     */
    @JsonProperty("source")
    private String source;
    /**
     * 时间
     */
    @JsonProperty("ts")
    private Long ts;
    /**
     * 物模型名称-取自redis中device:{deviceId}. entityTypeName -新增
     */
    @JsonProperty("entityTypeName")
    private String entityTypeName;
    /**
     * 频率，内容与period一致--新增
     */
    @JsonProperty("uploadFrequency")
    private String uploadFrequency;
    /**
     * 省--county内容的后4位替换为0000，新增
     */
    @JsonProperty("province")
    private String province;
    /**
     * 市—county内容的后2位替换为00，新增
     */
    @JsonProperty("city")
    private String city;
    /**
     * 县—取自redis中device:{deviceId}.areaCode新增
     */
    @JsonProperty("county")
    private String county;
    /**
     * 设备类型（子设备、网关、DTU等）--新增
     */
    @JsonProperty("deviceType")
    private String deviceType;
    /**
     * 父设备id-取自redis中device:{deviceId}.parentId-新增
     */
    @JsonProperty("parentId")
    private String parentId;
    /**
     * data
     */
    @JsonProperty("data")
    private List<KafkaNewMetric> data;

    /**
     * Item
     */
    @NoArgsConstructor
    @Data
    public static class KafkaNewMetric implements Serializable {
        /**
         * 测点编码
         */
        @JsonProperty("metric")
        private String metric;
        /**
         * 值
         */
        @JsonProperty("value")
        private Object value;
        /**
         * 单位--新增  获取产品缓存 metadata属性(JSON格式)的 measureProperties(数组)的 unit属性值
         */
        @JsonProperty("metricUnit")
        private String metricUnit;
        /**
         * 测点名称--新增  获取产品缓存 metadata属性(JSON格式)的 measureProperties(数组)的 name属性值
         */
        @JsonProperty("metricName")
        private String metricName;
        /**
         * 物模型上限--新增  获取产品缓存 metadata属性(JSON格式)的 measureProperties(数组)的 max属性值
         */
        @JsonProperty("max")
        private String max;
        /**
         * 物模型下限--新增  获取产品缓存 metadata属性(JSON格式)的 measureProperties(数组)的 min属性值
         */
        @JsonProperty("min")
        private String min;
        /**
         * 物模型数据类型--新增  获取产品缓存 metadata属性(JSON格式)的 measureProperties(数组)的 type属性值
         */
        @JsonProperty("type")
        private String type;

        private Long ts;
    }
}
