-- Populando a tabela Categoria
INSERT IGNORE INTO categoria (id, nome, created_at, created_by, updated_at, updated_by) VALUES 
(1, 'Ensino', NOW(), 'system', NOW(), 'system'),
(2, 'Pesquisa', NOW(), 'system', NOW(), 'system'),
(3, 'Extensão', NOW(), 'system', NOW(), 'system');

-- Populando a tabela Tipo Curso
INSERT IGNORE INTO tipo_curso (id, nome, created_at, created_by, updated_at, updated_by) VALUES
(1, 'Bacharelado', NOW(), 'system', NOW(), 'system'),
(2, 'Licenciatura', NOW(), 'system', NOW(), 'system'),
(3, 'Tecnólogo', NOW(), 'system', NOW(), 'system'),
(4, 'Especialização', NOW(), 'system', NOW(), 'system'),
(5, 'MBA', NOW(), 'system', NOW(), 'system'),
(6, 'Mestrado', NOW(), 'system', NOW(), 'system'),
(7, 'Doutorado', NOW(), 'system', NOW(), 'system');


-- Populando a tabela Fonte Financiadora
INSERT IGNORE INTO fonte_financiadora (id, nome, created_at, created_by, updated_at, updated_by) VALUES 
(1, 'UEA', NOW(), 'system', NOW(), 'system'),
(2, 'FAPEAM', NOW(), 'system', NOW(), 'system'),
(3, 'CAPES', NOW(), 'system', NOW(), 'system'),
(4, 'CNPq', NOW(), 'system', NOW(), 'system'),
(5, 'Outros', NOW(), 'system', NOW(), 'system');

-- Populando a tabela Role
INSERT IGNORE INTO role (id, nome) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE');

-- Script SQL para criar 10 pessoas na tabela Pessoa
INSERT IGNORE INTO pessoa (id, nome, cpf, created_at, created_by, updated_at, updated_by) VALUES
(1, 'Administrador do Sistema', '31452012040', NOW(), 'system', NOW(), 'system');

-- Populando a tabela Usuario com níveis de acesso e senhas criptografadas
INSERT IGNORE INTO usuario (id, email, senha, pessoa_id, created_at, updated_at) VALUES
(1, 'admin@uea.edu.br', '$2a$10$hVfJIfpLdpbxwPiRfT2eheqDQlgklnzXZu81UYBa3bjOb5QtAAz.W', 1, NOW(), NOW()); -- Senha: admin123



-- Populando a tabela Usuario_Roles para associar usuários a roles
INSERT IGNORE INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1); -- Admin
