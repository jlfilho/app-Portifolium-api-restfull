# Resumo das Alterações - API de Cursos

## 📋 Alterações Implementadas

### 1. Paginação em Todos os Endpoints de Listagem

#### ✅ Endpoint: `GET /api/cursos`
- **Antes**: Retornava `List<CursoDTO>` (todos os cursos de uma vez)
- **Depois**: Retorna `Page<CursoDTO>` (paginado)
- **Parâmetros Padrão**:
  - `size`: 10 itens por página
  - `sort`: ordenado por nome

#### ✅ Endpoint: `GET /api/cursos/usuarios`
- **Antes**: Retornava `List<CursoDTO>` (todos os cursos do usuário)
- **Depois**: Retorna `Page<CursoDTO>` (paginado com filtros)
- **Parâmetros Padrão**:
  - `size`: 10 itens por página
  - `sort`: ordenado por nome
- **Filtros Disponíveis**:
  - `ativo`: Filtrar por status (true/false)
  - `nome`: Buscar por nome (case insensitive, busca parcial)
- **Autenticação**: Requer roles `ADMINISTRADOR`, `GERENTE` ou `SECRETARIO`

### 2. Filtros Avançados

#### ✅ Filtro por Status (`ativo`)
- **Tipo**: Boolean (opcional)
- **Valores**:
  - `true`: Retorna apenas cursos ativos
  - `false`: Retorna apenas cursos inativos
  - `null` ou omitido: Retorna todos os cursos

#### ✅ Filtro por Nome (`nome`)
- **Tipo**: String (opcional)
- **Funcionalidade**:
  - Busca parcial (LIKE) no nome do curso
  - Case insensitive (não diferencia maiúsculas/minúsculas)
  - Busca por texto contido no nome
  - `null` ou omitido: Retorna todos os cursos

#### ✅ Combinação de Filtros
- Possibilidade de combinar `ativo` + `nome`
- Exemplo: `GET /api/cursos?ativo=true&nome=engenharia`

#### ✅ Aplicado aos Endpoints:
- `GET /api/cursos` - Todos os cursos
- `GET /api/cursos/usuarios` - Meus cursos

## 🔧 Arquivos Modificados

### Backend

1. **CursoRepository.java**
   - Adicionado método `findByAtivo(Boolean ativo, Pageable pageable)`
   - Adicionado método `findByNomeContainingIgnoreCase(String nome, Pageable pageable)`
   - Adicionado método `findByAtivoAndNomeContainingIgnoreCase(Boolean ativo, String nome, Pageable pageable)`
   - Adicionado método `findCursosByUsuarioIdPaginado(Long usuarioId, Pageable pageable)`
   - Adicionado método `findCursosByUsuarioIdAndAtivo(Long usuarioId, Boolean ativo, Pageable pageable)`
   - Adicionado método `findCursosByUsuarioIdAndNomeContaining(Long usuarioId, String nome, Pageable pageable)`
   - Adicionado método `findCursosByUsuarioIdAndAtivoAndNomeContaining(Long usuarioId, Boolean ativo, String nome, Pageable pageable)`

2. **CursoService.java**
   - Adicionado método `getAllCursosPaginado(Pageable pageable)`
   - Adicionado método `getAllCursosPaginadoComFiltro(Boolean ativo, Pageable pageable)`
   - Adicionado método `getAllCursosPaginadoComFiltros(Boolean ativo, String nome, Pageable pageable)`
   - Adicionado método `getCursosByUsuarioIdPaginado(Long usuarioId, Pageable pageable)`
   - Adicionado método `getCursosByUsuarioIdPaginadoComFiltros(Long usuarioId, Boolean ativo, String nome, Pageable pageable)`

3. **CursoController.java**
   - Atualizado `buscarTodosCursos()` para aceitar paginação e filtros por status e nome
   - Atualizado `getCursosByUsuarioId()` para aceitar paginação e filtros por status e nome
   - Adicionados imports: `Page`, `Pageable`, `PageableDefault`, `@RequestParam`

### Documentação

4. **CURSO_API_PAGINACAO.md** (novo)
   - Documentação completa sobre paginação
   - Exemplos de uso com todos os parâmetros
   - Exemplos com cURL
   - Exemplos com JavaScript/Fetch
   - Explicação sobre filtros
   - Casos de uso

5. **RESUMO_ALTERACOES_CURSOS.md** (este arquivo)
   - Resumo das alterações implementadas

## 📊 Parâmetros Disponíveis

| Parâmetro | Tipo | Padrão | Obrigatório | Descrição |
|-----------|------|--------|-------------|-----------|
| `page` | integer | 0 | Não | Número da página (começando em 0) |
| `size` | integer | 10 | Não | Quantidade de itens por página |
| `sort` | string | nome | Não | Campo para ordenação (ex: `nome,asc` ou `nome,desc`) |
| `ativo` | boolean | null | Não | Filtro por status em `/api/cursos` e `/api/cursos/usuarios` |
| `nome` | string | null | Não | Filtro por nome - busca parcial case insensitive em `/api/cursos` e `/api/cursos/usuarios` |

## 🎯 Exemplos de Uso

### Buscar Todos os Cursos

```bash
# Todos os cursos (primeira página)
GET /api/cursos

# Apenas cursos ativos
GET /api/cursos?ativo=true

# Apenas cursos inativos
GET /api/cursos?ativo=false

# Buscar por nome
GET /api/cursos?nome=sistemas

# Buscar cursos ativos com "engenharia" no nome
GET /api/cursos?ativo=true&nome=engenharia

# Cursos ativos, página 2, 20 itens, ordenado por nome
GET /api/cursos?ativo=true&page=1&size=20&sort=nome,asc

# Combinação completa: cursos ativos com "ciência", paginados
GET /api/cursos?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc
```

### Buscar Meus Cursos

```bash
# Meus cursos (primeira página)
GET /api/cursos/usuarios

# Apenas meus cursos ativos
GET /api/cursos/usuarios?ativo=true

# Apenas meus cursos inativos
GET /api/cursos/usuarios?ativo=false

# Meus cursos que contenham "sistemas" no nome
GET /api/cursos/usuarios?nome=sistemas

# Meus cursos ativos com "engenharia" no nome
GET /api/cursos/usuarios?ativo=true&nome=engenharia

# Meus cursos, página 2, 15 itens
GET /api/cursos/usuarios?page=1&size=15

# Meus cursos ordenados por status e nome
GET /api/cursos/usuarios?sort=ativo,desc&sort=nome,asc

# Combinação completa: meus cursos ativos com "ciência", paginados
GET /api/cursos/usuarios?ativo=true&nome=ciência&page=0&size=10&sort=nome,asc
```

## 📦 Formato da Resposta

Todos os endpoints paginados retornam um objeto `Page` com:

```json
{
  "content": [ /* Array de CursoDTO */ ],
  "totalElements": 42,      // Total de registros
  "totalPages": 5,          // Total de páginas
  "size": 10,               // Itens por página
  "number": 0,              // Página atual
  "first": true,            // É a primeira página?
  "last": false,            // É a última página?
  "numberOfElements": 10,   // Itens nesta página
  "empty": false            // Está vazia?
}
```

## ✨ Benefícios

1. **Performance**: Reduz carga no servidor e cliente
2. **Escalabilidade**: Suporta grandes volumes de dados
3. **UX**: Melhor navegação e exibição de dados
4. **Flexibilidade**: Filtros permitem visualizações personalizadas
5. **Eficiência**: Filtragem no banco de dados
6. **Compatibilidade**: Mantém compatibilidade com parâmetros padrão

## 🔄 Retrocompatibilidade

Os endpoints mantêm retrocompatibilidade:
- Se nenhum parâmetro for informado, usa valores padrão (página 0, 10 itens)
- O comportamento padrão é retornar a primeira página com 10 itens
- Aplicações existentes continuam funcionando sem alterações

## 🧪 Testando

### Com cURL
```bash
curl -X GET "http://localhost:8080/api/cursos?ativo=true&page=0&size=5" \
  -H "Authorization: Bearer {seu_token}"
```

### Com JavaScript
```javascript
// Buscar cursos ativos que contenham "engenharia" no nome
const params = new URLSearchParams({
  page: 0,
  size: 10,
  sort: 'nome,asc',
  ativo: true,
  nome: 'engenharia'
});

const response = await fetch(
  `http://localhost:8080/api/cursos?${params.toString()}`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);

const data = await response.json();
console.log(`Total: ${data.totalElements} cursos encontrados`);
console.log(data.content); // Array de cursos

// Ou usando a função completa com todos os parâmetros
async function buscarCursos(page = 0, size = 10, sort = 'nome,asc', ativo = null, nome = null) {
  const params = new URLSearchParams({ page, size, sort });
  if (ativo !== null) params.append('ativo', ativo);
  if (nome !== null && nome.trim() !== '') params.append('nome', nome);
  
  const response = await fetch(
    `http://localhost:8080/api/cursos?${params.toString()}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  
  return response.ok ? await response.json() : null;
}

// Exemplos de uso:
const cursosAtivos = await buscarCursos(0, 10, 'nome,asc', true);
const cursosSistemas = await buscarCursos(0, 10, 'nome,asc', null, 'sistemas');
const cursosAtivosCiencia = await buscarCursos(0, 10, 'nome,asc', true, 'ciência');
```

## 📝 Notas Importantes

1. ✅ Sem erros de linter
2. ✅ Todos os métodos antigos mantidos para compatibilidade
3. ✅ Documentação completa criada e atualizada
4. ✅ Filtros são opcionais e podem ser combinados
5. ✅ Paginação funciona em todos os endpoints de listagem
6. ✅ Suporte a múltiplos campos de ordenação
7. ✅ Busca por nome é case insensitive e parcial
8. ✅ Validação de strings vazias no filtro de nome
9. ✅ Performance otimizada com queries específicas no repository

## 🎨 Características dos Filtros

### Filtro por Nome
- **Case Insensitive**: "sistemas" encontra "Sistemas", "SISTEMAS", "SiStEmAs"
- **Busca Parcial**: "eng" encontra "Engenharia Civil", "Engenharia Elétrica"
- **Trim Automático**: Espaços em branco são removidos automaticamente
- **Compatível com Caracteres Especiais**: Suporta acentuação e caracteres especiais

### Combinação de Filtros
- Filtros podem ser usados isoladamente ou combinados
- A combinação usa operador AND (ambos devem ser satisfeitos)
- Queries otimizadas para cada combinação de filtros

## 🚀 Próximos Passos Sugeridos

- [x] ~~Implementar filtros adicionais (busca por nome)~~ ✅ Concluído
- [x] ~~Adicionar filtro por status e nome em "Meus Cursos"~~ ✅ Concluído
- [ ] Implementar paginação e filtros em outros controllers (Atividades, Evidências, etc.)
- [ ] Criar testes unitários para os novos endpoints
- [ ] Adicionar cache para melhorar performance
- [ ] Implementar busca fuzzy (busca com tolerância a erros)
- [ ] Adicionar filtros adicionais (data de criação, categoria, etc.)
- [ ] Implementar ordenação customizada por múltiplos campos
- [ ] Adicionar endpoint para exportação de cursos (CSV, Excel)

