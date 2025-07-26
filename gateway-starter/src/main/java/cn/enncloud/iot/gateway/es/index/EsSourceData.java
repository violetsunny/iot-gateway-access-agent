package cn.enncloud.iot.gateway.es.index;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author shq
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class EsSourceData extends EsBaseData {

    private static final long serialVersionUID = 5516075349620653480L;

    private long timestamp;

    private String sn;

    private String deviceId;

    private String type;

    private Object source;

    public EsSourceData(String sn, String deviceId, String type, String index, Object source) {
        this.sn = sn;
        this.deviceId = deviceId;
        this.type = type;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
        this.setId(sn + "_" + UUID.randomUUID().toString().toUpperCase().replaceAll("-", ""));
        this.setIndex(index);
    }

    public EsSourceData(String sn, String type, String index, Object source) {
        this.sn = sn;
        this.type = type;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
        this.setId(sn + "_" + UUID.randomUUID().toString().toUpperCase().replaceAll("-", ""));
        this.setIndex(index);
    }
}
