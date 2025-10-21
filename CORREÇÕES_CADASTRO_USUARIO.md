# ✅ Correções Aplicadas - Cadastro de Usuário

## 📋 Resumo dos Problemas Resolvidos

Durante o desenvolvimento e teste do endpoint `POST /api/usuarios`, foram identificados e corrigidos **3 problemas críticos**:

---

## 🐛 **Problema 1: Validação do CursoDTO**

### Sintoma
Erro ao enviar cursos com apenas o campo `id`:
```json
"cursos": [
  { "id": 1 },
  { "id": 2 }
]
```

### Causa
O `CursoDTO` tinha a anotação `@NotBlank` no campo `nome`, tornando-o obrigatório mesmo quando apenas referenciando cursos existentes.

### Solução Aplicada

#### 1. **CursoDTO.java**
```java
// REMOVIDA a validação @NotBlank do campo nome
public record CursoDTO(
    Long id,
    String nome,  // Agora é opcional
    Boolean ativo
) {}
```

#### 2. **CursoService.java**
Adicionadas validações contextuais nos métodos que criam/atualizam cursos:
```java
public CursoDTO saveCurso(CursoDTO cursoDTO, Usuario usuario) {
    if (cursoDTO.nome() == null || cursoDTO.nome().trim().isEmpty()) {
        throw new IllegalArgumentException("O nome do curso é obrigatório");
    }
    // ...
}

public CursoDTO updateCurso(Long cursoId, CursoDTO cursoDTO) {
    if (cursoDTO.nome() == null || cursoDTO.nome().trim().isEmpty()) {
        throw new IllegalArgumentException("O nome do curso é obrigatório");
    }
    // ...
}
```

---

## 🐛 **Problema 2: Relacionamento Usuario ↔ Pessoa**

### Sintoma
```
HTTP 500 - TransientPropertyValueException: 
object references an unsaved transient instance - 
save the transient instance before flushing : 
edu.uea.acadmanage.model.Usuario.pessoa -> edu.uea.acadmanage.model.Pessoa
```

### Causa
1. **Cascade incorreto:** Usuario tinha apenas `CascadeType.MERGE`, faltava `CascadeType.PERSIST`
2. **Relacionamento bidirecional incorreto:** Ambos Usuario e Pessoa tinham `@JoinColumn`, criando duas foreign keys

### Solução Aplicada

#### 1. **Usuario.java** (linha 47)
```java
// ANTES
@OneToOne(cascade = CascadeType.MERGE)
@JoinColumn(name = "pessoa_id", referencedColumnName = "id")
private Pessoa pessoa;

// DEPOIS
@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinColumn(name = "pessoa_id", referencedColumnName = "id")
private Pessoa pessoa;
```

#### 2. **Pessoa.java** (linhas 35-37)
```java
// ANTES
@OneToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "usuario_id", referencedColumnName = "id")
private Usuario usuario;

// DEPOIS
@OneToOne(mappedBy = "pessoa")
@JsonIgnore
private Usuario usuario;
```

**Explicação:**
- `Usuario` é o **lado dono** do relacionamento (tem a foreign key)
- `Pessoa` é o **lado inverso** (usa `mappedBy`)
- `@JsonIgnore` previne loops de serialização

---

## 🐛 **Problema 3: StackOverflowError - Recursão Infinita**

### Sintoma
```
HTTP 500 - StackOverflowError
Handler dispatch failed: java.lang.StackOverflowError
```

### Causa
Relacionamento bidirecional Usuario ↔ Pessoa causando **recursão infinita** durante:
- Serialização JSON (Jackson)
- Métodos `toString()`, `equals()`, `hashCode()` do Lombok

```
Usuario → serializa Pessoa → serializa Usuario → ∞
```

### Solução Aplicada

#### 1. **Pessoa.java** - Prevenir serialização JSON
```java
@OneToOne(mappedBy = "pessoa")
@JsonIgnore  // ← Impede serialização do Usuario
private Usuario usuario;

@OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnore  // ← Impede serialização das atividades
private List<AtividadePessoaPapel> atividades = new ArrayList<>();
```

#### 2. **Usuario.java** - Exclusões do Lombok
```java
@Entity
@Data
@EqualsAndHashCode(exclude = {"roles", "cursos"})
@ToString(exclude = {"roles", "cursos", "pessoa"})
@AllArgsConstructor
@NoArgsConstructor
public class Usuario implements UserDetails {
    // ...
}
```

#### 3. **Pessoa.java** - Exclusões do Lombok
```java
@Entity
@Data
@EqualsAndHashCode(exclude = {"usuario", "atividades"})
@ToString(exclude = {"usuario", "atividades"})
@AllArgsConstructor
@NoArgsConstructor
public class Pessoa implements Serializable {
    // ...
}
```

**Explicação:**
- `@JsonIgnore`: Impede que Jackson serialize o lado inverso do relacionamento
- `@EqualsAndHashCode(exclude)`: Evita loops em comparações
- `@ToString(exclude)`: Evita loops ao gerar strings de debug

---

## 🛠️ **Melhorias Adicionais**

### GlobalExceptionHandler.java
Adicionados handlers para capturar e retornar erros de forma estruturada:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Erro interno: " + ex.getMessage());
    error.put("type", ex.getClass().getSimpleName());
    ex.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
}
```

---

## 📁 **Arquivos Modificados**

### Modelos
- ✅ `src/main/java/edu/uea/acadmanage/model/Usuario.java`
- ✅ `src/main/java/edu/uea/acadmanage/model/Pessoa.java`

### DTOs
- ✅ `src/main/java/edu/uea/acadmanage/DTO/CursoDTO.java`

### Services
- ✅ `src/main/java/edu/uea/acadmanage/service/CursoService.java`
- ✅ `src/main/java/edu/uea/acadmanage/service/UsuarioService.java`

### Exception Handling
- ✅ `src/main/java/edu/uea/acadmanage/service/exception/GlobalExceptionHandler.java`

### Repositories
- ✅ `src/main/java/edu/uea/acadmanage/repository/PessoaRepository.java`

---

## 🚀 **Como Testar**

### 1. Reiniciar a aplicação
```bash
.\mvnw.cmd spring-boot:run
```

### 2. Fazer login para obter token
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin@uea.edu.br",
  "password": "admin123"
}
```

### 3. Cadastrar usuário
```bash
POST http://localhost:8080/api/usuarios
Authorization: Bearer {seu-token-jwt}
Content-Type: application/json

{
  "nome": "João da Mata",
  "cpf": "682.414.372.34",
  "email": "jlfilho@uea.edu.br",
  "senha": "joao123",
  "role": "ROLE_ADMINISTRADOR",
  "cursos": [
    { "id": 1 },
    { "id": 2 }
  ]
}
```

### 4. Resposta esperada (201 Created)
```json
{
  "id": 3,
  "nome": "João da Mata",
  "cpf": "682.414.372.34",
  "email": "jlfilho@uea.edu.br",
  "senha": null,
  "role": "ROLE_ADMINISTRADOR",
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

---

## ✅ **Status Final**

✅ **Compilação** - SUCCESS  
✅ **Validação de CursoDTO** - Corrigida e contextual  
✅ **Relacionamento Usuario↔Pessoa** - Corrigido (cascade + mappedBy)  
✅ **Serialização JSON** - Sem loops (@JsonIgnore)  
✅ **Lombok** - Exclusões configuradas (@ToString, @EqualsAndHashCode)  
✅ **Exception Handlers** - Completos e estruturados  
✅ **Sem erros de lint** - Código limpo  

---

## 📚 **Lições Aprendidas**

### 1. Relacionamentos Bidirecionais JPA
- Sempre definir um lado como **owner** (`@JoinColumn`)
- O lado inverso deve usar `mappedBy`
- Cuidado com `cascade` - usar o tipo correto para cada operação

### 2. Serialização JSON em Relacionamentos Bidirecionais
- Usar `@JsonIgnore` no lado inverso
- Ou usar `@JsonManagedReference` / `@JsonBackReference`
- Sempre testar a resposta JSON para detectar loops

### 3. Lombok e Relacionamentos
- `@Data` gera `toString()`, `equals()`, `hashCode()`
- Esses métodos podem causar loops em relacionamentos bidirecionais
- Usar `@ToString(exclude)` e `@EqualsAndHashCode(exclude)`

### 4. Validações Contextuais
- Nem sempre validações anotadas são apropriadas
- Às vezes é melhor validar manualmente no service
- Permite diferentes requisitos em diferentes contextos

---

## 🎯 **Próximos Passos Recomendados**

1. ✅ Testar todos os endpoints de usuário
2. ✅ Verificar se atualização de usuário funciona corretamente
3. ✅ Testar edge cases (CPF duplicado, email duplicado, etc.)
4. ✅ Adicionar testes unitários para os novos comportamentos
5. ✅ Documentar no Swagger as validações aplicadas

---

**Documentação criada em:** 19/10/2025  
**Versão da aplicação:** 0.0.1-SNAPSHOT  
**Status:** ✅ Todos os problemas resolvidos

