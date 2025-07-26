//package cn.enncloud.iot.gateway.service;
//
//import cn.enncloud.iot.gateway.es.ElasticSearchOperation;
//import cn.enncloud.iot.gateway.es.constant.EsDataTypeEnum;
//import cn.enncloud.iot.gateway.es.index.EsDataEvent;
//import cn.enncloud.iot.gateway.message.Message;
//import cn.enncloud.iot.gateway.protocol.supports.ProtocolActionListener;
//import cn.hutool.core.util.HexUtil;
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Slf4j
//@Component("protocolActionListener")
//public class DeviceEsLogService extends ProtocolActionListener {
//
//    private final ElasticSearchOperation es;
//
//    private static final String BINARY_LOG_TYPE_STRING = "string";
//
//    @Autowired
//    public DeviceEsLogService(ElasticSearchOperation elasticSearchOperation) {
//        this.es = elasticSearchOperation;
//    }
//
//    @Override
//    protected void onException(Throwable throwable) {
//        EsDataEvent event = new EsDataEvent(null, EsDataTypeEnum.rtg.getType(), throwable.getMessage());
//        es.saveDeviceErrorLog(event);
//    }
//
//    @Override
//    protected void afterDecode(String binaryType, byte[] bytes, List<? extends Message> messages) {
//        if(messages.isEmpty()){
//            return;
//        }
//        boolean first = true;
//        for(Message message: messages){
//            if(first){
//                saveDeviceOriginalMessage(binaryType, bytes, message.getSn(), message.getDeviceId());
//                first = false;
//            }
//            EsDataEvent reportEvent = new EsDataEvent(message.getSn(), EsDataTypeEnum.rtg.getType(), message.getDeviceId(), JSON.toJSONString(message));
//            es.saveDeviceLog(reportEvent);
//        }
//    }
//
//    @Override
//    protected void afterEncode(String binaryType, List<? extends Message> messages, byte[] bytes) {
//
//    }
//
//
//
//    private void saveDeviceOriginalMessage(String binaryType, byte[] bytes, String sn, String deviceId){
//        String originalDeviceReport;
//        if(BINARY_LOG_TYPE_STRING.equals(binaryType)){
//            originalDeviceReport = new String(bytes, StandardCharsets.UTF_8);
//        }else{
//            originalDeviceReport = HexUtil.encodeHexStr(bytes);
//        }
//        EsDataEvent event = new EsDataEvent(sn, EsDataTypeEnum.rtg.getType(), deviceId, originalDeviceReport);
//        es.saveDeviceLog(event);
//    }
//
//}
