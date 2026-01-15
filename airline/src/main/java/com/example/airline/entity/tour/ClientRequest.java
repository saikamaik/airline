package com.example.airline.entity.tour;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClientRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private com.example.airline.entity.user.Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private com.example.airline.entity.client.Client client;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    private String userPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestPriority priority = RequestPriority.NORMAL;

    private String comment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ClientRequest() {
    }

    public ClientRequest(Tour tour, String userName, String userEmail, String userPhone, String comment) {
        this.tour = tour;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.comment = comment;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public com.example.airline.entity.user.Employee getEmployee() {
        return employee;
    }

    public void setEmployee(com.example.airline.entity.user.Employee employee) {
        this.employee = employee;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public com.example.airline.entity.client.Client getClient() {
        return client;
    }

    public void setClient(com.example.airline.entity.client.Client client) {
        this.client = client;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }
}

