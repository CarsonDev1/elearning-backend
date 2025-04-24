package com.jplearning.service.impl;

import com.jplearning.dto.response.EducationResponse;
import com.jplearning.dto.response.ExperienceResponse;
import com.jplearning.dto.response.UserResponse;
import com.jplearning.entity.Role;
import com.jplearning.entity.Student;
import com.jplearning.entity.Tutor;
import com.jplearning.entity.User;
import com.jplearning.exception.BadRequestException;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.mapper.UserMapper;
import com.jplearning.repository.StudentRepository;
import com.jplearning.repository.TutorRepository;
import com.jplearning.repository.UserRepository;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.CloudinaryService;
import com.jplearning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public UserResponse getCurrentUser() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Long userId = userDetails.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Convert user roles to strings
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        // Create base user response
        UserResponse.UserResponseBuilder responseBuilder = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        // Check if user is a student
        if (isRolePresent(roles, Role.ERole.ROLE_STUDENT.name())) {
            responseBuilder.userType("STUDENT");

            // If needed, get more student-specific details
            studentRepository.findById(userId).ifPresent(student -> {
                // Add student-specific fields if any
            });
        }
        // Check if user is a tutor
        else if (isRolePresent(roles, Role.ERole.ROLE_TUTOR.name())) {
            responseBuilder.userType("TUTOR");

            // Get tutor-specific details
            tutorRepository.findById(userId).ifPresent(tutor -> {
                responseBuilder.teachingRequirements(tutor.getTeachingRequirements());

                // Map educations to DTOs
                if (tutor.getEducations() != null && !tutor.getEducations().isEmpty()) {
                    List<EducationResponse> educationResponses = tutor.getEducations().stream()
                            .map(userMapper::educationToEducationResponse)
                            .collect(Collectors.toList());
                    responseBuilder.educations(educationResponses);
                }

                // Map experiences to DTOs
                if (tutor.getExperiences() != null && !tutor.getExperiences().isEmpty()) {
                    List<ExperienceResponse> experienceResponses = tutor.getExperiences().stream()
                            .map(userMapper::experienceToExperienceResponse)
                            .collect(Collectors.toList());
                    responseBuilder.experiences(experienceResponses);
                }

                // Set certificate URLs
                responseBuilder.certificateUrls(tutor.getCertificateUrls());
            });
        }
        // Check if user is an admin
        else if (isRolePresent(roles, Role.ERole.ROLE_ADMIN.name())) {
            responseBuilder.userType("ADMIN");
        }

        return responseBuilder.build();
    }

    private boolean isRolePresent(Set<String> roles, String roleName) {
        return roles.stream().anyMatch(role -> role.equals(roleName));
    }

    @Override
    public UserResponse updateAvatar(Long userId, MultipartFile file) throws IOException {
        // Validate current user is updating their own avatar or is an admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userDetails.getId().equals(userId) &&
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You don't have permission to update this user's avatar");
        }

        // Validate file is an image
        if (file.isEmpty()) {
            throw new BadRequestException("Avatar image cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Avatar must be an image file");
        }

        // Check file size (max 5MB for avatars)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Avatar image size should not exceed 5MB");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Upload avatar to Cloudinary
        Map<String, String> uploadResult = cloudinaryService.uploadImage(file);

        // Update user's avatar URL
        user.setAvatarUrl(uploadResult.get("secureUrl"));
        userRepository.save(user);

        // Return updated user info
        return getCurrentUser();
    }
}