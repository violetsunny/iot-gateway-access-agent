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
// * @date: 下午3:28 2023/5/22
// */
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@TableName(value="cloud_docking_params", autoResultMap = true)
//public class CloudDockingParamsEntity {
//
//    @TableId(value = "id", type = IdType.ASSIGN_ID)
//    private String id;
//
//    /**
//     * 依赖ID
//     * */
//    private String hostId;
//
//    /**
//     * 请求code
//     */
//    private String dataCode;
//
//    /**
//     * 参数类型 Auth PullData Cmd
//     * */
//    private String type;
//
//    /**
//     * 参数名
//     * */
//    private String paramKey;
//
//    /**
//     * 参数值
//     * */
//    private String paramValue;
//
//    /**
//     * 参数类型， header, params, body, path
//     * */
//    private String paramType;
//
//    private String prodId;
//
//    /**
//     * 请求分组
//     */
//    private String reqGroup;
//
//}
