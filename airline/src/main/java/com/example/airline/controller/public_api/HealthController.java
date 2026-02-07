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
        return ResponseEntity.ok(Map.of(
            "password", password,
            "hash", hash,
            "matches", String.valueOf(matches),
            "length", String.valueOf(hash.length())
        ));
    }
}
