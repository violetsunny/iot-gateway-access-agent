///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.dal.entity.TrdPlatformTaskEntity;
//import cn.enncloud.iot.gateway.dal.mapper.TrdPlatformTaskMapper;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformTaskBo;
//import cn.enncloud.iot.gateway.service.converter.TrdPlatformBoConverter;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.Resource;
//
///**
// * @author kanglele
// * @version $Id: TrdPlatformTaskRepository, v 0.1 2024/3/13 17:04 kanglele Exp $
// */
//@Component
//@Slf4j
//public class TrdPlatformTaskRepository extends ServiceImpl<TrdPlatformTaskMapper, TrdPlatformTaskEntity> implements IService<TrdPlatformTaskEntity> {
//
//    @Resource
//    private TrdPlatformBoConverter trdPlatformBoConverter;
//
//    //一个平台对多个产品对多个任务，一个任务一个url
//    public TrdPlatformTaskEntity searchByCode(String code,String productId,String taskCode) {
//        LambdaQueryChainWrapper<TrdPlatformTaskEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), TrdPlatformTaskEntity::getPCode, code)
//                .eq(StringUtils.hasText(productId), TrdPlatformTaskEntity::getProductId, productId)
//                .eq(StringUtils.hasText(taskCode), TrdPlatformTaskEntity::getTaskCode, taskCode)
//                .eq(TrdPlatformTaskEntity::getIsDelete, 0);
//        return queryChainWrapper.one();
//    }
//
//    public TrdPlatformTaskBo getByCode(String code, String taskCode) {
//        TrdPlatformTaskEntity entity = searchByCode(code,null,taskCode);
//        return trdPlatformBoConverter.toTrdPlatformTask(entity);
//    }
//
//    public TrdPlatformTaskBo getByProductCode(String productId, String taskCode) {
//        TrdPlatformTaskEntity entity = searchByCode(null,productId,taskCode);
//        return trdPlatformBoConverter.toTrdPlatformTask(entity);
//    }
//}
