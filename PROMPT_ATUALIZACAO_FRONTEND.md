# 📋 Prompt: Atualização do Frontend - Role Coordenador de Atividade e Campo dataFim

## 🎯 Objetivo
Atualizar o frontend para suportar as alterações implementadas no backend:
1. **Novo campo `dataFim`** no `AtividadeDTO` (suporte a períodos de atividades)
2. **Nova role `COORDENADOR_ATIVIDADE`** com permissões específicas para coordenadores de atividades

---

## 📦 1. Alterações no AtividadeDTO

### 1.1. Campo Adicionado: `dataFim`

**Estrutura atualizada do AtividadeDTO:**

```typescript
interface AtividadeDTO {
  id: number;
  nome: string;
  objetivo?: string;
  publicoAlvo?: string;
  statusPublicacao: boolean;
  fotoCapa?: string;
  coordenador?: string;
  dataRealizacao: string; // ISO format (yyyy-MM-dd)
  dataFim?: string | null; // ⭐ NOVO CAMPO - ISO format (yyyy-MM-dd) - opcional
  curso: Curso;
  categoria: Categoria;
  fontesFinanciadora?: FonteFinanciadora[];
  integrantes?: PessoaPapelDTO[];
}
```

### 1.2. Tarefas no Frontend

#### ✅ Atualizar Interface/Model
- [ ] Adicionar `dataFim?: string | null` na interface/model de `AtividadeDTO`

#### ✅ Atualizar Formulários de Criação/Edição
- [ ] Adicionar campo opcional para `dataFim` no formulário de atividades
- [ ] Implementar validação:
  - Se `dataFim` for preenchido, garantir que `dataFim >= dataRealizacao`
  - Permitir deixar `dataFim` vazio/null para eventos em data única
- [ ] Adicionar checkbox ou toggle: "Evento em período" (quando marcado, habilita campo `dataFim`)

#### ✅ Atualizar Exibição de Datas
- [ ] Criar função auxiliar para formatar data da atividade:
  ```typescript
  function formatarDataAtividade(atividade: AtividadeDTO): string {
    if (!atividade.dataFim) {
      // Evento em data única
      return formatarData(atividade.dataRealizacao);
    } else {
      // Evento em período
      return `${formatarData(atividade.dataRealizacao)} a ${formatarData(atividade.dataFim)}`;
    }
  }
  ```
- [ ] Atualizar todos os componentes que exibem a data da atividade:
  - Listagens/tabelas de atividades
  - Cards de atividades
  - Detalhes da atividade
  - Relatórios/visualizações

#### ✅ Exemplo de Validação no Formulário
```typescript
// Validação de datas
function validarDatas(dataRealizacao: Date, dataFim?: Date | null): boolean {
  if (dataFim && dataFim < dataRealizacao) {
    return false; // dataFim deve ser >= dataRealizacao
  }
  return true;
}

// Exemplo de uso em formulário reativo (Angular)
this.atividadeForm = this.fb.group({
  nome: ['', Validators.required],
  dataRealizacao: ['', Validators.required],
  dataFim: [null], // Opcional
  // ... outros campos
}, {
  validators: (control) => {
    const dataRealizacao = control.get('dataRealizacao')?.value;
    const dataFim = control.get('dataFim')?.value;
    if (dataFim && new Date(dataFim) < new Date(dataRealizacao)) {
      return { dataFimInvalida: true };
    }
    return null;
  }
});
```

---

## 🔐 2. Nova Role: COORDENADOR_ATIVIDADE

### 2.1. Comportamento da Role

A role `COORDENADOR_ATIVIDADE` permite que usuários:
- ✅ **Criar atividades** em cursos onde estão associados
- ✅ **Editar atividades** apenas das atividades onde são coordenadores (`Papel.COORDENADOR`)
- ✅ **Gerenciar evidências** apenas das atividades onde são coordenadores
- ❌ **NÃO podem** editar atividades onde não são coordenadores

**Importante:** Um usuário com `COORDENADOR_ATIVIDADE` pode coordenar múltiplas atividades, mas só pode editar/gerenciar evidências das atividades onde tem o papel de coordenador.

### 2.2. Endpoints Afetados

Todos os endpoints abaixo agora aceitam `COORDENADOR_ATIVIDADE` além das roles existentes:

#### **AtividadeController**
- ✅ `POST /api/atividades` - Criar atividade
- ✅ `PUT /api/atividades/{atividadeId}` - Editar atividade
- ✅ `DELETE /api/atividades/{atividadeId}` - Excluir atividade
- ✅ `PUT /api/atividades/foto-capa/{atividadeId}` - Atualizar foto de capa
- ✅ `DELETE /api/atividades/{atividadeId}/foto-capa` - Excluir foto de capa

#### **EvidenciaController**
- ✅ `POST /api/evidencias` - Criar evidência
- ✅ `PUT /api/evidencias/{evidenciaId}` - Editar evidência
- ✅ `DELETE /api/evidencias/{evidenciaId}` - Excluir evidência
- ✅ `PUT /api/evidencias/atividade/{atividadeId}/ordem` - Reordenar evidências

### 2.3. Tarefas no Frontend

#### ✅ Atualizar Verificação de Permissões
- [ ] Adicionar `COORDENADOR_ATIVIDADE` nas verificações de permissão para:
  - Botões de criar/editar/excluir atividades
  - Botões de criar/editar/excluir evidências
  - Ações de gerenciamento de atividades

#### ✅ Implementar Lógica de Edição Condicional
- [ ] Verificar se o usuário é coordenador da atividade antes de permitir edição
- [ ] Implementar verificação via endpoint ou verificação local:
  ```typescript
  // Opção 1: Verificar via API (recomendado)
  async podeEditarAtividade(atividadeId: number): Promise<boolean> {
    try {
      // Tentar editar (o backend retornará 403 se não tiver permissão)
      // Ou criar endpoint específico para verificar permissão
      return await this.atividadeService.verificarPermissaoEdicao(atividadeId);
    } catch (error) {
      return false;
    }
  }

  // Opção 2: Verificar localmente (se tiver informação de coordenador)
  podeEditarAtividade(atividade: AtividadeDTO, usuarioLogado: Usuario): boolean {
    // Se for ADMINISTRADOR, GERENTE ou SECRETARIO, sempre pode editar
    if (usuarioLogado.roles.includes('ADMINISTRADOR') || 
        usuarioLogado.roles.includes('GERENTE') || 
        usuarioLogado.roles.includes('SECRETARIO')) {
      return true;
    }
    
    // Se for COORDENADOR_ATIVIDADE, verificar se é coordenador desta atividade
    if (usuarioLogado.roles.includes('COORDENADOR_ATIVIDADE')) {
      // Verificar se o usuário está na lista de integrantes como coordenador
      const usuarioEhCoordenador = atividade.integrantes?.some(
        integrante => integrante.pessoaId === usuarioLogado.pessoaId && 
                     integrante.papel === 'COORDENADOR'
      );
      return usuarioEhCoordenador || false;
    }
    
    return false;
  }
  ```

#### ✅ Atualizar UI para Coordenadores
- [ ] Desabilitar botões de edição/exclusão para atividades onde o usuário não é coordenador
- [ ] Mostrar mensagem explicativa: "Você só pode editar atividades onde é coordenador"
- [ ] Adicionar indicador visual nas atividades onde o usuário é coordenador
- [ ] Implementar feedback de erro quando tentar editar atividade sem permissão:
  ```typescript
  // Exemplo de tratamento de erro
  try {
    await this.atividadeService.atualizarAtividade(id, dados);
  } catch (error) {
    if (error.status === 403) {
      this.mostrarMensagem(
        'Você não tem permissão para editar esta atividade. ' +
        'Apenas coordenadores da atividade podem editá-la.'
      );
    }
  }
  ```

#### ✅ Atualizar Listagens/Filtros
- [ ] Considerar a role `COORDENADOR_ATIVIDADE` ao filtrar/exibir atividades editáveis
- [ ] Opcional: Adicionar filtro "Minhas atividades" para coordenadores verem apenas atividades onde são coordenadores

---

## 🔄 3. Fluxo de Autorização no Frontend

### 3.1. Criar Atividade
```
1. Verificar se usuário tem role: ADMINISTRADOR, GERENTE, SECRETARIO ou COORDENADOR_ATIVIDADE
2. Se COORDENADOR_ATIVIDADE, verificar se está associado ao curso selecionado
3. Permitir criação se passar nas verificações
```

### 3.2. Editar Atividade
```
1. Verificar se usuário tem role: ADMINISTRADOR, GERENTE, SECRETARIO ou COORDENADOR_ATIVIDADE
2. Se ADMINISTRADOR/GERENTE/SECRETARIO:
   - Verificar se está associado ao curso da atividade
   - Permitir edição se associado
3. Se COORDENADOR_ATIVIDADE:
   - Verificar se é coordenador desta atividade específica
   - Permitir edição apenas se for coordenador
```

### 3.3. Gerenciar Evidências
```
Mesma lógica de editar atividade
```

---

## 📝 4. Exemplos de Código

### 4.1. Interface Atualizada (TypeScript)
```typescript
export interface AtividadeDTO {
  id: number;
  nome: string;
  objetivo?: string;
  publicoAlvo?: string;
  statusPublicacao: boolean;
  fotoCapa?: string;
  coordenador?: string;
  dataRealizacao: string; // ISO: yyyy-MM-dd
  dataFim?: string | null; // ⭐ NOVO - ISO: yyyy-MM-dd
  curso: Curso;
  categoria: Categoria;
  fontesFinanciadora?: FonteFinanciadora[];
  integrantes?: PessoaPapelDTO[];
}

export interface Usuario {
  id: number;
  email: string;
  roles: string[]; // Inclui 'COORDENADOR_ATIVIDADE'
  pessoaId?: number;
  // ... outros campos
}
```

### 4.2. Função de Formatação de Data
```typescript
export function formatarDataAtividade(atividade: AtividadeDTO): string {
  const dataInicio = formatarData(atividade.dataRealizacao);
  
  if (!atividade.dataFim) {
    return dataInicio; // Evento em data única
  }
  
  const dataFim = formatarData(atividade.dataFim);
  return `${dataInicio} a ${dataFim}`; // Período
}

function formatarData(dataISO: string): string {
  const data = new Date(dataISO);
  return data.toLocaleDateString('pt-BR');
}
```

### 4.3. Verificação de Permissão (Angular/React)
```typescript
// Service de autorização
export class AutorizacaoService {
  
  podeCriarAtividade(usuario: Usuario, cursoId: number): boolean {
    const rolesPermitidas = ['ADMINISTRADOR', 'GERENTE', 'SECRETARIO', 'COORDENADOR_ATIVIDADE'];
    return usuario.roles.some(role => rolesPermitidas.includes(role));
  }
  
  podeEditarAtividade(usuario: Usuario, atividade: AtividadeDTO): boolean {
    // Admin, Gerente e Secretário sempre podem (se associados ao curso)
    if (usuario.roles.includes('ADMINISTRADOR') || 
        usuario.roles.includes('GERENTE') || 
        usuario.roles.includes('SECRETARIO')) {
      return true; // Backend fará verificação de associação ao curso
    }
    
    // Coordenador de Atividade só pode editar se for coordenador desta atividade
    if (usuario.roles.includes('COORDENADOR_ATIVIDADE')) {
      return atividade.integrantes?.some(
        integrante => integrante.pessoaId === usuario.pessoaId && 
                     integrante.papel === 'COORDENADOR'
      ) || false;
    }
    
    return false;
  }
}
```

### 4.4. Exemplo de Formulário (Angular Reactive Forms)
```typescript
export class AtividadeFormComponent {
  atividadeForm: FormGroup;
  eventoEmPeriodo: boolean = false;
  
  constructor(private fb: FormBuilder) {
    this.atividadeForm = this.fb.group({
      nome: ['', Validators.required],
      dataRealizacao: ['', Validators.required],
      dataFim: [null], // Opcional
      eventoEmPeriodo: [false],
      // ... outros campos
    });
    
    // Habilitar/desabilitar dataFim baseado no checkbox
    this.atividadeForm.get('eventoEmPeriodo')?.valueChanges.subscribe(
      (valor: boolean) => {
        this.eventoEmPeriodo = valor;
        const dataFimControl = this.atividadeForm.get('dataFim');
        if (valor) {
          dataFimControl?.setValidators([Validators.required]);
        } else {
          dataFimControl?.clearValidators();
          dataFimControl?.setValue(null);
        }
        dataFimControl?.updateValueAndValidity();
      }
    );
    
    // Validação cruzada de datas
    this.atividadeForm.setValidators(this.validarDatas.bind(this));
  }
  
  private validarDatas(control: AbstractControl): ValidationErrors | null {
    const dataRealizacao = control.get('dataRealizacao')?.value;
    const dataFim = control.get('dataFim')?.value;
    
    if (dataFim && new Date(dataFim) < new Date(dataRealizacao)) {
      return { dataFimInvalida: true };
    }
    
    return null;
  }
}
```

---

## 🧪 5. Checklist de Implementação

### Campo dataFim
- [ ] Atualizar interface/model `AtividadeDTO` adicionando `dataFim?: string | null`
- [ ] Adicionar campo `dataFim` no formulário de criar/editar atividade
- [ ] Implementar validação: `dataFim >= dataRealizacao` quando preenchido
- [ ] Atualizar exibição de data para mostrar período quando `dataFim` existir
- [ ] Testar criação de atividade sem `dataFim` (data única)
- [ ] Testar criação de atividade com `dataFim` (período)
- [ ] Testar edição de atividade adicionando/removendo `dataFim`
- [ ] Atualizar listagens/tabelas para exibir período corretamente

### Role COORDENADOR_ATIVIDADE
- [ ] Adicionar `COORDENADOR_ATIVIDADE` nas verificações de permissão
- [ ] Implementar lógica para verificar se usuário é coordenador da atividade
- [ ] Desabilitar botões de edição para atividades onde não é coordenador
- [ ] Adicionar mensagens de feedback quando tentar editar sem permissão
- [ ] Testar criação de atividade com usuário COORDENADOR_ATIVIDADE
- [ ] Testar edição de atividade onde é coordenador (deve funcionar)
- [ ] Testar edição de atividade onde NÃO é coordenador (deve negar)
- [ ] Testar gerenciamento de evidências (mesma lógica de edição)

---

## ⚠️ 6. Notas Importantes

### 6.1. Retrocompatibilidade
- ✅ Endpoints aceitam requisições sem `dataFim` (comporta-se como data única)
- ✅ Campos de data devem ser enviados no formato ISO (yyyy-MM-dd)
- ✅ `dataFim` pode ser `null`, `undefined` ou omitido no payload

### 6.2. Tratamento de Erros
- **403 Forbidden**: Usuário não tem permissão para editar atividade
  - Mensagem sugerida: "Você não tem permissão para editar esta atividade. Apenas coordenadores da atividade podem editá-la."
- **400 Bad Request**: Validação de `dataFim < dataRealizacao`
  - Mensagem sugerida: "A data final deve ser posterior ou igual à data de realização."

### 6.3. Performance
- Considerar cachear informações de permissão para evitar múltiplas verificações
- Verificação de coordenador pode ser feita localmente se `atividade.integrantes` estiver disponível

---

## 📚 7. Exemplo de Requisição Completa

### POST /api/atividades
```json
{
  "nome": "Workshop de Desenvolvimento",
  "dataRealizacao": "2024-03-15",
  "dataFim": "2024-03-20",  // ⭐ NOVO CAMPO - opcional
  "statusPublicacao": true,
  "curso": { "id": 1 },
  "categoria": { "id": 2 },
  "objetivo": "Capacitar desenvolvedores",
  "publicoAlvo": "Estudantes de TI"
}
```

### Resposta
```json
{
  "id": 1,
  "nome": "Workshop de Desenvolvimento",
  "dataRealizacao": "2024-03-15",
  "dataFim": "2024-03-20",  // Retornado quando preenchido
  "statusPublicacao": true,
  // ... outros campos
}
```

---

## 🎯 8. Resumo das Mudanças

| Item | Mudança | Impacto |
|------|---------|---------|
| **AtividadeDTO** | Adicionado campo `dataFim?: string \| null` | Formulários, interfaces, exibição |
| **POST /api/atividades** | Aceita `COORDENADOR_ATIVIDADE` | Verificações de permissão |
| **PUT /api/atividades/{id}** | Aceita `COORDENADOR_ATIVIDADE` + validação de coordenador | Lógica de edição condicional |
| **DELETE /api/atividades/{id}** | Aceita `COORDENADOR_ATIVIDADE` + validação de coordenador | Lógica de exclusão condicional |
| **POST /api/evidencias** | Aceita `COORDENADOR_ATIVIDADE` + validação de coordenador | Lógica de criação condicional |
| **PUT /api/evidencias/{id}** | Aceita `COORDENADOR_ATIVIDADE` + validação de coordenador | Lógica de edição condicional |
| **DELETE /api/evidencias/{id}** | Aceita `COORDENADOR_ATIVIDADE` + validação de coordenador | Lógica de exclusão condicional |

---

**✅ Implementação concluída quando todas as tarefas do checklist estiverem completas e testadas!**

