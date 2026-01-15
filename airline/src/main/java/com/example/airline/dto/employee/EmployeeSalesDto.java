package com.example.airline.dto.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeSalesDto {
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private long totalSales;
    private BigDecimal totalRevenue;
    private LocalDate startDate;
    private LocalDate endDate;

    public EmployeeSalesDto() {
    }

    public EmployeeSalesDto(Long employeeId, String employeeName, String employeeEmail, 
                           long totalSales, BigDecimal totalRevenue) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
    }

    // Getters and setters
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

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

