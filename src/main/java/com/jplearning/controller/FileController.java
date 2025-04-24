package com.jplearning.controller;

import com.jplearning.exception.BadRequestException;
import com.jplearning.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/files")
@Tag(name = "File Upload", description = "File upload APIs")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload image",
            description = "Upload an image file to cloud storage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            validateImageFile(file);
            Map<String, String> result = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload video",
            description = "Upload a video file to cloud storage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            validateVideoFile(file);
            Map<String, String> result = cloudinaryService.uploadVideo(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload video: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload document",
            description = "Upload a document file to cloud storage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            validateDocumentFile(file);
            Map<String, String> result = cloudinaryService.uploadFile(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{publicId}")
    @Operation(
            summary = "Delete file",
            description = "Delete a file from cloud storage by its public ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String publicId) {
        try {
            Map<String, String> result = cloudinaryService.deleteFile(publicId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("Image size should not exceed 10MB");
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new BadRequestException("File must be a video");
        }

        // Check file size (max 100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new BadRequestException("Video size should not exceed 100MB");
        }
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Content type is missing");
        }

        // Allow common document types
        boolean isValidType = contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        if (!isValidType) {
            throw new BadRequestException("File must be a document (PDF, DOC, DOCX, XLS, XLSX)");
        }

        // Check file size (max 20MB)
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BadRequestException("Document size should not exceed 20MB");
        }
    }
}