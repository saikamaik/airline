package com.example.airline.repository.tour;

import com.example.airline.entity.tour.RequestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    
    List<RequestHistory> findByRequestIdOrderByChangedAtDesc(Long requestId);
    
    Page<RequestHistory> findByRequestId(Long requestId, Pageable pageable);
    
    @Query("SELECT h FROM RequestHistory h WHERE h.request.id = :requestId ORDER BY h.changedAt DESC")
    List<RequestHistory> findHistoryByRequestId(@Param("requestId") Long requestId);
}

