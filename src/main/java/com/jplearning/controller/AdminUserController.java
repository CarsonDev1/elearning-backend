package com.jplearning.controller;

import com.jplearning.dto.response.UserResponse;
import com.jplearning.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin User Management", description = "APIs for admin to manage users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/students")
    @Operation(
            summary = "Get all students",
            description = "Get all student accounts with pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<UserResponse>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(adminUserService.getAllStudents(pageable));
    }

    @GetMapping("/tutors")
    @Operation(
            summary = "Get all tutors",
            description = "Get all tutor accounts with pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<UserResponse>> getAllTutors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(adminUserService.getAllTutors(pageable));
    }

    @GetMapping("/tutors/pending")
    @Operation(
            summary = "Get pending tutors",
            description = "Get all tutor accounts pending approval",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<UserResponse>> getPendingTutors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getPendingTutors(pageable));
    }

    @PutMapping("/tutors/{tutorId}/approve")
    @Operation(
            summary = "Approve tutor",
            description = "Approve a pending tutor account",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> approveTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(adminUserService.approveTutor(tutorId));
    }

    @PutMapping("/tutors/{tutorId}/reject")
    @Operation(
            summary = "Reject tutor",
            description = "Reject a pending tutor account",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> rejectTutor(
            @PathVariable Long tutorId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminUserService.rejectTutor(tutorId, reason));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search users",
            description = "Search users by email or name",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.searchUsers(query, pageable));
    }
}