package com.example.airline.controller.admin;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.service.request.ClientRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/requests")
public class AdminRequestController {
    private static final Logger logger = LoggerFactory.getLogger(AdminRequestController.class);
    
    private final ClientRequestService requestService;

    public AdminRequestController(ClientRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public ResponseEntity<Page<ClientRequestDto>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.debug("Getting requests: status={}, priority={}, page={}, size={}", status, priority, page, size);
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
            logger.debug("Found {} requests", requests.getTotalElements());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error getting requests", e);
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
            logger.info("Updating request {} status to {}", id, status);
            ClientRequestDto updated = requestService.updateStatus(id, status, employeeId);
            logger.info("Request {} status updated successfully", id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update request {} status: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error while updating request {} status", id, e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/priority")
    public ResponseEntity<ClientRequestDto> updatePriority(
            @PathVariable Long id,
            @RequestParam RequestPriority priority) {
        try {
            logger.info("Updating request {} priority to {}", id, priority);
            ClientRequestDto updated = requestService.updatePriority(id, priority, null);
            logger.info("Request {} priority updated successfully", id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update request {} priority: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error while updating request {} priority", id, e);
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

