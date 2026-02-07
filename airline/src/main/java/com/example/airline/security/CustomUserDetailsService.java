package com.example.airline.security;

import com.example.airline.entity.user.User;
import com.example.airline.repository.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);
        logger.info("=== CustomUserDetailsService: Загрузка пользователя: {}", username);
        
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    logger.error("=== CustomUserDetailsService: Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        logger.info("=== CustomUserDetailsService: Пользователь найден: {}, enabled: {}, ролей: {}", 
            user.getUsername(), user.isEnabled(), user.getRoles().size());
        
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    logger.info("=== CustomUserDetailsService: Роль пользователя {}: {}", username, role.getName());
                    return new SimpleGrantedAuthority(role.getName().name());
                })
                .collect(Collectors.toSet());

        logger.info("=== CustomUserDetailsService: Создан UserDetails для {} с {} ролями", username, authorities.size());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}

