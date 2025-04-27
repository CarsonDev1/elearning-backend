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
import com.jplearning.service.AdminUserService;
import com.jplearning.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public Page<UserResponse> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(this::mapStudentToResponse);
    }

    @Override
    public Page<UserResponse> getAllTutors(Pageable pageable) {
        Page<Tutor> tutors = tutorRepository.findAll(pageable);
        return tutors.map(this::mapTutorToResponse);
    }

    @Override
    public Page<UserResponse> getPendingTutors(Pageable pageable) {
        // Get tutors that are not enabled (pending approval)
        Page<Tutor> pendingTutors = tutorRepository.findByEnabled(false, pageable);
        return pendingTutors.map(this::mapTutorToResponse);
    }

    @Override
    @Transactional
    public UserResponse approveTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        // Check if already enabled
        if (tutor.isEnabled()) {
            throw new BadRequestException("Tutor is already approved");
        }

        // Enable tutor account
        tutor.setEnabled(true);
        Tutor updatedTutor = tutorRepository.save(tutor);

        // Send approval notification email
        emailService.sendEmail(
                tutor.getEmail(),
                "Chúc mừng! Hồ sơ giảng viên của bạn đã được phê duyệt",
                "Xin chào " + tutor.getFullName() + ",\n\n" +
                        "Chúng tôi rất vui thông báo rằng hồ sơ đăng ký trở thành giảng viên trên nền tảng Japanese Learning của bạn đã được phê duyệt!\n\n" +
                        "Bây giờ, bạn có thể đăng nhập vào tài khoản của mình để bắt đầu tạo khóa học, bài học và bài tập nhằm chia sẻ kiến thức với các học viên.\n\n" +
                        "Đăng nhập tại: " + frontendUrl + "/login\n\n" +
                        "Nếu bạn có bất kỳ câu hỏi hoặc cần hỗ trợ, vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Japanese Learning"
        );

        return mapTutorToResponse(updatedTutor);
    }

    @Override
    @Transactional
    public UserResponse rejectTutor(Long tutorId, String reason) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        // Chúng tôi không thực sự xóa tài khoản, chỉ giữ ở trạng thái bị vô hiệu hóa
        // Có thể thêm cờ "bị từ chối" trong triển khai thực tế

        // Soạn nội dung email từ chối
        String rejectionMessage = "Xin chào " + tutor.getFullName() + ",\n\n" +
                "Cảm ơn bạn đã quan tâm và đăng ký trở thành giảng viên trên nền tảng Japanese Learning.\n\n" +
                "Sau khi xem xét cẩn thận hồ sơ của bạn, chúng tôi rất tiếc phải thông báo rằng hiện tại chúng tôi không thể phê duyệt tài khoản giảng viên của bạn.";

        if (reason != null && !reason.isEmpty()) {
            rejectionMessage += "\n\nLý do: " + reason;
        }

        rejectionMessage += "\n\nNếu bạn cho rằng đây là một sự nhầm lẫn hoặc muốn bổ sung thêm thông tin để được xem xét lại, " +
                "vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.\n\n" +
                "Bạn vẫn có thể đăng nhập vào tài khoản của mình tại: " + frontendUrl + "/login\n\n" +
                "Chúng tôi rất trân trọng sự quan tâm của bạn.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Japanese Learning";

        emailService.sendEmail(
                tutor.getEmail(),
                "Cập nhật về hồ sơ gia sư của bạn",
                rejectionMessage
        );

        return mapTutorToResponse(tutor);
    }


    @Override
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        // Search by email or name, case-insensitive
        Page<User> users = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                query, query, pageable);

        return users.map(user -> {
            if (isStudent(user)) {
                Student student = studentRepository.findById(user.getId()).orElse(null);
                if (student != null) {
                    return mapStudentToResponse(student);
                }
            } else if (isTutor(user)) {
                Tutor tutor = tutorRepository.findById(user.getId()).orElse(null);
                if (tutor != null) {
                    return mapTutorToResponse(tutor);
                }
            }

            // Fallback to basic user info
            return mapUserToBasicResponse(user);
        });
    }

    // Helper methods

    private boolean isStudent(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.ERole.ROLE_STUDENT);
    }

    private boolean isTutor(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.ERole.ROLE_TUTOR);
    }

    private UserResponse mapStudentToResponse(Student student) {
        // Convert user roles to strings
        Set<String> roles = student.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .avatarUrl(student.getAvatarUrl())
                .roles(roles)
                .userType("STUDENT")
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    private UserResponse mapTutorToResponse(Tutor tutor) {
        // Convert user roles to strings
        Set<String> roles = tutor.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        // Map educations
        List<EducationResponse> educationResponses = null;
        if (tutor.getEducations() != null && !tutor.getEducations().isEmpty()) {
            educationResponses = tutor.getEducations().stream()
                    .map(userMapper::educationToEducationResponse)
                    .collect(Collectors.toList());
        }

        // Map experiences
        List<ExperienceResponse> experienceResponses = null;
        if (tutor.getExperiences() != null && !tutor.getExperiences().isEmpty()) {
            experienceResponses = tutor.getExperiences().stream()
                    .map(userMapper::experienceToExperienceResponse)
                    .collect(Collectors.toList());
        }

        return UserResponse.builder()
                .id(tutor.getId())
                .fullName(tutor.getFullName())
                .email(tutor.getEmail())
                .phoneNumber(tutor.getPhoneNumber())
                .avatarUrl(tutor.getAvatarUrl())
                .roles(roles)
                .userType("TUTOR")
                .teachingRequirements(tutor.getTeachingRequirements())
                .educations(educationResponses)
                .experiences(experienceResponses)
                .certificateUrls(tutor.getCertificateUrls())
                .createdAt(tutor.getCreatedAt())
                .updatedAt(tutor.getUpdatedAt())
                .build();
    }

    private UserResponse mapUserToBasicResponse(User user) {
        // Convert user roles to strings
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        // Determine user type
        String userType = "GUEST";
        if (roles.contains(Role.ERole.ROLE_ADMIN.name())) {
            userType = "ADMIN";
        } else if (roles.contains(Role.ERole.ROLE_TUTOR.name())) {
            userType = "TUTOR";
        } else if (roles.contains(Role.ERole.ROLE_STUDENT.name())) {
            userType = "STUDENT";
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .userType(userType)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}