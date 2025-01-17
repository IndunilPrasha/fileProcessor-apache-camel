package com.camel.fileProcessor.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.apache.camel.builder.RouteBuilder;

@Component
public class DatabaseHandler extends RouteBuilder{

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHandler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void configure() throws Exception {
        from("direct:DatabaseHandler")
                .routeId("DatabaseHandler")
                .process(exchange ->  {
                    String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                    String data = exchange.getIn().getBody(String.class);

                    if (fileName == null || data == null) {
                        log.info("File name or data is missing");
                        throw new IllegalArgumentException("File name or data is missing");
                    }
                    String sql = "INSERT INTO CSV_DATE (FILE_NAME, RECORD_DATA, PROCESSED_DATE) VALUES (?, ?, CURRENT_TIMESTAMP)";
                    jdbcTemplate.update(sql, fileName, data);
                });
    }
}
