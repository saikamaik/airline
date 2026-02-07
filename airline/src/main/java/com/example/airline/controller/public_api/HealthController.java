package com.example.airline.controller.public_api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint для Railway и других платформ деплоя.
 * Railway использует этот endpoint для проверки работоспособности сервиса.
 */
@RestController
public class HealthController {

    private final PasswordEncoder passwordEncoder;

    public HealthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "airline-backend"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "airline-backend"
        ));
    }

    /**
     * Временный endpoint для генерации BCrypt хеша пароля (только для отладки).
     * Удалите после использования!
     */
    @GetMapping("/test-hash")
    public ResponseEntity<Map<String, String>> testHash(@RequestParam(defaultValue = "password123") String password) {
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        
        // Проверяем существующий хеш из БД
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O";
        boolean existingMatches = passwordEncoder.matches(password, existingHash);
        
        return ResponseEntity.ok(Map.of(
            "password", password,
            "new_hash", hash,
            "new_hash_matches", String.valueOf(matches),
            "existing_hash", existingHash,
            "existing_hash_matches", String.valueOf(existingMatches),
            "length", String.valueOf(hash.length())
        ));
    }
}
