package com.example.airline.service.auth;

import com.example.airline.dto.auth.AuthRequest;
import com.example.airline.dto.auth.AuthResponse;
import com.example.airline.dto.auth.RegisterRequest;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.user.Role;
import com.example.airline.entity.user.RoleName;
import com.example.airline.entity.user.User;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.user.RoleRepository;
import com.example.airline.repository.user.UserRepository;
import com.example.airline.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse authenticate(AuthRequest request) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);
        logger.info("=== AuthService: Попытка аутентификации для пользователя: {}", request.getUsername());
        logger.info("=== AuthService: Длина пароля в запросе: {}", request.getPassword() != null ? request.getPassword().length() : 0);
        
        // Проверяем пароль вручную для диагностики
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user != null) {
            logger.info("=== AuthService: Пароль из БД (первые 20 символов): {}", 
                user.getPassword() != null && user.getPassword().length() > 20 
                    ? user.getPassword().substring(0, 20) + "..." 
                    : user.getPassword());
            boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            logger.info("=== AuthService: Проверка пароля вручную: {}", matches);
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        logger.info("=== AuthService: Аутентификация успешна для пользователя: {}", request.getUsername());
        return new AuthResponse(token, userDetails.getUsername(), roles);
    }
    
    /**
     * Регистрация нового клиента через мобильное приложение.
     * Создает аккаунт User с ролью ROLE_USER и связанную запись Client.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Проверяем, что username и email не заняты
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Клиент с таким email уже существует");
        }
        
        // Получаем роль USER
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));
        
        // Создаем пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        user = userRepository.save(user);
        
        // Создаем клиента
        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setUser(user);
        client.setActive(true);
        client.setVipStatus(false);
        
        clientRepository.save(client);
        
        // Возвращаем токен для автоматического входа после регистрации
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        
        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return new AuthResponse(token, userDetails.getUsername(), roleNames);
    }
}

