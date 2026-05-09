package com.example.demosass.controller;

import com.example.demosass.domain.model.User;
import com.example.demosass.domain.repository.UserRepository;
import com.example.demosass.dto.response.Responses.UserResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import com.example.demosass.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PatchMapping("/profile-picture")
    public ResponseEntity<UserResponse> updateProfilePicture(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        String url = body.get("profilePictureUrl");

        if (url == null || url.isBlank()) {
            throw new BadRequestException("La URL de la imagen es obligatoria");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BadRequestException("La URL debe comenzar con http:// o https://");
        }
        if (url.length() > 2048) {
            throw new BadRequestException("La URL es demasiado larga (máximo 2048 caracteres)");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setProfilePictureUrl(url);
        userRepository.save(user);

        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<UserResponse> removeProfilePicture(HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setProfilePictureUrl(null);
        userRepository.save(user);

        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
            u.getId(),
            u.getName(),
            u.getEmail(),
            u.getPhone(),
            u.getProfilePictureUrl(),
            u.getRole(),
            u.getCreatedAt()
        );
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
