-- =====================================================================
-- Tabelas fornecidas no enunciado
-- Obs.: o script original tinha uma virgula sobrando depois de
-- "updated_at ... CURRENT_TIMESTAMP," o que gera erro de sintaxe.
-- =====================================================================
CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- =====================================================================
-- Esquema padrao do Spring Security usado pelo JdbcUserDetailsManager
-- (org/springframework/security/core/userdetails/jdbc/users.ddl)
-- Guarda quem pode administrar a API (ROLE_ADMIN / ROLE_MANUTENCAO)
-- =====================================================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
    password VARCHAR_IGNORECASE(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR_IGNORECASE(50) NOT NULL,
    authority VARCHAR_IGNORECASE(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);
