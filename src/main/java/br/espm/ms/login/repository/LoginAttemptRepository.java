package br.espm.ms.login.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Acesso a tabela login_attempts (controle de falhas e bloqueio). */
@Repository
public class LoginAttemptRepository {

    private final JdbcTemplate jdbc;

    public LoginAttemptRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean estaBloqueado(String username) {
        Boolean locked = jdbc.query(
                "SELECT locked FROM login_attempts WHERE username = ?",
                rs -> rs.next() ? rs.getBoolean(1) : Boolean.FALSE, username);
        return Boolean.TRUE.equals(locked);
    }

    /** Soma uma falha e devolve o total de falhas acumuladas. */
    public int registrarFalha(String username) {
        int linhas = jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = failed_attempts + 1,
                       last_failed_at  = CURRENT_TIMESTAMP,
                       updated_at      = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
        if (linhas == 0) {
            jdbc.update("""
                    INSERT INTO login_attempts (username, failed_attempts, locked, last_failed_at, updated_at)
                    VALUES (?, 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, username);
        }
        Integer falhas = jdbc.queryForObject(
                "SELECT failed_attempts FROM login_attempts WHERE username = ?", Integer.class, username);
        return falhas == null ? 0 : falhas;
    }

    public void bloquear(String username) {
        jdbc.update("""
                UPDATE login_attempts
                   SET locked = TRUE, locked_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    /** Zera falhas e desbloqueia (usado no login valido e no /unlock). */
    public void resetar(String username) {
        jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = 0, locked = FALSE, locked_at = NULL, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    public long contarBloqueados() {
        Long qtd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE", Long.class);
        return qtd == null ? 0 : qtd;
    }
}
