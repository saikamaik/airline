package com.example.airline.mapper.employee;

import com.example.airline.dto.employee.EmployeeDto;
import com.example.airline.entity.user.Employee;
import com.example.airline.entity.user.User;

public class EmployeeMapper {
    
    public static EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setHireDate(employee.getHireDate());
        dto.setActive(employee.getActive());
        if (employee.getUser() != null) {
            dto.setUserId(employee.getUser().getId());
            dto.setUsername(employee.getUser().getUsername());
        }
        return dto;
    }
    
    public static Employee toEntity(EmployeeDto dto, User user) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        if (dto.getHireDate() != null) {
            employee.setHireDate(dto.getHireDate());
        }
        if (dto.getActive() != null) {
            employee.setActive(dto.getActive());
        }
        return employee;
    }
}

