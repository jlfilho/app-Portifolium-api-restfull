# 📋 Plano de Implementação: Role "Coordenador de Atividade"

## 🎯 Objetivo
Implementar a role `ROLE_COORDENADOR_ATIVIDADE` que permite aos usuários criar atividades em cursos onde estão associados e editar/gerenciar evidências apenas das atividades onde são coordenadores (Papel.COORDENADOR).

---

## ✅ Garantias
- **ADMINISTRADOR, GERENTE e SECRETÁRIO mantêm as mesmas permissões atuais** (precisam estar associados ao curso)
- Implementação incremental com testes entre etapas
- Sistema permanece funcional durante toda a implementação

---

## 📦 Etapa 1: Fundação (Base Infrastructure)

### Objetivo
Criar a infraestrutura base: role no banco, serviço de autorização e métodos no repository.

### Tarefas

#### 1.1. Adicionar Role no Banco de Dados
**Arquivo**: `src/main/resources/data.sql`

```sql
-- Adicionar nova role na tabela role
INSERT INTO role (id, nome) VALUES (4, 'ROLE_COORDENADOR_ATIVIDADE');
```

**Nota**: Verificar se o ID 4 está disponível ou usar o próximo ID disponível.

---

#### 1.2. Adicionar Método no Repository
**Arquivo**: `src/main/java/edu/uea/acadmanage/repository/AtividadePessoaPapelRepository.java`

```java
/**
 * Verifica se uma pessoa é coordenadora de uma atividade específica.
 * 
 * @param atividade A atividade a ser verificada.
 * @param pessoa A pessoa a ser verificada.
 * @param papel O papel a ser verificado (deve ser Papel.COORDENADOR).
 * @return true se a associação existir, false caso contrário.
 */
boolean existsByAtividadeAndPessoaAndPapel(Atividade atividade, Pessoa pessoa, Papel papel);
```

---

#### 1.3. Criar AtividadeAutorizacaoService
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeAutorizacaoService.java`

```java
package edu.uea.acadmanage.service;

import edu.uea.acadmanage.model.Atividade;
import edu.uea.acadmanage.model.Papel;
import edu.uea.acadmanage.model.Usuario;
import edu.uea.acadmanage.repository.AtividadePessoaPapelRepository;
import edu.uea.acadmanage.repository.AtividadeRepository;
import edu.uea.acadmanage.repository.UsuarioRepository;
import edu.uea.acadmanage.service.exception.AcessoNegadoException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AtividadeAutorizacaoService {
    
    private final AtividadePessoaPapelRepository atividadePessoaPapelRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoService cursoService;
    
    public AtividadeAutorizacaoService(
            AtividadePessoaPapelRepository atividadePessoaPapelRepository,
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            CursoService cursoService) {
        this.atividadePessoaPapelRepository = atividadePessoaPapelRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoService = cursoService;
    }
    
    /**
     * Verifica se o usuário tem permissão para criar atividades em um curso.
     * Permite: ADMINISTRADOR, GERENTE, SECRETARIO, COORDENADOR_ATIVIDADE (com acesso ao curso)
     * 
     * IMPORTANTE: ADMINISTRADOR, GERENTE e SECRETÁRIO precisam estar associados ao curso
     * (mantém comportamento atual)
     */
    public boolean podeCriarAtividadeNoCurso(String username, Long cursoId) {
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar roles de sistema
        boolean isAdmin = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        boolean isGerente = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_GERENTE"));
        boolean isSecretario = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_SECRETARIO"));
        boolean isCoordenadorAtividade = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_COORDENADOR_ATIVIDADE"));
        
        // Admin, Gerente e Secretário precisam estar associados ao curso (comportamento atual)
        if (isAdmin || isGerente || isSecretario) {
            return cursoService.verificarAcessoAoCurso(username, cursoId);
        }
        
        // Coordenador de Atividade precisa ter acesso ao curso
        if (isCoordenadorAtividade) {
            return cursoService.verificarAcessoAoCurso(username, cursoId);
        }
        
        return false;
    }
    
    /**
     * Verifica se o usuário é coordenador de uma atividade específica.
     * Busca na tabela AtividadePessoaPapel se o usuário tem Papel.COORDENADOR na atividade.
     */
    public boolean ehCoordenadorDaAtividade(String username, Long atividadeId) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new AcessoNegadoException("Atividade não encontrada"));
        
        // Buscar usuário e pessoa associada
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        if (usuario.getPessoa() == null) {
            return false;
        }
        
        // Verificar se existe associação com Papel.COORDENADOR
        return atividadePessoaPapelRepository.existsByAtividadeAndPessoaAndPapel(
            atividade, 
            usuario.getPessoa(), 
            Papel.COORDENADOR
        );
    }
    
    /**
     * Verifica se o usuário pode editar uma atividade.
     * Permite: ADMINISTRADOR, GERENTE, SECRETARIO (se associados ao curso),
     * ou COORDENADOR_ATIVIDADE (se for coordenador da atividade).
     */
    public boolean podeEditarAtividade(String username, Long atividadeId) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new AcessoNegadoException("Atividade não encontrada"));
        
        Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar roles de sistema
        boolean isAdmin = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_ADMINISTRADOR"));
        boolean isGerente = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_GERENTE"));
        boolean isSecretario = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_SECRETARIO"));
        
        // Admin, Gerente e Secretário precisam estar associados ao curso (comportamento atual)
        if (isAdmin || isGerente || isSecretario) {
            return cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId());
        }
        
        // Coordenador de Atividade só pode editar se for coordenador desta atividade específica
        boolean isCoordenadorAtividade = usuario.getRoles().stream()
            .anyMatch(role -> role.getNome().equals("ROLE_COORDENADOR_ATIVIDADE"));
        
        if (isCoordenadorAtividade) {
            return ehCoordenadorDaAtividade(username, atividadeId);
        }
        
        return false;
    }
    
    /**
     * Verifica se o usuário pode gerenciar evidências de uma atividade.
     * Mesma lógica de podeEditarAtividade.
     */
    public boolean podeGerenciarEvidencias(String username, Long atividadeId) {
        return podeEditarAtividade(username, atividadeId);
    }
}
```

---

### ✅ Checklist Etapa 1
- [ ] Adicionar role no banco de dados
- [ ] Adicionar método `existsByAtividadeAndPessoaAndPapel` no repository
- [ ] Criar `AtividadeAutorizacaoService.java`
- [ ] Compilar sem erros
- [ ] Verificar imports corretos

---

## 🔄 Etapa 2: Integração com Atividades

### Objetivo
Substituir verificações de acesso no `AtividadeService` para usar o novo `AtividadeAutorizacaoService`.

### Tarefas

#### 2.1. Injetar AtividadeAutorizacaoService no AtividadeService
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeService.java`

Adicionar na declaração de dependências:
```java
private final AtividadeAutorizacaoService atividadeAutorizacaoService;

// No construtor
public AtividadeService(
        ...,
        AtividadeAutorizacaoService atividadeAutorizacaoService) {
    ...
    this.atividadeAutorizacaoService = atividadeAutorizacaoService;
}
```

---

#### 2.2. Atualizar método `salvarAtividade`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeService.java`

**ANTES**:
```java
// Verificar se o usuário tem permissão para salvar a atividade
Long cursoId = atividadeDTO.curso().getId();
if (!cursoService.verificarAcessoAoCurso(username, cursoId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para salvar atividade no curso: " + cursoId);
}
```

**DEPOIS**:
```java
// Verificar se o usuário tem permissão para salvar a atividade
Long cursoId = atividadeDTO.curso().getId();
if (!atividadeAutorizacaoService.podeCriarAtividadeNoCurso(username, cursoId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para criar atividade no curso: " + cursoId);
}
```

---

#### 2.3. Atualizar método `atualizarAtividade`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeService.java`

**ANTES**:
```java
// Verificar se o usuário tem permissão para salvar a atividade
if (!cursoService.verificarAcessoAoCurso(username, atividadeDTO.curso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para atualizar atividade no curso: " + atividadeDTO.curso().getId());
}
```

**DEPOIS**:
```java
// Verificar se o usuário tem permissão para editar a atividade
if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para editar esta atividade: " + atividadeId);
}
```

---

#### 2.4. Atualizar método `excluirAtividade`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeService.java`

**ANTES**:
```java
if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para excluir atividade no curso: " + atividade.getCurso().getId());
}
```

**DEPOIS**:
```java
if (!atividadeAutorizacaoService.podeEditarAtividade(username, atividadeId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para excluir esta atividade: " + atividadeId);
}
```

---

#### 2.5. Atualizar métodos de foto de capa (opcional)
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/AtividadeService.java`

Para manter consistência, também atualizar:
- `atualizarFotoCapa` → usar `podeEditarAtividade`
- `excluirFotoCapa` → usar `podeEditarAtividade`

---

### ✅ Checklist Etapa 2
- [ ] Injetar `AtividadeAutorizacaoService` no `AtividadeService`
- [ ] Atualizar `salvarAtividade`
- [ ] Atualizar `atualizarAtividade`
- [ ] Atualizar `excluirAtividade`
- [ ] Atualizar métodos de foto de capa (opcional)
- [ ] Compilar sem erros
- [ ] Testar criação de atividade (admin/gerente/secretário)
- [ ] Testar edição de atividade (admin/gerente/secretário)

---

## 🖼️ Etapa 3: Integração com Evidências e Controllers

### Objetivo
Integrar autorização no `EvidenciaService` e adicionar `@PreAuthorize` nos controllers.

### Tarefas

#### 3.1. Injetar AtividadeAutorizacaoService no EvidenciaService
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/EvidenciaService.java`

Adicionar na declaração de dependências:
```java
private final AtividadeAutorizacaoService atividadeAutorizacaoService;

// No construtor
public EvidenciaService(
        ...,
        AtividadeAutorizacaoService atividadeAutorizacaoService) {
    ...
    this.atividadeAutorizacaoService = atividadeAutorizacaoService;
}
```

---

#### 3.2. Atualizar método `salvarEvidencia`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/EvidenciaService.java`

**ANTES**:
```java
if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para salvar a evidência no curso: " + atividade.getCurso().getId());
}
```

**DEPOIS**:
```java
if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
}
```

---

#### 3.3. Atualizar método `atualizarEvidencia`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/EvidenciaService.java`

**ANTES**:
```java
if (!cursoService.verificarAcessoAoCurso(username, evidenciaExistente.getAtividade().getCurso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para alterar a evidência no curso: "
                    + evidenciaExistente.getAtividade().getCurso().getId());
}
```

**DEPOIS**:
```java
Long atividadeId = evidenciaExistente.getAtividade().getId();
if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
}
```

---

#### 3.4. Atualizar método `excluirEvidencia`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/EvidenciaService.java`

**ANTES**:
```java
if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para excluir a evidência no curso: " + atividade.getCurso().getId());
}
```

**DEPOIS**:
```java
if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividade.getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividade.getId());
}
```

---

#### 3.5. Atualizar método `atualizarOrdem`
**Arquivo**: `src/main/java/edu/uea/acadmanage/service/EvidenciaService.java`

**ANTES**:
```java
if (!cursoService.verificarAcessoAoCurso(username, atividade.getCurso().getId())) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para reordenar evidências no curso: " + atividade.getCurso().getId());
}
```

**DEPOIS**:
```java
if (!atividadeAutorizacaoService.podeGerenciarEvidencias(username, atividadeId)) {
    throw new AcessoNegadoException(
            "Usuário não tem permissão para gerenciar evidências desta atividade: " + atividadeId);
}
```

---

#### 3.6. Atualizar Controllers - AtividadeController
**Arquivo**: `src/main/java/edu/uea/acadmanage/controller/AtividadeController.java`

**POST /api/atividades**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**PUT /api/atividades/{atividadeId}**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**DELETE /api/atividades/{atividadeId}**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**PUT /api/atividades/foto-capa/{atividadeId}**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**DELETE /api/atividades/{atividadeId}/foto-capa**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

---

#### 3.7. Atualizar Controllers - EvidenciaController
**Arquivo**: `src/main/java/edu/uea/acadmanage/controller/EvidenciaController.java`

**POST /api/evidencias**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**PUT /api/evidencias/{evidenciaId}**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**DELETE /api/evidencias/{evidenciaId}**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

**PUT /api/evidencias/atividade/{atividadeId}/ordem**:
```java
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO') or hasRole('COORDENADOR_ATIVIDADE')")
```

---

### ✅ Checklist Etapa 3
- [ ] Injetar `AtividadeAutorizacaoService` no `EvidenciaService`
- [ ] Atualizar `salvarEvidencia`
- [ ] Atualizar `atualizarEvidencia`
- [ ] Atualizar `excluirEvidencia`
- [ ] Atualizar `atualizarOrdem`
- [ ] Atualizar `@PreAuthorize` em `AtividadeController`
- [ ] Atualizar `@PreAuthorize` em `EvidenciaController`
- [ ] Compilar sem erros
- [ ] Testar criação de evidência (admin/gerente/secretário)
- [ ] Testar edição de evidência (admin/gerente/secretário)
- [ ] Testar com usuário COORDENADOR_ATIVIDADE (se disponível)

---

## 🧪 Testes Sugeridos

### Testes Manuais

#### Teste 1: Admin/Gerente/Secretário (comportamento atual)
1. Login com ADMINISTRADOR/GERENTE/SECRETÁRIO
2. Criar atividade em curso associado → ✅ Deve funcionar
3. Editar atividade em curso associado → ✅ Deve funcionar
4. Criar atividade em curso NÃO associado → ❌ Deve negar

#### Teste 2: Coordenador de Atividade
1. Login com COORDENADOR_ATIVIDADE
2. Associar usuário como coordenador de uma atividade
3. Criar atividade em curso associado → ✅ Deve funcionar
4. Editar atividade onde é coordenador → ✅ Deve funcionar
5. Editar atividade onde NÃO é coordenador → ❌ Deve negar
6. Gerenciar evidências de atividade onde é coordenador → ✅ Deve funcionar
7. Gerenciar evidências de atividade onde NÃO é coordenador → ❌ Deve negar

---

## 📝 Notas Importantes

1. **Associação Usuário-Pessoa**: O usuário com `ROLE_COORDENADOR_ATIVIDADE` deve ter uma `Pessoa` associada para funcionar corretamente.

2. **Migração de Dados**: Usuários existentes que devem ter a role `COORDENADOR_ATIVIDADE` precisam receber manualmente no banco:
   ```sql
   INSERT INTO usuario_roles (usuario_id, role_id) 
   VALUES (usuario_id, 4);
   ```

3. **Rollback**: Se precisar reverter:
   - Remover role do banco
   - Reverter mudanças nos services e controllers
   - Sistema voltará ao comportamento anterior

---

## 🎯 Resumo das Mudanças

| Arquivo | Mudança |
|---------|---------|
| `data.sql` | Adicionar role `ROLE_COORDENADOR_ATIVIDADE` |
| `AtividadePessoaPapelRepository.java` | Adicionar método `existsByAtividadeAndPessoaAndPapel` |
| `AtividadeAutorizacaoService.java` | **NOVO** - Criar serviço |
| `AtividadeService.java` | Substituir verificações por `atividadeAutorizacaoService` |
| `EvidenciaService.java` | Substituir verificações por `atividadeAutorizacaoService` |
| `AtividadeController.java` | Adicionar `COORDENADOR_ATIVIDADE` em `@PreAuthorize` |
| `EvidenciaController.java` | Adicionar `COORDENADOR_ATIVIDADE` em `@PreAuthorize` |

---

**✅ Implementação concluída quando todas as 3 etapas estiverem completas e testadas!**

