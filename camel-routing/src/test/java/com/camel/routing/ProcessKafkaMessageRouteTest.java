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

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelFileProcessorApplication.class})
public class ProcessKafkaMessageRouteTest {

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setupMocks() throws Exception {

        // Get the Model extension of CamelContext
        Model model = (Model) camelContext.getRoutes();

        // Access the route definition
        RouteDefinition routeDefinition = model.getRouteDefinition("ProcessKafkaMessageRoute");

        // Mock the route
        AdviceWith.adviceWith(routeDefinition, camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveByToUri("direct:DatabaseHandler").replace().to("mock:DatabaseHandler");
            }
        });

        // Start the Camel context
        camelContext.start();
    }

    @Test
    void testProcessKafkaMessageRoute() throws Exception {
        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
        mockDatabaseEndpoint.expectedMessageCount(1);

        // Simulate sending a message to ProcessKafkaMessage
        camelContext.createProducerTemplate().sendBodyAndHeader(
                "direct:ProcessKafkaMessage",
                "Kafka Test Message",
                "kafka.KEY",
                "test-key"
        );

        mockDatabaseEndpoint.assertIsSatisfied();

        Exchange exchange = mockDatabaseEndpoint.getExchanges().get(0);
        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("Kafka Test Message");
        assertThat(exchange.getIn().getHeader("CamelFileName")).isEqualTo("test-key");
    }
}