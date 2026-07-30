package com.tabariyya.authentication.social;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SocialTokenVerifierResolver {

    private final Map<SocialProvider, SocialTokenVerifier> verifiers;

    public SocialTokenVerifierResolver(List<SocialTokenVerifier> verifierList) {
        this.verifiers =
                verifierList.stream().collect(Collectors.toMap(SocialTokenVerifier::provider, Function.identity()));
    }

    public SocialTokenVerifier resolve(SocialProvider provider) {
        SocialTokenVerifier verifier = verifiers.get(provider);

        if (verifier == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported social provider: " + provider);
        }

        return verifier;
    }
}
