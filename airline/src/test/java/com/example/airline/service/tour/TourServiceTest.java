package com.example.airline.service.tour;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.entity.flight.Flight;
import com.example.airline.entity.tour.Tour;
import com.example.airline.repository.flight.FlightRepository;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.util.CustomValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private CustomValidator customValidator;

    @InjectMocks
    private TourService tourService;

    private TourDto validTourDto;
    private Tour tourEntity;

    @BeforeEach
    void setUp() {
        validTourDto = new TourDto();
        validTourDto.setName("Отдых в Сочи");
        validTourDto.setDescription("Комфортабельный отель на берегу моря");
        validTourDto.setPrice(new BigDecimal("45000.00"));
        validTourDto.setDurationDays(7);
        validTourDto.setDestinationCity("Сочи");
        validTourDto.setActive(true);

        tourEntity = new Tour();
        tourEntity.setId(1L);
        tourEntity.setName("Отдых в Сочи");
        tourEntity.setDescription("Комфортабельный отель на берегу моря");
        tourEntity.setPrice(new BigDecimal("45000.00"));
        tourEntity.setDurationDays(7);
        tourEntity.setDestinationCity("Сочи");
        tourEntity.setActive(true);
    }

    @Test
    void createTour_WithValidData_ShouldCreateTour() {
        // Given
        when(customValidator.validate(any(TourDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour tour = invocation.getArgument(0);
            tour.setId(1L);
            return tour;
        });

        // When
        TourDto result = tourService.createTour(validTourDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Отдых в Сочи");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("45000.00"));
        verify(customValidator).validate(validTourDto);
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    void createTour_WithFlightIds_ShouldAttachFlights() {
        // Given
        validTourDto.setFlightIds(List.of(1, 2));
        Flight flight1 = new Flight();
        flight1.setFlightId(1);
        Flight flight2 = new Flight();
        flight2.setFlightId(2);

        when(customValidator.validate(any(TourDto.class))).thenReturn(java.util.Collections.emptySet());
        when(flightRepository.findById(1)).thenReturn(Optional.of(flight1));
        when(flightRepository.findById(2)).thenReturn(Optional.of(flight2));
        when(tourRepository.save(any(Tour.class))).thenAnswer(invocation -> {
            Tour tour = invocation.getArgument(0);
            tour.setId(1L);
            return tour;
        });

        // When
        TourDto result = tourService.createTour(validTourDto);

        // Then
        assertThat(result).isNotNull();
        verify(flightRepository).findById(1);
        verify(flightRepository).findById(2);
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    void createTour_WithNonExistentFlight_ShouldThrowException() {
        // Given
        validTourDto.setFlightIds(List.of(999));
        when(customValidator.validate(any(TourDto.class))).thenReturn(java.util.Collections.emptySet());
        when(flightRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tourService.createTour(validTourDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Flight not found: 999");
        verify(tourRepository, never()).save(any(Tour.class));
    }

    @Test
    void updateTour_WithValidData_ShouldUpdateTour() {
        // Given
        Long tourId = 1L;
        validTourDto.setName("Обновленный тур");
        validTourDto.setPrice(new BigDecimal("50000.00"));

        when(customValidator.validate(any(TourDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tourEntity));
        when(tourRepository.save(any(Tour.class))).thenReturn(tourEntity);

        // When
        TourDto result = tourService.updateTour(tourId, validTourDto);

        // Then
        assertThat(result).isNotNull();
        verify(customValidator).validate(validTourDto);
        verify(tourRepository).findById(tourId);
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    void updateTour_WithNonExistentId_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(customValidator.validate(any(TourDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tourService.updateTour(nonExistentId, validTourDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tour not found: 999");
        verify(tourRepository, never()).save(any(Tour.class));
    }

    @Test
    void deleteTour_WithValidId_ShouldSetActiveToFalse() {
        // Given
        Long tourId = 1L;
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tourEntity));
        when(tourRepository.save(any(Tour.class))).thenReturn(tourEntity);

        // When
        tourService.deleteTour(tourId);

        // Then
        assertThat(tourEntity.isActive()).isFalse();
        verify(tourRepository).findById(tourId);
        verify(tourRepository).save(tourEntity);
    }

    @Test
    void deleteTour_WithNonExistentId_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(tourRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tourService.deleteTour(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tour not found: 999");
        verify(tourRepository, never()).save(any(Tour.class));
    }

    @Test
    void findById_WithValidId_ShouldReturnTour() {
        // Given
        Long tourId = 1L;
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tourEntity));

        // When
        Optional<Tour> result = tourService.findById(tourId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Отдых в Сочи");
        verify(tourRepository).findById(tourId);
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        Long nonExistentId = 999L;
        when(tourRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Tour> result = tourService.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(tourRepository).findById(nonExistentId);
    }

    @Test
    void findWithFilters_WithDestination_ShouldFilterByDestination() {
        // Given
        String destination = "Сочи";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tour> tourPage = new PageImpl<>(List.of(tourEntity));

        when(tourRepository.findWithFilters(destination, null, null, pageable))
                .thenReturn(tourPage);

        // When
        Page<TourDto> result = tourService.findWithFilters(destination, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(tourRepository).findWithFilters(destination, null, null, pageable);
    }

    @Test
    void findWithFilters_WithPriceRange_ShouldFilterByPrice() {
        // Given
        BigDecimal minPrice = new BigDecimal("40000");
        BigDecimal maxPrice = new BigDecimal("50000");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tour> tourPage = new PageImpl<>(List.of(tourEntity));

        when(tourRepository.findWithFilters("", minPrice, maxPrice, pageable))
                .thenReturn(tourPage);

        // When
        Page<TourDto> result = tourService.findWithFilters(null, minPrice, maxPrice, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(tourRepository).findWithFilters("", minPrice, maxPrice, pageable);
    }
}
