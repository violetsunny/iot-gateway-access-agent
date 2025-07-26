//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingAuthBo;
//import cn.enncloud.iot.gateway.service.converter.CloudDockingBoConverter;
//import cn.enncloud.iot.gateway.dal.entity.CloudDockingAuthEntity;
//import cn.enncloud.iot.gateway.dal.mapper.CloudDockingAuthMapper;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @Author: alec
// * Description:
// * @date: 下午3:44 2023/7/12
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CloudDockingAuthRepository extends ServiceImpl<CloudDockingAuthMapper, CloudDockingAuthEntity> implements IService<CloudDockingAuthEntity> {
//
//    private final CloudDockingBoConverter cloudDockingBoConverter;
//
//
//    public boolean save(CloudDockingAuthBo cloudDockingAuthBO) {
//        CloudDockingAuthEntity entity = cloudDockingBoConverter.toCloudDockingAuthEntity(cloudDockingAuthBO);
//        CloudDockingAuthEntity entityExist = searchByHostId(cloudDockingAuthBO.getHostId());
//        if (entityExist == null) {
//            return save(entity);
//        }
//        entity.setId(entityExist.getId());
//
//        return updateById(entity);
//    }
//
//    public CloudDockingAuthEntity searchByHostId(String hostId) {
//        LambdaQueryChainWrapper<CloudDockingAuthEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingAuthEntity::getHostId, hostId);
//        return queryChainWrapper.one();
//    }
//
//
//    public CloudDockingAuthBo searchOneByHostId(String hostId) {
//        LambdaQueryChainWrapper<CloudDockingAuthEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingAuthEntity::getHostId, hostId);
//        CloudDockingAuthEntity entity = queryChainWrapper.one();
//        return cloudDockingBoConverter.toCloudDockingRespBo(entity);
//    }
//
//    public void removeByHost(String hostId) {
//        LambdaQueryChainWrapper<CloudDockingAuthEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingAuthEntity::getHostId, hostId);
//        List<CloudDockingAuthEntity> authParamsEntityList = queryChainWrapper.list();
//        if (CollectionUtils.isEmpty(authParamsEntityList)) {
//            return;
//        }
//        removeBatchByIds(authParamsEntityList.stream().map(CloudDockingAuthEntity::getId).collect(Collectors.toList()));
//    }
//
//}
