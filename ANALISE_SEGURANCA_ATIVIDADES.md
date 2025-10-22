# ⚠️ Análise de Segurança - AtividadeController

## 🔴 PROBLEMA CRÍTICO ENCONTRADO

O endpoint **`GET /api/atividades/filtros`** e **TODOS os outros endpoints de atividades** estão **COMPLETAMENTE PÚBLICOS** (sem autenticação necessária).

---

## 🔍 Análise Detalhada

### 1. Controller (AtividadeController.java)

#### ❌ Endpoints GET SEM Proteção:

```java
// Linha 45-51: Buscar por curso - SEM @PreAuthorize
@GetMapping("/curso/{cursoId}")
public ResponseEntity<List<AtividadeDTO>> getAtividadesPorCurso(@PathVariable Long cursoId)

// Linha 54-58: Buscar por ID - SEM @PreAuthorize
@GetMapping("/{atividadeId}")
public ResponseEntity<AtividadeDTO> getAtividadeById(@PathVariable Long atividadeId)

// Linha 61-67: Buscar por ID e usuário - SEM @PreAuthorize
@GetMapping("/{atividadeId}/usuario/{usuarioId}")
public ResponseEntity<AtividadeDTO> getAtividadeByIdAndUsuario(...)

// Linha 71-96: Buscar com filtros - SEM @PreAuthorize ⚠️ ENDPOINT DESTACADO
@GetMapping("/filtros")
public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltros(...)
```

#### ✅ Endpoints de Modificação COM Proteção:

```java
// POST - COM @PreAuthorize
@PostMapping
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<AtividadeDTO> salvarAtividade(...)

// PUT - COM @PreAuthorize
@PutMapping("/{atividadeId}")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<AtividadeDTO> atualizarAtividade(...)

// DELETE - COM @PreAuthorize
@DeleteMapping("/{atividadeId}")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<Void> excluirAtividade(...)
```

---

### 2. Configuração de Segurança (PROBLEMA PRINCIPAL)

#### JwtSecurityConfig.java (linhas 42-47)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/login/**", "/logout**",
                    "/api/atividades/**",    // ⚠️ TODOS os endpoints de atividades
                    "/api/cursos/**",        // ⚠️ TODOS os endpoints de cursos
                    "/api/categorias/**",    // ⚠️ TODOS os endpoints de categorias
                    "/api/evidencias/**",    // ⚠️ TODOS os endpoints de evidências
                    "/api/evidencias/atividade/**",
                    "/api/categorias/curso/**")
    .permitAll()  // ⚠️ PERMITE ACESSO PÚBLICO TOTAL
    // ...
)
```

#### BasicSecurityConfig.java (linha 25)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/logout",
                    "/api/atividades/**",    // ⚠️ PÚBLICO
                    "/api/cursos/**",        // ⚠️ PÚBLICO
                    "/api/categorias/**",    // ⚠️ PÚBLICO
                    "/api/evidencias/**")    // ⚠️ PÚBLICO
    .permitAll()
    // ...
)
```

---

## 🚨 Impacto de Segurança

### 1. **Exposição Total dos Dados**
- ❌ Qualquer pessoa pode acessar **TODAS** as atividades sem autenticação
- ❌ Não há verificação de token JWT
- ❌ Não há verificação de roles/permissões
- ❌ Dados sensíveis podem ser expostos

### 2. **Endpoints Afetados**
```bash
# TODOS estes endpoints são PÚBLICOS:
GET /api/atividades/curso/{cursoId}
GET /api/atividades/{atividadeId}
GET /api/atividades/{atividadeId}/usuario/{usuarioId}
GET /api/atividades/filtros  ⬅️ ENDPOINT ANALISADO

# E também os endpoints de modificação, apesar de terem @PreAuthorize!
POST /api/atividades
PUT /api/atividades/{atividadeId}
DELETE /api/atividades/{atividadeId}
```

### 3. **Conflito de Configuração**

⚠️ **IMPORTANTE**: O `.permitAll()` na configuração de segurança **SOBRESCREVE** as anotações `@PreAuthorize` nos controllers!

Isso significa que mesmo os endpoints POST, PUT e DELETE que têm `@PreAuthorize` são acessíveis publicamente porque a configuração de segurança permite `/api/atividades/**`.

---

## 🔧 Soluções Recomendadas

### Opção 1: Proteger TODOS os Endpoints de Atividades (RECOMENDADO)

**Remover `/api/atividades/**` da lista de `.permitAll()`**

#### JwtSecurityConfig.java
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                    "/v3/api-docs.yaml", "/swagger-resources/**")
    .permitAll()
    .requestMatchers("/api/auth/login/**", "/logout**")
    .permitAll()
    // REMOVER: "/api/atividades/**"
    .requestMatchers("/api/cursos/**",        // Se cursos devem ser públicos
                    "/api/categorias/**")     // Se categorias devem ser públicas
    .permitAll()
    .requestMatchers("/api/recovery/generate/**",
                    "/api/recovery/reset-password/**")
    .permitAll()
    .requestMatchers("/css/**", "/js/**", "/images/**", "/api/files/**")
    .permitAll()
    .requestMatchers("/h2-console/**").hasRole("ADMINISTRADOR")
    .anyRequest().authenticated())  // REQUER AUTENTICAÇÃO para tudo mais
```

Depois, adicione `@PreAuthorize` em TODOS os endpoints GET:

```java
@GetMapping("/filtros")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltros(...)

@GetMapping("/curso/{cursoId}")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<List<AtividadeDTO>> getAtividadesPorCurso(...)

@GetMapping("/{atividadeId}")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<AtividadeDTO> getAtividadeById(...)

@GetMapping("/{atividadeId}/usuario/{usuarioId}")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
public ResponseEntity<AtividadeDTO> getAtividadeByIdAndUsuario(...)
```

---

### Opção 2: Endpoints Públicos Seletivos (MENOS SEGURO)

Se **REALMENTE** precisar que alguns endpoints sejam públicos, use paths mais específicos:

```java
.authorizeHttpRequests(auth -> auth
    // Permitir APENAS endpoints específicos
    .requestMatchers(HttpMethod.GET, "/api/atividades/publico/**")
    .permitAll()
    // Proteger todos os outros endpoints de atividades
    .requestMatchers("/api/atividades/**")
    .authenticated()
    // ...
)
```

E renomeie os endpoints públicos:
```java
@GetMapping("/publico/filtros")  // Ao invés de /filtros
public ResponseEntity<Page<AtividadeDTO>> getAtividadesPorFiltrosPublico(...)
```

---

### Opção 3: Acesso Público Apenas para Leitura (INTERMEDIÁRIO)

Permitir GET público, mas proteger POST/PUT/DELETE:

```java
.authorizeHttpRequests(auth -> auth
    // Permitir apenas GET em atividades
    .requestMatchers(HttpMethod.GET, "/api/atividades/**")
    .permitAll()
    // Todos os outros métodos (POST, PUT, DELETE) requerem autenticação
    .requestMatchers("/api/atividades/**")
    .authenticated()
    // ...
)
```

---

## 📊 Comparação de Segurança

| Endpoint | Anotação no Controller | Configuração Security | Status Atual | Recomendado |
|----------|------------------------|----------------------|--------------|-------------|
| `GET /api/atividades/filtros` | ❌ Sem `@PreAuthorize` | ✅ `.permitAll()` | 🔴 **PÚBLICO** | 🔒 **PROTEGIDO** |
| `GET /api/atividades/{id}` | ❌ Sem `@PreAuthorize` | ✅ `.permitAll()` | 🔴 **PÚBLICO** | 🔒 **PROTEGIDO** |
| `POST /api/atividades` | ✅ Com `@PreAuthorize` | ❌ `.permitAll()` | 🔴 **PÚBLICO*** | 🔒 **PROTEGIDO** |
| `PUT /api/atividades/{id}` | ✅ Com `@PreAuthorize` | ❌ `.permitAll()` | 🔴 **PÚBLICO*** | 🔒 **PROTEGIDO** |
| `DELETE /api/atividades/{id}` | ✅ Com `@PreAuthorize` | ❌ `.permitAll()` | 🔴 **PÚBLICO*** | 🔒 **PROTEGIDO** |

\* O `.permitAll()` sobrescreve o `@PreAuthorize`

---

## 🎯 Outros Controllers Afetados

Os mesmos problemas afetam:

- ✅ **CursoController** - `/api/cursos/**` está em `.permitAll()`
- ✅ **CategoriaController** - `/api/categorias/**` está em `.permitAll()`
- ✅ **EvidenciaController** - `/api/evidencias/**` está em `.permitAll()`

---

## ✅ Recomendação Final

### **URGENTE**: Revisar e Corrigir Configuração de Segurança

1. ✅ **Remover** `/api/atividades/**` de `.permitAll()`
2. ✅ **Adicionar** `@PreAuthorize` em TODOS os endpoints GET
3. ✅ **Revisar** se cursos, categorias e evidências devem ser públicos
4. ✅ **Testar** todos os endpoints após as mudanças
5. ✅ **Documentar** quais endpoints devem ser públicos e por quê

### Nível de Prioridade: 🔴 **CRÍTICO**

---

## 📝 Checklist de Segurança

- [ ] Remover `/api/atividades/**` de `.permitAll()` nas configurações de segurança
- [ ] Adicionar `@PreAuthorize` em todos os endpoints GET de atividades
- [ ] Testar acesso sem autenticação (deve retornar 401 ou 403)
- [ ] Testar acesso com autenticação válida (deve funcionar)
- [ ] Testar acesso com role incorreta (deve retornar 403)
- [ ] Verificar outros controllers (Cursos, Categorias, Evidências)
- [ ] Atualizar documentação da API
- [ ] Comunicar mudanças ao time de frontend

---

## 🚀 Status Atual

❌ **INSEGURO** - Todos os endpoints de atividades estão acessíveis publicamente, incluindo operações de modificação (POST, PUT, DELETE).

## 🎯 Status Desejado

✅ **SEGURO** - Apenas usuários autenticados e autorizados podem acessar os endpoints de atividades.

