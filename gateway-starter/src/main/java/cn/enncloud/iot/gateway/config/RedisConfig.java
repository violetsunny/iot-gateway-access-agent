package cn.enncloud.iot.gateway.config;

import cn.enncloud.iot.gateway.protocol.supports.RedisTopicRegistry;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

/**
 * redis配置类
 * 维护redis集群节点的在离线状态同步
 */
@Configuration
public class RedisConfig {

    /**
     * Redis序列化器定义 <String, Object>
     * 在此设定redis key value hash的序列化、反序列化方式
     *
     * @param connectionFactory 链接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        FstSerializationRedisSerializer serializer = new FstSerializationRedisSerializer(() ->
                FSTConfiguration.createDefaultConfiguration().setForceSerializable(true));
        // key序列化
        template.setKeySerializer(redisSerializer);
        // value序列化
        template.setValueSerializer(serializer);
        // hash key序列化
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);
        // hash value序列化
        template.setHashValueSerializer(serializer);
        // 初始化redisTemplate
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis消息监听器容器
     * 这个容器加载了RedisConnectionFactory和消息监听器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
     * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     * @param connectionFactory 链接工厂
     * @param registry 适配器
     * @return redis消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, RedisTopicRegistry registry) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        registry.getTopicListenerMapping().forEach((topic, adapter) -> container.addMessageListener(adapter, topic));
       return container;
    }

}

@AllArgsConstructor
class FstSerializationRedisSerializer implements RedisSerializer<Object> {
    private final FastThreadLocal<FSTConfiguration> configuration;

    public FstSerializationRedisSerializer(Supplier<FSTConfiguration> supplier) {
        this(new FastThreadLocal<FSTConfiguration>() {
            @Override
            protected FSTConfiguration initialValue() {
                return supplier.get();
            }
        });
    }

    @Override
    @SneakyThrows
    public byte[] serialize(Object o) throws SerializationException {
        ByteArrayOutputStream arr = new ByteArrayOutputStream(1024);
        try (FSTObjectOutput output = configuration.get().getObjectOutput(arr)) {
            output.writeObject(o);
        }
        return arr.toByteArray();
    }

    @Override
    @SneakyThrows
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        try (FSTObjectInput input = configuration.get().getObjectInput(bytes)) {
            return input.readObject();
        }
    }
}
