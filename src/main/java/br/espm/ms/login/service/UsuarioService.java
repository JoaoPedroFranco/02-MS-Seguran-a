package br.espm.ms.login.service;

import br.espm.ms.login.dto.UsuarioResponse;
import br.espm.ms.login.metrics.LoginMetrics;
import br.espm.ms.login.repository.LoginAttemptRepository;
import br.espm.ms.login.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final LoginAttemptRepository attempts;
    private final LoginMetrics metrics;
    private final int maxFalhasPermitidas;

    public UsuarioService(UsuarioRepository usuarios,
                          LoginAttemptRepository attempts,
                          LoginMetrics metrics,
                          @Value("${app.login.max-falhas-permitidas:1}") int maxFalhasPermitidas) {
        this.usuarios = usuarios;
        this.attempts = attempts;
        this.metrics = metrics;
        this.maxFalhasPermitidas = maxFalhasPermitidas;
    }

    @Transactional
    public ResultadoLogin login(String username, String senha) {
        Optional<String> senhaCadastrada = usuarios.buscarSenha(username);

        // Usuario inexistente: conta como login invalido, mas nao cria registro de bloqueio
        if (senhaCadastrada.isEmpty()) {
            metrics.registrarInvalido();
            return ResultadoLogin.CREDENCIAIS_INVALIDAS;
        }

        // Usuario bloqueado nao entra nem com a senha correta
        if (attempts.estaBloqueado(username)) {
            metrics.registrarInvalido();
            return ResultadoLogin.USUARIO_BLOQUEADO;
        }

        if (senhaCadastrada.get().equals(senha)) {
            attempts.resetar(username);
            metrics.registrarValido();
            return ResultadoLogin.SUCESSO;
        }

        int falhas = attempts.registrarFalha(username);
        metrics.registrarInvalido();
        if (falhas > maxFalhasPermitidas) {   // falhou mais de uma vez -> bloqueia
            attempts.bloquear(username);
            return ResultadoLogin.USUARIO_BLOQUEADO;
        }
        return ResultadoLogin.CREDENCIAIS_INVALIDAS;
    }

    @Transactional
    public ResultadoTrocaSenha trocarSenha(String username, String atual, String nova) {
        Optional<String> senhaCadastrada = usuarios.buscarSenha(username);
        if (senhaCadastrada.isEmpty()) {
            return ResultadoTrocaSenha.USUARIO_NAO_ENCONTRADO;
        }
        if (attempts.estaBloqueado(username)) {
            return ResultadoTrocaSenha.USUARIO_BLOQUEADO;
        }
        if (!senhaCadastrada.get().equals(atual)) {
            return ResultadoTrocaSenha.SENHA_ATUAL_INVALIDA;
        }
        usuarios.atualizarSenha(username, nova);
        return ResultadoTrocaSenha.SUCESSO;
    }

    /** @return false se o username ja existir */
    @Transactional
    public boolean cadastrar(String username, String senha) {
        if (usuarios.existe(username)) {
            return false;
        }
        usuarios.inserir(username, senha);
        return true;
    }

    /** @return false se o usuario nao existir */
    @Transactional
    public boolean desbloquear(String username) {
        if (!usuarios.existe(username)) {
            return false;
        }
        attempts.resetar(username);
        return true;
    }

    public List<UsuarioResponse> listar() {
        return usuarios.listarTodos();
    }
}
