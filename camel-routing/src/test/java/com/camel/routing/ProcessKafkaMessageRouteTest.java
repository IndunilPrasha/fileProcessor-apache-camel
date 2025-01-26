package com.camel.routing;

import com.camel.routing.service.ProcessKafkaMessageRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelFileProcessorApplication.class})
public class ProcessKafkaMessageRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    void testPrepareDatabaseRecordWithKafkaKey() {
        // Create a sample exchange
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("Test Kafka Message");
        exchange.getIn().setHeader("kafka.KEY", "test-key");

        // Instantiate the route class and call the method
        ProcessKafkaMessageRoute processKafkaMessageRoute = new ProcessKafkaMessageRoute();
        processKafkaMessageRoute.prepareDatabaseRecord(exchange);

        // Assert the CamelFileName header is set correctly
        String camelFileName = exchange.getIn().getHeader("CamelFileName", String.class);
        assertEquals("test-key", camelFileName, "CamelFileName should match the Kafka key");

        // Assert the body remains unchanged
        String body = exchange.getIn().getBody(String.class);
        assertEquals("Test Kafka Message", body, "Body should remain unchanged");
    }

    @Test
    void testPrepareDatabaseRecordWithoutKafkaKey() {
        // Create a sample exchange
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("Test Kafka Message");
        exchange.getIn().setHeader("kafka.KEY", null);

        // Instantiate the route class and call the method
        ProcessKafkaMessageRoute processKafkaMessageRoute = new ProcessKafkaMessageRoute();
        processKafkaMessageRoute.prepareDatabaseRecord(exchange);

        // Assert the CamelFileName header is set to default
        String camelFileName = exchange.getIn().getHeader("CamelFileName", String.class);
        assertEquals("kafka-message", camelFileName, "CamelFileName should default to 'kafka-message'");

        // Assert the body remains unchanged
        String body = exchange.getIn().getBody(String.class);
        assertEquals("Test Kafka Message", body, "Body should remain unchanged");
    }
}