# API Portifolium

API Spring Boot para gerenciar cursos, usuarios, atividades, evidencias, relatorios e arquivos do Portifolium.

## Stack

- Java 17
- Spring Boot 3
- Spring Security com JWT
- Spring Data JPA/Hibernate
- H2 para execucao local via Maven
- PostgreSQL para execucao via Docker Compose
- Swagger/OpenAPI

## Como Executar

### Opcao 1: local com H2

Use este caminho para desenvolvimento rapido sem Docker.

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

URLs:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health
- H2 Console: http://localhost:8080/h2-console

H2:

- JDBC URL: `jdbc:h2:file:./data/testdb`
- Usuario: `sa`
- Senha: vazia

### Opcao 2: Docker Compose com PostgreSQL

Use este caminho para validar a API em container com banco real.

```powershell
docker compose up -d --build
```

Comandos uteis:

```powershell
docker compose logs -f app
docker compose ps
docker compose down
docker compose down -v
```

URLs:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

O compose sobe apenas:

- `app`: API Spring Boot
- `db`: PostgreSQL 15

## Configuracao

O arquivo `.env` nao deve ser versionado. Para customizar portas, credenciais ou e-mail:

```powershell
Copy-Item .env.example .env
```

Principais variaveis:

```env
APP_PORT=8080
POSTGRES_DB=portifolium
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_SQL_INIT_MODE=always
JWT_SECRET_KEY=troque-por-uma-chave-segura
JWT_EXPIRATION_TIME=3600000
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
FRONTEND_URL=http://localhost:4200
MAIL_USERNAME=
MAIL_PASSWORD=
```

`APP_CORS_ALLOWED_ORIGINS` aceita multiplas origens separadas por virgula. Para producao, defina segredos por variaveis de ambiente do ambiente de deploy. Nao commite `.env` com senhas reais.

## Arquivos Essenciais de Execucao

- `mvnw` e `mvnw.cmd`: execucao local e testes via Maven Wrapper.
- `Dockerfile`: build da imagem da API.
- `docker-compose.yml`: ambiente local conteinerizado com PostgreSQL.
- `.env.example`: exemplo de configuracao local sem segredos.

Arquivos alternativos de MySQL, scripts de deploy e stacks de monitoramento foram removidos para manter um unico fluxo claro.

## Testes

Unitarios:

```powershell
.\mvnw.cmd test
```

Teste especifico:

```powershell
.\mvnw.cmd -Dtest=CursoControllerIT#deveCriarCursoComoAdministrador test
```

Integracao:

```powershell
.\mvnw.cmd verify
```

## Build

Jar local:

```powershell
.\mvnw.cmd clean package
```

Imagem Docker:

```powershell
docker build -t portifolium-api:local .
```

## Dados Iniciais

Na execucao local H2, `data.sql` popula dados basicos.

Na execucao Docker com PostgreSQL, `SPRING_SQL_INIT_MODE=always` faz o Spring Boot executar `data-postgresql.sql`.

Usuario administrador inicial:

- Email: `admin@uea.edu.br`
- Senha: `secretario123`

## Estrutura Principal

```text
src/main/java/edu/uea/acadmanage/
  controller/
  service/
  repository/
  model/
  DTO/
  security/
  config/

src/main/resources/
  application.properties
  application-postgresql.properties
  application-mysql.properties
  data.sql
  data-postgresql.sql
  data-mysql.sql
```

## Tratamento de Erros

As excecoes sao tratadas centralmente por `GlobalExceptionHandler` e retornam `ApiErrorResponse`:

```json
{
  "timestamp": "2026-05-24T18:30:18.1341478",
  "statusCode": 400,
  "status": "BAD_REQUEST",
  "error": "Erro de validacao",
  "message": "O nome do curso e obrigatorio",
  "path": "/api/cursos",
  "details": null,
  "action": null
}
```
