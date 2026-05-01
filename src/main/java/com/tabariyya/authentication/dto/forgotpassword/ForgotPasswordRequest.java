package com.tabariyya.authentication.dto.forgotpassword;

import javax.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank
    private final String userName;

    public ForgotPasswordRequest(String userName) {
        this.userName = userName;
    }

    public String userName() {
        return userName;
    }

}
