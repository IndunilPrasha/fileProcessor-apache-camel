package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileProcessingRoute extends RouteBuilder {

    @Value("${csv.input.directory}")
    private String inputDirectory;

    @Value("${csv.processed.directory}")
    private String processedDirectory;

    @Value("${csv.error.directory}")
    private String errorDirectory;

    private final DatabaseHandler databaseHandler;
    private final GlobalExceptionHandler globalExceptionHandler;

    public FileProcessingRoute(DatabaseHandler databaseHandler, GlobalExceptionHandler globalExceptionHandler) {
        this.databaseHandler = databaseHandler;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true)
                .process(globalExceptionHandler)
                .to("file:" + errorDirectory + "?fileName=${header:CamelFileName}")
                .log("File move to error directory due to processing exception: ${header.CamelFileName}");

        from("file:" + inputDirectory + "?include=.*.csv&initialDelay=1000&delay=5000&delete=false")
                .routeId("FileProcessingRoute")
                .convertBodyTo(String.class)
                .unmarshal(configureFileFormat())
                .split(body())
                .log("Reading file: ${header.CamelFileName}")
                .doTry()
                        .process(this::processFile)
                        .to("direct:DatabaseHandler")
                    .log("Successfully processed file: ${header.CamelFileName}")
                .doCatch(Exception.class)
                    .process(globalExceptionHandler)
                    .to("file:" + errorDirectory + "?fileName=${header.CamelFileName}")
                .doFinally()
                    .log("Finished processing file: ${header.CamelFileName}")
                .end();
    }


    private void processFile(Exchange exchange) throws Exception {
        List<List<String>> rows = exchange.getIn().getBody(List.class);
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);

        log.info("Validating file: {}", fileName);

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("This is empty: " + fileName);
        }

        exchange.getIn().setBody(rows);
    }

    private CsvDataFormat configureFileFormat(){
        CsvDataFormat csvDataFormat = new CsvDataFormat();
        csvDataFormat.setSkipHeaderRecord("true");
        return csvDataFormat;

    }
}
