package com.example.airline.service.client;

import com.example.airline.dto.client.ClientDto;
import com.example.airline.entity.client.Client;
import com.example.airline.mapper.client.ClientMapper;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.tour.ClientRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final ClientRequestRepository requestRepository;
    
    public ClientService(ClientRepository clientRepository, ClientRequestRepository requestRepository) {
        this.clientRepository = clientRepository;
        this.requestRepository = requestRepository;
    }
    
    @Transactional
    public ClientDto createClient(ClientDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Client with email '" + dto.getEmail() + "' already exists");
        }
        
        Client client = ClientMapper.toEntity(dto);
        client = clientRepository.save(client);
        
        ClientDto result = ClientMapper.toDto(client);
        // Подсчет количества заявок
        result.setTotalRequests(requestRepository.countByClientId(client.getId()));
        
        return result;
    }
    
    @Transactional(readOnly = true)
    public Page<ClientDto> searchClients(String search, Boolean vipStatus, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasVip = vipStatus != null;

        Page<Client> clients;
        if (hasSearch && hasVip) {
            // Оба фильтра — используем запрос с текстом и vipStatus (без null-параметров)
            clients = clientRepository.searchByTextAndVip(search.trim(), vipStatus, pageable);
        } else if (hasSearch) {
            // Только текстовый поиск
            clients = clientRepository.searchByText(search.trim(), pageable);
        } else if (hasVip) {
            // Только фильтр по VIP
            clients = clientRepository.findByVipStatus(vipStatus, pageable);
        } else {
            // Без фильтров — возвращаем всё
            clients = clientRepository.findAll(pageable);
        }

        return clients.map(client -> {
            ClientDto dto = ClientMapper.toDto(client);
            dto.setTotalRequests(requestRepository.countByClientId(client.getId()));
            return dto;
        });
    }
    
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(client -> {
                    ClientDto dto = ClientMapper.toDto(client);
                    dto.setTotalRequests(requestRepository.countByClientId(client.getId()));
                    return dto;
                });
    }
    
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(client -> {
                    ClientDto dto = ClientMapper.toDto(client);
                    dto.setTotalRequests(requestRepository.countByClientId(client.getId()));
                    return dto;
                });
    }
    
    @Transactional
    public ClientDto updateClient(Long id, ClientDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + id));
        
        // Проверка уникальности email (если изменился)
        if (!client.getEmail().equals(dto.getEmail()) && clientRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Client with email '" + dto.getEmail() + "' already exists");
        }
        
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
        
        client = clientRepository.save(client);
        
        ClientDto result = ClientMapper.toDto(client);
        result.setTotalRequests(requestRepository.countByClientId(client.getId()));
        
        return result;
    }
    
    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Client not found: " + id);
        }
        clientRepository.deleteById(id);
    }
}

