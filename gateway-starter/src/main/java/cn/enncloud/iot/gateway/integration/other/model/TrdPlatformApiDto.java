/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.other.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author kanglele
 * @version $Id: TrdPlatformBasic, v 0.1 2024/3/12 17:30 kanglele Exp $
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TrdPlatformApiDto {

    private Long id;

    @JsonProperty("pcode")
    private String pCode;

    private String apiName;

    private Integer apiType;

    private String fullUrl;

    private String method;

    private Boolean hasParam;

    private Integer bodyAnalysisType;

    private String bodyAnalysisCode;

    private Boolean hasPages;

    private Integer pageSize;

    private String pageSizeKey;

    private String pageNumberKey;

    private Integer pageStartNo;

    private Integer pagePosition;

    private Integer totalNumberType;

    private String totalNumberConfig;

    private Integer functionType;

    private Integer authType;

    private Long authApi;

    private Double callLimit;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer status;

    private String remark;

}
