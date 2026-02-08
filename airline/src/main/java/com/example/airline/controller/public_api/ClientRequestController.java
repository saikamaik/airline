package com.example.airline.controller.public_api;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.service.request.ClientRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы клиентов со своими заявками из мобильного приложения
 */
@RestController
@RequestMapping("/client")
public class ClientRequestController {

    private final ClientRequestService requestService;
    private final com.example.airline.repository.client.ClientRepository clientRepository;
    private final com.example.airline.repository.user.UserRepository userRepository;

    public ClientRequestController(
            ClientRequestService requestService,
            com.example.airline.repository.client.ClientRepository clientRepository,
            com.example.airline.repository.user.UserRepository userRepository) {
        this.requestService = requestService;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получить заявки текущего клиента
     */
    @GetMapping("/requests")
    public ResponseEntity<Page<ClientRequestDto>> getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        // Находим клиента по username
        Long clientId = userRepository.findByUsername(username)
                .flatMap(user -> clientRepository.findByUserId(user.getId()))
                .map(client -> client.getId())
                .orElse(null);
        
        if (clientId == null) {
            return ResponseEntity.notFound().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClientRequestDto> requests = requestService.findByClientId(clientId, pageable);
        
        return ResponseEntity.ok(requests);
    }
}
