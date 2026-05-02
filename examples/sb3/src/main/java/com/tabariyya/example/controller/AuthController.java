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
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
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
    private final JwtConsumer jwtConsumer;

    public AuthController(LocalAuthService<User> authService, JwtConsumer jwtConsumer) {
        this.authService = authService;
        this.jwtConsumer = jwtConsumer;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody User user) {
        return authService.register(user);
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

    /**
     * Called when the user clicks the reset-password link from their email.
     * The link contains the signed JWT token (type TWO_FA) that identifies the user.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestBody ResetPasswordRequest request) {
        if (!jwtConsumer.verifyToken(token, TokenType.TWO_FA)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int userId = jwtConsumer.extractClaims(token).payload().get(Claims.SUBJECT).getAsInt();
        return authService.resetPassword(request.newPassword(), userId);
    }

    /**
     * Sends an invitation email with a signed link so the user can prove they own the address.
     */
    @PostMapping("/email-auth")
    public ResponseEntity<Void> emailAuthentication(@RequestParam String email) {
        return authService.emailAuthentication(email);
    }
}
