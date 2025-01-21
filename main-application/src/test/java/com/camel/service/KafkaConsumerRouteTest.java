//package com.camel.service;
//
//import com.camel.routing.KafkaConsumerRoute;
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//import org.apache.camel.RoutesBuilder;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.mock.MockEndpoint;
//import org.apache.camel.test.junit5.CamelTestSupport;
//import org.apache.camel.test.junit5.params.Test;
//
//import static org.apache.camel.builder.AdviceWith.adviceWith;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class KafkaConsumerRouteTest extends CamelTestSupport {
//
//    @Override
//    protected RouteBuilder createRouteBuilder() {
//        return new KafkaConsumerRoute();
//    }
//
//    @Test
//    void testKafkaRoute() throws Exception {
//        // Simulate a Kafka message
//        Exchange exchange = createExchangeWithBody("Test Kafka Message");
//        exchange.getIn().setHeader("kafka.KEY", "testKey");
//
//        // Add mock processing
//        Processor processor = exchange1 -> {
//            String body = exchange1.getIn().getBody(String.class);
//            assertEquals("Test Kafka Message", body);
//        };
//
//        context.addRoutes(new RouteBuilder() {
//            @Override
//            public void configure() {
//                from("direct:kafkaRoute").process(processor);
//            }
//        });
//
//        // Send the message
//        template.send("direct:kafkaRoute", exchange);
//    }
//}
