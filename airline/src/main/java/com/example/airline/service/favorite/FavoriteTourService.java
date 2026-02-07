package com.example.airline.service.favorite;

import com.example.airline.dto.favorite.FavoriteTourDto;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.client.FavoriteTour;
import com.example.airline.entity.tour.Tour;
import com.example.airline.mapper.favorite.FavoriteTourMapper;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.client.FavoriteTourRepository;
import com.example.airline.repository.tour.TourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteTourService {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteTourService.class);
    
    private final FavoriteTourRepository favoriteTourRepository;
    private final ClientRepository clientRepository;
    private final TourRepository tourRepository;
    
    public FavoriteTourService(
            FavoriteTourRepository favoriteTourRepository,
            ClientRepository clientRepository,
            TourRepository tourRepository) {
        this.favoriteTourRepository = favoriteTourRepository;
        this.clientRepository = clientRepository;
        this.tourRepository = tourRepository;
    }
    
    /**
     * Добавить тур в избранное
     */
    public FavoriteTourDto addToFavorites(Long clientId, Long tourId) {
        logger.info("Добавление тура {} в избранное клиента {}", tourId, clientId);
        
        // Проверяем, существует ли клиент
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден: " + clientId));
        
        // Проверяем, существует ли тур
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Тур не найден: " + tourId));
        
        // Проверяем, не добавлен ли тур уже в избранное
        if (favoriteTourRepository.existsByClientIdAndTourId(clientId, tourId)) {
            logger.warn("Тур {} уже в избранном клиента {}", tourId, clientId);
            // Возвращаем существующий
            FavoriteTour existing = favoriteTourRepository.findByClientIdAndTourId(clientId, tourId)
                    .orElseThrow();
            return FavoriteTourMapper.toDto(existing);
        }
        
        // Создаем новую запись в избранном
        FavoriteTour favoriteTour = FavoriteTour.builder()
                .client(client)
                .tour(tour)
                .build();
        
        FavoriteTour saved = favoriteTourRepository.save(favoriteTour);
        logger.info("Тур {} добавлен в избранное клиента {}", tourId, clientId);
        
        return FavoriteTourMapper.toDto(saved);
    }
    
    /**
     * Удалить тур из избранного
     */
    public void removeFromFavorites(Long clientId, Long tourId) {
        logger.info("Удаление тура {} из избранного клиента {}", tourId, clientId);
        
        if (!favoriteTourRepository.existsByClientIdAndTourId(clientId, tourId)) {
            throw new IllegalArgumentException("Тур не найден в избранном");
        }
        
        favoriteTourRepository.deleteByClientIdAndTourId(clientId, tourId);
        logger.info("Тур {} удален из избранного клиента {}", tourId, clientId);
    }
    
    /**
     * Получить все избранные туры клиента
     */
    @Transactional(readOnly = true)
    public Page<FavoriteTourDto> getFavorites(Long clientId, Pageable pageable) {
        logger.debug("Получение избранных туров клиента {}", clientId);
        
        return favoriteTourRepository.findByClientId(clientId, pageable)
                .map(FavoriteTourMapper::toDto);
    }
    
    /**
     * Проверить, находится ли тур в избранном
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long clientId, Long tourId) {
        return favoriteTourRepository.existsByClientIdAndTourId(clientId, tourId);
    }
    
    /**
     * Получить количество избранных туров у клиента
     */
    @Transactional(readOnly = true)
    public long countFavorites(Long clientId) {
        return favoriteTourRepository.countByClientId(clientId);
    }
}
