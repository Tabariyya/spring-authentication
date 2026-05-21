package com.tabariyya.authentication.services;

import com.tabariyya.authentication.dto.forgotpassword.ForgotPasswordRequest;
import com.tabariyya.authentication.dto.LoginRequest;
import com.tabariyya.authentication.dto.TokenResponse;
import com.tabariyya.authentication.dto.resetpassword.ResetPasswordRequest;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.authentication.otp.OtpSender;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class LocalAuthService<T extends BaseUser<ID>, ID> extends BaseAuthService<T, ID> {

    private final OtpService otpService;
    private final Map<String, OtpSender> senders;

    public LocalAuthService(BaseUserRepository<T, ID> userRepository, PasswordEncoder passwordEncoder, JwtConsumer jwtConsumer, JwtProducer jwtProducer, String accessTokenExpiry, String refreshTokenExpiry, OtpService otpService, List<OtpSender> otpSenders, Function<String, ID> idParser) {
        super(userRepository, passwordEncoder, jwtConsumer, jwtProducer, accessTokenExpiry, refreshTokenExpiry, idParser);
        this.otpService = otpService;
        this.senders = new HashMap<>();
        for (OtpSender sender : otpSenders) {
            this.senders.put(sender.channel(), sender);
        }
    }

    @Override
    public ResponseEntity<TokenResponse> register(T user, String otp) {
        if (!otpService.verifyCode(user.getContactInfo().toLowerCase(), otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        user.setUserName(user.getUserName().toLowerCase());
        user.setContactInfo(user.getContactInfo().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.status(201).body(new TokenResponse(generateRefreshToken(user), generateAccessToken(user)));
    }

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest request) {
        T user = userRepository.findByIdentifier(request.identifier().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new TokenResponse(generateRefreshToken(user), generateAccessToken(user)));
    }

    @Override
    public ResponseEntity<Void> sendOtp(String recipient, String channel) {
        OtpSender sender = senders.get(channel);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String code = otpService.generateCode(recipient.toLowerCase());
        sender.send(recipient, code);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) throws InterruptedException {
        T user = userRepository.findByUserName(request.identifier().toLowerCase()).orElse(null);

        if (user != null) {
            OtpSender sender = senders.get(user.getContactInfoType());
            if (sender == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            String recipient = user.getContactInfo();
            String code = otpService.generateCode(recipient);
            sender.send(recipient, code);
        } else {
            // simulates send time so the caller cannot determine if the user exists
            Thread.sleep(ThreadLocalRandom.current().nextLong(1406, 1852));
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        T user = userRepository.findByUserName(request.identifier().toLowerCase()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!otpService.verifyCode(user.getContactInfo(), request.otp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
