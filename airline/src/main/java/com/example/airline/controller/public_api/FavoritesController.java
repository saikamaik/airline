package com.example.airline.controller.public_api;

import com.example.airline.dto.favorite.AddToFavoritesRequest;
import com.example.airline.dto.favorite.FavoriteTourDto;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.user.User;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.user.UserRepository;
import com.example.airline.service.favorite.FavoriteTourService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {
    
    private final FavoriteTourService favoriteTourService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    
    public FavoritesController(
            FavoriteTourService favoriteTourService,
            ClientRepository clientRepository,
            UserRepository userRepository) {
        this.favoriteTourService = favoriteTourService;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }
    
    private Long getClientIdFromAuth(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден для пользователя"));
        
        return client.getId();
    }
    
    @GetMapping
    public ResponseEntity<Page<FavoriteTourDto>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            Long clientId = getClientIdFromAuth(authentication);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<FavoriteTourDto> favorites = favoriteTourService.getFavorites(clientId, pageable);
            return ResponseEntity.ok(favorites);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<FavoriteTourDto> addToFavorites(
            @RequestBody AddToFavoritesRequest request,
            Authentication authentication) {
        try {
            Long clientId = getClientIdFromAuth(authentication);
            FavoriteTourDto favorite = favoriteTourService.addToFavorites(clientId, request.getTourId());
            return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{tourId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long tourId,
            Authentication authentication) {
        try {
            Long clientId = getClientIdFromAuth(authentication);
            favoriteTourService.removeFromFavorites(clientId, tourId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Проверить, находится ли тур в избранном
     */
    @GetMapping("/check/{tourId}")
    public ResponseEntity<Map<String, Boolean>> checkIsFavorite(
            @PathVariable Long tourId,
            Authentication authentication) {
        try {
            Long clientId = getClientIdFromAuth(authentication);
            boolean isFavorite = favoriteTourService.isFavorite(clientId, tourId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("isFavorite", isFavorite);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка проверки избранного: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Получить количество избранных туров
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFavoritesCount(Authentication authentication) {
        try {
            Long clientId = getClientIdFromAuth(authentication);
            long count = favoriteTourService.countFavorites(clientId);
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка получения количества избранных: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
