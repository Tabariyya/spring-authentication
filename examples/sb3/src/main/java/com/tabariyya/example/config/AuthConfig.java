package com.tabariyya.example.config;

import com.tabariyya.authentication.services.LocalAuthService;
import com.tabariyya.example.entity.User;
import com.tabariyya.example.repository.UserRepositoryAdapter;
import com.tabariyya.utils.jwt.JwtConsumer;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.mail.MailUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    // These three beans must be provided by the com.tabariyya:utils library
    // (either via its auto-configuration or manually defined by the application).
    private final JwtConsumer jwtConsumer;
    private final JwtProducer jwtProducer;
    private final MailUtils mailUtils;
    private final UserRepositoryAdapter userRepository;

    @Value("${auth.access-token-expiry:PT15M}")
    private String accessTokenExpiry;

    @Value("${auth.refresh-token-expiry:P7D}")
    private String refreshTokenExpiry;

    @Value("${auth.email-token-expiry:PT1H}")
    private String emailTokenExpiry;

    @Value("${auth.authentication-token-expiry:PT10M}")
    private String authenticationTokenExpiry;

    public AuthConfig(JwtConsumer jwtConsumer,
                      JwtProducer jwtProducer,
                      MailUtils mailUtils,
                      UserRepositoryAdapter userRepository) {
        this.jwtConsumer = jwtConsumer;
        this.jwtProducer = jwtProducer;
        this.mailUtils = mailUtils;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LocalAuthService<User> localAuthService() {
        return new LocalAuthService<>(
                userRepository,
                passwordEncoder(),
                jwtConsumer,
                jwtProducer,
                accessTokenExpiry,
                refreshTokenExpiry,
                emailTokenExpiry,
                mailUtils,
                authenticationTokenExpiry
        );
    }
}
