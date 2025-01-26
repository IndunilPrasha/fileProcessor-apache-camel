package com.camel.routing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ApplicationTest {

    // Application starts successfully with valid configuration
    @Test
    public void test_application_starts_with_valid_config() {
        String[] args = new String[]{"--spring.config.location=classpath:application.yml"};

        ConfigurableApplicationContext context = SpringApplication.run(CamelFileProcessorApplication.class, args);

        assertNotNull(context);
        assertTrue(context.isRunning());
        context.close();
    }

    // Application startup with no command line arguments
    @Test
    public void test_application_starts_with_no_args() {
        String[] args = new String[]{};

        ConfigurableApplicationContext context = SpringApplication.run(CamelFileProcessorApplication.class, args);

        assertNotNull(context);
        assertTrue(context.isRunning());
        context.close();
    }
}
