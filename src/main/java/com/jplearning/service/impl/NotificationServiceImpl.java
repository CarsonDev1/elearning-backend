package com.jplearning.service.impl;

import com.jplearning.dto.request.BulkNotificationRequest;
import com.jplearning.dto.request.NotificationRequest;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.NotificationCountResponse;
import com.jplearning.dto.response.NotificationResponse;
import com.jplearning.entity.Notification;
import com.jplearning.entity.User;
import com.jplearning.exception.BadRequestException;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.repository.NotificationRepository;
import com.jplearning.repository.UserRepository;
import com.jplearning.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToResponse);
    }

    @Override
    public NotificationCountResponse getUnreadCount(Long userId) {
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        return NotificationCountResponse.builder()
                .unreadCount(unreadCount)
                .totalCount(unreadCount) // For now, we return same value
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .user(user)
                .actionUrl(request.getActionUrl())
                .actionText(request.getActionText())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", user.getId(), notification.getTitle());

        return mapToResponse(savedNotification);
    }

    @Override
    @Transactional
    public MessageResponse createBulkNotifications(BulkNotificationRequest request) {
        List<User> users = userRepository.findAllById(request.getUserIds());
        
        if (users.size() != request.getUserIds().size()) {
            throw new BadRequestException("Some user IDs are invalid");
        }

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType())
                        .user(user)
                        .actionUrl(request.getActionUrl())
                        .actionText(request.getActionText())
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
        log.info("Created {} bulk notifications", notifications.size());

        return new MessageResponse("Bulk notifications created successfully");
    }

    @Override
    @Transactional
    public MessageResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify notification belongs to user
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Notification does not belong to user");
        }

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }

        return new MessageResponse("Notification marked as read");
    }

    @Override
    @Transactional
    public MessageResponse markAllAsRead(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", updatedCount, userId);
        return new MessageResponse("All notifications marked as read");
    }

    @Override
    @Transactional
    public MessageResponse deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify notification belongs to user
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Notification does not belong to user");
        }

        notificationRepository.delete(notification);
        return new MessageResponse("Notification deleted successfully");
    }

    @Override
    @Transactional
    public void createEnrollmentNotification(Long studentId, String courseName, Long courseId) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setTitle("Đăng ký khóa học thành công");
            request.setMessage("Bạn đã đăng ký thành công khóa học: " + courseName);
            request.setType(Notification.NotificationType.COURSE_ENROLLMENT);
            request.setActionUrl("/courses/" + courseId);
            request.setActionText("Xem khóa học");
            
            createNotification(request);
        } catch (Exception e) {
            log.error("Failed to create enrollment notification for student {}: {}", studentId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createCompletionNotification(Long studentId, String courseName, Long courseId) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setTitle("Hoàn thành khóa học");
            request.setMessage("Chúc mừng! Bạn đã hoàn thành khóa học: " + courseName);
            request.setType(Notification.NotificationType.COURSE_COMPLETION);
            request.setActionUrl("/courses/" + courseId + "/certificate");
            request.setActionText("Tải chứng chỉ");
            
            createNotification(request);
        } catch (Exception e) {
            log.error("Failed to create completion notification for student {}: {}", studentId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createPaymentSuccessNotification(Long studentId, String paymentDetails) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setTitle("Thanh toán thành công");
            request.setMessage("Thanh toán của bạn đã được xử lý thành công. " + paymentDetails);
            request.setType(Notification.NotificationType.PAYMENT_SUCCESS);
            request.setActionUrl("/profile/payment-history");
            request.setActionText("Xem lịch sử");
            
            createNotification(request);
        } catch (Exception e) {
            log.error("Failed to create payment notification for student {}: {}", studentId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createCourseApprovalNotification(Long tutorId, String courseName, Long courseId, boolean approved) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(tutorId);
            request.setTitle(approved ? "Khóa học được phê duyệt" : "Khóa học bị từ chối");
            request.setMessage(approved 
                ? "Khóa học '" + courseName + "' đã được phê duyệt và có thể được học viên đăng ký."
                : "Khóa học '" + courseName + "' chưa được phê duyệt. Vui lòng kiểm tra lại nội dung.");
            request.setType(Notification.NotificationType.COURSE_APPROVAL);
            request.setActionUrl("/tutor/courses/" + courseId);
            request.setActionText("Xem chi tiết");
            
            createNotification(request);
        } catch (Exception e) {
            log.error("Failed to create course approval notification for tutor {}: {}", tutorId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cleanupOldNotifications() {
        // Delete read notifications older than 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);
        log.info("Cleaned up {} old read notifications", deletedCount);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .actionUrl(notification.getActionUrl())
                .actionText(notification.getActionText())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
} 