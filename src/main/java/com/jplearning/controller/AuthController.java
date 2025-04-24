package com.jplearning.controller;

import com.jplearning.dto.request.LoginRequest;
import com.jplearning.dto.request.RegisterStudentRequest;
import com.jplearning.dto.request.RegisterTutorRequest;
import com.jplearning.dto.response.JwtResponse;
import com.jplearning.dto.response.MessageResponse;
import com.jplearning.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication API")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and generate JWT token")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register student", description = "Register new student account")
    public ResponseEntity<MessageResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest registerRequest) {
        return ResponseEntity.ok(authService.registerStudent(registerRequest));
    }

    @PostMapping("/register/tutor")
    @Operation(summary = "Register tutor", description = "Register new tutor account")
    public ResponseEntity<MessageResponse> registerTutor(@Valid @RequestBody RegisterTutorRequest registerRequest) {
        return ResponseEntity.ok(authService.registerTutor(registerRequest));
    }
}