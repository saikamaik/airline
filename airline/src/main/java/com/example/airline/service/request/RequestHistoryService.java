package com.example.airline.service.request;

import com.example.airline.dto.request.RequestHistoryDto;
import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestHistory;
import com.example.airline.entity.user.Employee;
import com.example.airline.mapper.request.RequestHistoryMapper;
import com.example.airline.repository.tour.RequestHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestHistoryService {
    
    private final RequestHistoryRepository historyRepository;
    
    public RequestHistoryService(RequestHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }
    
    @Transactional
    public void logChange(ClientRequest request, Employee changedBy, String fieldName, String oldValue, String newValue) {
        logChange(request, changedBy, fieldName, oldValue, newValue, null);
    }
    
    @Transactional
    public void logChange(ClientRequest request, Employee changedBy, String fieldName, String oldValue, String newValue, String description) {
        RequestHistory history = new RequestHistory(request, changedBy, fieldName, oldValue, newValue);
        if (description != null) {
            history.setDescription(description);
        }
        historyRepository.save(history);
    }
    
    @Transactional(readOnly = true)
    public List<RequestHistoryDto> getHistoryByRequestId(Long requestId) {
        return historyRepository.findHistoryByRequestId(requestId).stream()
                .map(RequestHistoryMapper::toDto)
                .collect(Collectors.toList());
    }
}

