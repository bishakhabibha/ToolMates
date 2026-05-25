package com.javaproj.ToolMates.auth.exception;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}