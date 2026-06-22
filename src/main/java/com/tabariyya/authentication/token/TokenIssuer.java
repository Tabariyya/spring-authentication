package com.tabariyya.authentication.token;

import com.tabariyya.authentication.dto.TokenResponse;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TokenIssuer<U extends BaseUser<ID>, ID> {

    private final JwtProducer jwtProducer;
    private final Duration accessTokenExpiry;
    private final Duration refreshTokenExpiry;

    public TokenIssuer(JwtProducer jwtProducer, String accessTokenExpiry, String refreshTokenExpiry) {
        this.jwtProducer = jwtProducer;
        this.accessTokenExpiry = Duration.parse(accessTokenExpiry);
        this.refreshTokenExpiry = Duration.parse(refreshTokenExpiry);
    }

    public TokenResponse issueTokens(U user) {
        return new TokenResponse(generateRefreshToken(user), generateAccessToken(user));
    }

    public String generateAccessToken(U user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));
        claims.put("username", user.getUserName());

        return jwtProducer.generateToken(
                claims,
                accessTokenExpiry,
                TokenType.ACCESS
        );
    }

    public String generateRefreshToken(U user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));

        return jwtProducer.generateToken(
                claims,
                refreshTokenExpiry,
                TokenType.REFRESH
        );
    }
}
