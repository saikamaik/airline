package com.example.airline.controller.public_api;

import com.example.airline.dto.auth.AuthRequest;
import com.example.airline.dto.auth.AuthResponse;
import com.example.airline.dto.auth.RegisterRequest;
import com.example.airline.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Вход в систему (для всех пользователей).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
            logger.info("=== AuthController: Попытка входа для пользователя: {}", request.getUsername());
            AuthResponse response = authService.authenticate(request);
            logger.info("=== AuthController: Успешный вход для пользователя: {}, роли: {}", 
                request.getUsername(), response.getRoles());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
            logger.warn("=== AuthController: Неверные учетные данные для пользователя: {}", request.getUsername());
            logger.warn("=== AuthController: Ошибка: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Неверное имя пользователя или пароль")
            );
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
            logger.error("=== AuthController: Ошибка аутентификации для пользователя: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Ошибка аутентификации: " + e.getMessage())
            );
        }
    }
    
    /**
     * Регистрация нового клиента (через мобильное приложение).
     * После успешной регистрации возвращает JWT-токен для автоматического входа.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Ошибка регистрации: " + e.getMessage())
            );
        }
    }
}

