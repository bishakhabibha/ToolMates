package com.javaproj.ToolMates.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Must be a valid email address.")
    private String email;

    @NotBlank(message = "OTP is required.")
    private String otp;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String newPassword;

    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }

    public String getOtp()                { return otp; }
    public void   setOtp(String v)        { this.otp = v; }

    public String getNewPassword()        { return newPassword; }
    public void   setNewPassword(String v){ this.newPassword = v; }
}