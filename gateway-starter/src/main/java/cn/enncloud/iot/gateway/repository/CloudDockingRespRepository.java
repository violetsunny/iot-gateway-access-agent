//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingAuthResBo;
//import cn.enncloud.iot.gateway.service.converter.CloudDockingBoConverter;
//import cn.enncloud.iot.gateway.dal.entity.CloudDockingAuthRespEntity;
//import cn.enncloud.iot.gateway.dal.mapper.CloudDockingRespMapper;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.RequiredArgsConstructor;
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
// * @date: 下午5:10 2023/5/29
// */
//@Service
//@RequiredArgsConstructor
//public class CloudDockingRespRepository extends ServiceImpl<CloudDockingRespMapper, CloudDockingAuthRespEntity> implements IService<CloudDockingAuthRespEntity> {
//
//    private final CloudDockingBoConverter cloudDockingBoConverter;
//
//    public CloudDockingAuthResBo getCloudDockingAuthResBO(String hostId) {
//        return cloudDockingBoConverter.toCloudDockingRespBo(searchByHostId(hostId));
//    }
//
//    public CloudDockingAuthRespEntity searchByHostId(String hostId) {
//        LambdaQueryChainWrapper<CloudDockingAuthRespEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingAuthRespEntity::getHostId,  hostId);
//        return queryChainWrapper.one();
//    }
//
//    public boolean saveRes(CloudDockingAuthResBo cloudDockingAuthBO) {
//        CloudDockingAuthRespEntity entity = cloudDockingBoConverter.toCloudDockingRespEntity(cloudDockingAuthBO);
//        CloudDockingAuthRespEntity entityExist = searchByHostId(cloudDockingAuthBO.getHostId());
//        if (entityExist == null) {
//            save(entity);
//        }
//        //更新
//        entity.setId(entityExist.getId());
//        return updateById(entity);
//    }
//
//    public void removeByHost(String code) {
//        LambdaQueryChainWrapper<CloudDockingAuthRespEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), CloudDockingAuthRespEntity::getHostId, code);
//        List<CloudDockingAuthRespEntity> authParamsEntityList = queryChainWrapper.list();
//        if (CollectionUtils.isEmpty(authParamsEntityList)) {
//            return;
//        }
//        removeBatchByIds(authParamsEntityList.stream().map(CloudDockingAuthRespEntity::getId).collect(Collectors.toList()));
//    }
//}
