package com.camel.fileProcessor.database;

import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import org.apache.camel.Exchange;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHandler extends RouteBuilder{

    private final JdbcTemplate jdbcTemplate;
    private final GlobalExceptionHandler globalExceptionHandler;

    public DatabaseHandler(JdbcTemplate jdbcTemplate, GlobalExceptionHandler globalExceptionHandler) {
        this.jdbcTemplate = jdbcTemplate;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler)
                .log("Database error handle for file: ${header.CamelFileName}")
                .end();

        from("direct:DatabaseHandler")
                .routeId("DatabaseHandlerRoute")
                .doTry()
                    .process(this::validateInput)
                    .process(this::persistDataToDatabase)
                .log("Data successfully persisted for file: ${header:CamelFileName}")
                .doCatch(Exception.class)
                    .process(globalExceptionHandler)
                .doFinally()
                    .log("DatabaseHandler processing completed for file: ${header.CamelFileName}")
                .end();
    }

    private void validateInput(Exchange exchange) throws IllegalArgumentException {
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        String data = exchange.getIn().getBody(String.class);

        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is required");
        }

        log.info("Input validated for file: " + fileName);

    }
    private void persistDataToDatabase(Exchange exchange) {

        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        String data = exchange.getIn().getBody(String.class);

        String sql = "INSERT INTO CSV_DATE (FILE_NAME, RECORD_DATA, PROCESSED_DATE) VALUES (?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, fileName, data);

        log.info("Data successfully persisted for file: " + fileName);
    }
}
