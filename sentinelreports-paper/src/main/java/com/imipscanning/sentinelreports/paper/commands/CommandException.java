package com.imipscanning.sentinelreports.paper.commands;

public final class CommandException extends RuntimeException {
    private final String messageKey;
    private final String syntax;
    private final String example;

    public CommandException(String messageKey, String message, String syntax, String example) {
        super(message);
        this.messageKey = messageKey;
        this.syntax = syntax;
        this.example = example;
    }

    public String messageKey() {
        return messageKey;
    }

    public String syntax() {
        return syntax;
    }

    public String example() {
        return example;
    }
}
