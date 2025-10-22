# API de Cursos - Documentação de Paginação

## Endpoints Atualizados

Os seguintes endpoints de busca de cursos foram atualizados para suportar paginação:

### 1. Buscar Todos os Cursos
```
GET /api/cursos
```

### 2. Buscar Meus Cursos (Cursos do Usuário Logado)
```
GET /api/cursos/usuarios
```
🔒 **Requer autenticação** e roles: `ADMINISTRADOR`, `GERENTE` ou `SECRETARIO`

## Parâmetros de Paginação e Filtros

A API agora aceita os seguintes parâmetros de query para controlar a paginação e filtros:

| Parâmetro | Tipo | Padrão | Obrigatório | Descrição |
|-----------|------|--------|-------------|-----------|
| `page` | integer | 0 | Não | Número da página (começando em 0) |
| `size` | integer | 10 | Não | Quantidade de itens por página |
| `sort` | string | nome | Não | Campo para ordenação (pode incluir direção: `nome,asc` ou `nome,desc`) |
| `ativo` | boolean | null | Não | Filtro por status: `true` (ativos), `false` (inativos), ou omitir para todos |
| `nome` | string | null | Não | Filtro por nome (busca por texto contido no nome, case insensitive) |

## Exemplos de Uso

### Endpoint: GET /api/cursos (Todos os Cursos)

#### 1. Buscar primeira página (padrão)
```
GET /api/cursos
```
Retorna os primeiros 10 cursos (ativos e inativos) ordenados por nome.

#### 2. Buscar apenas cursos ativos
```
GET /api/cursos?ativo=true
```
Retorna apenas cursos ativos.

#### 3. Buscar apenas cursos inativos
```
GET /api/cursos?ativo=false
```
Retorna apenas cursos inativos.

#### 4. Buscar página específica com filtro
```
GET /api/cursos?ativo=true&page=1&size=20
```
Retorna a 2ª página com 20 cursos ativos.

#### 5. Buscar com ordenação customizada
```
GET /api/cursos?sort=nome,desc
```
Retorna cursos ordenados por nome em ordem decrescente.

#### 6. Combinar filtro, paginação e ordenação
```
GET /api/cursos?ativo=true&page=0&size=15&sort=nome,asc
```
Retorna a primeira página com 15 cursos ativos, ordenados por nome.

#### 7. Buscar por nome ou parte do nome
```
GET /api/cursos?nome=sistemas
```
Retorna cursos que contenham "sistemas" no nome (ex: "Sistemas de Informação", "Análise de Sistemas").

#### 8. Combinar filtro de status e nome
```
GET /api/cursos?ativo=true&nome=eng
```
Retorna cursos ativos que contenham "eng" no nome (ex: "Engenharia Civil", "Engenharia Elétrica").

#### 9. Buscar por nome com paginação
```
GET /api/cursos?nome=administração&page=0&size=5
```
Retorna a primeira página com 5 cursos que contenham "administração" no nome.

#### 10. Múltiplos campos de ordenação
```
GET /api/cursos?sort=ativo,desc&sort=nome,asc
```
Ordena primeiro por status (ativos primeiro), depois por nome.

#### 11. Combinação completa de filtros
```
GET /api/cursos?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc
```
Retorna cursos ativos que contenham "ciência" no nome, primeira página com 10 itens, ordenados por nome.

### Endpoint: GET /api/cursos/usuarios (Meus Cursos)

#### 1. Buscar meus cursos (primeira página)
```
GET /api/cursos/usuarios
```
Retorna os primeiros 10 cursos do usuário logado, ordenados por nome.

#### 2. Buscar apenas meus cursos ativos
```
GET /api/cursos/usuarios?ativo=true
```
Retorna apenas os cursos ativos do usuário logado.

#### 3. Buscar apenas meus cursos inativos
```
GET /api/cursos/usuarios?ativo=false
```
Retorna apenas os cursos inativos do usuário logado.

#### 4. Buscar meus cursos por nome
```
GET /api/cursos/usuarios?nome=sistemas
```
Retorna os cursos do usuário que contenham "sistemas" no nome.

#### 5. Buscar meus cursos ativos por nome
```
GET /api/cursos/usuarios?ativo=true&nome=engenharia
```
Retorna cursos ativos do usuário que contenham "engenharia" no nome.

#### 6. Buscar meus cursos com paginação
```
GET /api/cursos/usuarios?page=1&size=5
```
Retorna a 2ª página com 5 cursos do usuário logado.

#### 7. Buscar meus cursos com ordenação
```
GET /api/cursos/usuarios?sort=ativo,desc&sort=nome,asc
```
Retorna cursos ativos primeiro, depois ordenados por nome.

#### 8. Combinação completa para meus cursos
```
GET /api/cursos/usuarios?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc
```
Retorna meus cursos ativos que contenham "ciência" no nome, paginados e ordenados.

## Formato da Resposta

A resposta retorna um objeto `Page` com os seguintes campos:

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Curso de Exemplo",
      "ativo": true
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 42,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "empty": false
}
```

### Campos Importantes da Resposta

- `content`: Array com os cursos da página atual
- `totalElements`: Total de cursos no banco de dados
- `totalPages`: Total de páginas disponíveis
- `size`: Quantidade de itens por página
- `number`: Número da página atual
- `first`: Indica se é a primeira página
- `last`: Indica se é a última página
- `empty`: Indica se a página está vazia

## Códigos de Status HTTP

- `200 OK`: Cursos encontrados e retornados
- `204 No Content`: Nenhum curso encontrado

## Notas Importantes

1. A paginação é **opcional**. Se você não passar parâmetros, a API usará os valores padrão.
2. O índice de páginas começa em **0** (zero).
3. O tamanho padrão da página é **10** itens.
4. A ordenação padrão é por **nome** em ordem alfabética.
5. O método antigo sem paginação foi substituído, mas a API mantém compatibilidade através dos parâmetros padrão.

## Exemplos com cURL

### Todos os Cursos
```bash
# Buscar primeira página (todos os cursos)
curl -X GET "http://localhost:8080/api/cursos" \
  -H "Authorization: Bearer {token}"

# Buscar apenas cursos ativos
curl -X GET "http://localhost:8080/api/cursos?ativo=true" \
  -H "Authorization: Bearer {token}"

# Buscar apenas cursos inativos
curl -X GET "http://localhost:8080/api/cursos?ativo=false" \
  -H "Authorization: Bearer {token}"

# Buscar cursos ativos com paginação
curl -X GET "http://localhost:8080/api/cursos?ativo=true&page=0&size=20" \
  -H "Authorization: Bearer {token}"

# Buscar cursos ativos com ordenação
curl -X GET "http://localhost:8080/api/cursos?ativo=true&sort=nome,desc" \
  -H "Authorization: Bearer {token}"

# Combinar filtro de status, paginação e ordenação
curl -X GET "http://localhost:8080/api/cursos?ativo=true&page=1&size=15&sort=nome,asc" \
  -H "Authorization: Bearer {token}"

# Buscar por nome
curl -X GET "http://localhost:8080/api/cursos?nome=sistemas" \
  -H "Authorization: Bearer {token}"

# Buscar cursos ativos por nome
curl -X GET "http://localhost:8080/api/cursos?ativo=true&nome=engenharia" \
  -H "Authorization: Bearer {token}"

# Combinação completa: status, nome, paginação e ordenação
curl -X GET "http://localhost:8080/api/cursos?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc" \
  -H "Authorization: Bearer {token}"
```

### Meus Cursos
```bash
# Buscar meus cursos (primeira página)
curl -X GET "http://localhost:8080/api/cursos/usuarios" \
  -H "Authorization: Bearer {token}"

# Buscar apenas meus cursos ativos
curl -X GET "http://localhost:8080/api/cursos/usuarios?ativo=true" \
  -H "Authorization: Bearer {token}"

# Buscar apenas meus cursos inativos
curl -X GET "http://localhost:8080/api/cursos/usuarios?ativo=false" \
  -H "Authorization: Bearer {token}"

# Buscar meus cursos por nome
curl -X GET "http://localhost:8080/api/cursos/usuarios?nome=sistemas" \
  -H "Authorization: Bearer {token}"

# Buscar meus cursos ativos que contenham "engenharia" no nome
curl -X GET "http://localhost:8080/api/cursos/usuarios?ativo=true&nome=engenharia" \
  -H "Authorization: Bearer {token}"

# Buscar meus cursos com paginação
curl -X GET "http://localhost:8080/api/cursos/usuarios?page=1&size=5" \
  -H "Authorization: Bearer {token}"

# Buscar meus cursos com ordenação
curl -X GET "http://localhost:8080/api/cursos/usuarios?sort=ativo,desc&sort=nome,asc" \
  -H "Authorization: Bearer {token}"

# Combinação completa: meus cursos ativos com "ciência", paginados
curl -X GET "http://localhost:8080/api/cursos/usuarios?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc" \
  -H "Authorization: Bearer {token}"
```

## Exemplos com JavaScript/Fetch

### Buscar Todos os Cursos (com filtros opcionais)
```javascript
// Buscar cursos com paginação e filtros opcionais por status e nome
async function buscarCursos(page = 0, size = 10, sort = 'nome,asc', ativo = null, nome = null) {
  // Construir URL com parâmetros
  const params = new URLSearchParams({
    page: page,
    size: size,
    sort: sort
  });
  
  // Adicionar filtro de status se informado
  if (ativo !== null) {
    params.append('ativo', ativo);
  }
  
  // Adicionar filtro de nome se informado
  if (nome !== null && nome.trim() !== '') {
    params.append('nome', nome);
  }
  
  const response = await fetch(
    `http://localhost:8080/api/cursos?${params.toString()}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  if (response.ok) {
    const data = await response.json();
    console.log(`Total de cursos: ${data.totalElements}`);
    console.log(`Página ${data.number + 1} de ${data.totalPages}`);
    return data.content;
  }
  
  return [];
}

// Exemplos de uso:
// Buscar todos os cursos (primeira página)
const todosCursos = await buscarCursos();

// Buscar apenas cursos ativos
const cursosAtivos = await buscarCursos(0, 10, 'nome,asc', true);

// Buscar apenas cursos inativos
const cursosInativos = await buscarCursos(0, 10, 'nome,asc', false);

// Buscar cursos por nome
const cursosSistemas = await buscarCursos(0, 10, 'nome,asc', null, 'sistemas');

// Buscar cursos ativos que contenham "engenharia" no nome
const cursosEngenharia = await buscarCursos(0, 10, 'nome,asc', true, 'engenharia');

// Buscar cursos ativos na segunda página com 20 itens
const cursosAtivosPag2 = await buscarCursos(1, 20, 'nome,asc', true);

// Combinação completa: cursos ativos com "ciência" no nome, paginados
const cursosCiencia = await buscarCursos(0, 10, 'nome,asc', true, 'ciência');
```

### Buscar Meus Cursos (com filtros opcionais)
```javascript
// Buscar cursos do usuário logado com filtros opcionais
async function buscarMeusCursos(page = 0, size = 10, sort = 'nome,asc', ativo = null, nome = null) {
  const params = new URLSearchParams({
    page: page,
    size: size,
    sort: sort
  });
  
  // Adicionar filtro de status se informado
  if (ativo !== null) {
    params.append('ativo', ativo);
  }
  
  // Adicionar filtro de nome se informado
  if (nome !== null && nome.trim() !== '') {
    params.append('nome', nome);
  }
  
  const response = await fetch(
    `http://localhost:8080/api/cursos/usuarios?${params.toString()}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  if (response.ok) {
    const data = await response.json();
    console.log(`Meus cursos: ${data.totalElements}`);
    return data.content;
  }
  
  return [];
}

// Exemplos de uso:
// Buscar todos os meus cursos
const meusCursos = await buscarMeusCursos();

// Buscar apenas meus cursos ativos
const meusCursosAtivos = await buscarMeusCursos(0, 10, 'nome,asc', true);

// Buscar meus cursos que contenham "sistemas" no nome
const meusCursosSistemas = await buscarMeusCursos(0, 10, 'nome,asc', null, 'sistemas');

// Buscar meus cursos ativos com "engenharia" no nome
const meusCursosEngenhariaAtivos = await buscarMeusCursos(0, 10, 'nome,asc', true, 'engenharia');

// Buscar meus cursos inativos
const meusCursosInativos = await buscarMeusCursos(0, 10, 'nome,asc', false);
```

## Filtros Disponíveis

Os filtros descritos abaixo estão disponíveis em **ambos** os endpoints:
- `GET /api/cursos` (Todos os cursos)
- `GET /api/cursos/usuarios` (Meus cursos)

### 1. Filtro por Status (`ativo`)

O parâmetro `ativo` permite filtrar cursos por seu status:

- **`ativo=true`**: Retorna apenas cursos ativos
- **`ativo=false`**: Retorna apenas cursos inativos
- **Sem parâmetro**: Retorna todos os cursos (ativos e inativos)

**Exemplos:**
```
GET /api/cursos?ativo=true
GET /api/cursos/usuarios?ativo=true
```

### 2. Filtro por Nome (`nome`)

O parâmetro `nome` permite buscar cursos que contenham o texto informado no nome:

- **Case Insensitive**: Não diferencia maiúsculas de minúsculas
- **Busca Parcial**: Busca por texto contido no nome (não precisa ser o nome completo)
- **Sem parâmetro**: Retorna todos os cursos

**Exemplos:**
```
GET /api/cursos?nome=sistemas
# Retorna: "Sistemas de Informação", "Análise de Sistemas", etc.

GET /api/cursos/usuarios?nome=eng
# Retorna meus cursos: "Engenharia Civil", "Engenharia Elétrica", etc.
```

### 3. Combinando Filtros

Você pode combinar múltiplos filtros para resultados mais precisos em **ambos** os endpoints:

**Todos os Cursos:**
```
# Cursos ativos que contenham "engenharia" no nome
GET /api/cursos?ativo=true&nome=engenharia

# Cursos inativos com "administração" no nome
GET /api/cursos?ativo=false&nome=administração

# Cursos ativos com "ciência", paginados e ordenados
GET /api/cursos?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc
```

**Meus Cursos:**
```
# Meus cursos ativos que contenham "engenharia" no nome
GET /api/cursos/usuarios?ativo=true&nome=engenharia

# Meus cursos inativos com "sistemas" no nome
GET /api/cursos/usuarios?ativo=false&nome=sistemas

# Meus cursos ativos com "direito", paginados e ordenados
GET /api/cursos/usuarios?ativo=true&nome=direito&page=0&size=5&sort=nome,asc
```

### Casos de Uso

1. **Dashboard de Administração**: Filtrar cursos ativos para gerenciamento
2. **Arquivo de Cursos**: Visualizar apenas cursos inativos/arquivados
3. **Busca de Cursos**: Permitir aos usuários buscar cursos por nome
4. **Relatórios**: Gerar estatísticas separadas por status ou área
5. **Interface de Usuário**: Filtros dinâmicos para melhor experiência
6. **Autocomplete**: Implementar sugestões de cursos ao digitar

## Benefícios da Paginação e Filtros

1. **Performance**: Reduz a carga no servidor e no cliente ao buscar apenas os dados necessários
2. **Escalabilidade**: Permite trabalhar com grandes volumes de dados
3. **UX Melhorada**: Facilita a navegação e exibição dos dados na interface
4. **Economia de Banda**: Transfere apenas os dados da página solicitada
5. **Flexibilidade**: Filtros permitem visualizações personalizadas dos dados
6. **Eficiência**: Reduz processamento desnecessário ao filtrar no banco de dados

