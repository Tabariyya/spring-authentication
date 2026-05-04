package com.tabariyya.authentication.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String userName,
        @NotBlank String password
) {}
