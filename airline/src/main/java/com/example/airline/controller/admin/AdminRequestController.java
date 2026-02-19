package com.example.airline.controller.admin;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.service.request.ClientRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/requests")
public class AdminRequestController {
    private final ClientRequestService requestService;

    public AdminRequestController(ClientRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<ClientRequestDto> createRequest(@Valid @RequestBody ClientRequestDto dto) {
        try {
            ClientRequestDto created = requestService.createRequest(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<ClientRequestDto>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending()
                .and(Sort.by("createdAt").descending()));
        
        Page<ClientRequestDto> requests;
        try {
            if (status != null && priority != null) {
                requests = requestService.findByStatusAndPriority(status, priority, pageable);
            } else if (status != null) {
                requests = requestService.findByStatus(status, pageable);
            } else if (priority != null) {
                requests = requestService.findByPriority(priority, pageable);
            } else if (startDate != null && endDate != null) {
                requests = requestService.findByDateRange(startDate, endDate, pageable);
            } else {
                requests = requestService.findAllRequests(pageable);
            }
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientRequestDto> getRequestById(@PathVariable Long id) {
        return requestService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClientRequestDto> updateStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status,
            @RequestParam(required = false) Long employeeId) {
        try {
            ClientRequestDto updated = requestService.updateStatus(id, status, employeeId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/priority")
    public ResponseEntity<ClientRequestDto> updatePriority(
            @PathVariable Long id,
            @RequestParam RequestPriority priority) {
        try {
            ClientRequestDto updated = requestService.updatePriority(id, priority, null);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<Page<ClientRequestDto>> getRequestsByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClientRequestDto> requests = requestService.findByTourId(tourId, pageable);
        return ResponseEntity.ok(requests);
    }
}

