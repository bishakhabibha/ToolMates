package com.javaproj.ToolMates.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Student ID or email is required.")
    private String username;

    @NotBlank(message = "Password is required.")
    private String password;

    private boolean rememberMe;

    public String getUsername()             { return username; }
    public void   setUsername(String v)     { this.username = v; }

    public String getPassword()             { return password; }
    public void   setPassword(String v)     { this.password = v; }

    public boolean isRememberMe()           { return rememberMe; }
    public void    setRememberMe(boolean v) { this.rememberMe = v; }
}