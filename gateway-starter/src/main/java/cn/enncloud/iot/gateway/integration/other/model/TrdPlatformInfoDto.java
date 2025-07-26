/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.other.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author kanglele
 * @version $Id: TrdPlatformBasic, v 0.1 2024/3/12 17:30 kanglele Exp $
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TrdPlatformInfoDto {

    private Long id;

    @JsonProperty("ptype")
    private Integer pType;
    @JsonProperty("pcode")
    private String pCode;
    @JsonProperty("pname")
    private String pName;
    @JsonProperty("psource")
    private String pSource;

    /**
     * {"jar":"1668888361635876866"} {"script":{"up":"c452df3b8ad24aefbc5561b209618bd4","down":""}}
     */
    private String protocolId;

    private Map<String, String> configJson;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer status;

    private String remark;

}
