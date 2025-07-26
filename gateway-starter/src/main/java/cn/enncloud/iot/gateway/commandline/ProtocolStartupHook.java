package cn.enncloud.iot.gateway.commandline;

import cn.enncloud.iot.gateway.protocol.loader.ProtocolAutoLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProtocolStartupHook implements CommandLineRunner {

    @Resource
    private ProtocolAutoLoader protocolAutoLoader;

    @Override
    public void run(String... args) throws Exception {
        protocolAutoLoader.load();
    }


}
