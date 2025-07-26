package cn.enncloud.iot.gateway.integration.other.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "点位映射对象")
@Getter
@Setter
@ToString
public class PointMappingDto {


    @Schema(description = "网关点位ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pointId;

    @Schema(description = "恩牛设备ID")
    private String deviceId;

    @Schema(description = "恩牛设备属性标识")
    private String metric;

    @Schema(description = "恩牛产品编码")
    private String productId;


}
