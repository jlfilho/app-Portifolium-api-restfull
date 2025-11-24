-- Script de dados iniciais para PostgreSQL
-- Este script é executado automaticamente pelo Spring Boot após a criação das tabelas
-- Sintaxe PostgreSQL: ON CONFLICT (id) DO NOTHING

-- Populando a tabela Categoria
INSERT INTO categoria (id, nome, created_at, created_by, updated_at, updated_by) VALUES 
(1, 'Ensino', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Pesquisa', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Extensão', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Tipo Curso
INSERT INTO tipo_curso (id, nome, created_at, created_by, updated_at, updated_by) VALUES
(1, 'Bacharelado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Licenciatura', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Tecnólogo', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'Especialização', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'MBA', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(6, 'Mestrado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(7, 'Doutorado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Fonte Financiadora
INSERT INTO fonte_financiadora (id, nome, created_at, created_by, updated_at, updated_by) VALUES 
(1, 'UEA', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'FAPEAM', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'CAPES', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'CNPq', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'Outros', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Role
INSERT INTO role (id, nome) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Pessoa (Administrador do Sistema)
-- CPF válido: 314.520.120-40
INSERT INTO pessoa (id, nome, cpf, created_at, created_by, updated_at, updated_by) VALUES
(1, 'Administrador do Sistema', '31452012040', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario (admin)
-- Senha: secretario123 (criptografada com BCrypt)
INSERT INTO usuario (id, email, senha, pessoa_id, created_at, updated_at) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario_Roles (associação admin com role de administrador)
INSERT INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1) -- Admin
ON CONFLICT DO NOTHING;

-- Sincronizar sequências do PostgreSQL após inserções manuais
-- Isso garante que os próximos IDs gerados automaticamente sejam corretos
-- O terceiro parâmetro 'true' significa que o próximo nextval() retornará MAX(id) + 1
SELECT setval('categoria_id_seq', COALESCE((SELECT MAX(id) FROM categoria), 0), true);
SELECT setval('tipo_curso_id_seq', COALESCE((SELECT MAX(id) FROM tipo_curso), 0), true);
SELECT setval('fonte_financiadora_id_seq', COALESCE((SELECT MAX(id) FROM fonte_financiadora), 0), true);
SELECT setval('role_id_seq', COALESCE((SELECT MAX(id) FROM role), 0), true);
SELECT setval('pessoa_id_seq', COALESCE((SELECT MAX(id) FROM pessoa), 0), true);
SELECT setval('usuario_id_seq', COALESCE((SELECT MAX(id) FROM usuario), 0), true);

