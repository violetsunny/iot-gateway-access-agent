//package cn.enncloud.iot.gateway.dal.entity;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.format.annotation.DateTimeFormat;
//
//import java.util.Date;
//import java.util.Map;
//
///**
// * @Author: alec
// * Description:
// * @date: 上午10:32 2023/7/12
// */
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@TableName(value="cloud_docking_base", autoResultMap = true)
//public class CloudDockingEntity {
//
//    @TableId(value = "id", type = IdType.ASSIGN_ID)
//    private String id;
//
//    private String code;
//
//    private String name;
//
//    private String baseUrl;
//
//    @TableField(typeHandler = FastjsonTypeHandler.class)
//    private Map<String, String> configuration;
//
//    private String state;
//
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date createTime;
//
//    //模式（push,pull）
//    private String model;
//}
