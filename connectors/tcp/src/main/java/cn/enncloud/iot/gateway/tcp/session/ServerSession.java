package cn.enncloud.iot.gateway.tcp.session;

/**
 * @author hanyilong@enn.cn
 * @since 2021-02-14 11:57:08
 */
public interface ServerSession {

    void writeAndFlush(Object pkg);

    String getSessionId();

    boolean isValid();

}
