package cn.enncloud.iot.gateway.es.index;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author shq
 */
@Data
public class EsDataEvent implements Serializable {

    private String sn;

    private String type;

    private List<String> deviceId;

    private long timestamp;

    private Object source;

    private String uuid;

    public EsDataEvent(String sn, String type, Object source) {
        this.source = source;
        this.sn = sn;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public EsDataEvent(String sn, String type, String deviceId, Object source) {
        this.source = source;
        this.sn = sn;
        this.type = type;
        this.deviceId = Collections.singletonList(deviceId);
        this.timestamp = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public EsDataEvent(String sn, String type, List<String> deviceId, Object source) {
        this.source = source;
        this.sn = sn;
        this.type = type;
        this.deviceId = deviceId;
        this.timestamp = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }
}
