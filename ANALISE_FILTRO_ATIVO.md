# Análise do Filtro `ativo` - Cursos

## ✅ Resumo da Análise

A implementação do filtro `ativo` está **CORRETA** no código Java, mas havia um **problema no SQL de inicialização** que foi corrigido.

---

## 🔍 Análise Completa

### 1. Model (Curso.java) ✅

```java
private Boolean ativo = true;
```

**Status**: ✅ **CORRETO**

- Usa `Boolean` (tipo wrapper), não `boolean` (primitivo)
- Isso permite valores `null`, `true` ou `false`
- Valor padrão `true` aplicado quando criado via Java

### 2. Repository (CursoRepository.java) ✅

```java
Page<Curso> findByAtivo(Boolean ativo, Pageable pageable);

@Query("SELECT c FROM Curso c JOIN c.usuarios u WHERE u.id = :usuarioId AND c.ativo = :ativo")
Page<Curso> findCursosByUsuarioIdAndAtivo(...);
```

**Status**: ✅ **CORRETO**

- Queries usam comparação de igualdade (`c.ativo = :ativo`)
- Funciona corretamente com valores `true` ou `false`
- Spring Data JPA gera queries otimizadas

### 3. Service (CursoService.java) ✅

```java
public Page<CursoDTO> getAllCursosPaginadoComFiltros(Boolean ativo, String nome, Pageable pageable) {
    if (ativo != null && nome != null && !nome.trim().isEmpty()) {
        // Filtrar por status E nome
        cursos = cursoRepository.findByAtivoAndNomeContainingIgnoreCase(ativo, nome.trim(), pageable);
    } else if (ativo != null) {
        // Filtrar apenas por status
        cursos = cursoRepository.findByAtivo(ativo, pageable);
    } else if (nome != null && !nome.trim().isEmpty()) {
        // Filtrar apenas por nome
        cursos = cursoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
    } else {
        // Sem filtros, retorna todos
        cursos = cursoRepository.findAll(pageable);
    }
    return cursos.map(curso -> new CursoDTO(curso.getId(), curso.getNome(), curso.getAtivo()));
}
```

**Status**: ✅ **CORRETO**

- Verifica se `ativo != null` antes de aplicar o filtro
- Se `ativo` for `null`, não aplica filtro de status
- Lógica condicional está correta e otimizada

### 4. Controller (CursoController.java) ✅

```java
@GetMapping
public ResponseEntity<Page<CursoDTO>> buscarTodosCursos(
        @RequestParam(required = false) Boolean ativo,
        @RequestParam(required = false) String nome,
        @PageableDefault(size = 10, sort = "nome") Pageable pageable)
```

**Status**: ✅ **CORRETO**

- `@RequestParam(required = false)` permite que o parâmetro seja omitido
- Se omitido, o valor será `null`
- Se fornecido, aceita `true` ou `false`

---

## 🔴 Problema Encontrado e Corrigido

### Arquivo: `data.sql`

**ANTES** (❌ INCORRETO):
```sql
INSERT INTO curso (id, nome) VALUES
(1, 'Curso de Engenharia de Software'),
(2, 'Curso de Sistemas de Informação'),
(3, 'Curso de Ciência da Computação');
```

**DEPOIS** (✅ CORRETO):
```sql
INSERT INTO curso (id, nome, ativo) VALUES
(1, 'Curso de Engenharia de Software', true),
(2, 'Curso de Sistemas de Informação', true),
(3, 'Curso de Ciência da Computação', true);
```

### Por que isso é importante?

1. **Valor padrão Java não se aplica a INSERT SQL direto**
   - O `private Boolean ativo = true;` no Java só funciona quando você cria objetos via código
   - Inserções SQL diretas ignoram valores padrão do Java

2. **Sem especificar o campo no SQL:**
   - O campo pode ficar `NULL` no banco
   - Cursos com `ativo = NULL` não são encontrados pelo filtro `?ativo=true`
   - Cursos com `ativo = NULL` não são encontrados pelo filtro `?ativo=false`
   - Isso causa inconsistência nos resultados

3. **Com a correção:**
   - Todos os cursos têm valor explícito (`true`)
   - Filtros funcionam corretamente
   - Comportamento consistente e previsível

---

## 📊 Como o Filtro Funciona

### Cenários de Uso

#### 1. Sem filtro (ativo não informado)
```
GET /api/cursos
GET /api/cursos/usuarios
```
- Valor de `ativo`: `null`
- Comportamento: Retorna **TODOS** os cursos (ativos e inativos)

#### 2. Filtro por cursos ativos
```
GET /api/cursos?ativo=true
GET /api/cursos/usuarios?ativo=true
```
- Valor de `ativo`: `true`
- Comportamento: Retorna **APENAS** cursos com `ativo = true`

#### 3. Filtro por cursos inativos
```
GET /api/cursos?ativo=false
GET /api/cursos/usuarios?ativo=false
```
- Valor de `ativo`: `false`
- Comportamento: Retorna **APENAS** cursos com `ativo = false`

---

## 🧪 Testando o Filtro

### Teste 1: Buscar todos os cursos
```bash
curl -X GET "http://localhost:8080/api/cursos" \
  -H "Authorization: Bearer {token}"
```
**Esperado**: Retorna todos os cursos (ativos e inativos)

### Teste 2: Buscar apenas cursos ativos
```bash
curl -X GET "http://localhost:8080/api/cursos?ativo=true" \
  -H "Authorization: Bearer {token}"
```
**Esperado**: Retorna apenas cursos com `ativo = true`

### Teste 3: Buscar apenas cursos inativos
```bash
curl -X GET "http://localhost:8080/api/cursos?ativo=false" \
  -H "Authorization: Bearer {token}"
```
**Esperado**: Retorna apenas cursos com `ativo = false`

### Teste 4: Combinar filtros
```bash
curl -X GET "http://localhost:8080/api/cursos?ativo=true&nome=engenharia" \
  -H "Authorization: Bearer {token}"
```
**Esperado**: Retorna cursos ativos que contenham "engenharia" no nome

---

## ✅ Conclusão

### Pontos Positivos
1. ✅ Código Java implementado corretamente
2. ✅ Lógica de filtros otimizada
3. ✅ Queries eficientes no banco de dados
4. ✅ Parâmetros opcionais funcionando
5. ✅ SQL de inicialização corrigido

### Recomendações

1. **Migração de Dados** (se já houver cursos em produção):
   ```sql
   UPDATE curso SET ativo = true WHERE ativo IS NULL;
   ```

2. **Constraint no Banco** (opcional):
   ```sql
   ALTER TABLE curso ALTER COLUMN ativo SET DEFAULT true;
   ALTER TABLE curso ALTER COLUMN ativo SET NOT NULL;
   ```

3. **Validação no Service** (opcional adicional):
   ```java
   if (cursoDTO.ativo() == null) {
       novoCurso.setAtivo(true); // Garantir valor padrão
   }
   ```

---

## 📝 Checklist Final

- [x] Model com tipo correto (`Boolean` wrapper)
- [x] Repository com queries corretas
- [x] Service com lógica de filtros otimizada
- [x] Controller com parâmetros opcionais
- [x] SQL de inicialização corrigido
- [x] Documentação atualizada
- [x] Sem erros de linter

## 🎉 Status: **APROVADO**

O filtro `ativo` está funcionando corretamente após a correção no arquivo `data.sql`!

