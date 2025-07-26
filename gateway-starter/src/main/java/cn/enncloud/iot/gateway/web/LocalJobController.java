package cn.enncloud.iot.gateway.web;


import cn.enncloud.iot.gateway.timer.manager.TimeJobManagerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import top.kdla.framework.dto.SingleResponse;
import top.kdla.framework.dto.exception.ErrorCode;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("/localJob")
@ConditionalOnProperty(prefix = "xxl.enabled", name = "local", havingValue = "true")
public class LocalJobController {


    @Resource
    TimeJobManagerService localJobManager;


    @PostMapping("/add")
    public SingleResponse addJob(@RequestParam String name,@RequestParam String cron,@RequestParam String handler,@RequestParam String param) {


        String taskId = localJobManager.addJob(name,cron, handler, param);

        if (Objects.nonNull(taskId)) {
            return SingleResponse.of(taskId);
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/start/{id}")
    public SingleResponse startJob(@PathVariable String id) {
        if (localJobManager.startJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/stop/{id}")
    public SingleResponse stopJob(@PathVariable String id) {
        if (localJobManager.stopJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/remove/{id}")
    public SingleResponse removeJob(@PathVariable String id) {
        if (localJobManager.removeJob(id)) {
            return SingleResponse.buildSuccess();
        }
        return SingleResponse.buildFailure(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/page")
    public SingleResponse pageJob(@RequestParam(required = false, defaultValue = "0") int start,
                                  @RequestParam(required = false, defaultValue = "10") int length, int triggerStatus) {
        return SingleResponse.of(localJobManager.pageList(start, length, triggerStatus));
    }


}
