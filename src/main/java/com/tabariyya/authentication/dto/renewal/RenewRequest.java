package com.tabariyya.authentication.dto.renewal;

import jakarta.validation.constraints.NotBlank;

public record RenewRequest(@NotBlank String refreshToken) {
}
