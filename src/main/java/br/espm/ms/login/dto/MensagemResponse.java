package br.espm.ms.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem de retorno")
public record MensagemResponse(@Schema(example = "Login efetuado com sucesso") String mensagem) {
}
