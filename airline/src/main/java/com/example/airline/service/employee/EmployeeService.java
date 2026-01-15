package com.example.airline.service.employee;

import com.example.airline.dto.employee.EmployeeDto;
import com.example.airline.dto.employee.EmployeeSalesDto;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.entity.user.Employee;
import com.example.airline.entity.user.Role;
import com.example.airline.entity.user.RoleName;
import com.example.airline.entity.user.User;
import com.example.airline.mapper.employee.EmployeeMapper;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.repository.user.EmployeeRepository;
import com.example.airline.repository.user.RoleRepository;
import com.example.airline.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    
    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ClientRequestRepository requestRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        // Проверка уникальности username и email
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Сотрудник с таким email уже существует");
        }
        
        // Создание пользователя
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setEnabled(true);
        
        // Добавление роли ROLE_EMPLOYEE
        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new IllegalArgumentException("Роль ROLE_EMPLOYEE не найдена"));
        user.getRoles().add(employeeRole);
        
        user = userRepository.save(user);
        
        // Создание сотрудника
        Employee employee = EmployeeMapper.toEntity(dto, user);
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }
        employee = employeeRepository.save(employee);
        
        return EmployeeMapper.toDto(employee);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDto> findAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(EmployeeMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDto> findActiveEmployees(Pageable pageable) {
        return employeeRepository.findByActiveTrue(pageable)
                .map(EmployeeMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> findById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> findByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .map(EmployeeMapper::toDto);
    }
    
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден"));
        
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPhone(dto.getPhone());
        if (dto.getActive() != null) {
            employee.setActive(dto.getActive());
        }
        
        employee = employeeRepository.save(employee);
        return EmployeeMapper.toDto(employee);
    }
    
    @Transactional(readOnly = true)
    public EmployeeSalesDto getEmployeeSales(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден"));
        
        // Подсчет завершенных заявок (продаж)
        long totalSales = requestRepository.countByEmployeeIdAndStatusAndDateRange(
                employeeId, RequestStatus.COMPLETED, startDate, endDate);
        
        // Подсчет общей выручки
        BigDecimal totalRevenue = requestRepository.calculateRevenueByEmployeeAndDateRange(
                employeeId, RequestStatus.COMPLETED, startDate, endDate)
                .orElse(BigDecimal.ZERO);
        
        EmployeeSalesDto salesDto = new EmployeeSalesDto(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                totalSales,
                totalRevenue
        );
        salesDto.setStartDate(startDate);
        salesDto.setEndDate(endDate);
        
        return salesDto;
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeSalesDto> getAllEmployeesSales(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employee -> {
                    long totalSales = requestRepository.countByEmployeeIdAndStatusAndDateRange(
                            employee.getId(), RequestStatus.COMPLETED, startDate, endDate);
                    BigDecimal totalRevenue = requestRepository.calculateRevenueByEmployeeAndDateRange(
                            employee.getId(), RequestStatus.COMPLETED, startDate, endDate)
                            .orElse(BigDecimal.ZERO);
                    
                    EmployeeSalesDto salesDto = new EmployeeSalesDto(
                            employee.getId(),
                            employee.getFullName(),
                            employee.getEmail(),
                            totalSales,
                            totalRevenue
                    );
                    salesDto.setStartDate(startDate);
                    salesDto.setEndDate(endDate);
                    return salesDto;
                });
    }
}

