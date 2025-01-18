package com.camel.fileProcessor.exception;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionHandler implements Processor {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void process(Exchange exchange) {

        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);

        log.error("Error processing file: {}",fileName, exception);

        exchange.getIn().getHeader("ErrorDetail",exception.getMessage());
    }
}
