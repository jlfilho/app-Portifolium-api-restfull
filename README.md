# API AcadManage

Esta API foi desenvolvida para gerenciar cursos, usuários, atividades e evidências em um sistema acadêmico. O objetivo é fornecer endpoints RESTful para operações CRUD e consultas, com autenticação e autorização implementadas.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.x**
- **Hibernate/JPA**
- **H2 Database (Desenvolvimento)**
- **PostgreSQL (Produção)**
- **Spring Security**
- **Swagger/OpenAPI**

## Recursos Disponíveis

### 1. Usuários

#### Endpoints
- **POST** `/api/usuarios` - Criar um novo usuário.
- **GET** `/api/usuarios` - Listar todos os usuários.
- **PUT** `/api/usuarios/{id}` - Atualizar informações de um usuário.
- **DELETE** `/api/usuarios/{id}` - Deletar um usuário.

### 2. Cursos

#### Endpoints
- **POST** `/api/cursos` - Criar um novo curso.
- **GET** `/api/cursos` - Listar todos os cursos.
- **GET** `/api/cursos/{id}` - Buscar detalhes de um curso pelo ID.
- **PUT** `/api/cursos/{id}` - Atualizar informações de um curso.
- **DELETE** `/api/cursos/{id}` - Deletar um curso.

### 3. Atividades

#### Endpoints
- **POST** `/api/atividades` - Criar uma nova atividade.
- **GET** `/api/atividades` - Listar todas as atividades.
- **GET** `/api/atividades/{id}` - Buscar detalhes de uma atividade pelo ID.
- **PUT** `/api/atividades/{id}` - Atualizar informações de uma atividade.
- **DELETE** `/api/atividades/{id}` - Deletar uma atividade.

### 4. Evidências

#### Endpoints
- **POST** `/api/evidencias` - Criar uma nova evidência.
- **GET** `/api/evidencias` - Listar todas as evidências.
- **GET** `/api/evidencias/{id}` - Buscar detalhes de uma evidência pelo ID.
- **PUT** `/api/evidencias/{id}` - Atualizar informações de uma evidência.
- **DELETE** `/api/evidencias/{id}` - Deletar uma evidência.

## Autenticação e Autorização

- **Autenticação:** Basic Authentication.
- **Autorização:** Baseada em roles (ADMINISTRADOR, GERENTE, SECRETÁRIO).

### Permissões
- **ADMINISTRADOR:** Acesso total a todos os recursos.
- **GERENTE:** CRUD nos cursos associados e consultas nos demais recursos.
- **SECRETÁRIO:** Consulta em cursos associados e CRUD em atividades e evidências associadas.

## Configuração

### Configuração de Banco de Dados

- **H2 (Desenvolvimento):**
  - URL: `jdbc:h2:mem:acadmanage`
  - Usuário: `sa`
  - Senha: `senha`
- **PostgreSQL (Produção):**
  - Configurar no `application.properties` ou `application.yml`.

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
```

## Documentação da API

A documentação da API pode ser acessada através do Swagger:
- URL: `/swagger-ui/index.html`

## Execução do Projeto

1. Clone o repositório.
2. Configure o arquivo `application.properties` conforme seu ambiente.
3. Execute o projeto usando o comando:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Acesse a aplicação em `http://localhost:8080`.

## Estrutura de Pastas

```
acadmanage/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/uea/acadmanage/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── security/
│   └── resources/
│       ├── application.properties
│       └── data.sql
└── pom.xml
```

## Contato
Para dúvidas ou sugestões, entre em contato:
- **E-mail:** jlfilho@uea.edu.br
- **GitHub:** [github.com/acadmanage](https://github.com/acadmanage)

