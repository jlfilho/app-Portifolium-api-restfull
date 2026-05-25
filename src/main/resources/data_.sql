-- Populando a tabela Categoria
MERGE INTO categoria (id, nome, created_at, created_by, updated_at, updated_by) KEY(id) VALUES 
(1, 'Ensino', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Pesquisa', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Extensão', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Populando a tabela Tipo Curso
MERGE INTO tipo_curso (id, nome, created_at, created_by, updated_at, updated_by) KEY(id) VALUES
(1, 'Bacharelado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Licenciatura', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Tecnólogo', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'Especialização', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'MBA', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(6, 'Mestrado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(7, 'Doutorado', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Populando a tabela Unidade Acadêmica
MERGE INTO unidade_academica (id, nome, descricao, created_at, created_by, updated_at, updated_by) KEY(id) VALUES
(1, 'Escola Superior de Tecnologia', 'Unidade acadêmica focada em cursos da área de tecnologia e engenharias.', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Centro de Ciências da Natureza', 'Unidade dedicada às ciências biológicas, ambientais e naturais.', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Centro de Ciências Humanas e Sociais', 'Unidade voltada aos cursos de ciências humanas, sociais e gestão.', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Populando a tabela Curso
MERGE INTO curso (id, nome, descricao, ativo, tipo_curso_id, unidade_academica_id, created_at, created_by, updated_at, updated_by) KEY(id) VALUES
(1, 'Engenharia de Software', 
    'Curso completo de Engenharia de Software que aborda metodologias ágeis, arquitetura de software, desenvolvimento web e mobile, gestão de projetos e qualidade de software.', 
    true, 1, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Sistemas de Informação', 
    'Curso que combina tecnologia da informação e gestão, abordando análise de sistemas, banco de dados, redes de computadores e empreendedorismo digital.', 
    true, 1, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Ciência da Computação', 
    'Formação sólida em algoritmos, estruturas de dados, inteligência artificial, computação gráfica e teoria da computação, voltada para pesquisa e inovação.', 
    true, 1, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'Licenciatura em Computação', 
    'Curso voltado à formação de professores de Computação, com ênfase em práticas pedagógicas, tecnologias educacionais e ensino de programação.', 
    true, 2, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'Tecnologia em Redes de Computadores', 
    'Curso tecnólogo que prepara profissionais para projetar, implantar e administrar redes locais e corporativas com foco em segurança e desempenho.', 
    true, 3, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(6, 'Especialização em Inteligência Artificial Aplicada', 
    'Curso de pós-graduação lato sensu que desenvolve competências em machine learning, visão computacional e processamento de linguagem natural.', 
    true, 4, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(7, 'MBA em Gestão de Projetos de TI', 
    'MBA voltado a profissionais que desejam atuar na liderança de equipes e gestão estratégica de projetos tecnológicos e de inovação.', 
    true, 5, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(8, 'Mestrado Profissional em Ensino de Computação (PROFCOMP)', 
    'Curso stricto sensu que forma professores e pesquisadores na área de ensino de Computação, com foco em práticas educativas inovadoras e tecnologias digitais.', 
    true, 6, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(9, 'Mestrado Acadêmico em Ciência da Computação', 
    'Curso stricto sensu dedicado à formação de pesquisadores em áreas como IA, sistemas distribuídos, computação gráfica e engenharia de software.', 
    true, 6, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(10, 'Doutorado em Engenharia de Computação', 
    'Curso stricto sensu que visa o desenvolvimento de pesquisas avançadas em hardware, sistemas embarcados, IoT e automação inteligente.', 
    true, 7, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');
-- Populando a tabela Fonte Financiadora
MERGE INTO fonte_financiadora (id, nome, created_at, created_by, updated_at, updated_by) KEY(id) VALUES 
(1, 'UEA', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'FAPEAM', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'CAPES', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'CNPq', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'Outros', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Populando a tabela Role
MERGE INTO role (id, nome) KEY(id) VALUES
(1, 'ROLE_ADMINISTRADOR'),
(2, 'ROLE_GERENTE'),
(3, 'ROLE_SECRETARIO'),
(4, 'ROLE_COORDENADOR_ATIVIDADE');

-- Script SQL para criar 10 pessoas na tabela Pessoa
MERGE INTO pessoa (id, nome, cpf, created_at, created_by, updated_at, updated_by) KEY(id) VALUES
(1, 'Administrador do Sistema', '31452012040', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Maria Oliveira', '96443376030', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Carlos Souza', '40126145091', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'Ana Paula', '58674811078', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'Pedro Henrique', '67530579002', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(6, 'Juliana Costa', '72378802099', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(7, 'Fernando Lima', '77210958088', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(8, 'Camila Almeida', '69570668008', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(9, 'Lucas Martins', '28800569005', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(10, 'Beatriz Santos', '40085795089', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Populando a tabela Usuario com níveis de acesso e senhas criptografadas
MERGE INTO usuario (id, email, senha, pessoa_id, created_at, updated_at) KEY(id) VALUES
(1, 'admin@uea.edu.br', '$2a$10$hVfJIfpLdpbxwPiRfT2eheqDQlgklnzXZu81UYBa3bjOb5QtAAz.W', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: admin123
(2, 'gerente1@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: gerente123
(3, 'gerente2@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: gerente123
(4, 'gerente3@uea.edu.br', '$2a$10$84EaPNF6J.4tAMWF8TrNduVFf7XOuUKJ8OmPMLbUR3vq3FiZilSk2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: gerente123
(5, 'secretario1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: secretario123
(6, 'secretario2@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: secretario123
(7, 'secretario3@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: secretario123
(8, 'secretario4@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Senha: secretario123
(9, 'coordenador1@uea.edu.br', '$2a$10$X6ex54jciqS6vBx2agfhweVqN730u0R3BLD8wCP21ljBEfN2jZIW.', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- Senha: secretario123



-- Populando a tabela Usuario_Roles para associar usuários a roles
INSERT INTO usuario_roles (usuario_id, role_id) VALUES
(1, 1), -- Admin
(2, 2), -- Gerente Software
(3, 2), -- Gerente Sistemas
(4, 2), -- Gerente Ciência
(5, 3), -- Secretário Software
(6, 3), -- Secretário Sistemas
(7, 3), -- Secretário Ciência
(8, 1), -- Admin
(9, 4); -- ROLE_COORDENADOR_ATIVIDADE');


/* =========================================================
   POPULANDO A TABELA CURSO_USUARIO
   Associações entre cursos e usuários
   ========================================================= */

-- Administradores (usuários 1 e 8) em todos os cursos (1..10)
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1),
(1, 8), (2, 8), (3, 8), (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8);

-- Gerentes associados aos cursos (ciclo: 2,3,4)
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 2),  -- Gerente curso 1
(2, 3),  -- Gerente curso 2
(3, 4),  -- Gerente curso 3
(4, 2),  -- Gerente curso 4
(5, 3),  -- Gerente curso 5
(6, 4),  -- Gerente curso 6
(7, 2),  -- Gerente curso 7
(8, 3),  -- Gerente curso 8
(9, 4),  -- Gerente curso 9
(10, 2); -- Gerente curso 10

-- Secretários associados aos cursos (ciclo: 5,6,7)
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 5),  -- Secretário curso 1
(2, 6),  -- Secretário curso 2
(3, 7),  -- Secretário curso 3
(4, 5),  -- Secretário curso 4
(5, 6),  -- Secretário curso 5
(6, 7),  -- Secretário curso 6
(7, 5),  -- Secretário curso 7
(8, 6),  -- Secretário curso 8
(9, 7),  -- Secretário curso 9
(10, 5); -- Secretário curso 10

-- Coordenadores de Atividade
INSERT INTO curso_usuario (curso_id, usuario_id) VALUES
(1, 9);  -- Coordenador de Atividade no curso 1

-- Populando a tabela Atividade com nomes e objetivos realistas
INSERT INTO atividade (id, nome, objetivo, foto_capa, publico_alvo, status_publicacao, data_realizacao, data_fim, categoria_id, curso_id, created_at, created_by, updated_at, updated_by) VALUES
(1, 'Oficina de Prototipagem com Arduino', 'Promover a aprendizagem prática sobre sensores e atuadores aplicados à automação.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-01-15', '2023-01-22', 1, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(2, 'Visita Técnica à Usina Hidrelétrica de Balbina', 'Compreender o funcionamento de sistemas de geração e transmissão de energia elétrica.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-02-20', '2023-02-27', 2, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(3, 'Semana de Engenharia e Inovação', 'Estimular o protagonismo estudantil e o intercâmbio de experiências em projetos tecnológicos.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-03-10', '2023-03-17', 3, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(4, 'Minicurso de AutoCAD e Modelagem 3D', 'Capacitar os alunos para o uso de ferramentas digitais de desenho técnico.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2023-04-05', '2023-04-12', 1, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(5, 'Projeto Pontes Sustentáveis', 'Desenvolver soluções estruturais com materiais alternativos e enfoque ambiental.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2023-05-18', '2023-05-25', 2, 1, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

(6, 'Hackathon de Desenvolvimento Web', 'Fomentar o trabalho em equipe e o uso de metodologias ágeis em projetos reais.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-06-12', NULL, 3, 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(7, 'Oficina de Banco de Dados com PostgreSQL', 'Aprender técnicas de modelagem e otimização de consultas SQL.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-07-08', NULL, 2, 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(8, 'Palestra: Cibersegurança e Ética Digital', 'Discutir desafios e boas práticas de segurança em sistemas de informação.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-08-22', NULL, 3, 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(9, 'Oficina de APIs com Python e Flask', 'Introduzir os alunos ao desenvolvimento de serviços web e integração de sistemas.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2023-09-15', NULL, 2, 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(10, 'Workshop de UX/UI Design para Aplicações Web', 'Explorar princípios de usabilidade e design centrado no usuário.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2023-10-30', NULL, 1, 2, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

(11, 'Seminário de Inteligência Artificial Aplicada à Educação', 'Apresentar projetos de pesquisa em IA voltados para ambientes de aprendizagem.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-11-05', NULL, 1, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(12, 'Oficina de Análise de Dados com Python', 'Desenvolver competências em coleta, limpeza e visualização de dados.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2023-12-15', NULL, 2, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(13, 'Mostra de Projetos de Computação', 'Divulgar resultados de projetos integradores e iniciação científica.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE, '2024-01-10', NULL, 3, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(14, 'Minicurso de Machine Learning com Scikit-Learn', 'Capacitar alunos na implementação de modelos de aprendizado supervisionado.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2024-02-20', NULL, 3, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(15, 'Roda de Conversa: Ética e Tecnologia', 'Refletir sobre os impactos sociais e éticos do uso de tecnologias emergentes.', '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2024-03-25', NULL, 1, 3, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO atividade
(id, nome, objetivo, foto_capa, publico_alvo, status_publicacao, data_realizacao, categoria_id, curso_id, created_at, created_by, updated_at, updated_by)
VALUES
-- Curso 4 (Licenciatura em Computação)
(16, 'Práticas Pedagógicas com Pensamento Computacional',
 '/ Promover estratégias de ensino com PC e ABP na Educação Básica.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-04-12', 1, 4, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(17, 'Oficina Scratch na Escola',
 'Desenvolver sequências didáticas usando Scratch para anos finais do EF.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-05-18', 2, 4, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 5 (Tecnologia em Redes de Computadores)
(18, 'Laboratório de Redes com Mikrotik',
 'Configurar roteamento, VLANs e QoS em ambiente laboratorial.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-06-08', 2, 5, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(19, 'Segurança de Redes e Firewall com pfSense',
 'Implantar regras, VPN e IDS/IPS voltados a segurança perimetral.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2024-07-20', 3, 5, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 6 (Especialização em IA Aplicada)
(20, 'Bootcamp de Machine Learning Aplicado',
 'Aplicar pipelines de ML para classificação e regressão em dados reais.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-08-10', 1, 6, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(21, 'Visão Computacional com OpenCV',
 'Introduzir detecção de objetos e segmentação em projetos práticos.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-09-05', 3, 6, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 7 (MBA em Gestão de Projetos de TI)
(22, 'PMI, OKRs e Canvas na TI',
 'Integrar boas práticas de gestão com planejamento estratégico em TI.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2024-10-03', 1, 7, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(23, 'Gestão de Riscos em Projetos de Software',
 'Mapear, priorizar e mitigar riscos ao longo do ciclo de vida do projeto.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2024-11-14', 2, 7, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 8 (Mestrado Profissional em Ensino de Computação - PROFCOMP)
(24, 'Oficina de Metodologias Ativas no Ensino de Computação',
 'Experimentar sala de aula invertida, PBL e aprendizagem baseada em projetos.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2025-03-18', 1, 8, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(25, 'Seminário Tecnocomp-LTI',
 'Apresentar módulos interoperáveis e objetos de aprendizagem LTI.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2025-04-22', 3, 8, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 9 (Mestrado Acadêmico em Ciência da Computação)
(26, 'Colóquio de Pesquisa em IA',
 'Debater linhas de pesquisa em PLN, RL e IA responsável.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2025-05-27', 1, 9, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(27, 'Workshop de Sistemas Distribuídos',
 'Explorar tolerância a falhas, consenso e observabilidade em clusters.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', FALSE, '2025-06-30', 2, 9, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),

-- Curso 10 (Doutorado em Engenharia de Computação)
(28, 'Seminário Avançado em IoT e Edge',
 'Apresentar arquiteturas de IoT com processamento em borda e 5G.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2025-08-09', 3, 10, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(29, 'Colóquio de Computação Embarcada',
 'Discutir RTOS, otimização energética e segurança em sistemas embarcados.',
 '/fotos-capa/1/1/2bdee765-3f33-4abf-83f8-0d96b48b5112.jpg', 'Estudantes', TRUE,  '2025-09-12', 1, 10, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

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

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(1, 1, 0, $$Abertura da oficina

🚀 Iniciamos a Oficina de Prototipagem com Arduino apresentando conceitos básicos de eletrônica e programação. Um primeiro passo para transformar ideias em projetos reais de automação e inovação tecnológica.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(2, 1, 1, $$Mão na massa com os circuitos

🔧 Durante a atividade, os participantes montaram circuitos com LEDs, resistores e sensores, explorando na prática o funcionamento do Arduino e desenvolvendo raciocínio lógico e pensamento computacional.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(3, 1, 2, $$Trabalho em equipe e colaboração

🤝 A oficina valorizou o trabalho em equipe: grupos colaboraram na montagem dos protótipos, discutindo soluções, testando possibilidades e aprendendo juntos a resolver problemas técnicos.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(4, 1, 3, $$Protótipos voltados à realidade local

🌱 Alguns protótipos foram pensados para o contexto amazônico, como ideias de monitoramento ambiental, controle de iluminação e uso eficiente de recursos, unindo tecnologia, sustentabilidade e inovação social.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(5, 1, 4, $$Encerramento e avaliação da atividade

✅ Ao final da oficina, os participantes apresentaram seus protótipos e compartilharam percepções sobre o aprendizado. A atividade reforçou o potencial do Arduino como ferramenta pedagógica para projetos criativos e interdisciplinares.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(6, 1, 5, $$Encerramento e avaliação da atividade

✅ Ao final da oficina, os participantes apresentaram seus protótipos e compartilharam percepções sobre o aprendizado. A atividade reforçou o potencial do Arduino como ferramenta pedagógica para projetos criativos e interdisciplinares.$$,
 'admin@uea.edu.br',
 '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

 -- Atividade 2 - Visita Técnica à Usina Hidrelétrica de Balbina
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(7, 2, 0, $$Evidência 1 – Visita Técnica à Usina Hidrelétrica de Balbina$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(8, 2, 1, $$Evidência 2 – Visita Técnica à Usina Hidrelétrica de Balbina$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(9, 2, 2, $$Evidência 3 – Visita Técnica à Usina Hidrelétrica de Balbina$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(10, 2, 3, $$Evidência 4 – Visita Técnica à Usina Hidrelétrica de Balbina$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(11, 2, 4, $$Evidência 5 – Visita Técnica à Usina Hidrelétrica de Balbina$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 3 - Semana de Engenharia e Inovação
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(12, 3, 0, $$Evidência 1 – Semana de Engenharia e Inovação$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(13, 3, 1, $$Evidência 2 – Semana de Engenharia e Inovação$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(14, 3, 2, $$Evidência 3 – Semana de Engenharia e Inovação$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(15, 3, 3, $$Evidência 4 – Semana de Engenharia e Inovação$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(16, 3, 4, $$Evidência 5 – Semana de Engenharia e Inovação$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 4 - Minicurso de AutoCAD e Modelagem 3D
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(17, 4, 0, $$Evidência 1 – Minicurso de AutoCAD e Modelagem 3D$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(18, 4, 1, $$Evidência 2 – Minicurso de AutoCAD e Modelagem 3D$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(19, 4, 2, $$Evidência 3 – Minicurso de AutoCAD e Modelagem 3D$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(20, 4, 3, $$Evidência 4 – Minicurso de AutoCAD e Modelagem 3D$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(21, 4, 4, $$Evidência 5 – Minicurso de AutoCAD e Modelagem 3D$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 5 - Projeto Pontes Sustentáveis
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(22, 5, 0, $$Evidência 1 – Projeto Pontes Sustentáveis$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(23, 5, 1, $$Evidência 2 – Projeto Pontes Sustentáveis$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(24, 5, 2, $$Evidência 3 – Projeto Pontes Sustentáveis$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(25, 5, 3, $$Evidência 4 – Projeto Pontes Sustentáveis$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(26, 5, 4, $$Evidência 5 – Projeto Pontes Sustentáveis$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 6 - Hackathon de Desenvolvimento Web
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(27, 6, 0, $$Evidência 1 – Hackathon de Desenvolvimento Web$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(28, 6, 1, $$Evidência 2 – Hackathon de Desenvolvimento Web$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(29, 6, 2, $$Evidência 3 – Hackathon de Desenvolvimento Web$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(30, 6, 3, $$Evidência 4 – Hackathon de Desenvolvimento Web$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(31, 6, 4, $$Evidência 5 – Hackathon de Desenvolvimento Web$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 7 - Oficina de Banco de Dados com PostgreSQL
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(32, 7, 0, $$Evidência 1 – Oficina de Banco de Dados com PostgreSQL$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(33, 7, 1, $$Evidência 2 – Oficina de Banco de Dados com PostgreSQL$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(34, 7, 2, $$Evidência 3 – Oficina de Banco de Dados com PostgreSQL$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(35, 7, 3, $$Evidência 4 – Oficina de Banco de Dados com PostgreSQL$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(36, 7, 4, $$Evidência 5 – Oficina de Banco de Dados com PostgreSQL$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 8 - Palestra: Cibersegurança e Ética Digital
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(37, 8, 0, $$Evidência 1 – Palestra: Cibersegurança e Ética Digital$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(38, 8, 1, $$Evidência 2 – Palestra: Cibersegurança e Ética Digital$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(39, 8, 2, $$Evidência 3 – Palestra: Cibersegurança e Ética Digital$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(40, 8, 3, $$Evidência 4 – Palestra: Cibersegurança e Ética Digital$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(41, 8, 4, $$Evidência 5 – Palestra: Cibersegurança e Ética Digital$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 9 - Oficina de APIs com Python e Flask
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(42, 9, 0, $$Evidência 1 – Oficina de APIs com Python e Flask$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(43, 9, 1, $$Evidência 2 – Oficina de APIs com Python e Flask$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(44, 9, 2, $$Evidência 3 – Oficina de APIs com Python e Flask$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(45, 9, 3, $$Evidência 4 – Oficina de APIs com Python e Flask$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(46, 9, 4, $$Evidência 5 – Oficina de APIs com Python e Flask$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 10 - Workshop de UX/UI Design para Aplicações Web
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(47, 10, 0, $$Evidência 1 – Workshop de UX/UI Design para Aplicações Web$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(48, 10, 1, $$Evidência 2 – Workshop de UX/UI Design para Aplicações Web$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(49, 10, 2, $$Evidência 3 – Workshop de UX/UI Design para Aplicações Web$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(50, 10, 3, $$Evidência 4 – Workshop de UX/UI Design para Aplicações Web$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(51, 10, 4, $$Evidência 5 – Workshop de UX/UI Design para Aplicações Web$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 11 - Seminário de Inteligência Artificial Aplicada à Educação
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(52, 11, 0, $$Evidência 1 – Seminário de Inteligência Artificial Aplicada à Educação$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(53, 11, 1, $$Evidência 2 – Seminário de Inteligência Artificial Aplicada à Educação$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(54, 11, 2, $$Evidência 3 – Seminário de Inteligência Artificial Aplicada à Educação$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(55, 11, 3, $$Evidência 4 – Seminário de Inteligência Artificial Aplicada à Educação$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(56, 11, 4, $$Evidência 5 – Seminário de Inteligência Artificial Aplicada à Educação$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 12 - Oficina de Análise de Dados com Python
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(57, 12, 0, $$Evidência 1 – Oficina de Análise de Dados com Python$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(58, 12, 1, $$Evidência 2 – Oficina de Análise de Dados com Python$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(59, 12, 2, $$Evidência 3 – Oficina de Análise de Dados com Python$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(60, 12, 3, $$Evidência 4 – Oficina de Análise de Dados com Python$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(61, 12, 4, $$Evidência 5 – Oficina de Análise de Dados com Python$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 13 - Mostra de Projetos de Computação
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(62, 13, 0, $$Evidência 1 – Mostra de Projetos de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(63, 13, 1, $$Evidência 2 – Mostra de Projetos de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(64, 13, 2, $$Evidência 3 – Mostra de Projetos de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(65, 13, 3, $$Evidência 4 – Mostra de Projetos de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(66, 13, 4, $$Evidência 5 – Mostra de Projetos de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 14 - Minicurso de Machine Learning com Scikit-Learn
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(67, 14, 0, $$Evidência 1 – Minicurso de Machine Learning com Scikit-Learn$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(68, 14, 1, $$Evidência 2 – Minicurso de Machine Learning com Scikit-Learn$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(69, 14, 2, $$Evidência 3 – Minicurso de Machine Learning com Scikit-Learn$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(70, 14, 3, $$Evidência 4 – Minicurso de Machine Learning com Scikit-Learn$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(71, 14, 4, $$Evidência 5 – Minicurso de Machine Learning com Scikit-Learn$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 15 - Roda de Conversa: Ética e Tecnologia
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(72, 15, 0, $$Evidência 1 – Roda de Conversa: Ética e Tecnologia$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(73, 15, 1, $$Evidência 2 – Roda de Conversa: Ética e Tecnologia$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(74, 15, 2, $$Evidência 3 – Roda de Conversa: Ética e Tecnologia$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(75, 15, 3, $$Evidência 4 – Roda de Conversa: Ética e Tecnologia$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(76, 15, 4, $$Evidência 5 – Roda de Conversa: Ética e Tecnologia$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 16 - Práticas Pedagógicas com Pensamento Computacional
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(77, 16, 0, $$Evidência 1 – Práticas Pedagógicas com Pensamento Computacional$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(78, 16, 1, $$Evidência 2 – Práticas Pedagógicas com Pensamento Computacional$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(79, 16, 2, $$Evidência 3 – Práticas Pedagógicas com Pensamento Computacional$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(80, 16, 3, $$Evidência 4 – Práticas Pedagógicas com Pensamento Computacional$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(81, 16, 4, $$Evidência 5 – Práticas Pedagógicas com Pensamento Computacional$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 17 - Oficina Scratch na Escola
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(82, 17, 0, $$Evidência 1 – Oficina Scratch na Escola$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(83, 17, 1, $$Evidência 2 – Oficina Scratch na Escola$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(84, 17, 2, $$Evidência 3 – Oficina Scratch na Escola$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(85, 17, 3, $$Evidência 4 – Oficina Scratch na Escola$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(86, 17, 4, $$Evidência 5 – Oficina Scratch na Escola$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 18 - Laboratório de Redes com Mikrotik
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(87, 18, 0, $$Evidência 1 – Laboratório de Redes com Mikrotik$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(88, 18, 1, $$Evidência 2 – Laboratório de Redes com Mikrotik$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(89, 18, 2, $$Evidência 3 – Laboratório de Redes com Mikrotik$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(90, 18, 3, $$Evidência 4 – Laboratório de Redes com Mikrotik$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(91, 18, 4, $$Evidência 5 – Laboratório de Redes com Mikrotik$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 19 - Segurança de Redes e Firewall com pfSense
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(92, 19, 0, $$Evidência 1 – Segurança de Redes e Firewall com pfSense$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(93, 19, 1, $$Evidência 2 – Segurança de Redes e Firewall com pfSense$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(94, 19, 2, $$Evidência 3 – Segurança de Redes e Firewall com pfSense$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(95, 19, 3, $$Evidência 4 – Segurança de Redes e Firewall com pfSense$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(96, 19, 4, $$Evidência 5 – Segurança de Redes e Firewall com pfSense$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 20 - Bootcamp de Machine Learning Aplicado
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(97, 20, 0, $$Evidência 1 – Bootcamp de Machine Learning Aplicado$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(98, 20, 1, $$Evidência 2 – Bootcamp de Machine Learning Aplicado$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(99, 20, 2, $$Evidência 3 – Bootcamp de Machine Learning Aplicado$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(100, 20, 3, $$Evidência 4 – Bootcamp de Machine Learning Aplicado$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(101, 20, 4, $$Evidência 5 – Bootcamp de Machine Learning Aplicado$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 21 - Visão Computacional com OpenCV
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(102, 21, 0, $$Evidência 1 – Visão Computacional com OpenCV$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(103, 21, 1, $$Evidência 2 – Visão Computacional com OpenCV$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(104, 21, 2, $$Evidência 3 – Visão Computacional com OpenCV$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(105, 21, 3, $$Evidência 4 – Visão Computacional com OpenCV$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(106, 21, 4, $$Evidência 5 – Visão Computacional com OpenCV$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 22 - PMI, OKRs e Canvas na TI
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(107, 22, 0, $$Evidência 1 – PMI, OKRs e Canvas na TI$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(108, 22, 1, $$Evidência 2 – PMI, OKRs e Canvas na TI$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(109, 22, 2, $$Evidência 3 – PMI, OKRs e Canvas na TI$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(110, 22, 3, $$Evidência 4 – PMI, OKRs e Canvas na TI$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(111, 22, 4, $$Evidência 5 – PMI, OKRs e Canvas na TI$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 23 - Gestão de Riscos em Projetos de Software
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(112, 23, 0, $$Evidência 1 – Gestão de Riscos em Projetos de Software$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(113, 23, 1, $$Evidência 2 – Gestão de Riscos em Projetos de Software$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(114, 23, 2, $$Evidência 3 – Gestão de Riscos em Projetos de Software$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(115, 23, 3, $$Evidência 4 – Gestão de Riscos em Projetos de Software$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(116, 23, 4, $$Evidência 5 – Gestão de Riscos em Projetos de Software$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 24 - Oficina de Metodologias Ativas no Ensino de Computação
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(117, 24, 0, $$Evidência 1 – Oficina de Metodologias Ativas no Ensino de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(118, 24, 1, $$Evidência 2 – Oficina de Metodologias Ativas no Ensino de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(119, 24, 2, $$Evidência 3 – Oficina de Metodologias Ativas no Ensino de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(120, 24, 3, $$Evidência 4 – Oficina de Metodologias Ativas no Ensino de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(121, 24, 4, $$Evidência 5 – Oficina de Metodologias Ativas no Ensino de Computação$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 25 - Seminário Tecnocomp-LTI
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(122, 25, 0, $$Evidência 1 – Seminário Tecnocomp-LTI$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(123, 25, 1, $$Evidência 2 – Seminário Tecnocomp-LTI$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(124, 25, 2, $$Evidência 3 – Seminário Tecnocomp-LTI$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(125, 25, 3, $$Evidência 4 – Seminário Tecnocomp-LTI$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(126, 25, 4, $$Evidência 5 – Seminário Tecnocomp-LTI$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 26 - Colóquio de Pesquisa em IA
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(127, 26, 0, $$Evidência 1 – Colóquio de Pesquisa em IA$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(128, 26, 1, $$Evidência 2 – Colóquio de Pesquisa em IA$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(129, 26, 2, $$Evidência 3 – Colóquio de Pesquisa em IA$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(130, 26, 3, $$Evidência 4 – Colóquio de Pesquisa em IA$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(131, 26, 4, $$Evidência 5 – Colóquio de Pesquisa em IA$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 27 - Workshop de Sistemas Distribuídos
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(132, 27, 0, $$Evidência 1 – Workshop de Sistemas Distribuídos$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(133, 27, 1, $$Evidência 2 – Workshop de Sistemas Distribuídos$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(134, 27, 2, $$Evidência 3 – Workshop de Sistemas Distribuídos$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(135, 27, 3, $$Evidência 4 – Workshop de Sistemas Distribuídos$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(136, 27, 4, $$Evidência 5 – Workshop de Sistemas Distribuídos$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 28 - Seminário Avançado em IoT e Edge
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(137, 28, 0, $$Evidência 1 – Seminário Avançado em IoT e Edge$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(138, 28, 1, $$Evidência 2 – Seminário Avançado em IoT e Edge$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(139, 28, 2, $$Evidência 3 – Seminário Avançado em IoT e Edge$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(140, 28, 3, $$Evidência 4 – Seminário Avançado em IoT e Edge$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(141, 28, 4, $$Evidência 5 – Seminário Avançado em IoT e Edge$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');

-- Atividade 29 - Colóquio de Computação Embarcada
INSERT INTO evidencia (id, atividade_id, ordem, legenda, criado_por, url_foto, created_at, created_by, updated_at, updated_by) VALUES
(142, 29, 0, $$Evidência 1 – Colóquio de Computação Embarcada$$, 'admin@uea.edu.br', '/evidencias/1/1/0bd6e87c-3617-4b19-bb1c-7c2416474637.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(143, 29, 1, $$Evidência 2 – Colóquio de Computação Embarcada$$, 'admin@uea.edu.br', '/evidencias/1/1/3de3e722-1f92-4ef9-a18f-a6a2b28ef2a8.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(144, 29, 2, $$Evidência 3 – Colóquio de Computação Embarcada$$, 'admin@uea.edu.br', '/evidencias/1/1/6af36642-4ca2-48f1-a130-8020659b59f0.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(145, 29, 3, $$Evidência 4 – Colóquio de Computação Embarcada$$, 'admin@uea.edu.br', '/evidencias/1/1/7b7b043d-66f6-456e-a017-10719c8f6abf.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
(146, 29, 4, $$Evidência 5 – Colóquio de Computação Embarcada$$, 'admin@uea.edu.br', '/evidencias/1/1/779fa089-bc72-4864-9d11-012631b8436b.jpg', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system');
