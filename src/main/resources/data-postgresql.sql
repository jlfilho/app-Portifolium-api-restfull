-- Script de dados iniciais para PostgreSQL
-- Este script é executado automaticamente pelo Spring Boot após a criação das tabelas
-- Apenas na primeira inicialização (quando as tabelas estão vazias)

-- Populando a tabela Categoria
INSERT INTO categoria (id, nome) VALUES 
(1, 'Ensino'),
(2, 'Pesquisa'),
(3, 'Extensão')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Tipo Curso
INSERT INTO tipo_curso (id, nome) VALUES
(1, 'Bacharelado'),
(2, 'Licenciatura'),
(3, 'Tecnólogo'),
(4, 'Especialização'),
(5, 'MBA'),
(6, 'Mestrado'),
(7, 'Doutorado')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Fonte Financiadora
INSERT INTO fonte_financiadora (id, nome) VALUES 
(1, 'UEA'),
(2, 'FAPEAM'),
(3, 'CAPES'),
(4, 'CNPq'),
(5, 'Outros')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Role
INSERT INTO role (id, nome) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Pessoa (Administrador do Sistema)
INSERT INTO pessoa (id, nome, cpf) VALUES
(1, 'Administrador do Sistema', '12345678901')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario (admin)
-- Senha: admin123 (criptografada com BCrypt)
INSERT INTO usuario (id, email, senha, pessoa_id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1)
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario_Roles (associação admin com role de administrador)
INSERT INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1) -- Admin
ON CONFLICT DO NOTHING;

