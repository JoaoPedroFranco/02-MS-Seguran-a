package br.espm.ms.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados do novo usuario")
public record NovoUsuarioRequest(
        @Schema(example = "carlos") @NotBlank @Size(max = 100) String username,
        @Schema(example = "carlos123", description = "Maximo de 10 caracteres (coluna usuarios.senha VARCHAR(10))")
        @NotBlank @Size(max = 10) String senha) {
}
