package com.camel.fileProcessor.processor;

import com.camel.fileProcessor.exception.FileProcessingException;
import com.camel.fileProcessor.service.FileValidationService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileProcessor implements Processor {

    private final FileValidationService fileValidationService;
    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    @Autowired
    public FileProcessor(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        String body = exchange.getIn().getBody(String.class);

        log.info("Processing file: {} with content length: {}", fileName,
                body != null ? body.length() : "null");

        if (body != null && !body.isEmpty()) {
            fileValidationService.validateFile(fileName, body);
        } else {
            throw new FileProcessingException("File is empty or null");
        }

        exchange.setProperty("fileName", fileName);
        exchange.getIn().setHeader("CamelFileName", fileName);
        exchange.setProperty("ProcessStartTime", System.currentTimeMillis());

    }
}
