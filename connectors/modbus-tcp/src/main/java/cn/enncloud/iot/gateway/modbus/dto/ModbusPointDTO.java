package cn.enncloud.iot.gateway.modbus.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ModbusPointDTO {


    /**
     * 点位id
     */
    private Long pointId;

    /**
     * 站点地址
     */
    private Integer slaveAddr;
    /**
     * 功能码，冗余存储，支持单元测试用例
     */
    private Integer functionCode;
    /**
     * 寄存器地址，从0开始
     */
    private Integer registerAddress;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 字节序
     */
    private String byteOrder;

}
