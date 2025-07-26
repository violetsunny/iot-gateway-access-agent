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
// * @date: 下午2:29 2023/5/30
// */
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@TableName(value="cloud_docking_metadata", autoResultMap = true)
//public class CloudDockingMetadataEntity {
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
//     * 属性
//     */
//    private String code;
//    /**
//     * 属性名
//     */
//    private String name;
//
//    /**
//     * 获取路径
//     * */
//    private String pathValue;
//
//    /**
//     * 数据类型
//     * */
//    private String dataType;
//
//    /**
//     * 是否是数组
//     * */
//    private Integer isList;
//
//    /**
//     * 父id
//     */
//    private String parentId;
//
//    /**
//     * 产品id
//     */
//    private String productId;
//
//}
