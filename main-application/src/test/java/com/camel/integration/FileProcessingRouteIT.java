//package com.camel.integration;
//
//import org.apache.camel.Exchange;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.apache.camel.test.junit5.CamelTestSupport;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@CamelSpringBootTest
//@SpringBootTest
//class FileProcessingRouteIT {
//
//    @Autowired
//    ProducerTemplate producerTemplate;
//
//    @Test
//    void testFileProcessingRoute() {
//        // Send a test file to the route
//        String fileContent = "Header1,Header2\nValue1,Value2";
//        Exchange exchange = producerTemplate.send("file:D:/BJB/ROC_BJB/fileProcessor/data/input", e -> {
//            e.getIn().setBody(fileContent);
//            e.getIn().setHeader(Exchange.FILE_NAME, "testFile.csv");
//        });
//
//        // Validate processing result
//        assertThat(exchange.isFailed()).isFalse();
//        assertThat(exchange.getIn().getHeader("CamelFileName")).isEqualTo("testFile.csv");
//    }
//}
