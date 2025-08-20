package com.jplearning.service.impl;

import com.jplearning.dto.request.*;
import com.jplearning.dto.response.*;
import com.jplearning.entity.*;
import com.jplearning.exception.BadRequestException;
import com.jplearning.exception.ResourceNotFoundException;
import com.jplearning.mapper.UserMapper;
import com.jplearning.repository.*;
import com.jplearning.security.jwt.JwtUtils;
import com.jplearning.service.AuthService;
import com.jplearning.service.CloudinaryService;
import com.jplearning.service.EmailService;
import com.jplearning.service.TutorEmailService;
import com.jplearning.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TutorEmailService tutorEmailService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get user details from the user service
        UserResponse userResponse = userService.getCurrentUser();

        return new JwtResponse(jwt, userResponse);
    }

    @Override
    @Transactional
    public MessageResponse registerStudent(RegisterStudentRequest registerRequest) {
        // Validate if the email is already in use
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Validate if the phone number is already in use (if provided)
        if (registerRequest.getPhoneNumber() != null &&
                !registerRequest.getPhoneNumber().isEmpty() &&
                userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new BadRequestException("Error: Phone number is already in use!");
        }

        // Validate if passwords match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException("Error: Passwords do not match!");
        }

        // Create new student account
        Student student = userMapper.studentRequestToStudent(registerRequest);
        student.setPassword(encoder.encode(registerRequest.getPassword()));

        // Set student role
        Set<Role> roles = new HashSet<>();
        Role studentRole = roleRepository.findByName(Role.ERole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Student Role is not found."));
        roles.add(studentRole);
        student.setRoles(roles);

        // Default is not enabled until email verification
        student.setEnabled(false);

        Student savedStudent = studentRepository.save(student);

        // Generate verification token and send email
        String token = UUID.randomUUID().toString();
        PasswordResetToken verificationToken = PasswordResetToken.builder()
                .token(token)
                .user(savedStudent)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedStudent.getEmail(), token);

        return new MessageResponse("Student registered successfully! Please check your email to verify your account.");
    }

    @Override
    @Transactional
    public MessageResponse registerTutor(RegisterTutorRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already in use!");
        }

        // Validate password confirmation
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match!");
        }

        // Create new tutor account
        Tutor tutor = userMapper.tutorRequestToTutor(registerRequest);
        tutor.setPassword(encoder.encode(registerRequest.getPassword()));

        // Set tutor role
        Set<Role> roles = new HashSet<>();
        Role tutorRole = roleRepository.findByName(Role.ERole.ROLE_TUTOR)
                .orElseThrow(() -> new RuntimeException("Error: Tutor Role is not found."));
        roles.add(tutorRole);
        tutor.setRoles(roles);

        // For tutors, account is disabled until admin approval
        tutor.setEnabled(false);

        // Process certificate uploads if provided
        if (registerRequest.getCertificates() != null && !registerRequest.getCertificates().isEmpty()) {
            logger.info("Processing {} certificate files for tutor registration", registerRequest.getCertificates().size());
            List<String> certificateUrls = new ArrayList<>();
            
            for (MultipartFile certificateFile : registerRequest.getCertificates()) {
                try {
                    logger.info("Validating certificate file: {} (size: {} bytes)", 
                              certificateFile.getOriginalFilename(), certificateFile.getSize());
                    
                    // Validate certificate file
                    validateCertificateFile(certificateFile);
                    
                    logger.info("Uploading certificate to Cloudinary: {}", certificateFile.getOriginalFilename());
                    
                    // Upload to Cloudinary as image
                    Map<String, String> uploadResult = cloudinaryService.uploadImage(certificateFile);
                    String certificateUrl = uploadResult.get("secureUrl");
                    certificateUrls.add(certificateUrl);
                    
                    logger.info("Successfully uploaded certificate. URL: {}", certificateUrl);
                } catch (IOException e) {
                    logger.error("Failed to upload certificate file: {}", certificateFile.getOriginalFilename(), e);
                    throw new BadRequestException("Failed to upload certificate: " + e.getMessage());
                }
            }
            
            tutor.setCertificateUrls(certificateUrls);
            logger.info("Successfully processed {} certificates for tutor registration", certificateUrls.size());
        } else {
            logger.info("No certificates provided for tutor registration");
        }

        Tutor savedTutor = tutorRepository.save(tutor);

        // Send welcome email to tutor
        tutorEmailService.sendWelcomeEmail(savedTutor.getEmail(), savedTutor.getFullName());

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(verificationToken);
        token.setUser(savedTutor);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24 hours
        tokenRepository.save(token);

        // Send verification email
        emailService.sendVerificationEmail(savedTutor.getEmail(), verificationToken);

        return new MessageResponse("Tutor registration submitted! Please check your email for confirmation. " +
                "Your application will be reviewed by an administrator.");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Delete any existing token for this user
        tokenRepository.findByUser(user).ifPresent(token -> tokenRepository.delete(token));

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return new MessageResponse("Password reset instructions have been sent to your email.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete used token
        tokenRepository.delete(resetToken);

        return new MessageResponse("Password has been reset successfully");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify current password
        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }

        // Update password
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponse("Password changed successfully");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        PasswordResetToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new BadRequestException("Verification token has expired");
        }

        User user = verificationToken.getUser();

        // Check if user is a Student (only students need email verification)
        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.ERole.ROLE_STUDENT);

        if (!isStudent) {
            throw new BadRequestException("Invalid verification request");
        }

        user.setEnabled(true);
        userRepository.save(user);

        // Delete used token
        tokenRepository.delete(verificationToken);

        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    private void validateCertificateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Certificate file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Content type is missing");
        }

        // Allow only image types for certificates (easier for Cloudinary)
        if (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/jpg") && 
            !contentType.equals("image/png") && 
            !contentType.equals("image/webp")) {
            throw new BadRequestException("Only image files (JPEG, JPG, PNG, WEBP) are allowed for certificates");
        }

        // Check file size (limit to 5MB for images)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new BadRequestException("Certificate image size cannot exceed 5MB");
        }
    }
}