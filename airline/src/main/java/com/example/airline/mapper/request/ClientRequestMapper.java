package com.example.airline.mapper.request;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.tour.ClientRequest;

public class ClientRequestMapper {
    
    public static ClientRequestDto toDto(ClientRequest request) {
        if (request == null) {
            return null;
        }

        ClientRequestDto dto = new ClientRequestDto();
        dto.setId(request.getId());
        dto.setTourId(request.getTour().getId());
        dto.setTourName(request.getTour().getName());
        dto.setUserName(request.getUserName());
        dto.setUserEmail(request.getUserEmail());
        dto.setUserPhone(request.getUserPhone());
        dto.setComment(request.getComment());
        dto.setStatus(request.getStatus());
        dto.setPriority(request.getPriority());
        dto.setCreatedAt(request.getCreatedAt());
        
        if (request.getEmployee() != null) {
            dto.setEmployeeId(request.getEmployee().getId());
            dto.setEmployeeName(request.getEmployee().getFullName());
        }

        return dto;
    }
}

