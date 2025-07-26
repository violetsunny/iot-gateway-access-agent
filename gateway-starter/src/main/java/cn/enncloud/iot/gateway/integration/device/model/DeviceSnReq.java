/**
 * llkang.com Inc.
 * Copyright (c) 2010-2022 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.integration.device.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kanglele
 * @version $Id: DeviceDataReq, v 0.1 2022/8/22 16:23 kanglele Exp $
 */
@Data
public class DeviceSnReq implements Serializable {

    private String sn;

    private String vehicleImei;

}
