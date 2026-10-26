-- Usuarios de exemplo do "sistema generico" (tabela usuarios)
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('maria', 'maria123');
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('joao', 'joao123');
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('ana', 'ana123');
