//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingDataBo;
//import cn.enncloud.iot.gateway.service.converter.CloudDockingBoConverter;
//import cn.enncloud.iot.gateway.dal.entity.CloudDockingDataEntity;
//import cn.enncloud.iot.gateway.dal.mapper.CloudDockingDataMapper;
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
// * @date: 下午2:03 2023/7/20
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CloudDockingDataRepository extends ServiceImpl<CloudDockingDataMapper, CloudDockingDataEntity> implements IService<CloudDockingDataEntity> {
//
//    private final CloudDockingBoConverter cloudDockingBoConverter;
//
//
//    public boolean save(CloudDockingDataBo cloudDockingDataBO) {
//        CloudDockingDataEntity entity = cloudDockingBoConverter.toCloudDockingDataEntity(cloudDockingDataBO);
//        CloudDockingDataEntity entityExist = searchByHostIdUrl(cloudDockingDataBO.getHostId(), cloudDockingDataBO.getRequestUrl());
//        if (entityExist == null) {
//            return save(entity);
//        }
//        entity.setId(entityExist.getId());
//        return updateById(entity);
//    }
//
//    public List<CloudDockingDataBo> getByHostId(String hostId, String dataCode, String updown) {
//        return cloudDockingBoConverter.toCloudDockingDataBOs(searchByHostId(hostId, dataCode, updown));
//    }
//
//    public List<CloudDockingDataEntity> searchByHostId(String hostId, String dataCode, String updown) {
//        LambdaQueryChainWrapper<CloudDockingDataEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingDataEntity::getHostId, hostId)
//                .eq(StringUtils.hasText(dataCode), CloudDockingDataEntity::getDataCode, dataCode)
//                .eq(StringUtils.hasText(updown), CloudDockingDataEntity::getUpdown, updown);
//        return queryChainWrapper.list();
//    }
//
//    public CloudDockingDataEntity searchByHostIdUrl(String hostId, String url) {
//        LambdaQueryChainWrapper<CloudDockingDataEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingDataEntity::getHostId, hostId)
//                .eq(StringUtils.hasText(url), CloudDockingDataEntity::getRequestUrl, url);
//        return queryChainWrapper.one();
//    }
//
//    public void removeByHost(String code) {
//        LambdaQueryChainWrapper<CloudDockingDataEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), CloudDockingDataEntity::getHostId, code);
//        List<CloudDockingDataEntity> authParamsEntityList = queryChainWrapper.list();
//        if (CollectionUtils.isEmpty(authParamsEntityList)) {
//            return;
//        }
//        removeBatchByIds(authParamsEntityList.stream().map(CloudDockingDataEntity::getId).collect(Collectors.toList()));
//    }
//}
