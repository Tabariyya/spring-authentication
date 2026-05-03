package com.tabariyya.authentication.services;

import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.resetpassword.ResetPasswordRequest;
import com.tabariyya.authentication.dto.login.LoginRequest;
import com.tabariyya.authentication.dto.login.LoginResponse;
import com.tabariyya.authentication.dto.register.RegisterResponse;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

public class LdapAuthService<T extends BaseUser> extends BaseAuthService<T> {

    private final BaseLdapRepository<T> ldapRepository;

    public LdapAuthService(BaseUserRepository<T> userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtConsumer jwtConsumer,
                           JwtProducer jwtProducer,
                           String accessTokenExpiry,
                           String refreshTokenExpiry,
                           BaseLdapRepository<T> ldapRepository) {
        super(userRepository, passwordEncoder, jwtConsumer, jwtProducer, accessTokenExpiry, refreshTokenExpiry);
        this.ldapRepository = ldapRepository;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        T user = ldapRepository.authenticate(request.userName(), request.password());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<T> dbUser = userRepository.findByUserName(request.userName());
        if (!dbUser.isPresent()) {
            user.setCreatedAt(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        } else {
            user.setId(dbUser.get().getId());
        }

        return ResponseEntity.ok(new LoginResponse(generateRefreshToken(user), generateAccessToken(user)));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(T user, String otp) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<Void> sendOtp(String recipient, String channel) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }


    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

}
