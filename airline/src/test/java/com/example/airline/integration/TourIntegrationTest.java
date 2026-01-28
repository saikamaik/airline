package com.example.airline.integration;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.entity.tour.Tour;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.service.tour.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для TourService с реальной БД (H2 in-memory)
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({com.example.airline.service.tour.TourService.class, 
        com.example.airline.util.CustomValidator.class,
        com.example.airline.mapper.tour.TourMapper.class})
class TourIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourService tourService;

    private Tour savedTour;

    @BeforeEach
    void setUp() {
        // Создаем тестовый тур в БД
        Tour tour = new Tour();
        tour.setName("Тестовый тур");
        tour.setDescription("Описание тестового тура");
        tour.setPrice(new BigDecimal("50000.00"));
        tour.setDurationDays(7);
        tour.setDestinationCity("Сочи");
        tour.setActive(true);
        
        savedTour = entityManager.persistAndFlush(tour);
    }

    @Test
    void createTour_ShouldPersistToDatabase() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Новый тур");
        dto.setDescription("Описание нового тура");
        dto.setPrice(new BigDecimal("60000.00"));
        dto.setDurationDays(10);
        dto.setDestinationCity("Турция");

        // When
        TourDto result = tourService.createTour(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Новый тур");
        
        // Проверяем, что тур сохранен в БД
        Tour foundTour = tourRepository.findById(result.getId()).orElse(null);
        assertThat(foundTour).isNotNull();
        assertThat(foundTour.getName()).isEqualTo("Новый тур");
        assertThat(foundTour.getPrice()).isEqualTo(new BigDecimal("60000.00"));
    }

    @Test
    void updateTour_ShouldUpdateInDatabase() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Обновленный тур");
        dto.setDescription("Новое описание");
        dto.setPrice(new BigDecimal("55000.00"));
        dto.setDurationDays(8);
        dto.setDestinationCity("Египет");

        // When
        TourDto result = tourService.updateTour(savedTour.getId(), dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Обновленный тур");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("55000.00"));
        
        // Проверяем, что изменения сохранены в БД
        Tour updatedTour = tourRepository.findById(savedTour.getId()).orElse(null);
        assertThat(updatedTour).isNotNull();
        assertThat(updatedTour.getName()).isEqualTo("Обновленный тур");
        assertThat(updatedTour.getPrice()).isEqualTo(new BigDecimal("55000.00"));
    }

    @Test
    void deleteTour_ShouldSetActiveToFalse() {
        // Given
        Long tourId = savedTour.getId();

        // When
        tourService.deleteTour(tourId);

        // Then
        Tour deletedTour = tourRepository.findById(tourId).orElse(null);
        assertThat(deletedTour).isNotNull();
        assertThat(deletedTour.isActive()).isFalse();
    }

    @Test
    void findById_ShouldReturnTourFromDatabase() {
        // When
        var result = tourService.findById(savedTour.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedTour.getId());
        assertThat(result.get().getName()).isEqualTo("Тестовый тур");
    }

    @Test
    void findActiveTours_ShouldReturnOnlyActiveTours() {
        // Given - создаем неактивный тур
        Tour inactiveTour = new Tour();
        inactiveTour.setName("Неактивный тур");
        inactiveTour.setPrice(new BigDecimal("30000.00"));
        inactiveTour.setDurationDays(5);
        inactiveTour.setDestinationCity("Москва");
        inactiveTour.setActive(false);
        entityManager.persistAndFlush(inactiveTour);

        // When
        var result = tourService.findActiveTours(
            org.springframework.data.domain.PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(t -> t.isActive());
        assertThat(result.getContent()).anyMatch(t -> t.getName().equals("Тестовый тур"));
        assertThat(result.getContent()).noneMatch(t -> t.getName().equals("Неактивный тур"));
    }
}
