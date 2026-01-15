package com.example.airline.service.analytics;

import com.example.airline.dto.statistics.StatisticsDto;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.repository.tour.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    
    private final TourRepository tourRepository;
    private final ClientRequestRepository requestRepository;
    
    public StatisticsService(TourRepository tourRepository, ClientRequestRepository requestRepository) {
        this.tourRepository = tourRepository;
        this.requestRepository = requestRepository;
    }
    
    @Transactional(readOnly = true)
    public StatisticsDto getStatistics() {
        return getStatistics(null, null);
    }
    
    @Transactional(readOnly = true)
    public StatisticsDto getStatistics(LocalDate startDate, LocalDate endDate) {
        StatisticsDto stats = new StatisticsDto();
        
        // Общая статистика
        stats.setTotalTours(tourRepository.count());
        stats.setActiveTours(tourRepository.countByActiveTrue());
        
        // Статистика заявок с учетом фильтров по датам
        if (startDate != null && endDate != null) {
            stats.setTotalRequests(requestRepository.countByDateRange(startDate, endDate));
            stats.setNewRequests(requestRepository.countByStatusAndDateRange(RequestStatus.NEW, startDate, endDate));
        } else {
            stats.setTotalRequests(requestRepository.count());
            stats.setNewRequests(requestRepository.countByStatus(RequestStatus.NEW));
        }
        
        // Статистика по статусам заявок
        Map<String, Long> statusStats = new LinkedHashMap<>();
        for (RequestStatus status : RequestStatus.values()) {
            long count;
            if (startDate != null && endDate != null) {
                count = requestRepository.countByStatusAndDateRange(status, startDate, endDate);
            } else {
                count = requestRepository.countByStatus(status);
            }
            statusStats.put(status.name(), count);
        }
        stats.setRequestsByStatus(statusStats);
        
        // Популярные направления
        List<Object[]> destinationsData = tourRepository.findTopDestinations();
        List<StatisticsDto.DestinationStat> topDestinations = destinationsData.stream()
                .map(row -> new StatisticsDto.DestinationStat(
                        (String) row[0],    // destination
                        ((Number) row[1]).longValue(),  // tourCount
                        ((Number) row[2]).longValue()   // requestCount
                ))
                .collect(Collectors.toList());
        stats.setTopDestinations(topDestinations);
        
        // Статистика по ценам
        Optional<BigDecimal> avgPrice = tourRepository.findAverageTourPrice();
        Optional<BigDecimal> minPrice = tourRepository.findMinTourPrice();
        Optional<BigDecimal> maxPrice = tourRepository.findMaxTourPrice();
        
        stats.setAvgTourPrice(avgPrice.orElse(BigDecimal.ZERO));
        stats.setMinTourPrice(minPrice.orElse(BigDecimal.ZERO));
        stats.setMaxTourPrice(maxPrice.orElse(BigDecimal.ZERO));
        
        // Заявки по датам
        List<StatisticsDto.RequestByDate> requestsByDate = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            long count = requestRepository.countByCreatedAtDate(current);
            requestsByDate.add(new StatisticsDto.RequestByDate(
                    current.format(formatter),
                    count
            ));
            current = current.plusDays(1);
        }
        stats.setRequestsByDate(requestsByDate);
        
        return stats;
    }
    
    @Transactional(readOnly = true)
    public String exportStatisticsToCsv(LocalDate startDate, LocalDate endDate) {
        StatisticsDto stats = getStatistics(startDate, endDate);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Статистика турагентства\n");
        if (startDate != null && endDate != null) {
            csv.append("Период: ").append(startDate).append(" - ").append(endDate).append("\n");
        }
        csv.append("\n");
        
        csv.append("Общая статистика\n");
        csv.append("Всего туров,").append(stats.getTotalTours()).append("\n");
        csv.append("Активных туров,").append(stats.getActiveTours()).append("\n");
        csv.append("Всего заявок,").append(stats.getTotalRequests()).append("\n");
        csv.append("Новых заявок,").append(stats.getNewRequests()).append("\n");
        csv.append("\n");
        
        csv.append("Статистика по статусам заявок\n");
        csv.append("Статус,Количество\n");
        stats.getRequestsByStatus().forEach((status, count) -> {
            csv.append(status).append(",").append(count).append("\n");
        });
        csv.append("\n");
        
        csv.append("Статистика по ценам\n");
        csv.append("Средняя цена,").append(stats.getAvgTourPrice()).append("\n");
        csv.append("Минимальная цена,").append(stats.getMinTourPrice()).append("\n");
        csv.append("Максимальная цена,").append(stats.getMaxTourPrice()).append("\n");
        csv.append("\n");
        
        csv.append("Популярные направления\n");
        csv.append("Направление,Количество туров,Количество заявок\n");
        stats.getTopDestinations().forEach(dest -> {
            csv.append(dest.getDestination()).append(",")
                .append(dest.getTourCount()).append(",")
                .append(dest.getRequestCount()).append("\n");
        });
        csv.append("\n");
        
        csv.append("Заявки по датам\n");
        csv.append("Дата,Количество\n");
        stats.getRequestsByDate().forEach(req -> {
            csv.append(req.getDate()).append(",").append(req.getCount()).append("\n");
        });
        
        return csv.toString();
    }
}

