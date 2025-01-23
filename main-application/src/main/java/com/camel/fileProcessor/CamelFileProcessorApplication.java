package com.camel.fileProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(scanBasePackages = {"com.camel"})
public class CamelFileProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamelFileProcessorApplication.class, args);
    }

}
