package com.tabariyya.authentication.dto.login;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private final String userName;

    @NotBlank
    private final String password;

    public LoginRequest(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String userName() {
        return userName;
    }

    public String password() {
        return password;
    }

}
