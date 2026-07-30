package com.tabariyya.authentication.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleTokenVerifier implements SocialTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(List<String> clientIds) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(clientIds)
                .build();
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        GoogleIdToken token;

        try {
            token = verifier.verify(idToken);
        } catch (IOException | GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token not verified", e);
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token is invalid or expired");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        return new SocialUserInfo(
                payload.getSubject(), payload.getEmail(), Boolean.TRUE.equals(payload.getEmailVerified()));
    }
}
