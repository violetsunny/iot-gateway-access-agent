///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.repository;
//
//import cn.enncloud.iot.gateway.dal.entity.*;
//import cn.enncloud.iot.gateway.dal.mapper.*;
//import cn.enncloud.iot.gateway.repository.bo.TrdPlatformInfoBo;
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
//public class TrdPlatformInfoRepository extends ServiceImpl<TrdPlatformInfoMapper, TrdPlatformInfoEntity> implements IService<TrdPlatformInfoEntity> {
//
//    @Resource
//    private TrdPlatformBoConverter trdPlatformBoConverter;
//
//    public TrdPlatformInfoEntity searchByCode(String code) {
//        LambdaQueryChainWrapper<TrdPlatformInfoEntity> queryChainWrapper = this.lambdaQuery()
//                .eq(StringUtils.hasText(code), TrdPlatformInfoEntity::getPCode, code);
//        return queryChainWrapper.one();
//    }
//
//    public TrdPlatformInfoBo getByCode(String code) {
//        TrdPlatformInfoEntity entity = searchByCode(code);
//        return trdPlatformBoConverter.toTrdPlatformInfo(entity);
//    }
//
//}
