//package cn.enncloud.iot.gateway.service.converter;
//
//import cn.enncloud.iot.gateway.dal.entity.*;
//import cn.enncloud.iot.gateway.repository.bo.*;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//import java.util.List;
//
///**
// * @Author: alec
// * Description:
// * @date: 下午1:38 2023/5/23
// */
//@Mapper(componentModel = "spring")
//public interface CloudDockingBoConverter {
//
//    /**
//     * BO转Entity
//     * @param cloudDockingBO BO
//     * @return Entity
//     * */
//    CloudDockingEntity toCloudDockingEntity(CloudDockingBo cloudDockingBO);
//
//    @Mapping(target = "state", expression = "java(cn.enncloud.iot.gateway.entity.cloud.NetworkConfigState.convert(entity.getState()))")
//    CloudDockingResBo toCloudDockingRes(CloudDockingEntity entity);
//
//    List<CloudDockingResBo> toCloudDockingRes(List<CloudDockingEntity> records);
//
//    CloudDockingAuthEntity toCloudDockingAuthEntity(CloudDockingAuthBo cloudDockingAuthBO);
//
//    CloudDockingAuthRespEntity toCloudDockingRespEntity(CloudDockingAuthResBo cloudDockingAuthBO);
//
//    CloudDockingDataEntity toCloudDockingDataEntity(CloudDockingDataBo cloudDockingDataBO);
//
//    @Mapping(target = "reqGroup", source = "reqGroup", defaultValue = "1")
//    CloudDockingParamsEntity toCloudDockingParamsEntity(CloudDockingDataParamsBo params);
//
//    List<CloudDockingParamsEntity> toCloudDockingParamsEntity(List<CloudDockingDataParamsBo> params);
//
//    CloudDockingDataParamsBo toDockingAuthParamsBo(CloudDockingParamsEntity cloudDockingParamsEntity);
//
//    List<CloudDockingDataParamsBo> toDockingAuthParamsBo(List<CloudDockingParamsEntity> cloudDockingParamsEntity);
//
//    CloudDockingAuthResBo toCloudDockingRespBo(CloudDockingAuthRespEntity cloudDockingAuthRespEntity);
//
//    CloudDockingAuthBo toCloudDockingRespBo(CloudDockingAuthEntity cloudDockingAuthEntity);
//
//    CloudDockingDataBo toCloudDockingDataBO(CloudDockingDataEntity cloudDockingDataEntity);
//
//    List<CloudDockingDataBo> toCloudDockingDataBOs(List<CloudDockingDataEntity> cloudDockingDataEntity);
//
//}
