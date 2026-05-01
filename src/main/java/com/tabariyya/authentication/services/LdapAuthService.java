package com.tabariyya.authentication.services;

import com.waleed.springutils.auth.dto.forgotpassword.ForgotPasswordRequest;
import com.waleed.springutils.auth.dto.login.LoginRequest;
import com.waleed.springutils.auth.dto.login.LoginResponse;
import com.waleed.springutils.auth.dto.register.RegisterResponse;
import com.waleed.springutils.auth.models.BaseUser;
import com.waleed.utils.jwt.JwtConsumer;
import com.waleed.utils.jwt.JwtProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

public class LdapAuthService<T extends BaseUser> extends BaseAuthService<T> {

    private final BaseLdapRepository<T> ldapRepository;

    public LdapAuthService(BaseUserRepository<T>  userRepository,
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
        if (dbUser.isEmpty()) {
            user.setCreatedAt(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        } else {
            user.setId(dbUser.get().getId());
        }

        return ResponseEntity.ok(new LoginResponse(generateRefreshToken(user), generateAccessToken(user)));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(T user) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<Void> emailAuthentication(String email){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();

    }


    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<?> resetPassword(String newPassword, int userId) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }


}