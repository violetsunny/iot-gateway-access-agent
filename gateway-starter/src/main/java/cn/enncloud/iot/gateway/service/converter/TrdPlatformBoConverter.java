//package cn.enncloud.iot.gateway.service.converter;
//
//import cn.enncloud.iot.gateway.dal.entity.*;
//import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformTask;
//import cn.enncloud.iot.gateway.entity.cloud.TrdPlatformTaskMessage;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformApiBo;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformApiParamBo;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformInfoBo;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformTaskBo;
//import org.mapstruct.Mapper;
//
//import java.util.List;
//
///**
// * @Author: alec
// * Description:
// * @date: 下午1:38 2023/5/23
// */
//@Mapper(componentModel = "spring")
//public interface TrdPlatformBoConverter {
//
//    TrdPlatformInfoBo toTrdPlatformInfo(TrdPlatformInfoEntity entity);
//
//    TrdPlatformTaskBo toTrdPlatformTask(TrdPlatformTaskEntity entity);
//
//    TrdPlatformApiBo toTrdPlatformApi(TrdPlatformApiEntity entity);
//
//    List<TrdPlatformApiParamBo> toTrdPlatformApiParams(List<TrdPlatformApiParamEntity> entity);
//
//    TrdPlatformApiParamBo toTrdPlatformApiParam(TrdPlatformApiParamEntity entity);
//
//    TrdPlatformTask fromTrdPlatformTaskMessage(TrdPlatformTaskMessage message);
//
//}
