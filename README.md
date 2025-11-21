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

#### Pré-requisitos
- Java 17+ instalado
- Maven instalado (ou use o `mvnw` wrapper incluído)

#### Executar
```bash
# Windows (PowerShell/CMD)
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Acessar
- **Aplicação:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/testdb`
  - Usuário: `sa`
  - Senha: (vazio)

### Opção 2: Execução com Docker (Desenvolvimento)

#### Executar
```bash
# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down

# Reconstruir e iniciar
docker-compose up -d --build
```

#### Acessar
- **Aplicação:** http://localhost:8080
- **Grafana:** http://localhost:3000 (admin/admin)
- **Prometheus:** http://localhost:9090
- **H2 Console:** http://localhost:8080/h2-console

### Opção 3: Deploy em Produção com Docker e MySQL

#### Pré-requisitos
- Docker e Docker Compose instalados
- Variáveis de ambiente configuradas (veja seção "Variáveis de Ambiente")

#### Variáveis de Ambiente Obrigatórias

Defina as variáveis de ambiente no sistema ou crie um arquivo `.env` (não commite no repositório):

```bash
# Banco de Dados MySQL
MYSQL_ROOT_PASSWORD=your_secure_root_password_here
MYSQL_DATABASE=portifolium
MYSQL_USER=portifolium_user
MYSQL_PASSWORD=your_secure_password_here

# Segurança JWT (gerar com: openssl rand -hex 32)
JWT_SECRET_KEY=your_jwt_secret_key_minimum_256_bits_here
JWT_EXPIRATION_TIME=3600000  # 1 hora em milissegundos

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tecnocomp@uea.edu.br
MAIL_PASSWORD=your_email_password_here

# Estratégia DDL (use 'create' na primeira vez, depois 'validate')
SPRING_JPA_HIBERNATE_DDL_AUTO=create  # Primeira vez
# SPRING_JPA_HIBERNATE_DDL_AUTO=validate  # Após primeira inicialização
```

**⚠️ IMPORTANTE:** 
- Todas as senhas devem ser fortes e únicas
- JWT_SECRET_KEY deve ter pelo menos 256 bits (64 caracteres hexadecimais)
- Após a primeira inicialização, mude `SPRING_JPA_HIBERNATE_DDL_AUTO` para `validate` para segurança

#### Dados Iniciais

Na primeira inicialização com `SPRING_JPA_HIBERNATE_DDL_AUTO=create`, os seguintes dados serão inseridos automaticamente:

- **Categorias:** Ensino, Pesquisa, Extensão
- **Tipos de Curso:** Bacharelado, Licenciatura, Tecnólogo, Especialização, MBA, Mestrado, Doutorado
- **Fontes Financiadoras:** UEA, FAPEAM, CAPES, CNPq, Outros
- **Roles:** ROLE_ADMINISTRADOR, ROLE_GERENTE, ROLE_SECRETARIO, ROLE_COORDENADOR_ATIVIDADE
- **Usuário Administrador:**
  - Email: `admin@uea.edu.br`
  - Senha: `admin123`
  - Role: ROLE_ADMINISTRADOR

#### Build e Deploy

**Usando Scripts (Recomendado):**
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

**Deploy Manual:**
```bash
# Build
docker build -f Dockerfile.production -t portifolium:production .

# Parar containers existentes
docker-compose -f docker-compose.production.yml down

# Iniciar serviços
docker-compose -f docker-compose.production.yml up -d

# Verificar status
docker-compose -f docker-compose.production.yml ps

# Ver logs
docker-compose -f docker-compose.production.yml logs -f
```

#### Verificação

```bash
# Verificar health check
curl http://localhost:8080/actuator/health

# Ver logs da aplicação
docker logs -f portifolium-app

# Ver logs do MySQL
docker logs -f portifolium-mysql
```

#### Manutenção

**Backup do Banco de Dados:**
```bash
docker exec portifolium-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} portifolium > backup-$(date +%Y%m%d).sql
```

**Restaurar Backup:**
```bash
docker exec -i portifolium-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} portifolium < backup-20241117.sql
```

**Atualizar Aplicação:**
```bash
# 1. Fazer backup (opcional)
./scripts/deploy-production.sh --backup

# 2. Fazer pull das mudanças
git pull

# 3. Rebuild e redeploy
./scripts/deploy-production.sh
```

### Opção 4: Deploy Automatizado (Staging)

```bash
# Deploy em staging
./scripts/deploy.sh staging
```

## Comandos Úteis

### Executar Testes

```bash
# Executar todos os testes
.\mvnw.cmd test

# Executar teste específico
.\mvnw.cmd test -Dtest=EvidenciaControllerIT

# Executar todos os testes de integração (ordem específica)
.\run-all-tests.ps1
```

### Compilar o Projeto

```bash
# Limpar e compilar
.\mvnw.cmd clean compile

# Compilar ignorando testes
.\mvnw.cmd clean package -DskipTests

# Compilar e gerar JAR
.\mvnw.cmd clean package
```

### Verificar Dependências

```bash
# Ver dependências do projeto
.\mvnw.cmd dependency:tree

# Verificar atualizações
.\mvnw.cmd versions:display-dependency-updates
```

### Troubleshooting

**Erro: Porta já em uso**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill
```

**Erro: Aplicação não inicia**
```bash
# Verificar logs
docker logs portifolium-app

# Verificar se o MySQL está rodando
docker ps | grep mysql

# Verificar variáveis de ambiente
docker exec portifolium-app env | grep -E "SPRING|JWT|MYSQL"
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

## Segurança em Produção

### Checklist de Segurança

- [ ] Todas as senhas são fortes e únicas
- [ ] JWT_SECRET_KEY tem pelo menos 256 bits
- [ ] SPRING_JPA_HIBERNATE_DDL_AUTO está configurado como 'validate' após primeira inicialização
- [ ] Portas do MySQL e Redis não estão expostas publicamente
- [ ] Variáveis de ambiente não estão em arquivos versionados
- [ ] Backups são feitos regularmente
- [ ] Logs são monitorados

### Recomendações

1. Use um gerenciador de segredos (ex: HashiCorp Vault, AWS Secrets Manager)
2. Configure firewall para restringir acesso às portas
3. Use HTTPS com certificado válido
4. Configure rate limiting
5. Monitore logs e métricas regularmente

## Documentação da API

A documentação interativa da API pode ser acessada através do Swagger:
- **URL:** http://localhost:8080/swagger-ui/index.html

## Contato
Para dúvidas ou sugestões, entre em contato:
- **E-mail:** jlfilho@uea.edu.br
- **GitHub:** [github.com/portifolium](https://github.com/portifolium)
