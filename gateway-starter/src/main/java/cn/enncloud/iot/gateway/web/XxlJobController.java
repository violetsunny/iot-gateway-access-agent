package cn.enncloud.iot.gateway.web;


import cn.enncloud.iot.gateway.timer.manager.TimeJobManagerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import top.kdla.framework.dto.SingleResponse;
import top.kdla.framework.dto.exception.ErrorCode;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("/xxlJob")
@ConditionalOnProperty(prefix = "xxl.enabled", name = "xxl", havingValue = "true")
public class XxlJobController {


    @Resource
    TimeJobManagerService ennIotXxlJobManager;


    @PostMapping("/add")
    public SingleResponse addJob(@RequestParam String name,@RequestParam String cron,@RequestParam String handler,@RequestParam String param) {


        String taskId = ennIotXxlJobManager.addJob(name,cron, handler, param);

        if (Objects.nonNull(taskId)) {
            return SingleResponse.of(taskId);
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/start/{id}")
    public SingleResponse startJob(@PathVariable String id) {
        if (ennIotXxlJobManager.startJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/stop/{id}")
    public SingleResponse stopJob(@PathVariable String id) {
        if (ennIotXxlJobManager.stopJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/remove/{id}")
    public SingleResponse removeJob(@PathVariable String id) {
        if (ennIotXxlJobManager.removeJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/page")
    public SingleResponse pageJob(@RequestParam(required = false, defaultValue = "0") int start,
                                  @RequestParam(required = false, defaultValue = "10") int length, int triggerStatus) {
        return SingleResponse.of(ennIotXxlJobManager.pageList(start, length, triggerStatus));
    }


}
