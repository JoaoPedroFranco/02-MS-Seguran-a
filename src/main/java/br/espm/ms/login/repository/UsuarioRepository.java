package br.espm.ms.login.repository;

import br.espm.ms.login.dto.UsuarioResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acesso a tabela usuarios (usuarios do "sistema generico"). */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existe(String username) {
        Integer qtd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?", Integer.class, username);
        return qtd != null && qtd > 0;
    }

    public Optional<String> buscarSenha(String username) {
        List<String> senhas = jdbc.queryForList(
                "SELECT senha FROM usuarios WHERE username = ?", String.class, username);
        return senhas.stream().findFirst();
    }

    public void inserir(String username, String senha) {
        jdbc.update("INSERT INTO usuarios (username, senha) VALUES (?, ?)", username, senha);
    }

    public void atualizarSenha(String username, String novaSenha) {
        jdbc.update("UPDATE usuarios SET senha = ? WHERE username = ?", novaSenha, username);
    }

    public List<UsuarioResponse> listarTodos() {
        return jdbc.query("""
                SELECT u.username,
                       COALESCE(a.locked, FALSE)      AS locked,
                       COALESCE(a.failed_attempts, 0) AS failed_attempts
                  FROM usuarios u
                  LEFT JOIN login_attempts a ON a.username = u.username
                 ORDER BY u.username
                """,
                (rs, i) -> new UsuarioResponse(
                        rs.getString("username"),
                        rs.getBoolean("locked"),
                        rs.getInt("failed_attempts")));
    }
}
