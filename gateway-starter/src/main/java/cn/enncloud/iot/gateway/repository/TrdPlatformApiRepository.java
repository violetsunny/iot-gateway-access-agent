///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.dal.entity.TrdPlatformApiEntity;
//import cn.enncloud.iot.gateway.dal.mapper.TrdPlatformApiMapper;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformApiBo;
//import cn.enncloud.iot.gateway.service.converter.TrdPlatformBoConverter;
//import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * @author kanglele
// * @version $Id: TrdPlatformTaskRepository, v 0.1 2024/3/13 17:04 kanglele Exp $
// */
//@Component
//@Slf4j
//public class TrdPlatformApiRepository extends ServiceImpl<TrdPlatformApiMapper, TrdPlatformApiEntity> implements IService<TrdPlatformApiEntity> {
//
//    @Resource
//    private TrdPlatformBoConverter trdPlatformBoConverter;
//
//    public TrdPlatformApiEntity searchById(Long id) {
//        LambdaQueryChainWrapper<TrdPlatformApiEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(id!=null, TrdPlatformApiEntity::getId, id);
//        return queryChainWrapper.one();
//    }
//
//    public TrdPlatformApiBo getById(Long id) {
//        TrdPlatformApiEntity entity = searchById(id);
//        return trdPlatformBoConverter.toTrdPlatformApi(entity);
//    }
//
//}
