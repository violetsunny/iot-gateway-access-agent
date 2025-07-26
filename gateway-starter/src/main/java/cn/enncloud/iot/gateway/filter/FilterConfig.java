//package cn.enncloud.iot.gateway.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.catalina.connector.RequestFacade;
//import org.apache.tomcat.util.http.MimeHeaders;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.servlet.Filter;
//import java.lang.reflect.Field;
//
//@Configuration
//@Slf4j
//public class FilterConfig {
//
//    @Bean
//    public Filter filter() {
//        return (request, response, chain) -> {
//            System.out.println("innerFilter doFilter");
//            try {
//                RequestFacade requestFacade = (RequestFacade) request;
//                // 反射获取request属性
//                Field request1 = requestFacade.getClass().getDeclaredField("request");
//                // 设置私有属性权限
//                request1.setAccessible(true);
//                Object o = request1.get(request);
//                // 获取coyoteRequest
//                Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
//                coyoteRequest.setAccessible(true);
//                Object o1 = coyoteRequest.get(o);
//                Field headers = o1.getClass().getDeclaredField("headers");
//                headers.setAccessible(true);
//                // 获取Header
//                MimeHeaders o2 = (MimeHeaders)headers.get(o1);
//                String entId = requestFacade.getParameter("entId");
//                // 根据entId确定路由
//                if(gotoEnniot(entId)){
//                    o2.addValue("processor").setString("enniot");
//                }
//
//                //
//
//            }catch (Exception e){
//                log.warn(e.getMessage());
//            }
//            chain.doFilter(request, response);
//        };
//    }
//
//    private boolean gotoEnniot(String entId) {
//        if("1".equals(entId)){
//            return true;
//        }
//        return false;
//    }
//
//}
