package com.example.airline.repository.tour;

import com.example.airline.entity.tour.TourCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourCategoryRepository extends JpaRepository<TourCategory, Long> {
    
    Optional<TourCategory> findByName(String name);
    
    List<TourCategory> findByActiveTrue();
    
    Page<TourCategory> findByActiveTrue(Pageable pageable);
    
    boolean existsByName(String name);
}

