package com.example.airline.entity.tour;

import com.example.airline.entity.user.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_history")
public class RequestHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ClientRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_employee_id")
    private Employee changedBy;

    @Column(nullable = false)
    private String fieldName;

    private String oldValue;

    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime changedAt;

    public RequestHistory() {
    }

    public RequestHistory(ClientRequest request, Employee changedBy, String fieldName, String oldValue, String newValue) {
        this.request = request;
        this.changedBy = changedBy;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public void setRequest(ClientRequest request) {
        this.request = request;
    }

    public Employee getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Employee changedBy) {
        this.changedBy = changedBy;
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
}

