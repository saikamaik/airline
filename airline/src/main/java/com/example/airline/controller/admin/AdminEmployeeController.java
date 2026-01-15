package com.example.airline.controller.admin;

import com.example.airline.dto.employee.EmployeeDto;
import com.example.airline.dto.employee.EmployeeSalesDto;
import com.example.airline.service.employee.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/employees")
public class AdminEmployeeController {
    
    private final EmployeeService employeeService;
    
    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        try {
            EmployeeDto created = employeeService.createEmployee(employeeDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName", "firstName"));
        Page<EmployeeDto> employees = active != null && active
                ? employeeService.findActiveEmployees(pageable)
                : employeeService.findAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto employeeDto) {
        try {
            EmployeeDto updated = employeeService.updateEmployee(id, employeeDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/sales")
    public ResponseEntity<EmployeeSalesDto> getEmployeeSales(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            EmployeeSalesDto sales = employeeService.getEmployeeSales(id, start, end);
            return ResponseEntity.ok(sales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/sales")
    public ResponseEntity<Page<EmployeeSalesDto>> getAllEmployeesSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalSales").descending());
        Page<EmployeeSalesDto> sales = employeeService.getAllEmployeesSales(start, end, pageable);
        return ResponseEntity.ok(sales);
    }
}

