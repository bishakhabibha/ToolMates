package com.javaproj.ToolMates.auth.model;

import java.time.LocalDateTime;

public class User {

    private Long userId;
    private String firstName;
    private String lastName;
    private String studentId;
    private String department;
    private String studentEmail;
    private String phone;
    private String address;
    private String passwordHash;
    private boolean emailVerified = false;
    private LocalDateTime createdAt;
    private int totalToolsRented = 0;
    private int totalToolsReceived = 0;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTotalToolsRented() { return totalToolsRented; }
    public void setTotalToolsRented(int totalToolsRented) { this.totalToolsRented = totalToolsRented; }

    public int getTotalToolsReceived() { return totalToolsReceived; }
    public void setTotalToolsReceived(int totalToolsReceived) { this.totalToolsReceived = totalToolsReceived; }
}
