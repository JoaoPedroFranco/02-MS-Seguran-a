package br.espm.ms.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario cadastrado (a senha nunca e devolvida)")
public record UsuarioResponse(
        @Schema(example = "maria") String username,
        @Schema(example = "false") boolean bloqueado,
        @Schema(example = "0") int tentativasFalhas) {
}
