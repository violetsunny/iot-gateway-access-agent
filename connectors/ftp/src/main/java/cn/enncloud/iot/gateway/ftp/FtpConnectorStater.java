package cn.enncloud.iot.gateway.ftp;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.protocol.Protocol;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hanyilong@enn.cn
 */
@Component
public class FtpConnectorStater implements Connector {
    @Override
    public void init() throws Exception {

    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus() {
        return null;
    }
}
