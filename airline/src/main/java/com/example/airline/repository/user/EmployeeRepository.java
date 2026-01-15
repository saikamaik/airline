package com.example.airline.repository.user;

import com.example.airline.entity.user.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByUserId(Long userId);
    
    Optional<Employee> findByEmail(String email);
    
    Page<Employee> findByActiveTrue(Pageable pageable);
    
    Page<Employee> findByActive(Boolean active, Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.active = true")
    long countActiveEmployees();
    
    @Query("SELECT COUNT(cr) FROM com.example.airline.entity.tour.ClientRequest cr WHERE cr.employee.id = :employeeId " +
           "AND cr.status = 'COMPLETED' " +
           "AND CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    long countCompletedRequestsByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

