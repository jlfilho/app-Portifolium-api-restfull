# API AcadManage

Esta API foi desenvolvida para gerenciar cursos, usuários, atividades, evidências e categorias em um sistema acadêmico. O objetivo é fornecer endpoints RESTful para operações CRUD, consultas avançadas e gerenciamento de arquivos, com autenticação e autorização implementadas.

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
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
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
