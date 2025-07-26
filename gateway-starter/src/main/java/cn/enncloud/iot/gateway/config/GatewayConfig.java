package cn.enncloud.iot.gateway.config;

import cn.enncloud.iot.gateway.protocol.cache.ProtocolCache;
import cn.enncloud.iot.gateway.protocol.loader.jar.JarProtocolInitializer;
import cn.enncloud.iot.gateway.protocol.loader.script.ScriptProtocolInitializer;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.protocol.manager.impl.ProtocolManagerImpl;
import cn.enncloud.iot.gateway.protocol.supports.ProtocolActionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.kdla.framework.common.help.ThreadPoolHelp;

@Configuration
public class GatewayConfig {

    @Autowired
    private ScriptProtocolInitializer scriptProtocolInitializer;

    @Autowired
    private JarProtocolInitializer jarProtocolInitializer;

//    @Bean
//    public ProtocolManager protocolManager(ProtocolCache protocolCache,
//                                           ProtocolActionListener protocolActionListener,
//                                           ThreadPoolHelp threadPoolHelp
//                                           ){
//        ProtocolManagerImpl protocolManager = new ProtocolManagerImpl(protocolCache, protocolActionListener, threadPoolHelp.getExecutorService());
//        protocolManager.addProtocolInitializer("jar", jarProtocolInitializer);
//        protocolManager.addProtocolInitializer("script", scriptProtocolInitializer);
//        return protocolManager;
//    }


    @Bean
    public ProtocolManager protocolManager(ProtocolCache protocolCache){
        ProtocolManagerImpl protocolManager = new ProtocolManagerImpl(protocolCache);
        protocolManager.addProtocolInitializer(jarProtocolInitializer.type().getName(), jarProtocolInitializer);
        protocolManager.addProtocolInitializer(scriptProtocolInitializer.type().getName(), scriptProtocolInitializer);
        return protocolManager;
    }



}
