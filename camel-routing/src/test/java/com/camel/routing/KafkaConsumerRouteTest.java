package com.camel.routing;

import com.camel.fileProcessor.CamelFileProcessorApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.Model;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelFileProcessorApplication.class})
public class KafkaConsumerRouteTest {

    @DynamicPropertySource
    static void dynamicProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap.servers", () -> "localhost:9092");
        registry.add("kafka.topic", () -> "test-topic");
        registry.add("kafka.group-id", () -> "test-group");
    }

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setupMocks() throws Exception {
        // Get the Model extension of CamelContext
        Model model = (Model) camelContext.getRoutes();

        // Access the route definition
        RouteDefinition routeDefinition = model.getRouteDefinition("KafkaConsumerRoute");

        // Mock the route
        AdviceWith.adviceWith(routeDefinition, camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveByToUri("direct:ProcessKafkaMessage").replace().to("mock:ProcessKafkaMessage");
            }
        });

        // Start the Camel context
        camelContext.start();
    }

    @Test
    void testKafkaConsumerRoute() throws Exception {
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:ProcessKafkaMessage", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);

        // Simulate a Kafka message
        camelContext.createProducerTemplate().sendBodyAndHeader(
                "kafka:test-topic",
                "Test Kafka Message",
                "kafka.KEY",
                "test-key"
        );

        mockEndpoint.assertIsSatisfied();

        Exchange exchange = mockEndpoint.getExchanges().get(0);
        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("Test Kafka Message");
        assertThat(exchange.getIn().getHeader("kafka.KEY")).isEqualTo("test-key");
    }
}