# API Portifolium

Esta API foi desenvolvida para gerenciar cursos, usuários, atividades, evidências e categorias em um sistema acadêmico. O objetivo é fornecer endpoints RESTful para operações CRUD, consultas avançadas e gerenciamento de arquivos, com autenticação e autorização implementadas.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.x**
- **Hibernate/JPA**
- **H2 Database (Desenvolvimento)**
- **PostgreSQL (Produção)**
- **MySQL 8.0 (Produção)**
- **Spring Security com JWT**
- **Swagger/OpenAPI**
- **Docker & Docker Compose**
- **Redis (Cache)**

## Recursos Disponíveis

### 1. Usuários

#### Endpoints
- **POST** `/api/usuarios` - Criar um novo usuário.
- **GET** `/api/usuarios` - Listar todos os usuários.
- **PUT** `/api/usuarios/{usuarioId}` - Atualizar informações de um usuário.
- **DELETE** `/api/usuarios/{usuarioId}` - Deletar um usuário.
- **PUT** `/api/usuarios/{usuarioId}/change-password` - Alterar a senha de um usuário.
- **GET** `/api/usuarios/checkAuthorities` - Verificar as permissões do usuário logado.

### 2. Cursos

#### Endpoints
- **POST** `/api/cursos` - Criar um novo curso.
- **GET** `/api/cursos` - Listar todos os cursos.
- **GET** `/api/cursos/{cursoId}` - Buscar detalhes de um curso pelo ID.
- **PUT** `/api/cursos/{cursoId}` - Atualizar informações de um curso.
- **DELETE** `/api/cursos/{cursoId}` - Deletar um curso.
- **GET** `/api/cursos/usuario` - Listar cursos associados ao usuário logado.

### 3. Atividades

#### Endpoints
- **POST** `/api/atividades` - Criar uma nova atividade.
- **GET** `/api/atividades` - Listar todas as atividades com filtros opcionais (cursoId, categoriaId, nome, dataInicio, dataFim, statusPublicacao).
- **GET** `/api/atividades/{atividadeId}` - Buscar detalhes de uma atividade pelo ID.
- **PUT** `/api/atividades/{atividadeId}` - Atualizar informações de uma atividade.
- **DELETE** `/api/atividades/{atividadeId}` - Deletar uma atividade.
- **GET** `/api/atividades/{atividadeId}/usuario/{usuarioId}` - Listar atividades associadas a um usuário específico.
- **GET** `/api/atividades/curso/{cursoId}` - Listar atividades associadas a um curso específico.

### 4. Evidências

#### Endpoints
- **POST** `/api/evidencias` - Salvar uma evidência com upload de arquivo (JPG ou PNG).
- **GET** `/api/evidencias` - Listar todas as evidências.
- **GET** `/api/evidencias/{evidenciaId}` - Buscar detalhes de uma evidência pelo ID.
- **PUT** `/api/evidencias/{evidenciaId}` - Atualizar informações de uma evidência com opção de alterar o arquivo associado.
- **DELETE** `/api/evidencias/{evidenciaId}` - Deletar uma evidência e o arquivo associado.
- **GET** `/api/evidencias/atividade/{atividadeId}` - Listar evidências associadas a uma atividade específica.

### 5. Categorias

#### Endpoints
- **POST** `/categorias` - Criar uma nova categoria.
- **GET** `/categorias` - Listar todas as categorias.
- **GET** `/categorias/{categoriaId}` - Buscar detalhes de uma categoria pelo ID.
- **PUT** `/categorias/{categoriaId}` - Atualizar informações de uma categoria.
- **DELETE** `/categorias/{categoriaId}` - Deletar uma categoria.
- **GET** `/categorias/usuario` - Listar categorias associadas ao usuário logado.

### 6. Recuperação de Senha

#### Endpoints
- **POST** `/api/recovery/reset-password` - Redefinir a senha do usuário com base em um código de recuperação.
- **POST** `/api/recovery/generate` - Gerar um código de recuperação de senha e enviá-lo por e-mail.

## Autenticação e Autorização

- **Autenticação:** JWT (JSON Web Tokens)
- **Autorização:** Baseada em roles (ADMINISTRADOR, GERENTE, SECRETÁRIO, COORDENADOR_ATIVIDADE).

### Permissões
- **ADMINISTRADOR:** Acesso total a todos os recursos.
- **GERENTE:** CRUD nos cursos associados e consultas nos demais recursos.
- **SECRETÁRIO:** Consulta em cursos associados e CRUD em atividades e evidências associadas.
- **COORDENADOR_ATIVIDADE:** Coordenação e gerenciamento de atividades.

## Configuração

### Configuração de Banco de Dados

- **H2 (Desenvolvimento):**
  - URL: `jdbc:h2:file:./data/testdb`
  - Usuário: `sa`
  - Senha: (vazio)
  - Console: `http://localhost:8080/h2-console`
- **PostgreSQL (Produção):**
  - Profile: `docker`
  - Configurar no `application-docker.properties`
- **MySQL 8.0 (Produção):**
  - Profile: `mysql,production`
  - Configurar no `application-mysql.properties`
  - Veja `ENV_VARIABLES.md` para variáveis de ambiente necessárias

### Dependências Importantes no `pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## Documentação da API

A documentação da API pode ser acessada através do Swagger:
- URL: `/swagger-ui/index.html`

## Execução do Projeto

### Opção 1: Execução Local
1. Clone o repositório.
2. Configure o arquivo `application.properties` conforme seu ambiente.
3. Execute o projeto usando o comando:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Acesse a aplicação em `http://localhost:8080`.

### Opção 2: Execução com Docker (Desenvolvimento)
1. Clone o repositório.
2. Execute o ambiente completo com:
   ```bash
   docker-compose up -d
   ```
3. Acesse:
   - **Aplicação:** http://localhost:8080
   - **Grafana:** http://localhost:3000 (admin/admin)
   - **Prometheus:** http://localhost:9090
   - **H2 Console:** http://localhost:8080/h2-console

### Opção 3: Deploy em Produção com MySQL

#### Pré-requisitos
1. Configure as variáveis de ambiente obrigatórias (veja `ENV_VARIABLES.md`):
   ```bash
   export JWT_SECRET_KEY="sua_chave_secreta"
   export MYSQL_ROOT_PASSWORD="senha_root"
   export MYSQL_PASSWORD="senha_usuario"
   export MAIL_PASSWORD="senha_email"
   ```

#### Build e Deploy
```bash
# Build da imagem de produção
./scripts/build-production.sh [tag]

# Deploy completo (build + deploy)
./scripts/deploy-production.sh

# Deploy sem rebuild
./scripts/deploy-production.sh --skip-build

# Deploy com backup do banco
./scripts/deploy-production.sh --backup
```

#### Deploy Manual
```bash
# Build
docker build -f Dockerfile.production -t portifolium:production .

# Deploy
docker-compose -f docker-compose.production.yml up -d

# Verificar status
docker-compose -f docker-compose.production.yml ps
```

**📖 Para mais detalhes, consulte:** `DOCKER_PRODUCTION.md`

### Opção 4: Deploy Automatizado (Staging)
Use o script de deploy:
```bash
# Deploy em staging
./scripts/deploy.sh staging
```

## Estrutura de Pastas

```
portifolium/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/uea/portifolium/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       │   └── exception/  # Exceções customizadas
│   │   │       ├── security/
│   │   │       └── config/
│   └── resources/
│       ├── application.properties
│       ├── application-docker.properties
│       ├── application-mysql.properties  # Config MySQL produção
│       └── data.sql
├── monitoring/
│   ├── prometheus.yml
│   └── grafana/
│       ├── dashboards/
│       └── datasources/
├── scripts/
│   ├── deploy.sh
│   ├── build-production.sh      # Build para produção
│   └── deploy-production.sh    # Deploy para produção
├── mysql-config/               # Configurações MySQL
│   └── my.cnf
├── .github/
│   └── workflows/
├── Dockerfile                  # Dockerfile desenvolvimento
├── Dockerfile.production       # Dockerfile otimizado produção
├── docker-compose.yml          # Compose desenvolvimento
├── docker-compose.production.yml  # Compose produção MySQL
├── init-mysql.sql              # Script inicialização MySQL
├── ENV_VARIABLES.md            # Documentação variáveis ambiente
├── DOCKER_PRODUCTION.md        # Guia deploy produção
└── pom.xml
```

## DevOps e Monitoramento

### Docker
- **Containerização:** Aplicação containerizada com Docker multi-stage build
- **Orquestração:** Docker Compose para desenvolvimento, staging e produção
- **Produção:** Dockerfile otimizado com MySQL 8.0, Redis e health checks
- **Segurança:** Usuário não-root, validações de segurança e variáveis de ambiente

### CI/CD Pipeline
- **GitHub Actions:** Pipeline automatizado para build, teste e deploy
- **Segurança:** Scan de vulnerabilidades com Trivy
- **Ambientes:** Deploy automático para staging e produção
- **Scripts:** Scripts automatizados de build e deploy para produção

### Monitoramento
- **Health Checks:** Spring Boot Actuator com endpoints de saúde
- **Métricas:** Prometheus para coleta de métricas
- **Visualização:** Grafana com dashboards personalizados
- **Cache:** Redis para melhorar performance
- **Logs:** Logging estruturado para produção

### Métricas Disponíveis
- **Aplicação:** HTTP requests, response time, JVM memory
- **Banco:** Conexões ativas, performance de queries (MySQL/PostgreSQL)
- **Sistema:** CPU, memória, disco
- **Customizadas:** Métricas de negócio específicas

## Tratamento de Exceções

A aplicação possui tratamento de exceções customizado para melhor experiência de desenvolvimento e produção:

### Exceções Customizadas
- **ArquivoInvalidoException:** Erros relacionados a arquivos inválidos (400 Bad Request)
- **ErroProcessamentoArquivoException:** Erros durante processamento de arquivos (500 Internal Server Error)
- **ValidacaoException:** Erros de validação de dados (400 Bad Request)
- **JWT Exceptions:** Tratamento específico para tokens expirados ou inválidos (401 Unauthorized)

### Global Exception Handler
Todas as exceções são tratadas centralmente pelo `GlobalExceptionHandler`, fornecendo:
- Mensagens de erro claras e consistentes
- Códigos HTTP apropriados
- Informações de ação para o frontend (ex: `refresh_token_required`)

## Documentação Adicional

- **DOCKER_PRODUCTION.md:** Guia completo de deploy em produção com MySQL
- **ENV_VARIABLES.md:** Documentação de todas as variáveis de ambiente necessárias
- **Swagger UI:** Documentação interativa da API em `/swagger-ui/index.html`

## Contato
Para dúvidas ou sugestões, entre em contato:
- **E-mail:** jlfilho@uea.edu.br
- **GitHub:** [github.com/portifolium](https://github.com/portifolium)
