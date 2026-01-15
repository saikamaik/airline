package com.example.airline.dto.request;

import java.time.LocalDateTime;

public class RequestHistoryDto {
    private Long id;
    private Long requestId;
    private Long changedByEmployeeId;
    private String changedByEmployeeName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String description;
    private LocalDateTime changedAt;

    public RequestHistoryDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getChangedByEmployeeId() {
        return changedByEmployeeId;
    }

    public void setChangedByEmployeeId(Long changedByEmployeeId) {
        this.changedByEmployeeId = changedByEmployeeId;
    }

    public String getChangedByEmployeeName() {
        return changedByEmployeeName;
    }

    public void setChangedByEmployeeName(String changedByEmployeeName) {
        this.changedByEmployeeName = changedByEmployeeName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}

