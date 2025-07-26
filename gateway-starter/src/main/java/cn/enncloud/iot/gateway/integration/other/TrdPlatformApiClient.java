/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.other;

import cn.enncloud.iot.gateway.integration.other.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.kdla.framework.dto.MultiResponse;
import top.kdla.framework.dto.SingleResponse;

import java.util.Map;

/**
 * @author kanglele
 * @version $Id: TrdPlatformApiClient, v 0.1 2024/3/20 11:26 kanglele Exp $
 */
@FeignClient(url = "${ennew.iot.other.url}", name = "gateway-std")
//@FeignClient(url = "127.0.0.1:8870/access", name = "gateway-std")
public interface TrdPlatformApiClient {
    @PostMapping("/trd/api/taskWorkList")
    MultiResponse<TrdPlatformTaskDto> taskWorkList(@RequestBody TrdPlatformReqDto reqDto);
    @PostMapping("/trd/api/taskWork")
    SingleResponse<TrdPlatformTaskDto> taskWork(@RequestBody TrdPlatformReqDto reqDto);
    @PostMapping("/trd/api/trdInfo")
    SingleResponse<TrdPlatformInfoDto> trdInfo(@RequestBody TrdPlatformReqDto reqDto);
    @GetMapping("/trd/api/trdInfo/{ptype}")
    MultiResponse<TrdPlatformInfoDto> trdInfos(@PathVariable("ptype") Integer ptype);
    @PostMapping("/trd/api/apiInfo")
    SingleResponse<TrdPlatformApiDto> apiInfo(@RequestBody TrdPlatformReqDto reqDto);
    @PostMapping("/trd/api/apiParam")
    MultiResponse<TrdPlatformApiParamDto> apiParam(@RequestBody TrdPlatformReqDto reqDto);
    @PostMapping("/trd/api/updateTaskStatus")
    SingleResponse<Boolean> updateTaskStatus(@RequestBody TrdPlatformReqDto reqDto);
    @GetMapping("/trd/api/enum/{type}")
    SingleResponse<Map<String, Map<String,Object>>> getEnum(@PathVariable(value = "type") String type);
    @PostMapping("/trd/api/modelRef")
    SingleResponse<TrdPlatformModelRefDto> modelRef(@RequestBody TrdPlatformReqDto reqDto);

    @GetMapping("/{gatewayCode}/modbus/points")
    MultiResponse<ModbusPointDto> getModbusPoints(@PathVariable(value = "gatewayCode") String gatewayCode);
    @GetMapping("/{gatewayCode}/point/mapping")
    MultiResponse<PointMappingDto> getPointMapping(@PathVariable(value = "gatewayCode") String gatewayCode);
}
