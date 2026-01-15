package com.example.airline.dto.request;

import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ClientRequestDto {
    private Long id;

    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @NotBlank(message = "User name is required")
    @Size(max = 100, message = "User name must be less than 100 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String userEmail;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String userPhone;

    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String comment;

    private RequestStatus status;
    
    private RequestPriority priority;

    private LocalDateTime createdAt;

    // For response with tour name
    private String tourName;
    
    // Employee information
    private Long employeeId;
    private String employeeName;

    public ClientRequestDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTourId() {
        return tourId;
    }

    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }
}

