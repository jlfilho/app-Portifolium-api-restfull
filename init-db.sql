-- Script de inicialização do banco PostgreSQL para AcadManage
-- Este script é executado automaticamente quando o container PostgreSQL é criado

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Populando a tabela Categoria
INSERT INTO categoria (id, nome) VALUES 
(1, 'Ensino'),
(2, 'Pesquisa'),
(3, 'Extensão')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Curso
INSERT INTO curso (id, nome) VALUES
(1, 'Curso de Engenharia de Software'),
(2, 'Curso de Sistemas de Informação'),
(3, 'Curso de Ciência da Computação')
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
(3, 'ROLE_SECRETARIO')
ON CONFLICT (id) DO NOTHING;

-- Script SQL para criar pessoas na tabela Pessoa
INSERT INTO pessoa (id, nome, cpf) VALUES
(1, 'João Silva', '12345678901'),
(2, 'Maria Oliveira', '23456789012'),
(3, 'Carlos Souza', '34567890123'),
(4, 'Ana Paula', '45678901234'),
(5, 'Pedro Henrique', '56789012345'),
(6, 'Juliana Costa', '67890123456'),
(7, 'Fernando Lima', '78901234567'),
(8, 'Camila Almeida', '89012345678'),
(9, 'Lucas Martins', '90123456789'),
(10, 'Beatriz Santos', '01234567890')
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario com senhas criptografadas
INSERT INTO usuario (id, email, senha, pessoa_id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1), -- Senha: admin123
(2, 'gerente1@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 2), -- Senha: gerente123
(3, 'gerente2@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 3), -- Senha: gerente123
(4, 'gerente3@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 4), -- Senha: gerente123
(5, 'secretario1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 5), -- Senha: secretario123
(6, 'secretario2@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 6), -- Senha: secretario123
(7, 'secretario3@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 7), -- Senha: secretario123
(8, 'jlfilho@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 8) -- Senha: secretario123
ON CONFLICT (id) DO NOTHING;

-- Populando a tabela Usuario_Roles
INSERT INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1), -- Admin
(2, 2), -- Gerente Software
(3, 2), -- Gerente Sistemas
(4, 2), -- Gerente Ciência
(5, 3), -- Secretário Software
(6, 3), -- Secretário Sistemas
(7, 3), -- Secretário Ciência
(8, 1) -- Admin
ON CONFLICT DO NOTHING;

-- Populando a tabela CURSO_USUARIO
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 1), (2, 1), (3, 1), -- Administrador em todos os cursos
(1, 8), (2, 8), (3, 8), -- Administrador em todos os cursos
(1, 2), -- Gerente do Curso de Engenharia de Software
(2, 3), -- Gerente do Curso de Sistemas de Informação
(3, 4), -- Gerente do Curso de Ciência da Computação
(1, 5), -- Secretário do Curso de Engenharia de Software
(2, 6), -- Secretário do Curso de Sistemas de Informação
(3, 7) -- Secretário do Curso de Ciência da Computação
ON CONFLICT DO NOTHING;

-- Populando a tabela Atividade
INSERT INTO atividade (id, nome, objetivo, foto_capa, publico_alvo, status_publicacao, data_realizacao, categoria_id, curso_id) VALUES
(1, 'Atividade 1 Engenharia', 'Objetivo 1', '/fotos-capa/1/1/def25309-ede6-41aa-a1ae-a253c3c5cd04.jpg', 'Estudantes', TRUE, '2023-01-15', 1, 1),
(2, 'Atividade 2 Engenharia', 'Objetivo 2', '/fotos-capa/1/2/3719e99b-a2da-49b9-a32e-c1baedc2abdb.jpg', 'Estudantes', TRUE, '2023-02-20', 2, 1),
(3, 'Atividade 3 Engenharia', 'Objetivo 3', '/fotos-capa/1/3/f046031d-7d4c-4f1f-af52-a09cd3190249.jpg', 'Estudantes', TRUE, '2023-03-10', 3, 1),
(4, 'Atividade 4 Engenharia', 'Objetivo 4', '/fotos-capa/1/4/c8c782ce-779e-46ac-bb30-a6dabc716ea9.jpg', 'Estudantes', FALSE, '2023-04-05', 1, 1),
(5, 'Atividade 5 Engenharia', 'Objetivo 5', '/fotos-capa/1/5/637787c4-1771-40d3-a150-48cf59fbfc3c.jpg', 'Estudantes', FALSE, '2023-05-18', 2, 1)
ON CONFLICT (id) DO NOTHING;

-- Mensagem de sucesso
SELECT 'Banco de dados AcadManage inicializado com sucesso!' as status;


