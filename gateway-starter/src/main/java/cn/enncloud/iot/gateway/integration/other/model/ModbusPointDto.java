package cn.enncloud.iot.gateway.integration.other.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Schema(description = "Modbus点位信息")
@Getter
@Setter
@ToString
public class ModbusPointDto {



    /**
     * 从站地址
     */
    @Schema(description = "从站地址")
    private Integer slaveAddress;

    /**
     * 原始设备名称
     */
    @Schema(description = "原始设备名称")
    private String realDeviceName;


    /**
     * 测点ID
     */
    @Schema(description = "点位ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pointId;


    /**
     * 测点顺序
     */
    @Schema(description = "点位顺序")
    private Integer sort;

    /**
     * 测点名称
     */
    @Schema(description = "点位名称")
    private String name;


    /**
     * 连接器类型
     */
    @Schema(description = "连接器类型")
    private Integer connectorType;


    /**
     * 云网关编码
     */
    @Schema(description = "网关编码")
    private String cloudGatewayCode;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    /**
     * 功能码
     */
    @Schema(description = "功能码")
    private Integer functionCode;


    /**
     * 寄存器地址
     */
    @Schema(description = "寄存器地址")
    private Integer registerAddress;


    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private String dataType;


    /**
     * 字节顺序
     */
    @Schema(description = "字节序")
    private String byteOrder;


    /**
     * 读写类型
     */
    @Schema(description = "读写类型")
    private String rw;

}
