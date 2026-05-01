package com.tabariyya.authentication.services;

import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class BaseAuthService<T extends BaseUser> {

    protected final BaseUserRepository<T> userRepository;
    protected final PasswordEncoder passwordEncoder;
    protected final JwtConsumer jwtConsumer;
    protected final JwtProducer jwtProducer;

    protected final Duration refreshTokenExpiry;
    protected final Duration accessTokenExpiry;

    protected BaseAuthService(BaseUserRepository<T> userRepository,
                              PasswordEncoder passwordEncoder,
                              JwtConsumer jwtConsumer,
                              JwtProducer jwtProducer,
                              String accessTokenExpiry,
                              String refreshTokenExpiry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConsumer = jwtConsumer;
        this.jwtProducer = jwtProducer;
        this.accessTokenExpiry = Duration.parse(accessTokenExpiry);
        this.refreshTokenExpiry = Duration.parse(refreshTokenExpiry);
    }

    public abstract ResponseEntity<RegisterResponse> register(T user);

    public abstract ResponseEntity<LoginResponse> login(LoginRequest request);

    public abstract ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) throws MessagingException, InterruptedException;

    public abstract ResponseEntity<?> resetPassword(String newPassword, int userId);

    public abstract ResponseEntity<Void> emailAuthentication(String email);

    public ResponseEntity<RenewResponse> renew(RenewRequest request) {
        boolean isRefreshTokenValid = jwtConsumer.verifyToken(request.refreshToken(), TokenType.REFRESH);
        if (!isRefreshTokenValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int userId = jwtConsumer.extractClaims(request.refreshToken()).payload().get(Claims.SUBJECT).getAsInt();

        T user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String accessToken = generateAccessToken(user);
        return ResponseEntity.ok(new RenewResponse(accessToken));
    }

    protected String generateRefreshToken(T user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));

        return jwtProducer.generateToken(
                claims,
                refreshTokenExpiry,
                TokenType.REFRESH
        );
    }

    protected String generateAccessToken(T user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));
        claims.put("username", user.getUserName());

        return jwtProducer.generateToken(
                claims,
                accessTokenExpiry,
                TokenType.ACCESS
        );
    }

    public Optional<T> getUser(int id) {
        return userRepository.findById(id);
    }

}

