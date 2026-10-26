package br.espm.ms.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Senha atual e nova senha")
public record TrocaSenhaRequest(
        @Schema(example = "maria123") @NotBlank String atual,
        @Schema(example = "nova123", description = "Maximo de 10 caracteres (coluna usuarios.senha VARCHAR(10))")
        @NotBlank @Size(max = 10) String nova) {
}
