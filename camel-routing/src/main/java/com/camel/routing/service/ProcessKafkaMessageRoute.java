package com.camel.routing.service;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProcessKafkaMessageRoute extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        from("direct:ProcessKafkaMessage")
                .routeId("ProcessKafkaMessageRoute")
                .process(this::prepareDatabaseRecord)
//                .process(this::convertMessageToList)
                .to("direct:DatabaseHandler")
                .log("Kafka message persisted to database: ${body}");
    }

    public void prepareDatabaseRecord(Exchange exchange) {
        String kafkaMessage = exchange.getIn().getBody(String.class);
        String kafkaKey = exchange.getIn().getHeader("kafka.KEY",String.class);

        exchange.getIn().setHeader("CamelFileName", kafkaKey != null ? kafkaKey : "kafka-message");
        exchange.getIn().setBody(kafkaMessage);
    }

//    private void convertMessageToList(Exchange exchange) {
//        String message = exchange.getIn().getBody(String.class);
//        List<String> messageList = Arrays.asList(message.split(","));
//        exchange.getIn().setBody(messageList);
//    }
}
