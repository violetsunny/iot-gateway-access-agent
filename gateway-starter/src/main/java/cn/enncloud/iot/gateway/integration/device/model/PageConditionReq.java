/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.device.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kanglele
 * @version $Id: PageReq, v 0.1 2023/5/23 17:16 kanglele Exp $
 */
@NoArgsConstructor
@Data
public class PageConditionReq {

    @JsonProperty("current")
    private Integer current;
    @JsonProperty("size")
    private Integer size;
    //租户ID 必传
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("deptId")
    private String deptId;
    //产品ID(支持多个, 用英文逗号隔开)
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("sn")
    private String sn;
    @JsonProperty("stationId")
    private String stationId;
    @JsonProperty("state")
    private String state;
}
