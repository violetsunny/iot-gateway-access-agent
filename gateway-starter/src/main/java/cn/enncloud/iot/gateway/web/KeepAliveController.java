package cn.enncloud.iot.gateway.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 保活页
 */
@Tag(name = "保活页")
@RestController
@RequestMapping("/keepAlive")
public class KeepAliveController {
    @Operation(summary = "保活页")
    @GetMapping()
    public String keepAlive(HttpServletRequest request) {
        return "ok";
    }
}
