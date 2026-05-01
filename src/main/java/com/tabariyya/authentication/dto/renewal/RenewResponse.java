package com.tabariyya.authentication.dto.renewal;

public class RenewResponse {

    private final String accessToken;

    public RenewResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String accessToken() {
        return accessToken;
    }

}
