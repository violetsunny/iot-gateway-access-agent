/**
 * llkang.com Inc.
 * Copyright (c) 2010-2023 All Rights Reserved.
 */
package cn.enncloud.iot.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.kdla.framework.common.help.ThreadPoolHelp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kanglele
 * @version $Id: ThreadPoolConfig, v 0.1 2023/2/16 17:41 kanglele Exp $
 */
@Configuration
public class ThreadPoolConfig {

    private ThreadPoolHelp threadPoolHelp() {
        return ThreadPoolHelp.builder()
                .corePoolSize(4)
                .maximumPoolSize(40)
                .keepAliveTime(0L)
                .unit(TimeUnit.SECONDS)
                .workQueue(new LinkedBlockingDeque<>())
                .handler(new ThreadPoolExecutor.AbortPolicy())
                .build();
    }

    @Bean
    public ExecutorService executorService() {
        return this.threadPoolHelp().getExecutorService();
    }

}
