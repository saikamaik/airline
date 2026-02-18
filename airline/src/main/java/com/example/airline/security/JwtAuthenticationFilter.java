package com.example.airline.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        logger.info("JwtAuthenticationFilter bean created and initialized");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Пропускаем публичные эндпоинты
        if (path.startsWith("/auth/") || 
            path.startsWith("/swagger-ui") || 
            path.equals("/swagger-ui.html") ||
            path.startsWith("/v3/api-docs") ||
            path.equals("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars/")) {
            logger.debug("JwtAuthenticationFilter: Skipping filter for public path: {}", path);
            return true;
        }
        logger.info("JwtAuthenticationFilter: shouldNotFilter check for path: {} - will filter", path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("JwtAuthenticationFilter: Processing request to {}", requestURI);
        logger.info("JwtAuthenticationFilter: Request method: {}", request.getMethod());
        logger.info("JwtAuthenticationFilter: Authorization header present: {}", request.getHeader("Authorization") != null);
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to extract username from token: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                logger.info("Authenticating user: {}, authorities: {}", username, userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.warn("Token validation failed for user: {}", username);
            }
        } else if (authorizationHeader == null) {
            logger.warn("No Authorization header found for request: {}", request.getRequestURI());
        } else if (!authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Invalid Authorization header format for request: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}

