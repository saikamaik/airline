package com.example.airline.mapper.request;

import com.example.airline.dto.request.RequestHistoryDto;
import com.example.airline.entity.tour.RequestHistory;

public class RequestHistoryMapper {
    
    public static RequestHistoryDto toDto(RequestHistory history) {
        if (history == null) {
            return null;
        }
        
        RequestHistoryDto dto = new RequestHistoryDto();
        dto.setId(history.getId());
        dto.setRequestId(history.getRequest().getId());
        
        if (history.getChangedBy() != null) {
            dto.setChangedByEmployeeId(history.getChangedBy().getId());
            dto.setChangedByEmployeeName(history.getChangedBy().getFullName());
        }
        
        dto.setFieldName(history.getFieldName());
        dto.setOldValue(history.getOldValue());
        dto.setNewValue(history.getNewValue());
        dto.setDescription(history.getDescription());
        dto.setChangedAt(history.getChangedAt());
        
        return dto;
    }
}

