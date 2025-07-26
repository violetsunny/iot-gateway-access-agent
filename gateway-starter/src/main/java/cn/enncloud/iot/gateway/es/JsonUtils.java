package cn.enncloud.iot.gateway.es;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JSON utility <br>
 *
 * @author shq dependency jackson-databind
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        // 设置FAIL_ON_EMPTY_BEANS属性，当序列化空对象不要抛异常
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 属性为NULL 不序列化,只对bean起作用；对Map List不起作用
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 设置FAIL_ON_UNKNOWN_PROPERTIES属性，当JSON字符串中存在Java对象没有的属性，忽略
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String writeValueAsString(Object obj){
        long startTime = 0;
        if (log.isTraceEnabled()) {
            startTime = System.nanoTime();
        }

        if (log.isTraceEnabled()) {
            log.trace("Object2Json 耗时{}ms", TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - startTime)));
        }
        String result = null;
        try {
            result = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Object2Json解析失败！obj：" + obj, e);
        }
        return result;

    }

    public static <T> T readObject(String jsonString, TypeReference<T> valueTypeRef) throws IOException {
        return OBJECT_MAPPER.readValue(jsonString, valueTypeRef);
    }

    public static <T> T readObject(String jsonString, Class<T> valueType) throws IOException {
        return OBJECT_MAPPER.readValue(jsonString, valueType);
    }
}


