package cn.enncloud.iot.gateway;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hanyilong@enn.cn
 */
@SpringBootApplication(scanBasePackages = "cn.enncloud.iot.gateway")
@MapperScan(basePackages={"cn.enncloud.iot.gateway.dal.mapper"})
@EnableFeignClients(basePackages = {"cn.enncloud.iot.gateway"})
@EnableCaching
@EnableScheduling
@EnableKafka
@EnableDiscoveryClient
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
public class EnnewApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnnewApplication.class, args);
        log.info("玉帝佛祖保佑，iot-gateway-ennew 终于能 start ！！！");
    }
}
