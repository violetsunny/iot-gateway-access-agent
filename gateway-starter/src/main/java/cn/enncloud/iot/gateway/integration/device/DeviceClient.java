/**
 * llkang.com Inc.
 * Copyright (c) 2010-2022 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.device;

import cn.enncloud.iot.gateway.integration.device.model.*;
import cn.enncloud.iot.gateway.entity.Device;
import cn.enncloud.iot.gateway.integration.device.model.ProductAccessConfigReq;
import cn.enncloud.iot.gateway.integration.device.model.ProductAccessConfigRes;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import top.kdla.framework.dto.MultiResponse;
import top.kdla.framework.dto.SingleResponse;

import java.util.List;

/**
 * 设备管理
 *
 * @author kanglele
 * @version $Id: DeviceClient, v 0.1 2022/8/22 16:03 kanglele Exp $
 */
@FeignClient(url = "${ennew.iot.device.url}", name = "iot-device")
public interface DeviceClient {

    @GetMapping(value = "/device/get/{deviceId}")
    SingleResponse<DeviceDataRes> getDeviceId(@PathVariable(value = "deviceId") String deviceId);

    @PostMapping(value = "/device/listByConditionPage", headers = {"accessCode=195ml06Y532uVR79Hg"})
    PageResponse<DeviceDataRes> listByConditionPage(@RequestBody PageConditionReq req);

    @GetMapping(value = "/device/get/bySN/{sn}")
    @Headers("app-key=iot-gateway-new")
    SingleResponse<DeviceDataRes> getBySN(@PathVariable(value = "sn") String sn);

    @GetMapping(value = "/device/getSnListByProductId")
    MultiResponse<DeviceDataRes> getSnListByProductId(@RequestParam(value = "productId") String productId);

    @PostMapping(value = "/device/batchUpdateVehicleImeiBySn")
    SingleResponse batchUpdate(@RequestBody List<DeviceSnReq> req);

    @PostMapping(value = "/device/batchCreate")
    MultiResponse<String> batchCreate(@RequestBody List<DeviceAddReq> req);

    @PostMapping(value = "/device/updateById")
    SingleResponse updateById(@RequestBody DevicePropReq req);

    @PostMapping(value = "/device/getUsedSnList")
    MultiResponse<DeviceDataRes> getUsedSnList(@RequestBody PageConditionReq req);


    @PostMapping(value = "/product/getAccessConfig", headers = {"accessCode=195ml06Y532uVR79Hg"})
    SingleResponse<ProductAccessConfigRes> getProductProtocol(@RequestBody ProductAccessConfigReq req);
}
