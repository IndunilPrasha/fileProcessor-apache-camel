package com.camel.routing;

import com.camel.fileProcessor.CamelFileProcessorApplication;
import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@CamelSpringBootTest
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

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setupMocks() {
        // Mock behavior of database handler and exception handler
        doNothing().when(databaseHandler).persistDataToDatabase(any());
        doNothing().when(globalExceptionHandler).process(any());

        // Start the Camel context before each test
        if (!camelContext.isStarted()) {
            camelContext.start();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Stop the Camel context after each test
        if (camelContext.isStarted()) {
            camelContext.stop();
        }
    }

    @Test
    void testFileProcessingRouteSuccess() throws Exception {
        // Prepare the mock endpoint
        CamelContext camelContext = new DefaultCamelContext();
//        Thread.sleep(1000);

        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
        mockDatabaseEndpoint.expectedMessageCount(1);

        // Send a sample CSV file to the input directory
        String csvContent = "Header1,Header2\nValue1,Value2";
        camelContext.createProducerTemplate().sendBodyAndHeader("file:input", csvContent, Exchange.FILE_NAME, "test.csv");

        // Verify the behavior
        mockDatabaseEndpoint.assertIsSatisfied(5000);
        verify(databaseHandler, times(1)).persistDataToDatabase(any());
    }

    @Test
    void testFileProcessingRouteErrorHandling() throws Exception {
        // Force an exception during processing
        CamelContext camelContext = new DefaultCamelContext();
        doThrow(new RuntimeException("Simulated Exception")).when(databaseHandler).persistDataToDatabase(any());

        // Prepare the mock endpoint
        MockEndpoint mockErrorEndpoint = camelContext.getEndpoint("mock:file:error", MockEndpoint.class);
        mockErrorEndpoint.expectedMessageCount(1);

        // Send a sample CSV file to the input directory
        String csvContent = "Header1,Header2\nValue1,Value2";
        camelContext.createProducerTemplate().sendBodyAndHeader("file:input", csvContent, Exchange.FILE_NAME, "error.csv");

        // Verify the behavior
        mockErrorEndpoint.assertIsSatisfied();
        verify(globalExceptionHandler, times(1)).process(any());
    }

    @Test
    void testEmptyFileValidation() throws Exception {
        // Send an empty CSV file
        CamelContext camelContext = new DefaultCamelContext();
        String emptyCsvContent = "";
        camelContext.createProducerTemplate().sendBodyAndHeader("file:input", emptyCsvContent, Exchange.FILE_NAME, "empty.csv");

        // Verify that the global exception handler is invoked
        verify(globalExceptionHandler, times(1)).process(any());
    }

    @Test
    void testCustomThreadPoolConfiguration() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        NotifyBuilder notifyBuilder = new NotifyBuilder(camelContext)
                .whenDone(1)
                .create();

        // Send a sample CSV file
        String csvContent = "Header1,Header2\nValue1,Value2";
        camelContext.createProducerTemplate().sendBodyAndHeader("file:input", csvContent, Exchange.FILE_NAME, "thread-test.csv");

        // Wait for the route to complete processing
        notifyBuilder.matches(5, TimeUnit.SECONDS);

        // Assert that processing used a custom thread pool
        // (you can also assert logging messages or mock specific validations)
    }

    @Test
    void testKafkaMessageProcessingRouteSuccess() throws Exception {
        // Prepare the mock endpoint
//        CamelContext camelContext = new DefaultCamelContext();
        Thread.sleep(1000);

        MockEndpoint mockDatabaseEndpoint = camelContext.getEndpoint("mock:DatabaseHandler", MockEndpoint.class);
        mockDatabaseEndpoint.expectedMessageCount(1);

        // Send a message to the route
        camelContext.createProducerTemplate().sendBodyAndHeader(
                "direct:ProcessKafkaMessage",
                "Test Kafka Message",
                "kafka.KEY",
                "test-key"
        );

        // Verify the mock endpoint received the message
        mockDatabaseEndpoint.assertIsSatisfied();
    }

    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new FileProcessingRoute(databaseHandler, globalExceptionHandler);
    }
}