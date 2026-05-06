package com.example.demosass.service;

import com.example.demosass.domain.enums.NotificationType;
import com.example.demosass.domain.model.Notification;
import com.example.demosass.domain.model.User;
import com.example.demosass.domain.repository.NotificationRepository;
import com.example.demosass.dto.response.Responses.NotificationResponse;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User user, String title, String body,
                                    NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .body(body)
            .type(type)
            .referenceId(referenceId)
            .build();
        notificationRepository.save(notification);
        log.info("Notificación creada para usuario {}: {}", user.getId(), title);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsRead(userId);
        log.info("Marcadas {} notificaciones como leídas para usuario {}", count, userId);
        return count;
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
            n.getId(),
            n.getTitle(),
            n.getBody(),
            n.getType(),
            n.getReferenceId(),
            n.getIsRead(),
            n.getCreatedAt()
        );
    }
}
