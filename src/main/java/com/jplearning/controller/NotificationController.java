package com.jplearning.controller;

import com.jplearning.dto.request.BulkNotificationRequest;
import com.jplearning.dto.request.NotificationRequest;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.NotificationCountResponse;
import com.jplearning.dto.response.NotificationResponse;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "APIs for notification management")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/my-notifications")
    @Operation(
            summary = "Get my notifications",
            description = "Get notifications for the current authenticated user with pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get unread notification count",
            description = "Get the count of unread notifications for the current user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<NotificationCountResponse> getUnreadCount() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    @PutMapping("/mark-all-read")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the current user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete notification",
            description = "Delete a specific notification",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.deleteNotification(id, userId));
    }

    @PostMapping
    @Operation(
            summary = "Create notification",
            description = "Create a new notification (admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Create bulk notifications",
            description = "Create notifications for multiple users (admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createBulkNotifications(
            @Valid @RequestBody BulkNotificationRequest request) {
        return ResponseEntity.ok(notificationService.createBulkNotifications(request));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
} 