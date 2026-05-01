package com.tabariyya.authentication.dto.register;

public class RegisterResponse {

    private final String refreshToken;
    private final String accessToken;

    public RegisterResponse(String refreshToken, String accessToken) {
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
