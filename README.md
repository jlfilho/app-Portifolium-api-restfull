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

### 1. Categorias

#### Endpoints
- **GET** `/categorias` - Listar todas as categorias.
- **GET** `/categorias/{categoriaId}` - Buscar detalhes de uma categoria pelo ID.
- **POST** `/categorias` - Criar uma nova categoria.
- **PUT** `/categorias/{categoriaId}` - Atualizar informações de uma categoria.
- **DELETE** `/categorias/{categoriaId}` - Deletar uma categoria.
- **GET** `/categorias/usuario` - Listar categorias associadas ao usuário autenticado.

### 2. Usuários

#### Endpoints
- **POST** `/api/usuarios` - Criar um novo usuário.
- **GET** `/api/usuarios` - Listar todos os usuários.
- **PUT** `/api/usuarios/{usuarioId}` - Atualizar informações de um usuário.
- **DELETE** `/api/usuarios/{usuarioId}` - Deletar um usuário.
- **PUT** `/api/usuarios/{usuarioId}/change-password` - Alterar a senha de um usuário.
- **GET** `/api/usuarios/checkAuthorities` - Verificar as autoridades (roles) do usuário autenticado.

### 3. Cursos

#### Endpoints
- **GET** `/api/cursos` - Listar todos os cursos.
- **GET** `/api/cursos/{cursoId}` - Buscar detalhes de um curso pelo ID.
- **POST** `/api/cursos` - Criar um novo curso.
- **PUT** `/api/cursos/{cursoId}` - Atualizar informações de um curso.
- **DELETE** `/api/cursos/{cursoId}` - Deletar um curso.
- **GET** `/api/cursos/usuario` - Listar cursos associados ao usuário autenticado.

### 4. Atividades

#### Endpoints
- **GET** `/api/atividades` - Listar todas as atividades.
- **GET** `/api/atividades/{atividadeId}` - Buscar detalhes de uma atividade pelo ID.
- **POST** `/api/atividades` - Criar uma nova atividade.
- **PUT** `/api/atividades/{atividadeId}` - Atualizar informações de uma atividade.
- **DELETE** `/api/atividades/{atividadeId}` - Deletar uma atividade.
- **GET** `/api/atividades/{atividadeId}/usuario/{usuarioId}` - Verificar a associação de uma atividade a um usuário.
- **GET** `/api/atividades/filtros` - Consultar atividades com filtros avançados.
- **GET** `/api/atividades/curso/{cursoId}` - Listar atividades associadas a um curso.

### 5. Evidências

#### Endpoints
- **GET** `/api/evidencias/atividade/{atividadeId}` - Listar evidências associadas a uma atividade.
- **POST** `/api/evidencias` - Criar uma nova evidência.
- **PUT** `/api/evidencias/{evidenciaId}` - Atualizar informações de uma evidência.
- **DELETE** `/api/evidencias/{evidenciaId}` - Deletar uma evidência.

### 6. Recuperação de Senha

#### Endpoints
- **POST** `/api/recovery/generate` - Gerar código de recuperação e enviar por email.
- **POST** `/api/recovery/reset-password` - Redefinir senha usando o código de recuperação.

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

### Configuração de Email
Adicionar no `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu-email@gmail.com
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
Utilize variáveis de ambiente para o campo `EMAIL_PASSWORD` para proteger a senha.

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

---
Se precisar de ajustes adicionais ou implementar novos recursos, entre em contato!

