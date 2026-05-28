package com.javaproj.ToolMates.auth.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String identifier) {
        super("No account found for: " + identifier);
    }
}