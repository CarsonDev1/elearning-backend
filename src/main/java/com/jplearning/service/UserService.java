package com.jplearning.service;

import com.jplearning.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    /**
     * Get current authenticated user details
     * @return User details of the authenticated user
     */
    UserResponse getCurrentUser();

    /**
     * Update user avatar
     * @param userId ID of the user
     * @param file Avatar image file
     * @return Updated user details
     * @throws IOException If an I/O error occurs
     */
    UserResponse updateAvatar(Long userId, MultipartFile file) throws IOException;
}