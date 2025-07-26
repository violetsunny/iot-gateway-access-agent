/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.es.index;

import lombok.Data;

import java.io.Serializable;

/**
 * es基础
 *
 * @author kanglele
 * @version $Id: EsBaseData, v 0.1 2023/1/13 15:01 kanglele Exp $
 */
@Data
public class EsBaseData implements ElasticIndex,Serializable {

    private String id;

    private String index;

}
