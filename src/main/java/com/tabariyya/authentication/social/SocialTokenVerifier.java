package com.tabariyya.authentication.social;

public interface SocialTokenVerifier {

    SocialProvider provider();

    SocialUserInfo verify(String idToken);
}
