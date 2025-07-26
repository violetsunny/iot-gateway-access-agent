package cn.enncloud.iot.gateway.integration.other.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TrdPlatformMeasureRefDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 平台编码
     */
    private String platformCode;

    /**
     * 模型映射ID
     */
    private Long modelRefId;

    /**
     * 恩牛模型标识
     */
    private String ennModelCode;

    /**
     * 恩牛测点标识
     */
    private String ennMeasureCode;

    /**
     * 恩牛测点名称
     */
    private String ennMeasureName;

    /**
     * 恩牛测点单位
     */
    private String ennMeasureUnit;

    /**
     * 平台测点名称
     */
    private String platformMeasureName;

    /**
     * 平台测点编码
     */
    private String platformMeasureCode;

    /**
     * 平台测点单位
     */
    private String platformMeasureUnit;


}
