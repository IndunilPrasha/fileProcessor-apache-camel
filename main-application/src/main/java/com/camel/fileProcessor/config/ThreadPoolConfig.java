package com.camel.fileProcessor.config;

import org.apache.camel.spi.ThreadPoolProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolProfile customThreadPoolProfile(@Value("${parallel.threads}") int poolSize) {
        ThreadPoolProfile threadPoolProfile = new ThreadPoolProfile();
        threadPoolProfile.setId("CustomThreadPoolProfile");
        threadPoolProfile.setPoolSize(poolSize);
        threadPoolProfile.setMaxPoolSize(poolSize * 2);
        return threadPoolProfile;
    }
}
