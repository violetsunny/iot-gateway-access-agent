package cn.enncloud.iot.gateway.modbus.poll;

import cn.enncloud.iot.gateway.entity.cloud.ModbusPointMapping;
import lombok.Data;

import java.util.List;

@Data
public class ModbusTcpClientConfig {

    private String configId;
    private List<ModbusPointMapping> pointDTOS;

    private String gatewayCode;

    private String host;

    private Integer port;


    /**
     * 读取间隔 秒
     */
    private Integer readDiff;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    /**
     * 重连间隔 秒
     */
    private Integer retryDelay;

    /**
     * 连接超时时间
     */
    private Integer connectTimeout;

}
