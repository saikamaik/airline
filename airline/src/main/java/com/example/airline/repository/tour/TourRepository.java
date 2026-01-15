package com.example.airline.repository.tour;

import com.example.airline.entity.tour.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    
    Page<Tour> findByActiveTrue(Pageable pageable);
    
    List<Tour> findByDestinationCity(String destinationCity);
    
    long countByActiveTrue();
    
    // Для админки - показываем все туры (активные и неактивные)
    // destination передается как пустая строка, если не указан
    @Query("SELECT t FROM Tour t WHERE " +
           "(:destination = '' OR LOWER(t.destinationCity) LIKE LOWER(CONCAT('%', :destination, '%'))) " +
           "AND (:minPrice IS NULL OR t.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR t.price <= :maxPrice)")
    Page<Tour> findWithFilters(@Param("destination") String destination,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);
    
    // Статистика: топ направлений с количеством туров и заявок
    @Query("SELECT t.destinationCity, COUNT(DISTINCT t.id), COUNT(cr.id) " +
           "FROM Tour t LEFT JOIN ClientRequest cr ON cr.tour.id = t.id " +
           "GROUP BY t.destinationCity " +
           "ORDER BY COUNT(cr.id) DESC")
    List<Object[]> findTopDestinations();
    
    // Статистика по ценам
    @Query("SELECT AVG(t.price) FROM Tour t WHERE t.active = true")
    Optional<BigDecimal> findAverageTourPrice();
    
    @Query("SELECT MIN(t.price) FROM Tour t WHERE t.active = true")
    Optional<BigDecimal> findMinTourPrice();
    
    @Query("SELECT MAX(t.price) FROM Tour t WHERE t.active = true")
    Optional<BigDecimal> findMaxTourPrice();
}

