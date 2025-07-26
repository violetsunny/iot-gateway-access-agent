package cn.enncloud.iot.gateway.http;


import cn.enncloud.iot.gateway.Connector;
import cn.enncloud.iot.gateway.ConnectorManager;
import cn.enncloud.iot.gateway.config.connectors.HttpConfig;
import cn.enncloud.iot.gateway.config.connectors.HttpConfigProtocol;
import cn.enncloud.iot.gateway.context.DeviceContext;
import cn.enncloud.iot.gateway.message.Message;
import cn.enncloud.iot.gateway.protocol.Protocol;
import cn.enncloud.iot.gateway.protocol.manager.ProtocolManager;
import cn.enncloud.iot.gateway.service.RedisService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.*;
import top.kdla.framework.dto.SingleResponse;
import top.kdla.framework.exception.BizException;
import top.kdla.framework.log.catchlog.CatchAndLog;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag(name = "http接入")
@RestController
@RequestMapping(value = "/api")
@Slf4j
@CatchAndLog
public class HttpConnectorStater implements Connector, CommandLineRunner {

    @Autowired
    HttpConfig httpConfig;
    @Autowired
    ProtocolManager protocolManager;
    @Autowired
    DeviceContext deviceContext;
    @Autowired
    ConnectorManager connectorManager;
    @Autowired
    RedisService redisService;

    @PostMapping("/{pkey}/**")
    @Operation(summary = "多种路径接入api")
    public SingleResponse request(HttpServletRequest request, @PathVariable String pkey, @RequestBody Object data) throws Exception {
        String wildcardPath = getWildcardPath(request);
        Map<String, String> headers = getHeaders(request);
        log.info("HttpConnectorStater request {} {} {} {}",pkey,wildcardPath,JSON.toJSONString(headers),JSON.toJSONString(data));
        //协议
        Protocol protocol = null;
        //转成产品找协议id
        List<Object> protocolIds = redisService.getThirdCloudProductId(pkey);
        if (CollectionUtils.isNotEmpty(protocolIds) && StringUtils.isNotBlank((String) protocolIds.get(0))) {
            //上行
            protocol = protocolManager.get((String) protocolIds.get(0));
        } else {
            if (CollectionUtils.isNotEmpty(httpConfig.getConfiguration())) {
                HttpConfigProtocol httpConfigProtocol = httpConfig.getConfiguration().stream().filter(config -> pkey.equals(config.getRequestPath())).findFirst().orElse(null);
                if (httpConfigProtocol != null && httpConfigProtocol.getProtocol() != null) {
                    protocol = protocolManager.get(httpConfigProtocol.getProtocol().getMainClass() + pkey);
                }
            }
        }
        if (protocol == null) {
            log.warn("request 没有找到协议 {}",pkey);
            throw new BizException(pkey+"没有找到协议");
        }

        protocol.setDeviceContext(deviceContext);
        //http server和 http client 应该统一入参：请求路径，请求体，返回体，映射关系
        List<? extends Message> messages = protocol.decodeMulti(JSONObject.toJSONBytes(data),pkey,headers,wildcardPath);
        log.info("HttpConnectorStater request 解析后数据：{}",JSON.toJSONString(messages));
        if(CollectionUtils.isNotEmpty(messages)){
            for(Message message:messages){
                deviceContext.storeMessage(message);
            }
        }
        return SingleResponse.buildSuccess("OK");
    }



    private String getWildcardPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        Pattern pattern = Pattern.compile("/api/.*?/(.*)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);  // 输出：alarm 或 alarm/code
        }
        return uri;
    }

    private Map<String,String> getHeaders(HttpServletRequest request){
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            this.init();
        } catch (Exception e) {
            log.error("HttpConnectorStater Exception",e);
        }
    }

    @SneakyThrows
    public void init() {
        List<HttpConfigProtocol> protocols = httpConfig.getConfiguration();
        for (HttpConfigProtocol protocolConfig : protocols) {
            if (protocolConfig != null && protocolConfig.getProtocol() != null) {
                try {
                    Protocol protocol = protocolManager.register(protocolConfig.getProtocol().getMainClass() + protocolConfig.getRequestPath(), protocolConfig.getProtocol().getMainClass(), protocolConfig.getProtocol().getPath(), protocolConfig.getProtocol().getParams(), false);
                    if(protocol!=null){
                        log.info("{} {} 协议加载成功",protocolConfig.getProtocol().getMainClass(),protocolConfig.getProtocol().getPath());
                    }
                } catch (Exception e) {
                    log.warn("{} {} 协议加载失败:{}",protocolConfig.getProtocol().getMainClass(),protocolConfig.getProtocol().getPath(),ExceptionUtils.getStackTrace(e));
                }
            }
        }

        connectorManager.addConnector(this);
    }

    @Override
    public void setupProtocol(Protocol protocol, Map params) {

    }

    @Override
    public Map getStatus() {
        return null;
    }
}
