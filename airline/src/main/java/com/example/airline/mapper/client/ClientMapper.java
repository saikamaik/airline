package com.example.airline.mapper.client;

import com.example.airline.dto.client.ClientDto;
import com.example.airline.entity.client.Client;

public class ClientMapper {
    
    public static ClientDto toDto(Client client) {
        if (client == null) {
            return null;
        }
        
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setBirthDate(client.getBirthDate());
        dto.setNotes(client.getNotes());
        dto.setVipStatus(client.getVipStatus());
        dto.setActive(client.getActive());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());
        
        return dto;
    }
    
    public static Client toEntity(ClientDto dto) {
        if (dto == null) {
            return null;
        }
        
        Client client = new Client();
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setBirthDate(dto.getBirthDate());
        client.setNotes(dto.getNotes());
        if (dto.getVipStatus() != null) {
            client.setVipStatus(dto.getVipStatus());
        }
        if (dto.getActive() != null) {
            client.setActive(dto.getActive());
        }
        
        return client;
    }
}

