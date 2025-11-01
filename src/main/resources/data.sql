-- Populando a tabela Categoria
INSERT INTO categoria (id, nome) VALUES 
(1, 'Ensino'),
(2, 'Pesquisa'),
(3, 'Extensão');

-- Populando a tabela TipoCurso com enumeração de tipos
INSERT INTO tipo_curso (id, codigo, nome) VALUES
(1, 'BACHARELADO', 'Bacharelado'),
(2, 'LICENCIATURA', 'Licenciatura'),
(3, 'TECNOLOGO', 'Tecnólogo'),
(4, 'ESPECIALIZACAO', 'Especialização'),
(5, 'MBA', 'MBA'),
(6, 'MESTRADO', 'Mestrado'),
(7, 'DOUTORADO', 'Doutorado');

-- Populando a tabela Curso
INSERT INTO curso (id, nome, descricao, ativo, tipo_curso_id) VALUES
(1, 'Engenharia de Software', 'Curso completo de Engenharia de Software que aborda metodologias ágeis, arquitetura de software, desenvolvimento web e mobile, gestão de projetos e qualidade de software. Forma profissionais capazes de projetar, desenvolver e manter sistemas de software complexos.', true, 1),
(2, 'Sistemas de Informação', 'Curso focado em Sistemas de Informação que combina conhecimentos em tecnologia da informação com gestão empresarial. Aborda banco de dados, redes de computadores, análise de sistemas, gestão de TI e empreendedorismo digital.', true, 1),
(3, 'Ciência da Computação', 'Curso abrangente de Ciência da Computação que oferece formação sólida em algoritmos, estruturas de dados, programação, inteligência artificial, computação gráfica e teoria da computação. Prepara profissionais para pesquisa e desenvolvimento tecnológico.', true, 1);

-- Populando a tabela Fonte Financiadora
INSERT INTO fonte_financiadora (id, nome) VALUES 
(1, 'UEA'),
(2, 'FAPEAM'),
(3, 'CAPES'),
(4, 'CNPq'),
(5, 'Outros');

-- Populando a tabela Role
INSERT INTO role (id, nome) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO');

-- Script SQL para criar 10 pessoas na tabela Pessoa

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
(10, 'Beatriz Santos', '01234567890');

-- Populando a tabela Usuario com níveis de acesso e senhas criptografadas
INSERT INTO usuario (id, email, senha, pessoa_id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$Ebmi/uPZlhTEB7e39gsPTOfADOsL0IdEcEQllZyogM/WI/WKUMYdW', 1), -- Senha: admin123
(2, 'gerente1@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 2), -- Senha: gerente123
(3, 'gerente2@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 3), -- Senha: gerente123
(4, 'gerente3@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 4), -- Senha: gerente123
(5, 'secretario1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 5), -- Senha: secretario123
(6, 'secretario2@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 6), -- Senha: secretario123
(7, 'secretario3@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 7), -- Senha: secretario123
(8, 'secretario4@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 8); -- Senha: secretario123



-- Populando a tabela Usuario_Roles para associar usuários a roles
INSERT INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1), -- Admin
(2, 2), -- Gerente Software
(3, 2), -- Gerente Sistemas
(4, 2), -- Gerente Ciência
(5, 3), -- Secretário Software
(6, 3), -- Secretário Sistemas
(7, 3), -- Secretário Ciência
(8, 1); -- Admin

-- Populando a tabela CURSO_USUARIO
-- O administrador tem acesso a todos os cursos
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 1), -- Administrador no Curso de Engenharia de Software
(2, 1), -- Administrador no Curso de Sistemas de Informação
(3, 1), -- Administrador no Curso de Ciência da Computação

-- O administrador tem acesso a todos os cursos
(1, 8), -- Administrador no Curso de Engenharia de Software
(2, 8), -- Administrador no Curso de Sistemas de Informação
(3, 8), -- Administrador no Curso de Ciência da Computação

-- Gerentes associados aos cursos
(1, 2), -- Gerente do Curso de Engenharia de Software
(2, 3), -- Gerente do Curso de Sistemas de Informação
(3, 4), -- Gerente do Curso de Ciência da Computação

-- Secretários associados aos cursos
(1, 5), -- Secretário do Curso de Engenharia de Software
(2, 6), -- Secretário do Curso de Sistemas de Informação
(3, 7); -- Secretário do Curso de Ciência da Computação

-- Populando a tabela Atividade com nomes e objetivos realistas
INSERT INTO atividade (id, nome, objetivo, foto_capa, publico_alvo, status_publicacao, data_realizacao, categoria_id, curso_id) VALUES
(1, 'Oficina de Prototipagem com Arduino', 'Promover a aprendizagem prática sobre sensores e atuadores aplicados à automação.', '/fotos-capa/1/1/def25309-ede6-41aa-a1ae-a253c3c5cd04.jpg', 'Estudantes', TRUE, '2023-01-15', 1, 1),
(2, 'Visita Técnica à Usina Hidrelétrica de Balbina', 'Compreender o funcionamento de sistemas de geração e transmissão de energia elétrica.', '/fotos-capa/1/2/3719e99b-a2da-49b9-a32e-c1baedc2abdb.jpg', 'Estudantes', TRUE, '2023-02-20', 2, 1),
(3, 'Semana de Engenharia e Inovação', 'Estimular o protagonismo estudantil e o intercâmbio de experiências em projetos tecnológicos.', '/fotos-capa/1/3/f046031d-7d4c-4f1f-af52-a09cd3190249.jpg', 'Estudantes', TRUE, '2023-03-10', 3, 1),
(4, 'Minicurso de AutoCAD e Modelagem 3D', 'Capacitar os alunos para o uso de ferramentas digitais de desenho técnico.', '/fotos-capa/1/4/c8c782ce-779e-46ac-bb30-a6dabc716ea9.jpg', 'Estudantes', FALSE, '2023-04-05', 1, 1),
(5, 'Projeto Pontes Sustentáveis', 'Desenvolver soluções estruturais com materiais alternativos e enfoque ambiental.', '/fotos-capa/1/5/637787c4-1771-40d3-a150-48cf59fbfc3c.jpg', 'Estudantes', FALSE, '2023-05-18', 2, 1),

(6, 'Hackathon de Desenvolvimento Web', 'Fomentar o trabalho em equipe e o uso de metodologias ágeis em projetos reais.', '/fotos-capa/2/6/91b93f4b-4324-41c5-b12e-9fe1d561dbd9.jpg', 'Estudantes', TRUE, '2023-06-12', 3, 2),
(7, 'Oficina de Banco de Dados com PostgreSQL', 'Aprender técnicas de modelagem e otimização de consultas SQL.', '/fotos-capa/2/7/4bc4ec42-a823-4379-8c4e-fd17f566a1ae.jpg', 'Estudantes', TRUE, '2023-07-08', 2, 2),
(8, 'Palestra: Cibersegurança e Ética Digital', 'Discutir desafios e boas práticas de segurança em sistemas de informação.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', TRUE, '2023-08-22', 3, 2),
(9, 'Oficina de APIs com Python e Flask', 'Introduzir os alunos ao desenvolvimento de serviços web e integração de sistemas.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', FALSE, '2023-09-15', 2, 2),
(10, 'Workshop de UX/UI Design para Aplicações Web', 'Explorar princípios de usabilidade e design centrado no usuário.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', FALSE, '2023-10-30', 1, 2),

(11, 'Seminário de Inteligência Artificial Aplicada à Educação', 'Apresentar projetos de pesquisa em IA voltados para ambientes de aprendizagem.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', TRUE, '2023-11-05', 1, 3),
(12, 'Oficina de Análise de Dados com Python', 'Desenvolver competências em coleta, limpeza e visualização de dados.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', TRUE, '2023-12-15', 2, 3),
(13, 'Mostra de Projetos de Computação', 'Divulgar resultados de projetos integradores e iniciação científica.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', TRUE, '2024-01-10', 3, 3),
(14, 'Minicurso de Machine Learning com Scikit-Learn', 'Capacitar alunos na implementação de modelos de aprendizado supervisionado.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', FALSE, '2024-02-20', 3, 3),
(15, 'Roda de Conversa: Ética e Tecnologia', 'Refletir sobre os impactos sociais e éticos do uso de tecnologias emergentes.', '/fotos-capa/1/1/2acf5151-1c41-4ddb-99c1-e8ad639c30c8.jpg', 'Estudantes', FALSE, '2024-03-25', 1, 3);


-- Associar atividades a pessoas com seus respectivos papéis
-- Cada atividade terá um coordenador e um bolsista ou voluntário
INSERT INTO atividade_pessoa_papel (atividade_id, pessoa_id, papel)
VALUES
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
    (6, 1, 'VOLUNTARIO'),
    (7, 7, 'COORDENADOR'),
    (7, 2, 'BOLSISTA'),
    (8, 1, 'COORDENADOR'),
    (8, 3, 'VOLUNTARIO'),
    (9, 2, 'COORDENADOR'),
    (9, 4, 'BOLSISTA'),
    (10, 3, 'COORDENADOR'),
    (10, 5, 'VOLUNTARIO'),
    (11, 4, 'COORDENADOR'),
    (11, 6, 'BOLSISTA'),
    (12, 5, 'COORDENADOR'),
    (12, 7, 'VOLUNTARIO'),
    (13, 6, 'COORDENADOR'),
    (13, 1, 'BOLSISTA'),
    (14, 7, 'COORDENADOR'),
    (14, 2, 'VOLUNTARIO'),
    (15, 1, 'COORDENADOR'),
    (15, 3, 'BOLSISTA');

INSERT INTO atividade_financiadora (atividade_id, financiadora_id) VALUES
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

-- Populando a tabela Evidencia com legendas realistas
INSERT INTO evidencia (id, url_foto, legenda, criado_por, atividade_id) VALUES
-- Oficina de Prototipagem com Arduino
(1, '/evidencias/1/1/62988d12-6561-412a-9bce-ab8cac4a48ce.jpeg', 'Alunos durante a montagem dos circuitos com sensores.', 'admin', 1),
(2, '/evidencias/1/1/70af9c27-765e-4235-aa77-c62cd9d33528.jpeg', 'Equipe testando o protótipo de automação residencial.', 'admin', 1),
(3, '/evidencias/1/1/77f82139-41d6-4079-aba5-6cbad1c7aedc.jpeg', 'Orientação prática sobre o uso do Arduino Uno.', 'admin', 1),
(4, '/evidencias/1/1/137377fe-18ac-4245-bfa4-51771bbbdc50.jpeg', 'Apresentação final dos projetos desenvolvidos na oficina.', 'admin', 1),
(5, '/evidencias/1/1/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Foto de encerramento com os participantes e o professor orientador.', 'admin', 1),

-- Visita Técnica à Usina Hidrelétrica de Balbina
(6, '/evidencias/1/2/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Grupo reunido na entrada principal da usina.', 'admin', 2),
(7, '/evidencias/1/2/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Explicação técnica sobre o funcionamento das turbinas.', 'admin', 2),
(8, '/evidencias/1/2/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Visita ao setor de controle e monitoramento da energia.', 'admin', 2),
(9, '/evidencias/1/2/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Registro do grupo no mirante da barragem.', 'admin', 2),
(10, '/evidencias/1/2/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Encerramento da visita com agradecimentos à equipe técnica.', 'admin', 2),

-- Semana de Engenharia e Inovação
(11, '/evidencias/1/3/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Abertura oficial da Semana de Engenharia e Inovação.', 'admin', 3),
(12, '/evidencias/1/3/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Palestra sobre sustentabilidade e novas tecnologias.', 'admin', 3),
(13, '/evidencias/1/3/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Apresentação de projeto acadêmico na mostra tecnológica.', 'admin', 3),
(14, '/evidencias/1/3/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Roda de conversa com egressos sobre mercado de trabalho.', 'admin', 3),
(15, '/evidencias/1/3/91082684-938b-423c-82e1-6cfa5a36f801.jpeg', 'Encerramento com entrega de certificados aos participantes.', 'admin', 3);
