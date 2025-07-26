/**
 * llkang.com Inc.
 * Copyright (c) 2010-2022 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.device.model;

import cn.enncloud.iot.gateway.entity.Device;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备简要信息
 *
 * @author kanglele
 * @version $Id: DeviceInfo, v 0.1 2022/8/22 16:10 kanglele Exp $
 */
@NoArgsConstructor
@Data
public class DeviceDataRes {

    private String id;
    private String sn;
    private String name;
    private String cloudCode;
    private String productId;
    private String productCode;
    private String productName;
    private String categoryCode;
    private String categoryName;
    private String brandCode;
    private String brandName;
    private String brandModelCode;
    private String brandModelName;
    private Integer state;
    private Integer onlineState;
    private Integer testFlag;
    private String registryTime;
    private String photoUrl;
    private String address;
    private String longitude;
    private String latitude;
    private String altitude;
//        private List<?> productProperties;
//        private List<?> inherentProperties;
//        private List<?> tags;
//        private ConfigsDTO configs;
    private String description;
    private String plateUrl;
    private String distantViewUrl;
    private String createTime;
    private String ckInstanceId;
    private String entityTypeId;
    private String entityTypeCode;
    private String entityTypeSource;
    private String deviceType;
    private Boolean gatewayDeviceFlag;
    private String updateUser;
    private String updateTime;
    private Integer isDeleted;
    private String createUser;
    private String parentId;
    private String entId;
    private String deptId;
    private String tenantId;
    private String vehicleImei;

    public static Device transformDevice(DeviceDataRes deviceDataRes) {
        if (deviceDataRes == null) {
            return null;
        }
        Device device = new Device();
        device.setDeviceId(deviceDataRes.getId());
        device.setSn(deviceDataRes.getSn());
        device.setName(deviceDataRes.getName());
        device.setProductId(deviceDataRes.getProductId());
        device.setDeptId(deviceDataRes.getDeptId());
        device.setTenantId(deviceDataRes.getTenantId());
        device.setVehicleImei(deviceDataRes.getVehicleImei());
        return device;
    }
}
