package br.espm.ms.login.controller;

import br.espm.ms.login.dto.LoginRequest;
import br.espm.ms.login.dto.MensagemResponse;
import br.espm.ms.login.dto.NovoUsuarioRequest;
import br.espm.ms.login.dto.TrocaSenhaRequest;
import br.espm.ms.login.dto.UsuarioResponse;
import br.espm.ms.login.service.ResultadoLogin;
import br.espm.ms.login.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Usuarios", description = "Login, troca de senha, cadastro, desbloqueio e listagem")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua login (publico)",
            description = "Valida usuario e senha na tabela usuarios. Depois de mais de uma falha o usuario e bloqueado.")
    @ApiResponse(responseCode = "200", description = "Usuario e senha validos")
    @ApiResponse(responseCode = "401", description = "Usuario/senha invalidos ou usuario bloqueado")
    @ApiResponse(responseCode = "400", description = "JSON invalido")
    public ResponseEntity<MensagemResponse> login(@Valid @RequestBody LoginRequest req) {
        ResultadoLogin resultado = service.login(req.username(), req.senha());
        return switch (resultado) {
            case SUCESSO -> ResponseEntity.ok(new MensagemResponse("Login efetuado com sucesso"));
            case USUARIO_BLOQUEADO -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuario bloqueado por excesso de tentativas"));
            case CREDENCIAIS_INVALIDAS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuario ou senha invalidos"));
        };
    }

    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha de um usuario (publico)",
            description = "Confere a senha atual na base de dados antes de gravar a nova.")
    @ApiResponse(responseCode = "200", description = "Senha alterada")
    @ApiResponse(responseCode = "401", description = "Senha atual nao confere")
    @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    @ApiResponse(responseCode = "423", description = "Usuario bloqueado")
    @ApiResponse(responseCode = "400", description = "JSON invalido (nova senha com mais de 10 caracteres, por exemplo)")
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "username do usuario", example = "maria") @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest req) {
        return switch (service.trocarSenha(userid, req.atual(), req.nova())) {
            case SUCESSO -> ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso"));
            case SENHA_ATUAL_INVALIDA -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Senha atual nao confere"));
            case USUARIO_NAO_ENCONTRADO -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponse("Usuario nao encontrado"));
            case USUARIO_BLOQUEADO -> ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new MensagemResponse("Usuario bloqueado - solicite o desbloqueio a um administrador"));
        };
    }

    @PostMapping("/")
    @Operation(summary = "Cadastra um novo usuario (ROLE_MANUTENCAO)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "201", description = "Usuario criado")
    @ApiResponse(responseCode = "409", description = "Usuario ja existe")
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    public ResponseEntity<MensagemResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest req) {
        if (!service.cadastrar(req.username(), req.senha())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MensagemResponse("Usuario ja existe"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensagemResponse("Usuario cadastrado com sucesso"));
    }

    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia um usuario (ROLE_ADMIN)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Usuario desbloqueado")
    @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content)
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "username do usuario", example = "maria") @PathVariable String userid) {
        if (!service.desbloquear(userid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponse("Usuario nao encontrado"));
        }
        return ResponseEntity.ok(new MensagemResponse("Usuario desbloqueado com sucesso"));
    }

    @GetMapping("/")
    @Operation(summary = "Lista todos os usuarios (ROLE_MANUTENCAO)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Lista de usuarios",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    public List<UsuarioResponse> listar() {
        return service.listar();
    }
}
