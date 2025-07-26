/**
 * llkang.com Inc.
 * Copyright (c) 2010-2024 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.service.converter;

import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformTask;
import cn.enncloud.iot.gateway.integration.other.model.TrdPlatformTaskDto;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * @author kanglele
 * @version $Id: TrdPlatformConverter, v 0.1 2024/3/20 13:43 kanglele Exp $
 */
@Mapper(componentModel = "spring")
public interface TrdPlatformConverter {

    TrdPlatformTask toTrdPlatformTask(TrdPlatformTaskDto data);

    List<TrdPlatformTask> toTrdPlatformTasks(Collection<TrdPlatformTaskDto> data);

}
