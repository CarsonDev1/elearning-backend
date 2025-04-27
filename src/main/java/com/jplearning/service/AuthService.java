package com.jplearning.service;

import com.jplearning.dto.request.*;
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

    /**
     * Initiates the password reset process
     * @param request contains the email address
     * @return message response
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets the password using a token
     * @param request contains token and new password
     * @return message response
     */
    MessageResponse resetPassword(ResetPasswordRequest request);

    /**
     * Changes the password for authenticated user
     * @param request contains current and new password
     * @param userId ID of the user changing password
     * @return message response
     */
    MessageResponse changePassword(ChangePasswordRequest request, Long userId);

    /**
     * Verifies user email with a token
     * @param token verification token
     * @return message response
     */
    MessageResponse verifyEmail(String token);
}