package cn.enncloud.iot.gateway.web;


import cn.enncloud.iot.gateway.kafka.KafkaProducer;
import cn.enncloud.iot.gateway.message.OperationRequest;
import cn.enncloud.iot.gateway.utils.CommonUtils;
import cn.enncloud.iot.gateway.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.kdla.framework.dto.SingleResponse;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/device")
public class DeviceController {

    @Value("${ennew.iot.topics.commandTopic:device_command_topic}")
    private String deviceCommandTopic;

    @Resource
    private KafkaProducer kafkaProducer;

    @PostMapping("/op/{deviceId}/{function}")
    public SingleResponse<String> operation(@PathVariable String deviceId, @PathVariable String function, @RequestBody Map<String, Object> params) {
        OperationRequest req = new OperationRequest();
        req.setDeviceId(deviceId);
        req.setFunction(function);
        req.setMessageId(CommonUtils.getUUID());
        req.setTimeStamp(System.currentTimeMillis());
        req.setParam(params);
        kafkaProducer.send(deviceCommandTopic, JsonUtil.pojoToJson(req));
        return SingleResponse.buildSuccess("success");
    }

}
