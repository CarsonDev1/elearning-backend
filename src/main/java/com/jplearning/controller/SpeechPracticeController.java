package com.jplearning.controller;

import com.jplearning.dto.request.SpeechPracticeRequest;
import com.jplearning.dto.response.SpeechPracticeResponse;
import com.jplearning.exception.BadRequestException;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.SpeechPracticeService;
import com.jplearning.service.impl.SpeechPracticeServiceImpl;
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
import java.util.Map;

@RestController
@RequestMapping("/speech-practice")
@Tag(name = "Speech Practice", description = "Japanese speech practice API with Web Speech API support")
@CrossOrigin(origins = "*")
public class SpeechPracticeController {

    @Autowired
    private SpeechPracticeService speechPracticeService;

    @Autowired
    private SpeechPracticeServiceImpl speechPracticeServiceImpl;

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

    @PostMapping(value = "/{practiceId}/submit-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Submit practice audio",
            description = "Submit audio recording for storage (recognition done on frontend)",
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

    @PostMapping("/{practiceId}/submit-recognition")
    @Operation(
            summary = "Submit recognition result",
            description = "Submit speech recognition result from Web Speech API",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SpeechPracticeResponse> submitRecognitionResult(
            @PathVariable Long practiceId,
            @RequestBody Map<String, String> request) {
        try {
            String recognizedText = request.get("recognizedText");
            if (recognizedText == null || recognizedText.trim().isEmpty()) {
                throw new BadRequestException("Recognized text is required");
            }

            return ResponseEntity.ok(speechPracticeServiceImpl.submitRecognitionResult(practiceId, recognizedText));
        } catch (IOException e) {
            throw new BadRequestException("Failed to process recognition result: " + e.getMessage());
        }
    }

    @PostMapping("/{practiceId}/submit-complete")
    @Operation(
            summary = "Submit complete practice",
            description = "Submit both audio and recognition result in one request",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SpeechPracticeResponse> submitCompletePractice(
            @PathVariable Long practiceId,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("recognizedText") String recognizedText) {
        try {
            // First submit the audio
            speechPracticeService.submitPracticeAudio(practiceId, audioFile);

            // Then submit the recognition result
            return ResponseEntity.ok(speechPracticeServiceImpl.submitRecognitionResult(practiceId, recognizedText));
        } catch (IOException e) {
            throw new BadRequestException("Failed to process complete practice: " + e.getMessage());
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

    @GetMapping("/web-speech-config")
    @Operation(
            summary = "Get Web Speech API configuration",
            description = "Get configuration for Web Speech API usage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getWebSpeechConfig() {
        return ResponseEntity.ok(Map.of(
                "supportedLanguages", List.of("ja-JP", "en-US"),
                "defaultLanguage", "ja-JP",
                "maxRecordingTime", 30, // seconds
                "audioFormat", "webm",
                "instructions", Map.of(
                        "en", "Click the microphone button to start recording. Speak clearly and at a normal pace.",
                        "ja", "マイクボタンをクリックして録音を開始してください。はっきりと通常のペースで話してください。"
                )
        ));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}