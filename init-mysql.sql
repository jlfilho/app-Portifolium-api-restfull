-- Script de inicialização do MySQL para Portifolium
-- Este script é executado automaticamente quando o container MySQL é criado pela primeira vez

-- Garantir que o charset e collation estão corretos
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Usar o database (geralmente já é criado via variáveis de ambiente)
USE portifolium;

-- =========================================================
-- DADOS INICIAIS - Executado apenas na primeira inicialização
-- =========================================================

-- Populando a tabela Categoria
INSERT IGNORE INTO categoria (id, nome) VALUES 
(1, 'Ensino'),
(2, 'Pesquisa'),
(3, 'Extensão');

-- Populando a tabela Tipo Curso
INSERT IGNORE INTO tipo_curso (id, nome) VALUES
(1, 'Bacharelado'),
(2, 'Licenciatura'),
(3, 'Tecnólogo'),
(4, 'Especialização'),
(5, 'MBA'),
(6, 'Mestrado'),
(7, 'Doutorado');

-- Populando a tabela Fonte Financiadora
INSERT IGNORE INTO fonte_financiadora (id, nome) VALUES 
(1, 'UEA'),
(2, 'FAPEAM'),
(3, 'CAPES'),
(4, 'CNPq'),
(5, 'Outros');

-- Populando a tabela Role
INSERT IGNORE INTO role (id, nome) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE');

-- Populando a tabela Pessoa (Administrador do Sistema)
INSERT IGNORE INTO pessoa (id, nome, cpf) VALUES
(1, 'Administrador do Sistema', '12345678901');

-- Populando a tabela Usuario (admin)
-- Senha: admin123 (criptografada com BCrypt)
INSERT IGNORE INTO usuario (id, email, senha, pessoa_id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1);

-- Populando a tabela Usuario_Roles (associação admin com role de administrador)
INSERT IGNORE INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1); -- Admin

-- Nota: As tabelas serão criadas automaticamente pelo Hibernate
-- quando SPRING_JPA_HIBERNATE_DDL_AUTO estiver configurado como 'create' ou 'update'
-- Em produção, use 'validate' e execute migrations manualmente
