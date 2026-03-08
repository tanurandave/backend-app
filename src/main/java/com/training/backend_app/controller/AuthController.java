package com.training.backend_app.controller;

import com.training.backend_app.dto.AuthResponse;
import com.training.backend_app.dto.LoginRequest;
import com.training.backend_app.dto.OtpRequest;
import com.training.backend_app.dto.OtpResponse;
import com.training.backend_app.dto.OtpVerifyRequest;
import com.training.backend_app.dto.PasswordResetRequest;
import com.training.backend_app.dto.PasswordResetResponse;
import com.training.backend_app.dto.RegisterRequest;
import com.training.backend_app.dto.UserResponse;
import com.training.backend_app.service.AuthService;
import com.training.backend_app.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.debug("Login attempt for email={}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    /**
     * Step 1: Send OTP to the provided email address.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        logger.info("OTP send request for email={}", request.getEmail());
        return ResponseEntity.ok(otpService.sendOtp(request));
    }

    /**
     * Step 2: Verify the OTP entered by the user.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        logger.info("OTP verify request for email={}", request.getEmail());
        return ResponseEntity.ok(otpService.verifyOtp(request));
    }

    /**
     * Step 3: Reset the password (only allowed after OTP is verified).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
