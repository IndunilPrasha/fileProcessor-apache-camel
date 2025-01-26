package com.camel.routing;

import com.camel.routing.CamelFileProcessorApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.Model;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = {CamelFileProcessorApplication.class})
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092",
        "port=9092",
        "auto.create.topics.enable=true"})
public class KafkaConsumerRouteTest {

    private final String kafkaTopic = "my-kafka-topic";
//    private final String bootstrapServers = "localhost:9092";
//    private final String groupId = "test-group";

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setupMocks() throws Exception {
        // Add KafkaConsumerRoute to Camel context
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("kafka:" + kafkaTopic + "?brokers=localhost:9092&groupId=test-group&autoOffsetReset=earliest")
                        .routeId("KafkaConsumerRoute")
                        .log("Received message from kafka: ${body}")
                        .to("mock:ProcessKafkaMessage");
            }
        });

        // Start the Camel context
        camelContext.start();
    }

    @Test
    void testKafkaConsumerRoute() throws Exception {
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:ProcessKafkaMessage", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.expectedBodiesReceived("Test Kafka Message");

        // Simulate a Kafka message
        camelContext.createProducerTemplate().sendBody("kafka:" + kafkaTopic + "?brokers=localhost:9092&groupId=test-group",
                "Test Kafka Message");

        mockEndpoint.assertIsSatisfied();

//        Exchange exchange = mockEndpoint.getExchanges().get(0);
//        assertThat(exchange.getIn().getBody(String.class)).isEqualTo("Test Kafka Message");
//        assertThat(exchange.getIn().getHeader("kafka.KEY")).isEqualTo("test-key");
    }
}