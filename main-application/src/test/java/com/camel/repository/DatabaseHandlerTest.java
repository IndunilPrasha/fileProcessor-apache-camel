//package com.camel.repository;
//
//import com.camel.fileProcessor.database.DatabaseHandler;
//import org.apache.camel.CamelContext;
//import org.apache.camel.Exchange;
//import org.apache.camel.impl.DefaultCamelContext;
//import org.apache.camel.support.DefaultExchange;
//import org.apache.camel.test.junit5.CamelTestSupport;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//public class DatabaseHandlerTest extends CamelTestSupport {
//
//    private DatabaseHandler databaseHandler;
//    private Exchange exchange;
//
//    @BeforeEach
//    public void setUp() {
//        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
//        databaseHandler = new DatabaseHandler(jdbcTemplate, null); // Mock GlobalExceptionHandler if needed
//        exchange = new DefaultExchange(context);
//    }
//
//    @Test
//    void testValidateInput_ValidInput() {
//        exchange.getIn().setHeader("CamelFileName", "test-file.csv");
//        exchange.getIn().setBody("Test data content");
//
//        // No exception should be thrown
//        databaseHandler.validateInput(exchange);
//    }
//
//    @Test
//    void testValidateInput_EmptyFileName() {
//        exchange.getIn().setHeader("CamelFileName", "");
//        exchange.getIn().setBody("Test data content");
//
//        assertThrows(IllegalArgumentException.class, () -> databaseHandler.validateInput(exchange));
//    }
//
//    @Test
//    void testValidateInput_EmptyBody() {
//        exchange.getIn().setHeader("CamelFileName", "test-file.csv");
//        exchange.getIn().setBody("");
//
//        assertThrows(IllegalArgumentException.class, () -> databaseHandler.validateInput(exchange));
//    }
//}
