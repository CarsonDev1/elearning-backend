package com.jplearning.service;

import com.jplearning.dto.request.LoginRequest;
import com.jplearning.dto.request.RegisterStudentRequest;
import com.jplearning.dto.request.RegisterTutorRequest;
import com.jplearning.dto.response.JwtResponse;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.dto.response.UserResponse;

public interface AuthService {
    /**
     * Authenticates a user and returns JWT token along with user information
     * @param loginRequest login credentials
     * @return JWT token and user details
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);

    /**
     * Registers a new student
     * @param registerRequest student registration data
     * @return registration status message
     */
    MessageResponse registerStudent(RegisterStudentRequest registerRequest);

    /**
     * Registers a new tutor
     * @param registerRequest tutor registration data
     * @return registration status message
     */
    MessageResponse registerTutor(RegisterTutorRequest registerRequest);
}