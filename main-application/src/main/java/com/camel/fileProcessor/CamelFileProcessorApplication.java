package com.camel.fileProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.camel.routing"
        , "com.camel.fileProcessor.database"
        , "com.camel.fileProcessor.exception"
        , "com.camel.fileProcessor.logging"})
public class CamelFileProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamelFileProcessorApplication.class, args);
    }

//    @Bean
//    public CamelSpringBootApplicationController applicationController() {
//        return new CamelSpringBootApplicationController();
//    }
}
