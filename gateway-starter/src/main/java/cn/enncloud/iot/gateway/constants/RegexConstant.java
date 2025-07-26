package cn.enncloud.iot.gateway.constants;

/**
 * @Description: 通用正则表达式
 */
public interface RegexConstant {

    /**
     * 名称校验正则
     */
    String NAME_PATTER = "^[\\u4E00-\\u9FA5\\w\\-]{0,32}$";

    String NAME_ILLEGAL_MESSAGE = "只能包含中文字符、英文字符、数字、下划线和中横线，0~20个字符";

    /**
     * 内容校验正则
     */
    String CONTENT_PATTER = "[\\u4E00-\\u9FA5\\w\\-\\_]*";

    String CONTENT_ILLEGAL_MESSAGE = "只能包含中文字符、英文字符、数字、下划线和中横线";

    /**
     * url校验正则
     */
    String URL_PATTER = "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";

    String URL_ILLEGAL_MESSAGE = "请输入正确格式的URL";

}
