package com.example.airline.controller.admin;

import com.example.airline.dto.request.RequestHistoryDto;
import com.example.airline.service.request.RequestHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/requests/{requestId}/history")
public class RequestHistoryController {
    
    private final RequestHistoryService historyService;
    
    public RequestHistoryController(RequestHistoryService historyService) {
        this.historyService = historyService;
    }
    
    @GetMapping
    public ResponseEntity<List<RequestHistoryDto>> getRequestHistory(@PathVariable Long requestId) {
        List<RequestHistoryDto> history = historyService.getHistoryByRequestId(requestId);
        return ResponseEntity.ok(history);
    }
}

