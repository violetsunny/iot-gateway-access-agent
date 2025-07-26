//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.repository.bo.CloudDockingDataParamsBo;
//import cn.enncloud.iot.gateway.service.converter.CloudDockingBoConverter;
//import cn.enncloud.iot.gateway.dal.entity.CloudDockingParamsEntity;
//import cn.enncloud.iot.gateway.dal.mapper.CloudDockingParamsMapper;
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
// * @date: 下午1:33 2023/5/25
// */
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class CloudDockingParamsRepository extends ServiceImpl<CloudDockingParamsMapper, CloudDockingParamsEntity>
//        implements IService<CloudDockingParamsEntity> {
//
//    private final CloudDockingBoConverter cloudDockingBoConverter;
//
//    public List<CloudDockingDataParamsBo> searchByCode(String hostId, String dataCode, String type, String prodId) {
//        LambdaQueryChainWrapper<CloudDockingParamsEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingParamsEntity::getHostId, hostId)
//                .eq(StringUtils.hasText(dataCode), CloudDockingParamsEntity::getDataCode, dataCode)
//                .eq(StringUtils.hasText(type), CloudDockingParamsEntity::getType, type)
//                .eq(StringUtils.hasText(prodId), CloudDockingParamsEntity::getProdId, prodId);
//        List<CloudDockingParamsEntity> authParamsEntityList = queryChainWrapper.list();
//        return cloudDockingBoConverter.toDockingAuthParamsBo(authParamsEntityList);
//    }
//
//
//    public boolean batchSaveEntity(String hostId, String dataCode, String type, List<CloudDockingDataParamsBo> params) {
//        LambdaQueryChainWrapper<CloudDockingParamsEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingParamsEntity::getHostId, hostId)
//                .eq(StringUtils.hasText(type), CloudDockingParamsEntity::getType, type)
//                .eq(StringUtils.hasText(dataCode), CloudDockingParamsEntity::getDataCode, dataCode);
//
//        List<CloudDockingParamsEntity> authParamsEntityList = queryChainWrapper.list();
//
//        List<CloudDockingParamsEntity> entityList = cloudDockingBoConverter.toCloudDockingParamsEntity(params);
//        if (CollectionUtils.isEmpty(authParamsEntityList)) {
//            return saveBatch(entityList);
//        }
//        log.info("data {}", authParamsEntityList);
//        removeBatchByIds(authParamsEntityList.stream().map(CloudDockingParamsEntity::getId).collect(Collectors.toList()));
//        return saveBatch(entityList);
//    }
//
//    public void removeByHost(String hostId) {
//        LambdaQueryChainWrapper<CloudDockingParamsEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(hostId), CloudDockingParamsEntity::getHostId, hostId);
//        List<CloudDockingParamsEntity> authParamsEntityList = queryChainWrapper.list();
//        if (CollectionUtils.isEmpty(authParamsEntityList)) {
//            return;
//        }
//        removeBatchByIds(authParamsEntityList.stream().map(CloudDockingParamsEntity::getId).collect(Collectors.toList()));
//    }
//
//}
