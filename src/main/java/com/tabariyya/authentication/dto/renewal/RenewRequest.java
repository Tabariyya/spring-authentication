package com.tabariyya.authentication.dto.renewal;

import javax.validation.constraints.NotBlank;

public class RenewRequest {

    @NotBlank
    private final String refreshToken;

    public RenewRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

}
