package cn.enncloud.iot.gateway.modbus.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ModbusPointWriteDTO {


    /**
     * 点位id
     */
    private Integer pointId;

    /**
     * 站点地址
     */
    private Object value;


}
