package com.jplearning.controller;

import com.jplearning.dto.request.SpeechPracticeRequest;
import com.jplearning.dto.response.SpeechPracticeResponse;
import com.jplearning.exception.BadRequestException;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.SpeechPracticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/speech-practice")
@Tag(name = "Speech Practice", description = "Japanese speech practice API")
@CrossOrigin(origins = "*")
public class SpeechPracticeController {

    @Autowired
    private SpeechPracticeService speechPracticeService;

    @PostMapping
    @Operation(
            summary = "Create new practice",
            description = "Create a new speech practice session",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SpeechPracticeResponse> createPractice(
            @Valid @RequestBody SpeechPracticeRequest request,
            @RequestParam(required = false) Long lessonId) {
        Long studentId = getCurrentUserId();
        return ResponseEntity.ok(speechPracticeService.createPractice(studentId, lessonId, request));
    }

    @PostMapping(value = "/{practiceId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Submit practice audio",
            description = "Submit audio recording for speech recognition and evaluation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SpeechPracticeResponse> submitPracticeAudio(
            @PathVariable Long practiceId,
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            return ResponseEntity.ok(speechPracticeService.submitPracticeAudio(practiceId, audioFile));
        } catch (IOException e) {
            throw new BadRequestException("Failed to process audio: " + e.getMessage());
        }
    }

    @GetMapping("/{practiceId}")
    @Operation(
            summary = "Get practice details",
            description = "Get details of a specific practice session",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<SpeechPracticeResponse> getPracticeById(@PathVariable Long practiceId) {
        return ResponseEntity.ok(speechPracticeService.getPracticeById(practiceId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(
            summary = "Get student practices",
            description = "Get all practice sessions for a student",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<SpeechPracticeResponse>> getStudentPractices(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(speechPracticeService.getStudentPractices(studentId, pageable));
    }

    @GetMapping("/my-practices")
    @Operation(
            summary = "Get current user practices",
            description = "Get practice sessions for the current authenticated student",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<SpeechPracticeResponse>> getMyPractices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Long studentId = getCurrentUserId();
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(speechPracticeService.getStudentPractices(studentId, pageable));
    }

    @GetMapping("/my-recent-practices")
    @Operation(
            summary = "Get recent practices",
            description = "Get recent practice sessions for the current authenticated student",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SpeechPracticeResponse>> getMyRecentPractices() {
        Long studentId = getCurrentUserId();
        return ResponseEntity.ok(speechPracticeService.getRecentPractices(studentId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}