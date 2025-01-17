package com.camel.fileProcessor.route;

import com.camel.fileProcessor.exception.FileProcessingException;
import com.camel.fileProcessor.processor.FileProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingRoute extends RouteBuilder {

    @Value("${csv.input.directory}")
    private String inputDirectory;

    @Value("${csv.processed.directory}")
    private String processedDirectory;

    @Value("${csv.error.directory}")
    private String errorDirectory;

    private final FileProcessor fileProcessor;

    @Autowired
    public FileProcessingRoute(FileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
    }

    @Override
    public void configure() throws Exception {

        //handle the error global
        errorHandler(deadLetterChannel("direct:globalErrorHandler")
                .maximumRedeliveries(3)
                .redeliveryDelay(1000)
                .backOffMultiplier(2)
                .useExponentialBackOff()
                .logRetryAttempted(true));

        // Error handling routes
        configureErrorHandlers();

        from("file:" + inputDirectory + "?include=.*.csv&initialDelay=1000&delay=5000&delete=true")
                .routeId("fileProcessingRoute")
                .log(LoggingLevel.INFO, "Started processing file: ${header.CamelFileName}")
                .convertBodyTo(String.class)
                .setHeader("originalContent", simple("${body}"))
                .process(fileProcessor)
                .log(LoggingLevel.DEBUG, "After processor: ${body}")
                .choice()
                    .when(simple("${body} != null"))
                        .log(LoggingLevel.INFO, "Valid file detected: ${header.fileName}")
                        .to("direct:processRecord")
                    .otherwise()
                        .log(LoggingLevel.INFO, "Empty file detected: ${header.fileName}")
                        .to("direct:handleInvalidFile")
                .end();

        from("direct:processRecord")
            .routeId("recordProcessingRoute")
                .unmarshal(cofigureFileFormat())
                .split(body())
                .log(LoggingLevel.DEBUG, "Record to insert: ${body}")
                .setBody(simple("${body.toString()}"))
                .log(LoggingLevel.INFO, "Inserting record: FileName=${header.CamelFileName}, Data=${body}")
                .to("sql:INSERT INTO CSV_DATE (FILE_NAME, RECORD_DATA, PROCESSED_DATE) " +
                        "VALUES (:#${header.CamelFileName}, :#${body}, CURRENT_TIMESTAMP)")
                .log(LoggingLevel.INFO, "Inserted record from file: ${header.CamelFileName}, Data=${header.RecordData}")
                .to("direct:moveToProcessed");

        from("direct:moveToProcessed")
            .routeId("successHandlerRoute")
                .split(body())
                .setBody(header("originalContent"))
                .log(LoggingLevel.INFO, "Successfully processed file: ${header.fileName}")
                .to("file:" + processedDirectory);

        from("direct:handleError")
            .routeId("errorHandlerRoute")
                .log(LoggingLevel.ERROR, "Error processing file: ${header.fileName}")
                .to("file:" + errorDirectory);
    }

    private void configureErrorHandlers() {
        from("direct:globalErrorHandler")
                .routeId("globalErrorHandler")
                .log(LoggingLevel.ERROR, "Error processing: ${header.CamelFileName}. Error: ${exception.message}")
                .choice()
                .when(simple("${exception.class} == 'org.apache.camel.CamelException'"))
                .to("direct:handleCamelError")
                .when(simple("${exception.class} == 'java.sql.SQLException'"))
                .to("direct:handleDatabaseError")
                .otherwise()
                .to("direct:handleGenericError")
                .end();

        // File movement routes for different error scenarios
        from("direct:handleInvalidFile")
                .routeId("invalidFileHandler")
                .log(LoggingLevel.WARN, "Invalid file detected: ${header.CamelFileName}")
                .to("file:" + errorDirectory + "/invalid");

        from("direct:handleDatabaseError")
                .routeId("databaseErrorHandler")
                .log(LoggingLevel.ERROR, "Database error: ${exception.message}")
                .to("file:" + errorDirectory + "/database");

        from("direct:handleGenericError")
                .routeId("genericErrorHandler")
                .log(LoggingLevel.ERROR, "Generic error: ${exception.message}")
                .to("file:" + errorDirectory + "/generic");
    }

    private CsvDataFormat cofigureFileFormat(){
        CsvDataFormat csvDataFormat = new CsvDataFormat();
        csvDataFormat.setSkipHeaderRecord(true);
        csvDataFormat.setUseMaps(true);
//        csvDataFormat.setDelimiter(";");
        csvDataFormat.setRecordSeparator("\n");
        csvDataFormat.setTrim(true);
        return csvDataFormat;

    }
}
