package com.example.airline.repository.client;

import com.example.airline.entity.client.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByEmail(String email);
    
    Page<Client> findByActiveTrue(Pageable pageable);
    
    Page<Client> findByVipStatus(Boolean vipStatus, Pageable pageable);
    
    // Поиск только по тексту (без фильтра VIP)
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Client> searchByText(@Param("search") String search, Pageable pageable);

    // Поиск по тексту + фильтр VIP
    @Query("SELECT c FROM Client c WHERE " +
           "c.vipStatus = :vipStatus AND (" +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> searchByTextAndVip(@Param("search") String search,
                                    @Param("vipStatus") Boolean vipStatus,
                                    Pageable pageable);

    // Оставляем для обратной совместимости (не вызывается напрямую)
    @Query("SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:vipStatus IS NULL OR c.vipStatus = :vipStatus)")
    Page<Client> searchClients(@Param("search") String search,
                               @Param("vipStatus") Boolean vipStatus,
                               Pageable pageable);
    
    boolean existsByEmail(String email);
    
    // Поиск клиента по связанному User
    Optional<Client> findByUserId(Long userId);
}

