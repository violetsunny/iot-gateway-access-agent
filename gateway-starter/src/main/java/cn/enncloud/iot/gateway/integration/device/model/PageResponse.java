/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.device.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author kanglele
 * @version $Id: PageResponse, v 0.1 2023/5/23 17:14 kanglele Exp $
 */
@NoArgsConstructor
@Data
public class PageResponse<T> {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("data")
    private DataRes<T> data;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("success")
    private Boolean success;

    @NoArgsConstructor
    @Data
    public static class DataRes<T> {
        @JsonProperty("current")
        private Integer current;
        @JsonProperty("list")
        private List<T> list;
        @JsonProperty("size")
        private Integer size;
        @JsonProperty("total")
        private Integer total;
    }
}
