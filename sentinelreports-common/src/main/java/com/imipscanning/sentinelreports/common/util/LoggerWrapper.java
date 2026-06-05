package com.imipscanning.sentinelreports.common.util;

import org.slf4j.Logger;

public final class LoggerWrapper {
    private final Logger logger;

    public LoggerWrapper(Logger logger) {
        this.logger = logger;
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
