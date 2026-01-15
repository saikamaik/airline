package com.example.airline.controller.admin;

import com.example.airline.dto.statistics.StatisticsDto;
import com.example.airline.service.analytics.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    @GetMapping
    public ResponseEntity<StatisticsDto> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StatisticsDto stats = statisticsService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportStatisticsToCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String csv = statisticsService.exportStatisticsToCsv(startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", 
            "statistics_" + LocalDate.now() + ".csv");
        
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}

