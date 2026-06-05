package com.imipscanning.sentinelreports.common.service;

public final class ValidationException extends RuntimeException {
    private final String messageKey;

    public ValidationException(String messageKey, String message) {
        super(message);
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}
