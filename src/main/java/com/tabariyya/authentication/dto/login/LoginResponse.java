package com.tabariyya.authentication.dto.login;

public class LoginResponse {

    private final String refreshToken;
    private final String accessToken;

    public LoginResponse(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public String accessToken() {
        return accessToken;
    }

}
