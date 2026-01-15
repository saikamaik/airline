package com.example.airline.repository.tour;

import com.example.airline.entity.tour.RequestComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {
    
    List<RequestComment> findByRequestIdOrderByCreatedAtDesc(Long requestId);
    
    Page<RequestComment> findByRequestId(Long requestId, Pageable pageable);
    
    List<RequestComment> findByRequestIdAndIsInternal(Long requestId, Boolean isInternal);
    
    @Query("SELECT c FROM RequestComment c WHERE c.request.id = :requestId ORDER BY c.createdAt DESC")
    List<RequestComment> findCommentsByRequestId(@Param("requestId") Long requestId);
}

