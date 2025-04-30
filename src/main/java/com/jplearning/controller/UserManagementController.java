package com.jplearning.controller;

import com.jplearning.dto.request.BlockUserRequest;
import com.jplearning.dto.request.UnblockUserRequest;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.UserResponse;
import com.jplearning.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user-management")
@Tag(name = "User Management", description = "APIs for user account management")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/block")
    @Operation(
            summary = "Block user",
            description = "Block a user account with optional reason",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> blockUser(@Valid @RequestBody BlockUserRequest request) {
        return ResponseEntity.ok(userManagementService.blockUser(request.getUserId(), request.getReason()));
    }

    @PostMapping("/unblock")
    @Operation(
            summary = "Unblock user",
            description = "Unblock a previously blocked user account",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> unblockUser(@Valid @RequestBody UnblockUserRequest request) {
        return ResponseEntity.ok(userManagementService.unblockUser(request.getUserId()));
    }

    @GetMapping("/{userId}/block-status")
    @Operation(
            summary = "Get user block status",
            description = "Get block status information for a user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> getUserBlockStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.getUserBlockStatus(userId));
    }

    @PutMapping("/{userId}/block")
    @Operation(
            summary = "Block user with path variable",
            description = "Alternative endpoint to block a user using path variable",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> blockUserPath(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(userManagementService.blockUser(userId, reason));
    }

    @PutMapping("/{userId}/unblock")
    @Operation(
            summary = "Unblock user with path variable",
            description = "Alternative endpoint to unblock a user using path variable",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> unblockUserPath(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.unblockUser(userId));
    }
}