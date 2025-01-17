package com.camel.fileProcessor.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonLogger {

    private static final Logger logger = LoggerFactory.getLogger(CommonLogger.class);

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
