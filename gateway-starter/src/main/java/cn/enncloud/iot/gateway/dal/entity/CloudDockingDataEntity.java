//package cn.enncloud.iot.gateway.dal.entity;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
///**
// * @Author: alec
// * Description:
// * @date: 下午2:00 2023/7/20
// */
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@TableName(value="cloud_docking_data", autoResultMap = true)
//public class CloudDockingDataEntity {
//
//    @TableId(value = "id", type = IdType.ASSIGN_ID)
//    private String id;
//
//    private String hostId;
//
//    /**
//     * 请求code
//     */
//    private String dataCode;
//
//    private String requestUrl;
//
//    /**
//     * 请求类型
//     * form, json
//     * */
//    private String requestType;
//
//    private String requestMethod;
//
//    private String rootPath;
//
//    //TODO 可废弃
////    private String devicePath;
//    //TODO 可废弃
////    private Integer split;
//
//    private Integer reqLimit;
//
//    //上下行
//    private String updown;
//
////    @TableField(typeHandler = FastjsonTypeHandler.class)
//    private String extContent;
//}
