package com.camel.fileProcessor.service;

import com.camel.fileProcessor.exception.FileProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

@Service
public class FileValidationService {

    private static final Logger log = LoggerFactory.getLogger(FileValidationService.class);

    public void validateFile(String fileName, String content) throws FileProcessingException {

        log.info("Validating file: {}", fileName);
        if (content == null || content.trim().isEmpty()) {
            throw new FileProcessingException("File is empty");
        }
        if (!fileName.toLowerCase().endsWith(".csv")) {
            throw new FileProcessingException("File must be a CSV file");
        }
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String headerLine = reader.readLine();
            if (headerLine == null || !headerLine.contains(";")) {
                throw new FileProcessingException("Invalid CSV format: missing header or wrong delimiter");
            }
        } catch (IOException e) {
            throw new FileProcessingException("Error reading file content: " + e.getMessage());
        }
    }
}
