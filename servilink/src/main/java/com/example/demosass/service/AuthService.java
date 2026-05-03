package com.example.demosass.service;

import com.example.demosass.domain.model.User;
import com.example.demosass.domain.repository.UserRepository;
import com.example.demosass.dto.request.AuthRequest.LoginRequest;
import com.example.demosass.dto.request.AuthRequest.RegisterRequest;
import com.example.demosass.dto.response.Responses.AuthResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El email ya está registrado");
        }

        User user = User.builder()
            .name(request.name())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .role(request.role())
            .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
