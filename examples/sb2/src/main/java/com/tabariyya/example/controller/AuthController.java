package com.tabariyya.example.controller;

import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.login.LoginRequest;
import com.tabariyya.authentication.dto.login.LoginResponse;
import com.tabariyya.authentication.dto.register.RegisterResponse;
import com.tabariyya.authentication.dto.renewal.RenewRequest;
import com.tabariyya.authentication.dto.renewal.RenewResponse;
import com.tabariyya.authentication.dto.resetpassword.ResetPasswordRequest;
import com.tabariyya.authentication.services.LocalAuthService;
import com.tabariyya.example.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LocalAuthService<User> authService;

    public AuthController(LocalAuthService<User> authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody User user, @RequestParam String otp) {
        return authService.register(user, otp);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/renew")
    public ResponseEntity<RenewResponse> renew(@RequestBody RenewRequest request) {
        return authService.renew(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) throws InterruptedException {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@RequestParam String recipient, @RequestParam String channel) {
        return authService.sendOtp(recipient, channel);
    }
}
