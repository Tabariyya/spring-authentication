package com.tabariyya.authentication.dto.forgotpassword;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank
    private final String userName;

    @NotBlank
    private final String channel;

    public ForgotPasswordRequest(String userName, String channel) {
        this.userName = userName;
        this.channel = channel;
    }

    public String userName() {
        return userName;
    }

    public String channel() {
        return channel;
    }

}
