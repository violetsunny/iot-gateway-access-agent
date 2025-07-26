package cn.enncloud.iot.gateway.tcp.process;

import cn.enncloud.iot.gateway.message.LoginRequest;
import cn.enncloud.iot.gateway.message.LoginResponse;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.tcp.session.TcpSessionManger;
import cn.enncloud.iot.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author hanyilong@enn.cn
 * @since 2022-02-14 12:34:02
 */
@Slf4j
@Component("loginProcesser")
public class LoginProcesser {

    @Autowired
    TcpSessionManger tcpSessionManger;


    public LoginResponse action(Protocol protocol, LoginRequest loginRequest) {
        // 验证deviceId
        LoginResponse loginResponse = new LoginResponse();
        try {
            if (null == loginRequest.getDeviceId() && !protocol.login(loginRequest)) {
                loginResponse.setMessageId(CommonUtils.getUUID());
                loginResponse.setLogin(false);
                loginResponse.setTimeStamp(System.currentTimeMillis());
            }
        }catch (Exception e){
            log.error("Login exception", e);
        }

        loginResponse.setMessageId(CommonUtils.getUUID());
        loginResponse.setDeviceId(loginRequest.getDeviceId());
        loginResponse.setLogin(true);
        loginResponse.setTimeStamp(System.currentTimeMillis());
        return loginResponse;
    }

}
