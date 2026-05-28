package com.javaproj.ToolMates.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid Student ID / email or password.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}