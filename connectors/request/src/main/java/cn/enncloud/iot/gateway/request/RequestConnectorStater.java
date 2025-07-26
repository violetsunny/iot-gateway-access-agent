package cn.enncloud.iot.gateway.request;

import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.HttpRequestConfig;
import cn.enncloud.iot.gateway.config.connectors.HttpRequestMapping;
import cn.enncloud.iot.gateway.config.connectors.HttpServerConfig;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.context.TrdPlatformCloudServer;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformEnum;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformTask;
import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformTaskMessage;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.service.RedisService;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import top.kdla.framework.exception.BizException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RequestConnectorStater implements Connector, CommandLineRunner {
    @Autowired
    HttpRequestConfig httpRequestConfig;
    @Autowired
    ProtocolManager protocolManager;
    @Autowired
    DeviceContext deviceContext;
    @Resource
    TrdPlatformCloudServer trdPlatformCloudServer;
    @Autowired
    ConnectorManager connectorManager;
    @Autowired
    RedisService redisService;

    @Override
    public void run(String... args) throws Exception {
        try {
            this.init();
        } catch (Exception e) {
            log.error("RequestConnectorStater Exception",e);
        }
    }

    @SneakyThrows
    public void init() {
        List<TrdPlatformTask> taskList = trdPlatformCloudServer.taskWorkList(null);
        if(CollectionUtils.isEmpty(taskList)){
            return;
        }

        for(TrdPlatformTask task:taskList){
            if (CollectionUtils.isNotEmpty(httpRequestConfig.getConfiguration())) {
                HttpServerConfig httpConfigProtocol = httpRequestConfig.getConfiguration().stream().filter(protocolConfig -> task.getProductId().equals(protocolConfig.getProductId()) && task.getTaskCode().equalsIgnoreCase(protocolConfig.getServer())).findFirst().orElse(null);
                if (httpConfigProtocol != null && httpConfigProtocol.getProtocol() != null) {
                    try {
                        Protocol protocol = protocolManager.register(httpConfigProtocol.getProtocol().getMainClass() + httpConfigProtocol.getProductId(), httpConfigProtocol.getProtocol().getMainClass(), httpConfigProtocol.getProtocol().getPath(), httpConfigProtocol.getProtocol().getParams(), false);
                        if(protocol!=null){
                            log.info("{} {} 协议加载成功",httpConfigProtocol.getProtocol().getMainClass() + httpConfigProtocol.getProductId(),httpConfigProtocol.getProtocol().getPath());
                        }
                    } catch (Exception e) {
                        log.warn("{} {} 协议加载失败:{}",httpConfigProtocol.getProtocol().getMainClass() + httpConfigProtocol.getProductId(),httpConfigProtocol.getProtocol().getPath(),ExceptionUtils.getStackTrace(e));
                    }
                }
            }

            if (task != null) {
                trdPlatformCloudServer.operateTaskWork(task, TrdPlatformEnum.ADD.getCode(),httpRequestConfig.getJobType());
            }

        }

        connectorManager.addConnector(this);
    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus() {
        return null;
    }


    @KafkaListener(topics = {"${ennew.iot.topics.cloudTopic:iot_gateway_trdPlatform_data}"}, groupId = "TrdPlatformCloudKafkaConsumer")
    public void listen(ConsumerRecord<String, Object> consumerRecord) {
        try {
            log.info("grop TrdPlatformCloudKafkaConsumer received command {}", consumerRecord.value());
            TrdPlatformTaskMessage message = JsonUtil.jsonToPojo(consumerRecord.value().toString(), TrdPlatformTaskMessage.class);
            if(message!=null){
                TrdPlatformTask task = transformTrdPlatformTask(message);
                trdPlatformCloudServer.operateTaskWork(task,message.getOperate(),httpRequestConfig.getJobType());
            }
        } catch (BizException e) {
            log.warn("TrdPlatformCloudKafkaConsumer 失败，原因：{}", ExceptionUtils.getMessage(e));
        } catch (Exception e) {
            log.error("TrdPlatformCloudKafkaConsumer error", e);
        }
    }

    private TrdPlatformTask transformTrdPlatformTask(TrdPlatformTaskMessage message) {
        if (message == null) {
            return null;
        }
        TrdPlatformTask trdPlatformTask = new TrdPlatformTask();
        trdPlatformTask.setId(message.getId());
        trdPlatformTask.setPCode(message.getPCode());
        trdPlatformTask.setTaskCode(message.getTaskCode());
        trdPlatformTask.setTaskName(message.getTaskName());
        trdPlatformTask.setApiId(message.getApiId());
        trdPlatformTask.setFrequency(message.getFrequency());
        trdPlatformTask.setProductId(message.getProductId());
        trdPlatformTask.setStatus(message.getStatus());
        return trdPlatformTask;
    }
}
