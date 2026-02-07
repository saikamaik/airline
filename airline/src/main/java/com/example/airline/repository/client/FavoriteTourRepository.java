package com.example.airline.repository.client;

import com.example.airline.entity.client.FavoriteTour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, Long> {
    
    /**
     * Найти все избранные туры клиента
     */
    Page<FavoriteTour> findByClientId(Long clientId, Pageable pageable);
    
    /**
     * Проверить, добавлен ли тур в избранное
     */
    boolean existsByClientIdAndTourId(Long clientId, Long tourId);
    
    /**
     * Найти избранный тур по клиенту и туру
     */
    Optional<FavoriteTour> findByClientIdAndTourId(Long clientId, Long tourId);
    
    /**
     * Удалить тур из избранного
     */
    void deleteByClientIdAndTourId(Long clientId, Long tourId);
    
    /**
     * Подсчитать количество избранных туров у клиента
     */
    long countByClientId(Long clientId);
}
