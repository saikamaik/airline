package com.example.airline.service.request;

import com.example.airline.dto.request.RequestCommentDto;
import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestComment;
import com.example.airline.entity.user.Employee;
import com.example.airline.mapper.request.RequestCommentMapper;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.repository.user.EmployeeRepository;
import com.example.airline.repository.tour.RequestCommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestCommentService {
    
    private final RequestCommentRepository commentRepository;
    private final ClientRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;
    
    public RequestCommentService(
            RequestCommentRepository commentRepository,
            ClientRequestRepository requestRepository,
            EmployeeRepository employeeRepository) {
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional
    public RequestCommentDto createComment(RequestCommentDto dto, Long employeeId) {
        ClientRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + dto.getRequestId()));
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        
        RequestComment comment = new RequestComment(
                request,
                employee,
                dto.getComment(),
                dto.getIsInternal() != null ? dto.getIsInternal() : true
        );
        
        comment = commentRepository.save(comment);
        return RequestCommentMapper.toDto(comment);
    }
    
    @Transactional(readOnly = true)
    public List<RequestCommentDto> getCommentsByRequestId(Long requestId, Boolean isInternal) {
        List<RequestComment> comments;
        if (isInternal != null) {
            comments = commentRepository.findByRequestIdAndIsInternal(requestId, isInternal);
        } else {
            comments = commentRepository.findByRequestIdOrderByCreatedAtDesc(requestId);
        }
        
        return comments.stream()
                .map(RequestCommentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<RequestCommentDto> getCommentsByRequestId(Long requestId, Pageable pageable) {
        return commentRepository.findByRequestId(requestId, pageable)
                .map(RequestCommentMapper::toDto);
    }
    
    @Transactional
    public void deleteComment(Long commentId, Long employeeId) {
        RequestComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        
        // Проверка: только автор или админ может удалить комментарий
        if (!comment.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
    }
}

