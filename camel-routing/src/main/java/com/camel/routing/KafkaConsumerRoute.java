package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.logging.CommonLogger;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerRoute extends RouteBuilder {

    private final DatabaseHandler databaseHandler;

    public KafkaConsumerRoute(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @Override
    public void configure() throws Exception {
        from("kafka:data-topic?brokers=localhost:9092&groupId=kafka-practice-group&autoOffsetReset=earliest")
                .routeId("KafkaConsumerRoute")
                .process(exchange -> {
                    String fileName = exchange.getIn().getHeader("fileName", String.class);
                    String fileContent = exchange.getIn().getBody(String.class);
                    log.info("Processing Kafka message: " );

                    exchange.getIn().setBody(fileContent);
                    exchange.getIn().setHeader("fileName", fileName);
                })
                .to("direct:DatabaseHandler")
                .log("Kafka message processed successfully");
    }
}
