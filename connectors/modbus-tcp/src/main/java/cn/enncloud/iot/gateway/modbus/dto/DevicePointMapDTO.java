package cn.enncloud.iot.gateway.modbus.dto;


import lombok.Data;

@Data
public class DevicePointMapDTO {


    /**
     * 点位id
     */
    private Long pointId;

    /**
     * 读写标识（r-读，w-写）
     */
    private String rw;



    private String deviceId;


    private String metric;


    private String productCode;

    private String productId;
}
