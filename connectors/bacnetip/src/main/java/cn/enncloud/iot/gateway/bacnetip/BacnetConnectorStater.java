package cn.enncloud.iot.gateway.bacnetip;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.BacnetConfig;
import cn.enncloud.iot.gateway.config.connectors.BacnetServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.service.RedisService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BacnetConnectorStater 负责初始化和管理 Bacnet 连接和任务。
 * 它实现了 Connector 接口，并作为 Spring 组件进行管理。
 * <p>
 *
 * @author qinkun@enn.cn
 */
@Component
@Slf4j
public class BacnetConnectorStater implements Connector, CommandLineRunner {

    @Autowired
    private BacnetConfig bacnetConfig;

    @Autowired
    private ProtocolManager protocolManager;

    @Autowired
    private DeviceContext deviceContext;

    @Autowired
    private ConnectorManager connectorManager;

    @Autowired
    private RedisService redisService;

    private final Map<String, BacnetIpTimerTask> tasks = new HashMap<>();

    public void run(String... args) throws Exception {
        try {
            this.init();
        } catch (Exception e) {
            log.error("BacnetConnectorStater Exception", e);
        }
    }

    /**
     * 初始化 Bacnet 连接器并根据配置调度任务。
     */
    @SneakyThrows
    public void init() {
        log.info("正在初始化 Bacnet 连接器...");

        List<BacnetServerConfig> protocols = bacnetConfig.getConfiguration();
        if (CollectionUtils.isEmpty(protocols)) {
            log.info("Bacnet 协议配置为空，跳过初始化。");
            return;
        }
        for (BacnetServerConfig server : protocols) {

            Protocol protocol = protocolManager.register(
                    server.getProtocol().getMainClass() + server.getLocalBindAddress() + server.getPort(),
                    server.getProtocol().getMainClass(),
                    server.getProtocol().getPath(),
                    server.getProtocol().getParams(),
                    false
            );
            if (protocol != null) {
                protocol.setDeviceContext(deviceContext);
                log.info("{} {} 协议加载成功", server.getProtocol().getMainClass() + server.getLocalBindAddress() + server.getPort(), server.getProtocol().getPath());
            }

            if (StringUtils.isNotBlank(server.getCron())) {
                BacnetIpTimerTask bacnetIpTimerTask = new BacnetIpTimerTask(protocol, server, redisService);
                TaskScheduler timer = new ConcurrentTaskScheduler();
                CronTrigger cronTrigger = new CronTrigger(server.getCron());
                timer.schedule(bacnetIpTimerTask, cronTrigger);
                tasks.put(server.getLocalBindAddress() + server.getPort(), bacnetIpTimerTask);
                log.info("Bacnet {} {}  定时任务初始化成功。", server.getLocalBindAddress() + ":" + server.getPort(), server.getCron());
            }

        }

        connectorManager.addConnector(this);
    }

    /**
     * 使用给定的参数设置协议。
     *
     * @param protocol 要设置的协议
     * @param params   协议的参数
     */
    @Override
    @SneakyThrows
    public void setupProtocol(Protocol protocol, Map<String, Object> params) {
        // 设置协议的实现
    }

    /**
     * 获取 Bacnet 连接器的状态。
     *
     * @return 包含状态信息的 Map
     */
    @Override
    public Map<String, Object> getStatus() {
        log.info("获取 Bacnet 连接器状态...");
        return tasks.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getStatus()
                ));
    }

}
