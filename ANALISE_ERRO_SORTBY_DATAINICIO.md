# 🔴 Análise do Erro - sortBy com dataInicio

## 📋 Erro Reportado

```
http://localhost:8080/api/atividades/filtros?page=0&size=9&sortBy=dataInicio&sortDirection=DESC&cursoId=1

ERROR: Could not resolve attribute 'dataInicio' of 'edu.uea.acadmanage.model.Atividade'
```

---

## 🔍 Causa Raiz

### 1. O Campo Correto na Entidade é `dataRealizacao`

**Arquivo: `Atividade.java` (linha 45)**
```java
@Column(nullable = false)
private LocalDate dataRealizacao;  // ✅ Nome CORRETO do campo
```

### 2. O Usuário Está Usando `dataInicio` no sortBy

**Requisição:**
```
sortBy=dataInicio  // ❌ Campo INCORRETO
```

### 3. O Controller Não Valida o Campo de Ordenação

**Arquivo: `AtividadeController.java` (linha 81)**
```java
@RequestParam(defaultValue = "id") String sortBy  // ❌ Aceita QUALQUER string
```

Depois passa direto para o Sort:
```java
Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
```

---

## ⚠️ O Problema

O controller aceita **qualquer** valor para `sortBy` sem validação, causando:

1. ❌ Erro de execução quando o campo não existe
2. ❌ Mensagem de erro técnica exposta ao usuário
3. ❌ Falta de documentação sobre campos válidos
4. ❌ Possíveis vulnerabilidades de segurança (SQL injection via sorting)

---

## ✅ Soluções

### Solução 1: Mapeamento de Campos (RECOMENDADO)

Criar um mapeamento entre nomes amigáveis (frontend) e nomes reais (backend).

**Adicionar ao Controller:**

```java
private static final Map<String, String> FIELD_MAPPING = Map.of(
    "id", "id",
    "nome", "nome",
    "dataInicio", "dataRealizacao",     // Mapeamento dataInicio → dataRealizacao
    "dataRealizacao", "dataRealizacao",
    "statusPublicacao", "statusPublicacao",
    "curso", "curso.nome",
    "categoria", "categoria.nome"
);

private static final Set<String> ALLOWED_SORT_FIELDS = FIELD_MAPPING.keySet();

@GetMapping("/filtros")
public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltros(
        @RequestParam(required = false) Long cursoId,
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        @RequestParam(required = false) Boolean statusPublicacao,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

    // Validar e mapear o campo de ordenação
    String mappedSortBy = FIELD_MAPPING.getOrDefault(sortBy, "id");
    
    // Ou lançar exceção se o campo não for válido:
    if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
        throw new IllegalArgumentException(
            "Campo de ordenação inválido: " + sortBy + 
            ". Campos permitidos: " + String.join(", ", ALLOWED_SORT_FIELDS)
        );
    }

    AtividadeFiltroDTO filtros = new AtividadeFiltroDTO(cursoId, categoriaId, nome, dataInicio, dataFim,
            statusPublicacao);

    Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mappedSortBy));

    Page<AtividadeDTO> atividades = atividadeService.getAtividadesPorFiltrosPaginado(filtros, pageable);

    return atividades.isEmpty()
            ? ResponseEntity.noContent().build()
            : ResponseEntity.ok(atividades);
}
```

---

### Solução 2: Validação com Enum

Criar um Enum com os campos válidos:

```java
public enum AtividadeSortField {
    ID("id"),
    NOME("nome"),
    DATA_INICIO("dataRealizacao"),      // Frontend usa dataInicio
    DATA_REALIZACAO("dataRealizacao"),  // Backend usa dataRealizacao
    STATUS_PUBLICACAO("statusPublicacao"),
    CURSO("curso.nome"),
    CATEGORIA("categoria.nome");

    private final String fieldName;

    AtividadeSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static String getFieldNameByKey(String key) {
        return Arrays.stream(values())
            .filter(field -> field.name().equalsIgnoreCase(key.replace("_", "")))
            .findFirst()
            .map(AtividadeSortField::getFieldName)
            .orElse("id");
    }
}
```

---

### Solução 3: Usar @Valid e DTO

Criar um DTO específico para os parâmetros de paginação:

```java
public class PageRequestDTO {
    @Min(0)
    private int page = 0;
    
    @Min(1)
    @Max(100)
    private int size = 10;
    
    @Pattern(regexp = "id|nome|dataInicio|dataRealizacao|statusPublicacao")
    private String sortBy = "id";
    
    @Pattern(regexp = "ASC|DESC")
    private String sortDirection = "ASC";
    
    // getters, setters
}
```

---

## 🛠️ Correção Rápida (Mínima)

Se você quiser apenas corrigir rapidamente sem grandes mudanças:

**Opção A: Documentar o campo correto**
- Informar ao frontend para usar `sortBy=dataRealizacao` ao invés de `dataInicio`

**Opção B: Adicionar mapeamento simples**

```java
@GetMapping("/filtros")
public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltros(
        // ... parâmetros ...
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

    // Mapeamento rápido
    String realSortField = sortBy.equals("dataInicio") ? "dataRealizacao" : sortBy;

    AtividadeFiltroDTO filtros = new AtividadeFiltroDTO(cursoId, categoriaId, nome, dataInicio, dataFim,
            statusPublicacao);

    Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, realSortField));

    Page<AtividadeDTO> atividades = atividadeService.getAtividadesPorFiltrosPaginado(filtros, pageable);

    return atividades.isEmpty()
            ? ResponseEntity.noContent().build()
            : ResponseEntity.ok(atividades);
}
```

---

## 📊 Campos Disponíveis para Ordenação

### Na Entidade Atividade:

| Campo Frontend | Campo Backend | Tipo | Descrição |
|----------------|---------------|------|-----------|
| `id` | `id` | Long | ID da atividade |
| `nome` | `nome` | String | Nome da atividade |
| `dataInicio` ❌ | `dataRealizacao` ✅ | LocalDate | Data de realização |
| `dataRealizacao` | `dataRealizacao` | LocalDate | Data de realização |
| `statusPublicacao` | `statusPublicacao` | Boolean | Se está publicada |
| `curso` | `curso.nome` | String | Nome do curso |
| `categoria` | `categoria.nome` | String | Nome da categoria |

---

## 🎯 Recomendação Final

1. ✅ **Implementar mapeamento de campos** (Solução 1)
2. ✅ **Validar campos de ordenação**
3. ✅ **Retornar erro 400 com mensagem clara** se campo for inválido
4. ✅ **Documentar campos permitidos** na API
5. ✅ **Atualizar frontend** para usar os campos corretos

---

## 📝 Checklist

- [ ] Adicionar mapeamento de campos no controller
- [ ] Validar campo de ordenação antes de usar
- [ ] Tratar exceção com mensagem clara
- [ ] Documentar campos válidos no Swagger
- [ ] Atualizar frontend para usar campos corretos
- [ ] Testar todos os campos de ordenação
- [ ] Adicionar testes unitários

---

## 🚀 Status

**Atual**: ❌ Campo `dataInicio` não existe na entidade  
**Esperado**: ✅ Mapeamento automático de `dataInicio` → `dataRealizacao`

---

## 💡 Observação Importante

O frontend está usando a nomenclatura `dataInicio`, que faz sentido do ponto de vista semântico (data de início da atividade). Porém, no backend, o campo se chama `dataRealizacao`.

**Opções:**
1. Manter o backend como está e fazer mapeamento no controller ✅ Recomendado
2. Renomear o campo no backend para `dataInicio` (requer migração de banco)
3. Adicionar um alias na entidade JPA (complexo)

