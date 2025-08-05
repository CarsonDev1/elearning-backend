package com.jplearning.service;

import com.jplearning.dto.request.BulkNotificationRequest;
import com.jplearning.dto.request.NotificationRequest;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.NotificationCountResponse;
import com.jplearning.dto.response.NotificationResponse;
import com.jplearning.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    /**
     * Get notifications for a user with pagination
     */
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Get unread notification count for a user
     */
    NotificationCountResponse getUnreadCount(Long userId);

    /**
     * Create a new notification
     */
    NotificationResponse createNotification(NotificationRequest request);

    /**
     * Create bulk notifications
     */
    MessageResponse createBulkNotifications(BulkNotificationRequest request);

    /**
     * Mark notification as read
     */
    MessageResponse markAsRead(Long notificationId, Long userId);

    /**
     * Mark all notifications as read for a user
     */
    MessageResponse markAllAsRead(Long userId);

    /**
     * Delete a notification
     */
    MessageResponse deleteNotification(Long notificationId, Long userId);

    /**
     * Create notification for course enrollment
     */
    void createEnrollmentNotification(Long studentId, String courseName, Long courseId);

    /**
     * Create notification for course completion
     */
    void createCompletionNotification(Long studentId, String courseName, Long courseId);

    /**
     * Create notification for payment success
     */
    void createPaymentSuccessNotification(Long studentId, String paymentDetails);

    /**
     * Create notification for course approval (for tutors)
     */
    void createCourseApprovalNotification(Long tutorId, String courseName, Long courseId, boolean approved);

    /**
     * Clean up old read notifications
     */
    void cleanupOldNotifications();
} 