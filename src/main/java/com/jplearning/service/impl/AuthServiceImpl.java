package com.jplearning.service.impl;

import com.jplearning.dto.request.LoginRequest;
import com.jplearning.dto.request.RegisterStudentRequest;
import com.jplearning.dto.request.RegisterTutorRequest;
import com.jplearning.dto.response.JwtResponse;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.UserResponse;
import com.jplearning.entity.Role;
import com.jplearning.entity.Student;
import com.jplearning.entity.Tutor;
import com.jplearning.exception.BadRequestException;
import com.jplearning.mapper.UserMapper;
import com.jplearning.repository.RoleRepository;
import com.jplearning.repository.StudentRepository;
import com.jplearning.repository.TutorRepository;
import com.jplearning.repository.UserRepository;
import com.jplearning.security.jwt.JwtUtils;
import com.jplearning.security.services.UserDetailsImpl;
import com.jplearning.service.AuthService;
import com.jplearning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
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
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

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

        // Enable the account (in a real production scenario, you might want to add email verification)
        student.setEnabled(true);

        studentRepository.save(student);

        return new MessageResponse("Student registered successfully!");
    }

    @Override
    @Transactional
    public MessageResponse registerTutor(RegisterTutorRequest registerRequest) {
        // Validate if the email is already in use
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Validate if the phone number is already in use
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new BadRequestException("Error: Phone number is already in use!");
        }

        // Validate if passwords match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException("Error: Passwords do not match!");
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

        // The account will be disabled until approved by admin
        tutor.setEnabled(false);

        tutorRepository.save(tutor);

        return new MessageResponse("Tutor registered successfully! Your account will be reviewed by an administrator.");
    }
}