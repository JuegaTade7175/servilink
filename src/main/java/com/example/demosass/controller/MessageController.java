package com.example.demosass.controller;

import com.example.demosass.dto.response.Responses.MessageResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    /**
     * Enviar mensaje en el contexto de una reserva.
     * Body: { "content": "Hola, ¿a qué hora llegas?" }
     */
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<MessageResponse> send(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long senderId = extractUserId(request);
        String content = body.get("content");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(messageService.send(senderId, bookingId, content));
    }

    /**
     * Obtener todos los mensajes de una reserva (chat thread).
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<MessageResponse>> getByBooking(
            @PathVariable Long bookingId,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(messageService.getByBooking(userId, bookingId));
    }

    /**
     * Marcar como leídos los mensajes de una reserva para el usuario autenticado.
     */
    @PatchMapping("/booking/{bookingId}/read")
    public ResponseEntity<Map<String, Integer>> markAsRead(
            @PathVariable Long bookingId,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        int count = messageService.markAsRead(userId, bookingId);
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    /**
     * Contar mensajes no leídos del usuario autenticado.
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(Map.of("unreadCount", messageService.countUnread(userId)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
