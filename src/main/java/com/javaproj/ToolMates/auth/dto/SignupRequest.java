package com.javaproj.ToolMates.auth.dto;

import jakarta.validation.constraints.*;

public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60)
    private String lastName;

    @NotBlank(message = "Student ID is required")
    @Pattern(regexp = "^[A-Za-z0-9\\-]{4,20}$", message = "Invalid Student ID format")
    private String studentId;

    private String department;

    @NotBlank(message = "Institutional email is required")
    @Email(message = "Must be a valid email address")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@student\\.cuet\\.ac\\.bd$", message = "Please enter a valid CUET student email address.")
    private String studentEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    private String address;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128)
    private String password;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
