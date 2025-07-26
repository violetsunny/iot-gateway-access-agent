///**
// * llkang.com Inc.
// * Copyright (c) 2010-2023 All Rights Reserved.
// */
//package cn.enncloud.iot.gateway.es;
//
//
//import cn.enncloud.iot.gateway.es.index.EsDataEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.bulk.BulkProcessor;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.xcontent.XContentType;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import top.kdla.framework.common.utils.DateUtil;
//
//import java.util.Date;
//
///**
// * 7.17版本
// * @author kanglele
// * @version $Id: ElasticSearchService, v 0.1 2023/2/20 11:36 kanglele Exp $
// */
//@Slf4j
//@Service
//public class ElasticSearchOperation {
//
////    @Resource
////    private ElasticsearchTemplate elasticsearchOperations;
////    @Resource
////    private ThreadPoolHelp threadPoolHelp;
//    @Autowired
//    protected BulkProcessor bulkProcessor;
//
//    public void saveEsLog(EsDataEvent esDataEvent, boolean isErrorLog) {
//        IndexRequest request = new IndexRequest(getIndexName(esDataEvent, isErrorLog));
//        request.id(esDataEvent.getSn() + "_" + esDataEvent.getUuid());
//        request.source(JsonUtils.writeValueAsString(esDataEvent), XContentType.JSON);
//        request.type("_doc");
//        bulkProcessor.add(request);
//    }
//
//    private String getIndexName(EsDataEvent esDataEvent, boolean isErrorLog) {
//        if(isErrorLog){
//            return "custom_metric_error" + "_" +  DateUtil.format(new Date(), "yyyy-MM-dd-HH");
//        }
//        return "custom_metric" + "_" +  DateUtil.format(new Date(), "yyyy-MM-dd-HH");
//    }
//
//
//
//
//
//    public void saveDeviceLog(EsDataEvent event){
//        saveEsLog(event, false);
//    }
//
//
//    public void saveDeviceErrorLog(EsDataEvent event){
//        saveEsLog(event, true);
//    }
//
//}
