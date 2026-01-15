package com.example.airline.dto.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatisticsDto {
    
    // Общая статистика
    private long totalTours;
    private long activeTours;
    private long totalRequests;
    private long newRequests;
    
    // Статистика по статусам заявок
    private Map<String, Long> requestsByStatus;
    
    // Популярные направления
    private List<DestinationStat> topDestinations;
    
    // Статистика по ценам
    private BigDecimal avgTourPrice;
    private BigDecimal minTourPrice;
    private BigDecimal maxTourPrice;
    
    // Заявки по времени (последние 7 дней)
    private List<RequestByDate> requestsByDate;
    
    // Конструкторы
    public StatisticsDto() {}

    // Getters and Setters
    public long getTotalTours() {
        return totalTours;
    }

    public void setTotalTours(long totalTours) {
        this.totalTours = totalTours;
    }

    public long getActiveTours() {
        return activeTours;
    }

    public void setActiveTours(long activeTours) {
        this.activeTours = activeTours;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getNewRequests() {
        return newRequests;
    }

    public void setNewRequests(long newRequests) {
        this.newRequests = newRequests;
    }

    public Map<String, Long> getRequestsByStatus() {
        return requestsByStatus;
    }

    public void setRequestsByStatus(Map<String, Long> requestsByStatus) {
        this.requestsByStatus = requestsByStatus;
    }

    public List<DestinationStat> getTopDestinations() {
        return topDestinations;
    }

    public void setTopDestinations(List<DestinationStat> topDestinations) {
        this.topDestinations = topDestinations;
    }

    public BigDecimal getAvgTourPrice() {
        return avgTourPrice;
    }

    public void setAvgTourPrice(BigDecimal avgTourPrice) {
        this.avgTourPrice = avgTourPrice;
    }

    public BigDecimal getMinTourPrice() {
        return minTourPrice;
    }

    public void setMinTourPrice(BigDecimal minTourPrice) {
        this.minTourPrice = minTourPrice;
    }

    public BigDecimal getMaxTourPrice() {
        return maxTourPrice;
    }

    public void setMaxTourPrice(BigDecimal maxTourPrice) {
        this.maxTourPrice = maxTourPrice;
    }

    public List<RequestByDate> getRequestsByDate() {
        return requestsByDate;
    }

    public void setRequestsByDate(List<RequestByDate> requestsByDate) {
        this.requestsByDate = requestsByDate;
    }

    // Вложенные классы для статистики
    public static class DestinationStat {
        private String destination;
        private long tourCount;
        private long requestCount;

        public DestinationStat() {}

        public DestinationStat(String destination, long tourCount, long requestCount) {
            this.destination = destination;
            this.tourCount = tourCount;
            this.requestCount = requestCount;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public long getTourCount() {
            return tourCount;
        }

        public void setTourCount(long tourCount) {
            this.tourCount = tourCount;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(long requestCount) {
            this.requestCount = requestCount;
        }
    }

    public static class RequestByDate {
        private String date;
        private long count;

        public RequestByDate() {}

        public RequestByDate(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}

