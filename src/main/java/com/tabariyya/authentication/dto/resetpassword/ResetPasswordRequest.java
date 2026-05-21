package com.tabariyya.authentication.dto.resetpassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String identifier,
        @NotBlank String otp,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        String newPassword
) {}
