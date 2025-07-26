///**
// * llkang.com Inc.
// * Copyright (c) 2010-2024 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.dal.entity;
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
///**
// * @author kanglele
// * @version $Id: TrdPlatformBasic, v 0.1 2024/3/12 17:30 kanglele Exp $
// */
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@TableName(value="trd_platform_info", autoResultMap = true)
//public class TrdPlatformInfoEntity {
//
//    private Long id;
//
//    private Integer pType;
//
//    private String pCode;
//
//    private String pName;
//
//    @TableField(typeHandler = FastjsonTypeHandler.class)
//    private Map<String, String> configJson;
//
//    private String createUser;
//
//    private String updateUser;
//
//    private LocalDateTime createTime;
//
//    private LocalDateTime updateTime;
//
//    private Integer status;
//
//    private String remark;
//
//    private Boolean isDelete;
//
//}
