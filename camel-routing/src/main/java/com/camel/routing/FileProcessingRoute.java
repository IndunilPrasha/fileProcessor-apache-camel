package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalException;
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

    private final DatabaseHandler databaseHandler;

    public FileProcessingRoute(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    log.error("Error occurred during file processing", exception);
                    throw new GlobalException("Error processing file", exception);
                });

        from("file:" + inputDirectory + "?include=.*.csv&initialDelay=1000&delay=5000&delete=false")
                .routeId("FileProcessingRoute")
                .unmarshal(configureFileFormat())
                .split(body())
                .process(exchange -> {
                    List<List<String>> rows = exchange.getIn().getBody(List.class);

                    String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                    String fileContent = exchange.getIn().getBody(String.class);
                    log.info("Processing file: " + fileName);
                    log.info("Processing file content: " + fileContent);

                    if (rows == null || rows.isEmpty()) {
                        throw new GlobalException("The file is empty");
                    }
                    if (rows.size() == 1){
                        throw new GlobalException("The file is contains only headers");
                    }
                    exchange.getIn().setBody(fileContent);
                    exchange.getIn().setHeader("CamelFileName", fileName);
//                    databaseHandler.persistData(fileName, fileContent);
                    log.info("Data persisted successfully");
                })
                .to("direct:DatabaseHandler")
//                .to("file:" + processedDirectory + "?fileName=${header.CamelFileName}")
                .log("File processing completed");
    }

    private CsvDataFormat configureFileFormat(){
        CsvDataFormat csvDataFormat = new CsvDataFormat();
        csvDataFormat.setSkipHeaderRecord("true");
        return csvDataFormat;

    }
}
