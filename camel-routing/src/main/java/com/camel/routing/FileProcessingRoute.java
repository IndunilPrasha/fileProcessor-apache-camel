package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class FileProcessingRoute extends RouteBuilder {

    @Value("${csv.input.directory}")
    String inputDirectory;

    @Value("${csv.processed.directory}")
    private String processedDirectory;

    @Value("${csv.error.directory}")
    private String errorDirectory;

    @Value("${batch.size}")
    private int batchSize;

    @Value("${parallel.threads}")
    private int parallelThreads;

    @Value("${parallel.maxThreads}")
    private int maxThreads;

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
                    .streaming()
                    .parallelProcessing()
                    .executorService(customThreadPool())
                    .aggregate(constant(true), new BatchAggregationStrategy())
                        .completionSize(batchSize)
                        .completionTimeout(5000)
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


    public void processFile(Exchange exchange) throws Exception {
        List<List<String>> batch = exchange.getIn().getBody(List.class);
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);

        log.info("Validating file: {}", fileName);

        if (batch == null || batch.isEmpty()) {
            throw new IllegalArgumentException("This is empty: " + fileName);
        }

        exchange.getIn().setBody(batch);
    }

    private CsvDataFormat configureFileFormat(){
        CsvDataFormat csvDataFormat = new CsvDataFormat();
        csvDataFormat.setSkipHeaderRecord("true");
        return csvDataFormat;

    }

//    private ExecutorService customThreadPool() {
//        return getContext().getExecutorServiceManager().newThreadPool(this, "CustomThreadPool", parallelThreads, parallelThreads * 2);
//    }

    private java.util.concurrent.ExecutorService customThreadPool() {
        return getContext().getExecutorServiceManager().newThreadPool(
                this, "CustomThreadPoolProfile",
                parallelThreads,maxThreads);
    }
}
