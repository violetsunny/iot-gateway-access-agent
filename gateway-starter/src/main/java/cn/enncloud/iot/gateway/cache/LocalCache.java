package cn.enncloud.iot.gateway.cache;

public interface LocalCache {

    String getDeviceId(String sn);

    String getProductId(String deviceId);

    String getProtocolId(String productId);

    void putDeviceId(String sn, String deviceId);

    void putProductId(String deviceId, String productId);

    void putProtocolId(String productId, String protocolId);

    void putSn(String deviceId, String sn);

    String getSn(String deviceId);

    void putSnProductId(String sn, String productId);

    String getSnProductId(String sn);
}
