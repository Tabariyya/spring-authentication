package com.tabariyya.authentication.services;

import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.LoginRequest;
import com.tabariyya.authentication.dto.TokenResponse;
import com.tabariyya.authentication.dto.renewal.RenewRequest;
import com.tabariyya.authentication.dto.renewal.RenewResponse;
import com.tabariyya.authentication.dto.resetpassword.ResetPasswordRequest;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.authentication.token.TokenIssuer;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseAuthService<T extends BaseUser<ID>, ID> {

    protected final BaseUserRepository<T, ID> userRepository;
    protected final PasswordEncoder passwordEncoder;
    protected final JwtConsumer jwtConsumer;
    protected final JwtProducer jwtProducer;
    protected final Function<String, ID> idParser;

    protected final Duration refreshTokenExpiry;
    protected final Duration accessTokenExpiry;

    protected final TokenIssuer<T, ID> tokenIssuer;

    protected BaseAuthService(BaseUserRepository<T, ID> userRepository,
                              PasswordEncoder passwordEncoder,
                              JwtConsumer jwtConsumer,
                              JwtProducer jwtProducer,
                              String accessTokenExpiry,
                              String refreshTokenExpiry,
                              Function<String, ID> idParser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConsumer = jwtConsumer;
        this.jwtProducer = jwtProducer;
        this.accessTokenExpiry = Duration.parse(accessTokenExpiry);
        this.refreshTokenExpiry = Duration.parse(refreshTokenExpiry);
        this.idParser = idParser;
        this.tokenIssuer = new TokenIssuer<>(jwtProducer, accessTokenExpiry, refreshTokenExpiry);
    }

    public abstract ResponseEntity<TokenResponse> register(T user, String otp);

    public abstract ResponseEntity<TokenResponse> login(LoginRequest request);

    public abstract ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) throws InterruptedException;

    public abstract ResponseEntity<?> resetPassword(ResetPasswordRequest request);

    public abstract ResponseEntity<Void> sendOtp(String recipient, String channel);

    public ResponseEntity<RenewResponse> renew(RenewRequest request) {
        boolean isRefreshTokenValid = jwtConsumer.verifyToken(request.refreshToken(), TokenType.REFRESH);
        if (!isRefreshTokenValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rawId = jwtConsumer.extractClaims(request.refreshToken()).payload().get(Claims.SUBJECT).getAsString();
        ID userId = idParser.apply(rawId);

        T user = userRepository.findById(userId).orElseThrow(
                (Supplier<ResponseStatusException>) () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        );

        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = generateAccessToken(user);
        return ResponseEntity.ok(new RenewResponse(accessToken));
    }

    protected String generateRefreshToken(T user) {
        return tokenIssuer.generateRefreshToken(user);
    }

    protected String generateAccessToken(T user) {
        return tokenIssuer.generateAccessToken(user);
    }

    public Optional<T> getUser(ID id) {
        return userRepository.findById(id);
    }

}
