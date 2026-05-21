package com.tabariyya.authentication.dto.forgotpassword;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank String identifier
) {}
