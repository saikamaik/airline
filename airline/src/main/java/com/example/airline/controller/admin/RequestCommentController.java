package com.example.airline.controller.admin;

import com.example.airline.dto.request.RequestCommentDto;
import com.example.airline.service.request.RequestCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/requests/{requestId}/comments")
public class RequestCommentController {
    
    private final RequestCommentService commentService;
    private final com.example.airline.repository.user.EmployeeRepository employeeRepository;
    private final com.example.airline.repository.user.UserRepository userRepository;
    
    public RequestCommentController(
            RequestCommentService commentService,
            com.example.airline.repository.user.EmployeeRepository employeeRepository,
            com.example.airline.repository.user.UserRepository userRepository) {
        this.commentService = commentService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }
    
    @PostMapping
    public ResponseEntity<RequestCommentDto> createComment(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestCommentDto dto,
            Authentication authentication) {
        Long employeeId = getCurrentEmployeeId(authentication);
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        dto.setRequestId(requestId);
        RequestCommentDto created = commentService.createComment(dto, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<RequestCommentDto>> getComments(
            @PathVariable Long requestId,
            @RequestParam(required = false) Boolean isInternal) {
        List<RequestCommentDto> comments = commentService.getCommentsByRequestId(requestId, isInternal);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            Authentication authentication) {
        Long employeeId = getCurrentEmployeeId(authentication);
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            commentService.deleteComment(commentId, employeeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private Long getCurrentEmployeeId(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
                .flatMap(user -> employeeRepository.findByUserId(user.getId()))
                .map(emp -> emp.getId())
                .orElse(null);
    }
}

