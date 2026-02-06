package com.example.airline.config;

import com.example.airline.security.CustomUserDetailsService;
import com.example.airline.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final com.example.airline.security.JwtUtil jwtUtil;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
    
    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         com.example.airline.security.JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        logger.info("SecurityConfig: JwtUtil injected: {}", jwtUtil != null);
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        logger.info("SecurityConfig: Creating JwtAuthenticationFilter bean");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        logger.info("SecurityConfig: JwtAuthenticationFilter bean created");
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("SecurityConfig: filterChain method called");
        JwtAuthenticationFilter jwtFilter = jwtAuthenticationFilter();
        logger.info("SecurityConfig: JWT filter obtained: {}", jwtFilter != null);
        
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    String origin = request.getHeader("Origin");
                    logger.info("CORS request from origin: {}", origin);
                    
                    // Разрешаем все localhost порты для разработки через паттерн
                    // 10.0.2.2 - это специальный адрес Android эмулятора для доступа к localhost хоста
                    // Разрешаем все домены Vercel для продакшена
                    // Паттерны поддерживают wildcards: * для любого поддомена
                    java.util.List<String> allowedPatterns = java.util.List.of(
                        "http://localhost:*",
                        "http://10.0.2.2:*",
                        "https://*.vercel.app"
                    );
                    corsConfig.setAllowedOriginPatterns(allowedPatterns);
                    logger.info("CORS allowed patterns: {}", allowedPatterns);
                    
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;
                }))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (доступны без авторизации)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        // Просмотр туров и рейсов - публичный
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/tours/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/flights/**").permitAll()
                        // Создание заявки - только для авторизованных пользователей
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/tours/*/request").authenticated()
                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Employee endpoints
                        .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        // Boarding requires authentication
                        .requestMatchers("/boardings/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("AuthenticationEntryPoint: {} - {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized: " + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("AccessDeniedHandler: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
                            logger.warn("Current authentication: {}", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden: " + accessDeniedException.getMessage() + "\"}");
                        })
                );
        
        logger.info("SecurityConfig: Filter chain configured, JWT filter added before AuthorizationFilter");
        
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

