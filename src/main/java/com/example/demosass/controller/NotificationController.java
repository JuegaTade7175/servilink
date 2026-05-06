package com.example.demosass.controller;

import com.example.demosass.dto.response.Responses.NotificationResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(notificationService.getUnreadByUser(userId));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(HttpServletRequest request) {
        Long userId = extractUserId(request);
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
