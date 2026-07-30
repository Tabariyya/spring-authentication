package com.tabariyya.authentication.token;

import com.tabariyya.authentication.dto.TokenResponse;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.authentication.models.TokenSubject;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TokenIssuer<U extends TokenSubject<ID>, ID> {

    private final JwtProducer jwtProducer;
    private final Duration accessTokenExpiry;
    private final Duration refreshTokenExpiry;
    private final Function<U, Map<String, Object>> accessTokenClaims;

    public TokenIssuer(JwtProducer jwtProducer, String accessTokenExpiry, String refreshTokenExpiry) {
        this(jwtProducer, accessTokenExpiry, refreshTokenExpiry, TokenIssuer::defaultAccessTokenClaims);
    }

    public TokenIssuer(
            JwtProducer jwtProducer,
            String accessTokenExpiry,
            String refreshTokenExpiry,
            Function<U, Map<String, Object>> accessTokenClaims) {
        this.jwtProducer = jwtProducer;
        this.accessTokenExpiry = Duration.parse(accessTokenExpiry);
        this.refreshTokenExpiry = Duration.parse(refreshTokenExpiry);
        this.accessTokenClaims = accessTokenClaims;
    }

    public TokenResponse issueTokens(U user) {
        return new TokenResponse(generateRefreshToken(user), generateAccessToken(user));
    }

    public String generateAccessToken(U user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));
        claims.putAll(accessTokenClaims.apply(user));

        return jwtProducer.generateToken(claims, accessTokenExpiry, TokenType.ACCESS);
    }

    public String generateRefreshToken(U user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, String.valueOf(user.getId()));

        return jwtProducer.generateToken(claims, refreshTokenExpiry, TokenType.REFRESH);
    }

    private static <U> Map<String, Object> defaultAccessTokenClaims(U user) {
        if (user instanceof BaseUser<?> baseUser && baseUser.getUserName() != null) {
            return Map.of("username", baseUser.getUserName());
        }

        return Map.of();
    }
}
