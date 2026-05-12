package com.tabariyya.authentication.services;

import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.resetpassword.ResetPasswordRequest;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.authentication.otp.OtpSender;
import com.tabariyya.authentication.dto.login.LoginRequest;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalAuthServiceTest {

    // ── minimal BaseUser implementation for tests ────────────────────────────

    static class TestUser implements BaseUser {
        private int id;
        private String userName;
        private String password;
        private String email;
        private LocalDateTime createdAt;

        TestUser(int id, String userName, String password, String email) {
            this.id = id;
            this.userName = userName;
            this.password = password;
            this.email = email;
        }

        public Object getId()                       { return id; }
        public void setId(Object id)               { this.id = (Integer) id; }
        public String getUserName()                 { return userName; }
        public void setUserName(String u)           { this.userName = u; }
        public String getPassword()                 { return password; }
        public void setPassword(String p)           { this.password = p; }
        public String getEmail()                    { return email; }
        public void setEmail(String e)              { this.email = e; }
        public LocalDateTime getCreatedAt()         { return createdAt; }
        public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
    }

    // ── mocks ────────────────────────────────────────────────────────────────

    @Mock BaseUserRepository<TestUser> userRepository;
    @Mock PasswordEncoder              passwordEncoder;
    @Mock JwtConsumer                  jwtConsumer;
    @Mock JwtProducer                  jwtProducer;
    @Mock
    OtpService otpService;
    @Mock OtpSender                    emailSender;
    @Mock OtpSender                    smsSender;

    LocalAuthService<TestUser> service;

    @BeforeEach
    void setUp() {
        when(emailSender.channel()).thenReturn("email");
        when(smsSender.channel()).thenReturn("sms");
        service = new LocalAuthService<>(
                userRepository, passwordEncoder, jwtConsumer, jwtProducer,
                "PT15M", "P7D",
                otpService, Arrays.asList(emailSender, smsSender)
        );
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_validOtp_returns201WithTokens() {
        TestUser user = new TestUser(0, "Alice", "raw", "alice@example.com");
        when(otpService.verifyCode("alice@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("raw")).thenReturn("hashed");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), any(TokenType.class))).thenReturn("tok");

        ResponseEntity<?> response = service.register(user, "123456");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository).save(user);
        assertEquals("hashed", user.getPassword());
    }

    @Test
    void register_invalidOtp_returns401() {
        TestUser user = new TestUser(0, "Alice", "raw", "alice@example.com");
        when(otpService.verifyCode("alice@example.com", "000000")).thenReturn(false);

        ResponseEntity<?> response = service.register(user, "000000");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_normalisesEmailAndUsername() {
        TestUser user = new TestUser(0, "Alice", "raw", "Alice@Example.COM");
        when(otpService.verifyCode("alice@example.com", "111111")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), any(TokenType.class))).thenReturn("tok");

        service.register(user, "111111");

        assertEquals("alice", user.getUserName());
        assertEquals("alice@example.com", user.getEmail());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() {
        TestUser user = new TestUser(1, "alice", "hashed", "alice@example.com");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), any(TokenType.class))).thenReturn("tok");

        ResponseEntity<?> response = service.login(new LoginRequest("alice", "raw"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_wrongPassword_returns401() {
        TestUser user = new TestUser(1, "alice", "hashed", "alice@example.com");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ResponseEntity<?> response = service.login(new LoginRequest("alice", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_unknownUser_returns401() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.login(new LoginRequest("ghost", "any"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ── sendOtp ───────────────────────────────────────────────────────────────

    @Test
    void sendOtp_emailChannel_generatesAndSends() {
        when(otpService.generateCode("alice@example.com")).thenReturn("482931");

        ResponseEntity<Void> response = service.sendOtp("alice@example.com", "email");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailSender).send("alice@example.com", "482931");
        verify(smsSender, never()).send(any(), any());
    }

    @Test
    void sendOtp_smsChannel_routesToSmsSender() {
        when(otpService.generateCode("+9725xxxxxxx")).thenReturn("111222");

        service.sendOtp("+9725xxxxxxx", "sms");

        verify(smsSender).send("+9725xxxxxxx", "111222");
        verify(emailSender, never()).send(any(), any());
    }

    @Test
    void sendOtp_unknownChannel_returns400() {
        ResponseEntity<Void> response = service.sendOtp("alice@example.com", "pigeon");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(otpService, never()).generateCode(any());
    }

    // ── forgotPassword ────────────────────────────────────────────────────────

    @Test
    void forgotPassword_existingUser_sendsOtpViaRequestedChannel() throws InterruptedException {
        TestUser user = new TestUser(1, "alice", "hashed", "alice@example.com");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(otpService.generateCode("alice@example.com")).thenReturn("382910");

        ResponseEntity<?> response = service.forgotPassword(new ForgotPasswordRequest("alice"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailSender).send("alice@example.com", "382910");
    }

    @Test
    void forgotPassword_unknownUser_returns200WithoutSendingOtp() throws InterruptedException {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.forgotPassword(new ForgotPasswordRequest("ghost"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailSender, never()).send(any(), any());
        verify(otpService, never()).generateCode(any());
    }


    @Test
    void resetPassword_validOtp_updatesPasswordAndReturns200() {
        TestUser user = new TestUser(1, "alice", "old-hash", "alice@example.com");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(otpService.verifyCode("alice@example.com", "382910")).thenReturn(true);
        when(passwordEncoder.encode("New$ecret1")).thenReturn("new-hash");

        ResponseEntity<?> response = service.resetPassword(
                new ResetPasswordRequest("alice", "382910", "New$ecret1"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_invalidOtp_returns401() {
        TestUser user = new TestUser(1, "alice", "old-hash", "alice@example.com");
        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(otpService.verifyCode("alice@example.com", "000000")).thenReturn(false);

        ResponseEntity<?> response = service.resetPassword(
                new ResetPasswordRequest("alice", "000000", "New$ecret1"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_unknownUser_returns401() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.resetPassword(
                new ResetPasswordRequest("ghost", "382910", "New$ecret1"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(otpService, never()).verifyCode(any(), any());
    }
}
