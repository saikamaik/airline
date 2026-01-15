package com.example.airline.repository.tour;

import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {
    
    Page<ClientRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    @Query("SELECT cr FROM ClientRequest cr JOIN FETCH cr.tour WHERE cr.id = :id")
    ClientRequest findByIdWithTour(@Param("id") Long id);
    
    Page<ClientRequest> findByTourId(Long tourId, Pageable pageable);
    
    // Статистика: количество заявок по статусу
    long countByStatus(RequestStatus status);
    
    // Статистика: количество заявок за определенную дату
    @Query("SELECT COUNT(cr) FROM ClientRequest cr WHERE CAST(cr.createdAt AS date) = :date")
    long countByCreatedAtDate(@Param("date") java.time.LocalDate date);
    
    // Статистика: количество заявок в диапазоне дат
    @Query("SELECT COUNT(cr) FROM ClientRequest cr WHERE CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") java.time.LocalDate startDate, 
                          @Param("endDate") java.time.LocalDate endDate);
    
    // Статистика: количество заявок по статусу в диапазоне дат
    @Query("SELECT COUNT(cr) FROM ClientRequest cr WHERE cr.status = :status " +
           "AND CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") RequestStatus status,
                                    @Param("startDate") java.time.LocalDate startDate,
                                    @Param("endDate") java.time.LocalDate endDate);
    
    // Статистика: количество заявок по сотруднику, статусу и диапазону дат
    @Query("SELECT COUNT(cr) FROM ClientRequest cr WHERE cr.employee.id = :employeeId " +
           "AND cr.status = :status " +
           "AND CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    long countByEmployeeIdAndStatusAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("status") RequestStatus status,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);
    
    // Статистика: выручка по сотруднику, статусу и диапазону дат
    @Query("SELECT SUM(cr.tour.price) FROM ClientRequest cr WHERE cr.employee.id = :employeeId " +
           "AND cr.status = :status " +
           "AND CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    java.util.Optional<java.math.BigDecimal> calculateRevenueByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("status") RequestStatus status,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);
    
    // Получить заявки по сотруднику
    Page<ClientRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    
    // Получить заявки по сотруднику и статусу
    Page<ClientRequest> findByEmployeeIdAndStatus(Long employeeId, RequestStatus status, Pageable pageable);
    
    // Получить заявки по клиенту
    Page<ClientRequest> findByClientId(Long clientId, Pageable pageable);
    
    // Количество заявок клиента
    long countByClientId(Long clientId);
    
    // Получить заявки по приоритету
    Page<ClientRequest> findByPriority(RequestPriority priority, Pageable pageable);
    
    // Получить заявки по статусу и приоритету
    Page<ClientRequest> findByStatusAndPriority(RequestStatus status, RequestPriority priority, Pageable pageable);
    
    // Получить заявки в диапазоне дат
    @Query("SELECT cr FROM ClientRequest cr WHERE CAST(cr.createdAt AS date) BETWEEN :startDate AND :endDate")
    Page<ClientRequest> findByDateRange(@Param("startDate") java.time.LocalDate startDate, 
                                        @Param("endDate") java.time.LocalDate endDate, 
                                        Pageable pageable);
}

