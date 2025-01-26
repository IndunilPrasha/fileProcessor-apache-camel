package com.camel.routing.service;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerRoute extends RouteBuilder {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    @Value("${kafka.group-id}")
    private String groupId;

    @Override
    public void configure() throws Exception {
        from("kafka:" + kafkaTopic + "?brokers=" + bootstrapServers + "&groupId=" + groupId + "&autoOffsetReset=earliest")
                .routeId("KafkaConsumerRoute")
                .log("Received message from kafka: ${body}")
                .to("direct:ProcessKafkaMessage");
    }
}
