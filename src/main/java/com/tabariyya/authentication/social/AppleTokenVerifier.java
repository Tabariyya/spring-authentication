package com.tabariyya.authentication.social;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Set;

public class AppleTokenVerifier implements SocialTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public AppleTokenVerifier(String clientId) {
        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(APPLE_JWKS_URL));

            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(keySelector);

            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(clientId, null, Set.of("sub")));

            this.jwtProcessor = processor;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Apple verifier could not be initialized", e);
        }
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.APPLE;
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        JWTClaimsSet claims;

        try {
            claims = jwtProcessor.process(idToken, null);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token could not be verified", e);
        }

        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token issuer is different than expected");
        }

        try {
            String sub = claims.getSubject();
            String email = claims.getStringClaim("email");
            Boolean emailVerified = parseAppleBoolean(claims.getClaim("email_verified"));

            return new SocialUserInfo(sub, email, Boolean.TRUE.equals(emailVerified));
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple token claims could not be read", e);
        }
    }

    private Boolean parseAppleBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}
