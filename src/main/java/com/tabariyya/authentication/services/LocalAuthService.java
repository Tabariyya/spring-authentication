package com.tabariyya.authentication.services;

import com.tabariyya.authentication.config.MailTemplateConfig;
import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.login.LoginRequest;
import com.tabariyya.authentication.dto.login.LoginResponse;
import com.tabariyya.authentication.dto.register.RegisterResponse;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import com.tabariyya.utils.mail.MailUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LocalAuthService<T extends BaseUser> extends BaseAuthService<T> {

    private final Duration emailTokenExpiry;
    private final MailUtils mailUtils;
    private final Duration authenticationTokenExpiry;

    @Value("${app.frontend-authorization-baseurl}")
    private String authorizationBaseUrl;

    @Value("${app.frontend-reset-baseurl}")
    private String resetBaseUrl;

    public LocalAuthService(BaseUserRepository<T> userRepository, PasswordEncoder passwordEncoder, JwtConsumer jwtConsumer, JwtProducer jwtProducer, String accessTokenExpiry, String refreshTokenExpiry, String emailTokenExpiry, MailUtils mailUtils, String authenticationTokenExpiry) {
        super(userRepository, passwordEncoder, jwtConsumer, jwtProducer, accessTokenExpiry, refreshTokenExpiry);
        this.authenticationTokenExpiry = Duration.parse(authenticationTokenExpiry);
        this.emailTokenExpiry = Duration.parse(emailTokenExpiry);
        this.mailUtils = mailUtils;
    }

    @Override
    public ResponseEntity<RegisterResponse> register(T user) {
        user.setUserName(user.getUserName().toLowerCase());
        user.setEmail(user.getEmail().toLowerCase());
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        String refreshToken = generateRefreshToken(user);
        String accessToken = generateAccessToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(refreshToken, accessToken));
    }


    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        T user = userRepository.findByUserName(request.userName().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String refreshToken = generateRefreshToken(user);
        String accessToken = generateAccessToken(user);
        return ResponseEntity.ok(new LoginResponse(refreshToken, accessToken));
    }

    @Override
    public ResponseEntity<Void> emailAuthentication(String email) {
        String userEmail = email.toLowerCase();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEmail);
        String authenticationToken = jwtProducer.generateToken(claims, authenticationTokenExpiry, TokenType.MAIL_CHECK);

        String template = MailTemplateConfig.getInvitation();

        String resetLink = authorizationBaseUrl + "?token=" + authenticationToken;
        String body = template.replace("${token}", resetLink);

        mailUtils.sendEmail(Collections.singletonList(email), new ArrayList<String>(), new ArrayList<String>(), "Mail Authentication", body);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) throws InterruptedException {
        T user = userRepository.findByUserName(request.userName().toLowerCase()).orElse(null);

        if (user != null) {
            Map<String, Object> claims = new HashMap<>();
            claims.put(Claims.SUBJECT, String.valueOf(user.getId()));
            String emailVerificationToken = jwtProducer.generateToken(claims, emailTokenExpiry, TokenType.TWO_FA);

            String template = MailTemplateConfig.getForgotPassword();

            String resetLink = resetBaseUrl + "?token=" + emailVerificationToken;
            String body = template.replace("${token}", resetLink);

            mailUtils.sendEmail(Collections.singletonList(user.getEmail()), new ArrayList<String>(), new ArrayList<String>(), "Verification", body);

        } else {
            // this simulates us sending the mail in execution time
            // so client will not know if the user exists or not
            Thread.sleep(ThreadLocalRandom.current().nextLong(1406, 1852));
        }
        return ResponseEntity.ok().build();
    }


    @Override
    public ResponseEntity<?> resetPassword(String newPassword, int userId) {
        T user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

}
