package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import com.camel.routing.service.FileProcessingRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultRoute;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.support.LifecycleStrategySupport.adapt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@CamelSpringBootTest
//@CamelSpringTest
@SpringBootTest(classes = {CamelFileProcessorApplication.class})
public class FileProcessingRouteTest {

    @DynamicPropertySource
    static void dynamicProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("csv.input.directory", () -> "src/test/resources/input");
        registry.add("batch.size", () -> "10");
        registry.add("parallel.threads", () -> "2");
        registry.add("parallel.maxThreads", () -> "4");
    }

    @MockBean
    private DatabaseHandler databaseHandler;

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setup() throws Exception {
        // Mock the database handler if needed
        doNothing().when(databaseHandler).persistDataToDatabase(any());
        // Add the FileProcessingRoute to the Camel context
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:FileProcessingRoute")
                        .routeId("FileProcessingRoute")
                        .log("Processing file: ${header.CamelFileName}")
                        .process(exchange -> {
                            Exchange body = exchange.getIn().getBody(Exchange.class);
                            databaseHandler.persistDataToDatabase(body); // Explicitly invoke the mock
                        })
                        .to("mock:DatabaseHandler");
            }
        });

        // Start the Camel context
        camelContext.start();
    }

    @Test
    void testFileProcessingRoute() throws Exception {
        // Prepare MockEndpoint
        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
        mockDatabaseEndpoint.expectedMessageCount(1);

        // Simulate file input
        String csvContent = "Header1,Header2\nValue1,Value2";
        camelContext.createProducerTemplate().sendBodyAndHeader("direct:FileProcessingRoute", csvContent, "CamelFileName", "test.csv");

        // Assert mock endpoint
        mockDatabaseEndpoint.assertIsSatisfied();

        // Verify that database handler logic was invoked (if relevant)
        verify(databaseHandler, times(1)).persistDataToDatabase(any());
    }

    @Test
    void testProcessFileValidBatch() throws Exception {
        // Simulate a valid batch
        List<List<String>> batch = Arrays.asList(
                Arrays.asList("Header1", "Header2"),
                Arrays.asList("Value1", "Value2")
        );

        // Prepare Exchange with valid data
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(batch);
        exchange.getIn().setHeader("CamelFileName", "test.csv");

        // Call the processFile method
        FileProcessingRoute fileProcessingRoute = new FileProcessingRoute(databaseHandler,null);
        fileProcessingRoute.processFile(exchange);

        // Assert the batch is correctly set in the exchange body
        List<List<String>> result = exchange.getIn().getBody(List.class);
        assertNotNull(result, "Batch should not be null");
        assertEquals(batch, result, "The processed batch should match the input batch");
    }

    @Test
    void testProcessFileEmptyBatch() {
        // Simulate an empty batch
        List<List<String>> emptyBatch = Arrays.asList();

        // Prepare Exchange with empty batch
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(emptyBatch);
        exchange.getIn().setHeader("CamelFileName", "empty.csv");

        // Verify that an exception is thrown
        FileProcessingRoute fileProcessingRoute = new FileProcessingRoute(databaseHandler, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            fileProcessingRoute.processFile(exchange);
        });

        // Assert the exception message
        assertEquals("This is empty: empty.csv", exception.getMessage());
    }

    @Test
    void testProcessFileNullBatch() {
        // Simulate a null batch
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(null);
        exchange.getIn().setHeader("CamelFileName", "null.csv");

        // Verify that an exception is thrown
        FileProcessingRoute fileProcessingRoute = new FileProcessingRoute(databaseHandler, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            fileProcessingRoute.processFile(exchange);
        });

        // Assert the exception message
        assertEquals("This is empty: null.csv", exception.getMessage());
    }


    @Test
    void testCustomThreadPoolConfiguration() throws Exception {

        // Prepare MockEndpoint
        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
        mockDatabaseEndpoint.expectedMessageCount(1);

        NotifyBuilder notifyBuilder = new NotifyBuilder(camelContext)
                .whenDone(1)
                .create();

        // Send a sample CSV file
        String csvContent = "Header1,Header2\nValue1,Value2";
        camelContext.createProducerTemplate().sendBodyAndHeader("direct:FileProcessingRoute", csvContent, Exchange.FILE_NAME, "thread-test.csv");

        // Wait for the route to complete processing
        notifyBuilder.matches(5, TimeUnit.SECONDS);

        // Assert that processing used a custom thread pool
        // (you can also assert logging messages or mock specific validations)
    }

//    @Test
//    void testKafkaMessageProcessingRouteSuccess() throws Exception {
//        // Prepare the mock endpoint
////        CamelContext camelContext = new DefaultCamelContext();
//        Thread.sleep(1000);
//
//        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
//        mockDatabaseEndpoint.expectedMessageCount(1);
//
//        // Send a message to the route
//        camelContext.createProducerTemplate().sendBodyAndHeader(
//                "direct:ProcessKafkaMessage",
//                "Test Kafka Message",
//                "kafka.KEY",
//                "test-key"
//        );
//
//        // Verify the mock endpoint received the message
//        mockDatabaseEndpoint.assertIsSatisfied();
//    }

    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new FileProcessingRoute(databaseHandler, null);
    }
}