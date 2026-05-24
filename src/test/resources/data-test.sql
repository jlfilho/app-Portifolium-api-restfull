-- Script de dados iniciais para testes (H2)
-- Versão simplificada e compatível com H2

-- Populando a tabela Categoria
MERGE INTO categoria (id, nome) KEY(id) VALUES 
(1, 'Ensino'),
(2, 'Pesquisa'),
(3, 'Extensão');

-- Populando a tabela Tipo Curso
MERGE INTO tipo_curso (id, nome) KEY(id) VALUES
(1, 'Bacharelado'),
(2, 'Licenciatura'),
(3, 'Tecnólogo'),
(4, 'Especialização'),
(5, 'MBA'),
(6, 'Mestrado'),
(7, 'Doutorado');

-- Populando a tabela Unidade Acadêmica
MERGE INTO unidade_academica (id, nome, descricao) KEY(id) VALUES
(1, 'Escola Superior de Tecnologia', 'Unidade acadêmica focada em cursos da área de tecnologia e engenharias.'),
(2, 'Centro de Ciências da Natureza', 'Unidade dedicada às ciências biológicas, ambientais e naturais.'),
(3, 'Centro de Ciências Humanas e Sociais', 'Unidade voltada aos cursos de ciências humanas, sociais e gestão.');

-- Populando a tabela Curso (apenas alguns para testes)
MERGE INTO curso (id, nome, descricao, ativo, tipo_curso_id, unidade_academica_id) KEY(id) VALUES
(1, 'Engenharia de Software', 'Curso completo de Engenharia de Software', true, 1, 1),
(2, 'Sistemas de Informação', 'Curso que combina tecnologia da informação e gestão', true, 1, 1),
(3, 'Ciência da Computação', 'Formação sólida em algoritmos e estruturas de dados', true, 1, 1);

-- Populando a tabela Fonte Financiadora
MERGE INTO fonte_financiadora (id, nome) KEY(id) VALUES 
(1, 'UEA'),
(2, 'FAPEAM'),
(3, 'CAPES'),
(4, 'CNPq'),
(5, 'Outros');

-- Populando a tabela Role
MERGE INTO role (id, nome) KEY(id) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE');

-- Populando a tabela Pessoa
MERGE INTO pessoa (id, nome, cpf) KEY(id) VALUES
(1, 'Administrador do Sistema', '31452012040'),
(2, 'Maria Oliveira', '96443376030'),
(3, 'Carlos Souza', '40126145091'),
(4, 'Ana Paula', '58674811078'),
(5, 'Pedro Henrique', '67530579002'),
(6, 'Juliana Costa', '72378802099'),
(7, 'Fernando Lima', '77210958088'),
(8, 'Camila Almeida', '69570668008'),
(9, 'Lucas Martins', '28800569005'),
(10, 'Beatriz Santos', '40085795089');

-- Populando a tabela Usuario com senhas criptografadas
MERGE INTO usuario (id, email, senha, pessoa_id) KEY(id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1), -- Senha: admin123
(2, 'gerente1@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 2), -- Senha: gerente123
(3, 'gerente2@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 3), -- Senha: gerente123
(4, 'gerente3@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 4), -- Senha: gerente123
(5, 'secretario1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 5), -- Senha: secretario123
(6, 'secretario2@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 6), -- Senha: secretario123
(7, 'secretario3@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 7), -- Senha: secretario123
(8, 'secretario4@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 8), -- Senha: secretario123
(9, 'coordenador1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 9); -- Senha: secretario123

-- Populando a tabela Usuario_Roles
MERGE INTO usuario_roles (usuario_id, role_id) KEY(usuario_id, role_id) VALUES
(1, 1), -- Admin
(2, 2), -- Gerente Software
(3, 2), -- Gerente Sistemas
(4, 2), -- Gerente Ciência
(5, 3), -- Secretário Software
(6, 3), -- Secretário Sistemas
(7, 3), -- Secretário Ciência
(8, 1), -- Admin
(9, 4); -- ROLE_COORDENADOR_ATIVIDADE

-- Populando a tabela CURSO_USUARIO
MERGE INTO curso_usuario (curso_id, usuario_id) KEY(curso_id, usuario_id) VALUES
(1, 1), (2, 1), (3, 1), -- Administrador em todos os cursos
(1, 8), (2, 8), (3, 8), -- Administrador em todos os cursos
(1, 2), -- Gerente do Curso de Engenharia de Software
(2, 3), -- Gerente do Curso de Sistemas de Informação
(3, 4), -- Gerente do Curso de Ciência da Computação
(1, 5), -- Secretário do Curso de Engenharia de Software
(2, 6), -- Secretário do Curso de Sistemas de Informação
(3, 7); -- Secretário do Curso de Ciência da Computação

-- Populando a tabela Atividade (dados básicos para testes)
MERGE INTO atividade (id, nome, objetivo, foto_capa, publico_alvo, status_publicacao, data_realizacao, data_fim, categoria_id, curso_id) KEY(id) VALUES
(1, 'Oficina de Prototipagem com Arduino', 'Promover a aprendizagem prática sobre sensores e atuadores aplicados à automação.', NULL, 'Estudantes', TRUE, '2023-01-15', '2023-01-22', 1, 1),
(2, 'Visita Técnica à Usina Hidrelétrica', 'Compreender o funcionamento de sistemas de geração e transmissão de energia elétrica.', NULL, 'Estudantes', TRUE, '2023-02-20', '2023-02-27', 2, 1),
(3, 'Semana de Engenharia e Inovação', 'Estimular o protagonismo estudantil e o intercâmbio de experiências em projetos tecnológicos.', NULL, 'Estudantes', TRUE, '2023-03-10', '2023-03-17', 3, 1),
(4, 'Minicurso de AutoCAD', 'Capacitar os alunos para o uso de ferramentas digitais de desenho técnico.', NULL, 'Estudantes', FALSE, '2023-04-05', '2023-04-12', 1, 1),
(5, 'Projeto Pontes Sustentáveis', 'Desenvolver soluções estruturais com materiais alternativos e enfoque ambiental.', NULL, 'Estudantes', FALSE, '2023-05-18', '2023-05-25', 2, 1),
(6, 'Hackathon de Desenvolvimento Web', 'Fomentar o trabalho em equipe e o uso de metodologias ágeis em projetos reais.', NULL, 'Estudantes', TRUE, '2023-06-12', NULL, 3, 2),
(7, 'Oficina de Banco de Dados', 'Aprender técnicas de modelagem e otimização de consultas SQL.', NULL, 'Estudantes', TRUE, '2023-07-08', NULL, 2, 2),
(8, 'Palestra de Cibersegurança', 'Discutir desafios e boas práticas de segurança em sistemas de informação.', NULL, 'Estudantes', TRUE, '2023-08-22', NULL, 3, 2),
(9, 'Oficina de APIs', 'Introduzir os alunos ao desenvolvimento de serviços web e integração de sistemas.', NULL, 'Estudantes', FALSE, '2023-09-15', NULL, 2, 2),
(10, 'Workshop de UX/UI Design', 'Explorar princípios de usabilidade e design centrado no usuário.', NULL, 'Estudantes', FALSE, '2023-10-30', NULL, 1, 2),
(11, 'Seminário de IA Aplicada', 'Apresentar projetos de pesquisa em IA voltados para ambientes de aprendizagem.', NULL, 'Estudantes', TRUE, '2023-11-05', NULL, 1, 3),
(12, 'Oficina de Análise de Dados', 'Desenvolver competências em coleta, limpeza e visualização de dados.', NULL, 'Estudantes', TRUE, '2023-12-15', NULL, 2, 3),
(13, 'Mostra de Projetos', 'Divulgar resultados de projetos integradores e iniciação científica.', NULL, 'Estudantes', TRUE, '2024-01-10', NULL, 3, 3),
(14, 'Minicurso de Machine Learning', 'Capacitar alunos na implementação de modelos de aprendizado supervisionado.', NULL, 'Estudantes', FALSE, '2024-02-20', NULL, 3, 3),
(15, 'Roda de Conversa: Ética e Tecnologia', 'Refletir sobre os impactos sociais e éticos do uso de tecnologias emergentes.', NULL, 'Estudantes', FALSE, '2024-03-25', NULL, 1, 3);

-- Populando a tabela atividade_pessoa_papel
MERGE INTO atividade_pessoa_papel (atividade_id, pessoa_id, papel) KEY(atividade_id, pessoa_id) VALUES
(1, 1, 'COORDENADOR'),
(1, 3, 'BOLSISTA'),
(2, 2, 'COORDENADOR'),
(2, 4, 'VOLUNTARIO'),
(3, 3, 'COORDENADOR'),
(3, 5, 'BOLSISTA'),
(4, 4, 'COORDENADOR'),
(4, 6, 'VOLUNTARIO'),
(5, 5, 'COORDENADOR'),
(5, 7, 'BOLSISTA'),
(6, 6, 'COORDENADOR'),
(7, 7, 'COORDENADOR'),
(8, 1, 'COORDENADOR'),
(9, 2, 'COORDENADOR'),
(10, 3, 'COORDENADOR'),
(11, 4, 'COORDENADOR'),
(12, 5, 'COORDENADOR'),
(13, 6, 'COORDENADOR'),
(14, 7, 'COORDENADOR'),
(15, 1, 'COORDENADOR');

-- Populando a tabela atividade_financiadora
MERGE INTO atividade_financiadora (atividade_id, financiadora_id) KEY(atividade_id, financiadora_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 1),
(5, 2),
(6, 2),
(7, 1),
(8, 2),
(9, 2),
(10, 1),
(11, 2),
(12, 2),
(13, 1),
(14, 2),
(15, 3);

-- Populando a tabela EVIDENCIA (dados básicos para testes)
MERGE INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto) KEY(id) VALUES
(1, 1, 0, 'Abertura da oficina', 'admin@uea.edu.br', '/evidencias/1/1/test1.jpg'),
(2, 1, 1, 'Mão na massa com os circuitos', 'admin@uea.edu.br', '/evidencias/1/1/test2.jpg'),
(3, 1, 2, 'Trabalho em equipe e colaboração', 'admin@uea.edu.br', '/evidencias/1/1/test3.jpg'),
(4, 1, 3, 'Protótipos voltados à realidade local', 'admin@uea.edu.br', '/evidencias/1/1/test4.jpg'),
(5, 1, 4, 'Encerramento e avaliação da atividade', 'admin@uea.edu.br', '/evidencias/1/1/test5.jpg'),
(6, 2, 0, 'Evidência 1 – Visita Técnica', 'admin@uea.edu.br', '/evidencias/1/1/test6.jpg'),
(7, 2, 1, 'Evidência 2 – Visita Técnica', 'admin@uea.edu.br', '/evidencias/1/1/test7.jpg'),
(8, 2, 2, 'Evidência 3 – Visita Técnica', 'admin@uea.edu.br', '/evidencias/1/1/test8.jpg'),
(9, 3, 0, 'Evidência 1 – Semana de Engenharia', 'admin@uea.edu.br', '/evidencias/1/1/test9.jpg'),
(10, 3, 1, 'Evidência 2 – Semana de Engenharia', 'admin@uea.edu.br', '/evidencias/1/1/test10.jpg');

