package com.camel.routing;

import com.camel.fileProcessor.database.DatabaseHandler;
import com.camel.fileProcessor.exception.GlobalExceptionHandler;

public class FileProcessingRouteBuilder {
    private DatabaseHandler databaseHandler;
    private GlobalExceptionHandler globalExceptionHandler;

    public FileProcessingRouteBuilder setDatabaseHandler(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
        return this;
    }

    public FileProcessingRouteBuilder setGlobalExceptionHandler(GlobalExceptionHandler globalExceptionHandler) {
        this.globalExceptionHandler = globalExceptionHandler;
        return this;
    }

    public FileProcessingRoute createFileProcessingRoute() {
        return new FileProcessingRoute(databaseHandler, globalExceptionHandler);
    }
}