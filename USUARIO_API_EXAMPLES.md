# API de Gestão de Usuários - Exemplos de Uso

## Resumo das Melhorias Implementadas

### ✅ Correções Realizadas
1. **Endpoint DELETE** - Corrigido para retornar `200 OK` conforme documentação
2. **Endpoint checkAuthorities** - Agora retorna JSON com dados do usuário e permissões
3. **Atualização de CPF** - Implementada no método `PUT /api/usuarios/{usuarioId}`
4. **Validação de CPF duplicado** - Validação tanto na criação quanto na atualização
5. **Validação de Email duplicado** - Verificação melhorada no update
6. **Lógica de Cursos** - Corrigida para gerenciar corretamente associações bidirecionais
7. **Validações** - Adicionada anotação `@Validated` nos endpoints POST e PUT

---

## Endpoints Disponíveis

### 1. Verificar Autoridades do Usuário Logado
**GET** `/api/usuarios/checkAuthorities`

#### Resposta de Sucesso (200 OK)
```json
{
  "username": "admin@uea.edu.br",
  "authorities": [
    "ROLE_ADMINISTRADOR"
  ]
}
```

#### Exemplo de Uso
```bash
curl -X GET "http://localhost:8080/api/usuarios/checkAuthorities" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

---

### 2. Buscar Usuário por ID
**GET** `/api/usuarios/{usuarioId}`

**Permissão:** ADMINISTRADOR, GERENTE ou SECRETARIO

#### Resposta de Sucesso (200 OK)
```json
{
  "id": 1,
  "nome": "João Silva",
  "cpf": "12345678900",
  "email": "joao@uea.edu.br",
  "senha": null,
  "role": "ROLE_GERENTE",
  "cursos": [
    {
      "id": 1,
      "nome": "Engenharia de Software",
      "ativo": true
    }
  ]
}
```

#### Exemplo de Uso
```bash
curl -X GET "http://localhost:8080/api/usuarios/1" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

#### Possíveis Erros
- **403 Forbidden** - Usuário não tem permissão
- **404 Not Found** - Usuário não encontrado

---

### 3. Listar Todos os Usuários
**GET** `/api/usuarios`

**Permissão:** Apenas ADMINISTRADOR

#### Resposta de Sucesso (200 OK)
```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "cpf": "12345678900",
    "email": "joao@uea.edu.br",
    "senha": null,
    "role": "ROLE_GERENTE",
    "cursos": [
      {
        "id": 1,
        "nome": "Engenharia de Software",
        "ativo": true
      }
    ]
  },
  {
    "id": 2,
    "nome": "Maria Santos",
    "cpf": "98765432100",
    "email": "maria@uea.edu.br",
    "senha": null,
    "role": "ROLE_SECRETARIO",
    "cursos": [
      {
        "id": 2,
        "nome": "Ciência da Computação",
        "ativo": true
      }
    ]
  }
]
```

#### Exemplo de Uso
```bash
curl -X GET "http://localhost:8080/api/usuarios" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

---

### 4. Criar Novo Usuário
**POST** `/api/usuarios`

**Permissão:** Apenas ADMINISTRADOR

#### Request Body
```json
{
  "nome": "Carlos Eduardo",
  "cpf": "11122233344",
  "email": "carlos@uea.edu.br",
  "senha": "senha123",
  "role": "ROLE_GERENTE",
  "cursos": [
    {
      "id": 1,
      "nome": "Engenharia de Software",
      "ativo": true
    }
  ]
}
```

#### Resposta de Sucesso (201 Created)
```json
{
  "id": 3,
  "nome": "Carlos Eduardo",
  "cpf": "11122233344",
  "email": "carlos@uea.edu.br",
  "senha": null,
  "role": "ROLE_GERENTE",
  "cursos": [
    {
      "id": 1,
      "nome": "Engenharia de Software",
      "ativo": true
    }
  ]
}
```

#### Validações
- Nome é obrigatório
- Email é obrigatório e deve ser válido
- Email não pode estar duplicado
- CPF não pode estar duplicado
- Senha é obrigatória na criação
- Role deve existir no sistema

#### Exemplo de Uso
```bash
curl -X POST "http://localhost:8080/api/usuarios" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Carlos Eduardo",
    "cpf": "11122233344",
    "email": "carlos@uea.edu.br",
    "senha": "senha123",
    "role": "ROLE_GERENTE",
    "cursos": [
      {
        "id": 1,
        "nome": "Engenharia de Software",
        "ativo": true
      }
    ]
  }'
```

#### Possíveis Erros
- **403 Forbidden** - Usuário não tem permissão
- **400 Bad Request** - Dados inválidos (email já existe, CPF duplicado, etc.)

---

### 5. Atualizar Usuário
**PUT** `/api/usuarios/{usuarioId}`

**Permissão:** ADMINISTRADOR, GERENTE ou SECRETARIO

#### Request Body
```json
{
  "nome": "Carlos Eduardo Silva",
  "cpf": "11122233344",
  "email": "carlos.silva@uea.edu.br",
  "senha": "novaSenha123",
  "role": "ROLE_GERENTE",
  "cursos": [
    {
      "id": 1,
      "nome": "Engenharia de Software",
      "ativo": true
    },
    {
      "id": 2,
      "nome": "Ciência da Computação",
      "ativo": true
    }
  ]
}
```

#### Resposta de Sucesso (200 OK)
```json
{
  "id": 3,
  "nome": "Carlos Eduardo Silva",
  "cpf": "11122233344",
  "email": "carlos.silva@uea.edu.br",
  "senha": null,
  "role": "ROLE_GERENTE",
  "cursos": [
    {
      "id": 1,
      "nome": "Engenharia de Software",
      "ativo": true
    },
    {
      "id": 2,
      "nome": "Ciência da Computação",
      "ativo": true
    }
  ]
}
```

#### Validações
- CPF não pode ser duplicado (se alterado)
- Email não pode ser duplicado (se alterado)
- Senha é opcional (se não fornecida, mantém a atual)
- Remove associações antigas de cursos e adiciona novas

#### Exemplo de Uso
```bash
curl -X PUT "http://localhost:8080/api/usuarios/3" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Carlos Eduardo Silva",
    "cpf": "11122233344",
    "email": "carlos.silva@uea.edu.br",
    "role": "ROLE_GERENTE",
    "cursos": [
      {
        "id": 1,
        "nome": "Engenharia de Software",
        "ativo": true
      }
    ]
  }'
```

---

### 6. Alterar Senha do Usuário
**PUT** `/api/usuarios/{usuarioId}/change-password`

**Permissão:** 
- ADMINISTRADOR pode alterar qualquer senha
- Outros usuários podem alterar apenas sua própria senha

#### Request Body
```json
{
  "currentPassword": "senhaAtual123",
  "newPassword": "novaSenha456"
}
```

#### Validações
- Senha atual é obrigatória
- Nova senha é obrigatória
- Nova senha deve ter pelo menos 8 caracteres
- Senha atual deve estar correta

#### Resposta de Sucesso (200 OK)
```json
{
  "message": "Senha alterada com sucesso",
  "usuarioId": "3"
}
```

#### Exemplo de Uso
```bash
curl -X PUT "http://localhost:8080/api/usuarios/3/change-password" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "senha123",
    "newPassword": "novaSenha456"
  }'
```

#### Possíveis Erros
- **403 Forbidden** - Usuário não tem permissão para alterar esta senha
- **400 Bad Request** - Senha atual incorreta ou nova senha inválida
- **404 Not Found** - Usuário não encontrado

---

### 7. Deletar Usuário
**DELETE** `/api/usuarios/{usuarioId}`

**Permissão:** Apenas ADMINISTRADOR

#### Resposta de Sucesso (200 OK)
```
Corpo vazio
```

#### Exemplo de Uso
```bash
curl -X DELETE "http://localhost:8080/api/usuarios/3" \
  -H "Authorization: Bearer {seu-token-jwt}"
```

#### Possíveis Erros
- **403 Forbidden** - Usuário não tem permissão
- **404 Not Found** - Usuário não encontrado

---

## Regras de Negócio

### Roles Disponíveis
- `ROLE_ADMINISTRADOR` - Acesso total ao sistema
- `ROLE_GERENTE` - Gerencia cursos e atividades
- `ROLE_SECRETARIO` - Gerencia atividades e evidências

### Associação de Cursos
- **ADMINISTRADOR**: Automaticamente associado a todos os cursos
- **GERENTE/SECRETARIO**: Deve especificar os cursos na criação/atualização

### Segurança
- Senhas são sempre criptografadas usando BCrypt
- Tokens JWT expiram após 1 hora (configurável)
- CPF e Email são únicos no sistema

---

## Tratamento de Erros

### Erros Comuns

#### 400 Bad Request
```json
{
  "error": "CPF já cadastrado: 11122233344"
}
```

#### 403 Forbidden
```json
{
  "error": "Usuário não tem permissão para alterar a senha de: carlos@uea.edu.br"
}
```

#### 404 Not Found
```json
{
  "error": "Usuário não encontrado: 999"
}
```

---

## Testes com Swagger

Acesse a documentação interativa em:
- **URL:** `http://localhost:8080/swagger-ui.html`

### Como Autenticar no Swagger
1. Faça login no endpoint `/auth/login` para obter o token JWT
2. Clique no botão **"Authorize"** no topo da página
3. Digite: `Bearer {seu-token-jwt}`
4. Clique em **"Authorize"**
5. Agora você pode testar os endpoints diretamente na interface

---

## Notas Importantes

1. **Senha no Response**: Por questões de segurança, a senha nunca é retornada nos responses (sempre `null`)
2. **CPF Opcional**: O CPF é opcional, mas se fornecido, deve ser único
3. **Transações**: Todas as operações de criação/atualização são transacionais
4. **Cursos Bidirecionais**: A associação entre usuários e cursos é gerenciada automaticamente

