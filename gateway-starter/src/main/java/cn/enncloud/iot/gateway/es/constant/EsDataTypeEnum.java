package cn.enncloud.iot.gateway.es.constant;

/**
 * @author ruanhong
 */

public enum EsDataTypeEnum {

    rtg("rtg", "网关实时上报数据"),
    history("history", "断点续传数据上报"),
    info("info", "上报设备信息"),
    status("status", "上报设备工况"),
    event("event", "事件上报"),
    other("other", "其他");


    private final String type;
    private final String desc;

    EsDataTypeEnum(String type, String desc) {
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public static String getEsIndexType(String type){
        return valueOf(type).getEsIndexType();
    }
    public String getEsIndexType(){
        switch (this) {
            case rtg:
                return "metric";
            case history:
                return "metric";
            case status:
                return "metric";
            case info:
                return "info";
            default:
                return "other";
        }
    }
}
