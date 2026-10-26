package br.espm.ms.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login")
public record LoginRequest(
        @Schema(example = "maria") @NotBlank String username,
        @Schema(example = "maria123") @NotBlank String senha) {
}
