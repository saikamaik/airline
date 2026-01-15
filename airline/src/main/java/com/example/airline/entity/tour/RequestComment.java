package com.example.airline.entity.tour;

import com.example.airline.entity.user.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_comments")
public class RequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ClientRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(nullable = false)
    private Boolean isInternal = true; // true - внутренний комментарий, false - видимый клиенту

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public RequestComment() {
    }

    public RequestComment(ClientRequest request, Employee employee, String comment, Boolean isInternal) {
        this.request = request;
        this.employee = employee;
        this.comment = comment;
        this.isInternal = isInternal != null ? isInternal : true;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

