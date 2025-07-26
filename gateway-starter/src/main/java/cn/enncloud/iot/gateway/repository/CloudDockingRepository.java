//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.entity.cloud.*;
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingBo;
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingResBo;
//import cn.enncloud.iot.gateway.service.converter.CloudDockingBoConverter;
//import cn.enncloud.iot.gateway.dal.entity.CloudDockingEntity;
//import cn.enncloud.iot.gateway.dal.mapper.CloudDockingMapper;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import top.kdla.framework.dto.exception.ErrorCode;
//import top.kdla.framework.exception.BizException;
//
//import java.util.Date;
//import java.util.Objects;
//
///**
// * @Author: alec
// * Description:
// * @date: 下午1:30 2023/5/23
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CloudDockingRepository extends ServiceImpl<CloudDockingMapper, CloudDockingEntity> implements IService<CloudDockingEntity> {
//
//    private final CloudDockingBoConverter cloudDockingBoConverter;
//
//    public Boolean saveCloudDocking(CloudDockingBo cloudDockingBO) {
//        CloudDockingEntity cloudDockingEntity = cloudDockingBoConverter.toCloudDockingEntity(cloudDockingBO);
//        if (searchByCode(cloudDockingEntity.getCode()) != null) {
//            log.error("三方平台编码已存在{}", cloudDockingEntity.getCode());
//            throw new BizException(ErrorCode.BIZ_ERROR, "三方平台编码已存在" + cloudDockingEntity.getCode());
//        }
//        cloudDockingEntity.setState(NetworkConfigState.paused.getName());
//        cloudDockingEntity.setCreateTime(new Date());
//        return save(cloudDockingEntity);
//    }
//
//    public boolean updateState(String id, NetworkConfigState state) {
//        return lambdaUpdate().set(CloudDockingEntity::getState, state.getName()).eq(CloudDockingEntity::getId, id).update();
//    }
//
//    public CloudDockingEntity searchByCode(String code) {
//        LambdaQueryChainWrapper<CloudDockingEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), CloudDockingEntity::getCode, code);
//        return queryChainWrapper.one();
//    }
//
//    public CloudDockingResBo getByCode(String code) {
//        CloudDockingEntity entity = searchByCode(code);
//        return cloudDockingBoConverter.toCloudDockingRes(entity);
//    }
//
//    public CloudDockingResBo getCloudDockingBO(String id) {
//        CloudDockingEntity entity = getById(id);
//        return cloudDockingBoConverter.toCloudDockingRes(entity);
//    }
//
//    public void removeByCode(String code) {
//        LambdaQueryChainWrapper<CloudDockingEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), CloudDockingEntity::getCode, code);
//        CloudDockingEntity entity = queryChainWrapper.one();
//        if (Objects.isNull(entity)) {
//            return;
//        }
//        removeById(entity.getId());
//    }
//}
