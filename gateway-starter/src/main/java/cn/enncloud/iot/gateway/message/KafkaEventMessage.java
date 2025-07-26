package cn.enncloud.iot.gateway.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author shq
 */
@Data
public class KafkaEventMessage implements Serializable {

    private String version;

    private String identifier;

    private String type;

    private String devId;
    //物模型
    private String devType;
    //物模型
    private String productId;
    //物模型
    private String tenantId;

    private String source;
    //物模型
    private String staId;

    private Long ts;
    //物模型
    private String domain;
    //物模型
    private String deviceCode;

    //增加"eventStatus":1
    private Map<String, Object> value;

}
