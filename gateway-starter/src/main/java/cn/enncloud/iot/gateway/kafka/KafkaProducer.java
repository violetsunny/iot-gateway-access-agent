package cn.enncloud.iot.gateway.kafka;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;


@Slf4j
@Component
public class KafkaProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    public boolean send(String topic, Object message) {
        try {
            // 异步获取发送结果
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    log.error("{} 生产者 发送消息失败 - {} ", topic,JSONObject.toJSONString(message), throwable);
                }
                @Override
                public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                    log.info("{} 生产者 发送消息成功 - {}", topic, JSONObject.toJSONString(message));
                }
            });
        } catch (Exception e) {
            log.error("{} 生产者 发送消息异常 - {} ", topic, JSONObject.toJSONString(message), e);
            return false;
        }
        return true;
    }
}
