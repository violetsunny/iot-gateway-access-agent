///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.dal.entity.TrdPlatformApiParamEntity;
//import cn.enncloud.iot.gateway.dal.mapper.TrdPlatformApiParamMapper;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformApiParamBo;
//import cn.enncloud.iot.gateway.service.converter.TrdPlatformBoConverter;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * @author kanglele
// * @version $Id: TrdPlatformTaskRepository, v 0.1 2024/3/13 17:04 kanglele Exp $
// */
//@Component
//@Slf4j
//public class TrdPlatformApiParamRepository extends ServiceImpl<TrdPlatformApiParamMapper, TrdPlatformApiParamEntity> implements IService<TrdPlatformApiParamEntity> {
//
//    @Resource
//    private TrdPlatformBoConverter trdPlatformBoConverter;
//
//    public List<TrdPlatformApiParamEntity> searchById(Long apiId) {
//        LambdaQueryChainWrapper<TrdPlatformApiParamEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(apiId!=null, TrdPlatformApiParamEntity::getApiId, apiId);
//        return queryChainWrapper.list();
//    }
//
//    public List<TrdPlatformApiParamBo> getById(Long apiId) {
//        List<TrdPlatformApiParamEntity> entity = searchById(apiId);
//        return trdPlatformBoConverter.toTrdPlatformApiParams(entity);
//    }
//
//}
