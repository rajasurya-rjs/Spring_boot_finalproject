package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.LoginRequest;
import com.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.Role;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

        private static final Logger log = LoggerFactory.getLogger(AuthService.class);

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager,
                        EmailService emailService) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
                this.authenticationManager = authenticationManager;
                this.emailService = emailService;
        }

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                log.info("Registering new user: {}", request.getUsername());

                // Check if username already exists
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new BadRequestException("Username is already taken");
                }

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BadRequestException("Email is already registered");
                }

                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());

                userRepository.save(user);
                log.info("User registered successfully: {}", user.getUsername());

                // Send welcome email asynchronously
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

                // Generate JWT token
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(user.getRoles().stream()
                                                .map(role -> "ROLE_" + role.name())
                                                .toArray(String[]::new))
                                .build();

                String token = jwtTokenProvider.generateToken(userDetails);

                Set<String> roleNames = user.getRoles().stream()
                                .map(Role::name)
                                .collect(Collectors.toSet());

                return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRoles());
        }

        public AuthResponse login(LoginRequest request) {
                log.info("User attempting to login: {}", request.getUsername());

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                // Generate JWT token for the new user
                String token = jwtTokenProvider.generateToken(userDetails);

                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username",
                                                request.getUsername()));

                log.info("User logged in successfully: {}", request.getUsername());

                return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRoles());
        }
}
