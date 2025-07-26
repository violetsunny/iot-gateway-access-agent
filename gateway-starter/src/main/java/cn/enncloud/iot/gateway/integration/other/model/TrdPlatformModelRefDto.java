package cn.enncloud.iot.gateway.integration.other.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class TrdPlatformModelRefDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 平台编码
     */
    private String platformCode;

    /**
     * 恩牛模型来源
     */
    private String ennModelSource;

    /**
     * 恩牛模型名称
     */
    private String ennModelName;

    /**
     * 恩牛模型标识
     */
    private String ennModelCode;

    /**
     * 产品ID
     */
    private String ennProductId;

    /**
     * 恩牛产品名称
     */
    private String ennProductName;

    /**
     * 平台模型名称
     */
    private String platformModelName;

    /**
     * 平台模型编码
     */
    private String platformModelCode;

    /**
     * 平台品牌名称
     */
    private String platformBrand;

    /**
     * 平台型号
     */
    private String platformSpec;

    /**
     * 测点映射
     */
    private List<TrdPlatformMeasureRefDto> trdPlatformMeasureRefList;

}
