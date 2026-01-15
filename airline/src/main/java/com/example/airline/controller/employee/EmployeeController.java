package com.example.airline.controller.employee;

import com.example.airline.dto.employee.EmployeeDto;
import com.example.airline.dto.employee.EmployeeSalesDto;
import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.service.employee.EmployeeService;
import com.example.airline.service.request.ClientRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final ClientRequestService requestService;
    private final com.example.airline.repository.user.UserRepository userRepository;
    private final com.example.airline.repository.user.EmployeeRepository employeeRepository;
    
    public EmployeeController(
            EmployeeService employeeService,
            ClientRequestService requestService,
            com.example.airline.repository.user.UserRepository userRepository,
            com.example.airline.repository.user.EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.requestService = requestService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<EmployeeDto> getProfile(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
                .flatMap(user -> employeeService.findByUserId(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/requests")
    public ResponseEntity<Page<ClientRequestDto>> getMyRequests(
            Authentication authentication,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long employeeId = getCurrentEmployeeId(authentication);
        if (employeeId == null) {
            return ResponseEntity.notFound().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClientRequestDto> requests = status != null
                ? requestService.findByEmployeeIdAndStatus(employeeId, status, pageable)
                : requestService.findByEmployeeId(employeeId, pageable);
        
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/sales")
    public ResponseEntity<EmployeeSalesDto> getMySales(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long employeeId = getCurrentEmployeeId(authentication);
        if (employeeId == null) {
            return ResponseEntity.notFound().build();
        }
        
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        
        EmployeeSalesDto sales = employeeService.getEmployeeSales(employeeId, start, end);
        return ResponseEntity.ok(sales);
    }
    
    private Long getCurrentEmployeeId(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
                .flatMap(user -> employeeRepository.findByUserId(user.getId()))
                .map(emp -> emp.getId())
                .orElse(null);
    }
}

