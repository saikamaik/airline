package com.example.airline.mapper.request;

import com.example.airline.dto.request.RequestCommentDto;
import com.example.airline.entity.tour.RequestComment;

public class RequestCommentMapper {
    
    public static RequestCommentDto toDto(RequestComment comment) {
        if (comment == null) {
            return null;
        }
        
        RequestCommentDto dto = new RequestCommentDto();
        dto.setId(comment.getId());
        dto.setRequestId(comment.getRequest().getId());
        
        if (comment.getEmployee() != null) {
            dto.setEmployeeId(comment.getEmployee().getId());
            dto.setEmployeeName(comment.getEmployee().getFullName());
        }
        
        dto.setComment(comment.getComment());
        dto.setIsInternal(comment.getIsInternal());
        dto.setCreatedAt(comment.getCreatedAt());
        
        return dto;
    }
}

