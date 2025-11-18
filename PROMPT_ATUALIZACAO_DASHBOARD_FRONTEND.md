# 📋 Prompt: Atualização do Frontend - Dashboard com Filtro por Usuário Logado

## 🎯 Objetivo
Atualizar o frontend para integrar o novo endpoint de dashboard que retorna métricas filtradas baseadas no usuário logado. Administradores visualizam números globais, enquanto Gerentes, Secretários e Coordenadores visualizam apenas estatísticas dos cursos associados.

---

## 🔑 1. Novo Endpoint de Dashboard

### 1.1. Endpoint

```
GET /api/dashboard
Authorization: Bearer {token}
Roles: ADMINISTRADOR, GERENTE, SECRETARIO, COORDENADOR_ATIVIDADE
```

### 1.2. Autenticação
- **Requerido**: Token JWT no header `Authorization: Bearer {token}`
- **Roles permitidas**: 
  - `ROLE_ADMINISTRADOR` - Visualiza números globais
  - `ROLE_GERENTE` - Visualiza apenas cursos associados
  - `ROLE_SECRETARIO` - Visualiza apenas cursos associados
  - `ROLE_COORDENADOR_ATIVIDADE` - Visualiza apenas cursos associados

### 1.3. Comportamento por Role

#### **ADMINISTRADOR**
- ✅ Visualiza números globais (todos os cursos do sistema)
- ✅ Visualiza distribuição de usuários
- ✅ Todos os totais são calculados globalmente

#### **GERENTE, SECRETARIO, COORDENADOR_ATIVIDADE**
- ✅ Visualiza apenas cursos associados ao usuário
- ❌ Não visualiza distribuição de usuários (retorna array vazio)
- ✅ Todos os totais são calculados apenas para os cursos do usuário
- ⚠️ Se não tiver cursos associados, recebe dashboard vazio (todos zeros)

---

## 📦 2. Estrutura de Dados (DTOs)

### 2.1. DashboardDTO (Resposta Principal)

```typescript
interface DashboardDTO {
  metricasGerais: MetricasGeraisDTO;
  atividadesPorCategoria: AtividadePorCategoriaDTO[];
  statusPublicacao: StatusPublicacaoDTO;
  distribuicaoUsuarios: DistribuicaoUsuarioDTO[]; // Vazio para não-admin
  cursosDestaque: CursoDestaqueDTO[];
  atividadesRecentes: AtividadeRecenteDTO[];
  metasProgresso: MetaProgressoDTO[];
}
```

### 2.2. MetricasGeraisDTO

```typescript
interface MetricasGeraisDTO {
  totalCursos: MetricaDTO;
  atividadesAtivas: MetricaDTO;
  usuariosCadastrados: MetricaDTO;
  pessoasCadastradas: MetricaDTO; // ⭐ NOVO CAMPO
  fontesFinanciadoras: MetricaDTO;
  publicacoes: MetricaDTO;
  taxaConclusao: MetricaDTO;
}
```

### 2.3. MetricaDTO

```typescript
interface MetricaDTO {
  percentualCrescimento: number; // Pode ser negativo (ex: -5.0)
  valor: number; // Valor atual da métrica
  descricaoCrescimento: string; // Ex: "mais que o mês anterior", "de aumento"
}
```

**Exemplo**:
```json
{
  "percentualCrescimento": 12.5,
  "valor": 15,
  "descricaoCrescimento": "mais que o mês anterior"
}
```

### 2.4. Outros DTOs

```typescript
interface AtividadePorCategoriaDTO {
  categoria: string;
  quantidade: number;
}

interface StatusPublicacaoDTO {
  publicadas: number;
  naoPublicadas: number;
  percentualPublicadas: number; // Percentual (0-100)
}

interface DistribuicaoUsuarioDTO {
  tipo: string; // "Administradores", "Gerentes", "Secretários", "Alunos", "Professores"
  quantidade: number;
}
// ⚠️ IMPORTANTE: Este array estará VAZIO para usuários não-admin

interface CursoDestaqueDTO {
  nome: string;
  quantidadeAtividades: number;
  quantidadeUsuarios: number;
}

interface AtividadeRecenteDTO {
  tipo: string; // "Publicação", "Sistema"
  descricao: string;
  dataHora: string; // ISO 8601 format
  tempoDecorrido: string; // Ex: "2 horas atrás", "1 dia atrás"
}

interface MetaProgressoDTO {
  nome: string;
  atual: number;
  meta: number;
  percentual: number; // Percentual de conclusão (0-100+)
}
```

---

## 📊 3. Exemplo de Resposta Completa

### 3.1. Resposta para ADMINISTRADOR

```json
{
  "metricasGerais": {
    "totalCursos": {
      "percentualCrescimento": 12.0,
      "valor": 15,
      "descricaoCrescimento": "mais que o mês anterior"
    },
    "atividadesAtivas": {
      "percentualCrescimento": 8.0,
      "valor": 48,
      "descricaoCrescimento": "de crescimento"
    },
    "usuariosCadastrados": {
      "percentualCrescimento": 15.0,
      "valor": 234,
      "descricaoCrescimento": "de aumento"
    },
    "pessoasCadastradas": {
      "percentualCrescimento": 10.0,
      "valor": 250,
      "descricaoCrescimento": "de aumento"
    },
    "fontesFinanciadoras": {
      "percentualCrescimento": -5.0,
      "valor": 12,
      "descricaoCrescimento": "novas fontes este mês"
    },
    "publicacoes": {
      "percentualCrescimento": 20.0,
      "valor": 89,
      "descricaoCrescimento": "mais publicações"
    },
    "taxaConclusao": {
      "percentualCrescimento": 5.0,
      "valor": 87,
      "descricaoCrescimento": "de melhoria"
    }
  },
  "atividadesPorCategoria": [
    {
      "categoria": "Ensino",
      "quantidade": 32
    },
    {
      "categoria": "Pesquisa",
      "quantidade": 28
    },
    {
      "categoria": "Extensão",
      "quantidade": 24
    },
    {
      "categoria": "Inovação",
      "quantidade": 16
    }
  ],
  "statusPublicacao": {
    "publicadas": 65,
    "naoPublicadas": 35,
    "percentualPublicadas": 65.0
  },
  "distribuicaoUsuarios": [
    {
      "tipo": "Administradores",
      "quantidade": 4
    },
    {
      "tipo": "Gerentes",
      "quantidade": 10
    },
    {
      "tipo": "Secretários",
      "quantidade": 25
    },
    {
      "tipo": "Alunos",
      "quantidade": 150
    },
    {
      "tipo": "Professores",
      "quantidade": 45
    }
  ],
  "cursosDestaque": [
    {
      "nome": "Engenharia de Software",
      "quantidadeAtividades": 18,
      "quantidadeUsuarios": 85
    },
    {
      "nome": "Ciência da Computação",
      "quantidadeAtividades": 15,
      "quantidadeUsuarios": 72
    },
    {
      "nome": "Sistemas de Informação",
      "quantidadeAtividades": 12,
      "quantidadeUsuarios": 64
    },
    {
      "nome": "Análise e Desenvolvimento",
      "quantidadeAtividades": 10,
      "quantidadeUsuarios": 58
    }
  ],
  "atividadesRecentes": [
    {
      "tipo": "Publicação",
      "descricao": "Atividade \"Workshop de IA\" publicada",
      "dataHora": "2024-11-17T10:00:00",
      "tempoDecorrido": "2 horas atrás"
    },
    {
      "tipo": "Publicação",
      "descricao": "Atividade \"Seminário de Pesquisa\" publicada",
      "dataHora": "2024-11-16T14:00:00",
      "tempoDecorrido": "1 dia atrás"
    }
  ],
  "metasProgresso": [
    {
      "nome": "Atividades de Extensão",
      "atual": 24,
      "meta": 30,
      "percentual": 80.0
    },
    {
      "nome": "Projetos de Pesquisa",
      "atual": 18,
      "meta": 20,
      "percentual": 90.0
    },
    {
      "nome": "Publicações Científicas",
      "atual": 42,
      "meta": 50,
      "percentual": 84.0
    },
    {
      "nome": "Captação de Recursos (R$)",
      "atual": 350000,
      "meta": 500000,
      "percentual": 70.0
    }
  ]
}
```

### 3.2. Resposta para GERENTE/SECRETARIO (sem cursos associados)

```json
{
  "metricasGerais": {
    "totalCursos": {
      "percentualCrescimento": 0.0,
      "valor": 0,
      "descricaoCrescimento": ""
    },
    "atividadesAtivas": {
      "percentualCrescimento": 0.0,
      "valor": 0,
      "descricaoCrescimento": ""
    },
    // ... todos os valores são 0
  },
  "atividadesPorCategoria": [],
  "statusPublicacao": {
    "publicadas": 0,
    "naoPublicadas": 0,
    "percentualPublicadas": 0.0
  },
  "distribuicaoUsuarios": [], // ⚠️ VAZIO para não-admin
  "cursosDestaque": [],
  "atividadesRecentes": [],
  "metasProgresso": []
}
```

---

## 🔧 4. Tarefas no Frontend

### 4.1. Criar Service/API Client

```typescript
// dashboard.service.ts ou api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardDTO } from '../models/dashboard.dto';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = '/api/dashboard';

  constructor(private http: HttpClient) {}

  obterDadosDashboard(): Observable<DashboardDTO> {
    return this.http.get<DashboardDTO>(this.apiUrl);
  }
}
```

### 4.2. Criar Interfaces/Models

```typescript
// models/dashboard.dto.ts
export interface DashboardDTO {
  metricasGerais: MetricasGeraisDTO;
  atividadesPorCategoria: AtividadePorCategoriaDTO[];
  statusPublicacao: StatusPublicacaoDTO;
  distribuicaoUsuarios: DistribuicaoUsuarioDTO[];
  cursosDestaque: CursoDestaqueDTO[];
  atividadesRecentes: AtividadeRecenteDTO[];
  metasProgresso: MetaProgressoDTO[];
}

export interface MetricasGeraisDTO {
  totalCursos: MetricaDTO;
  atividadesAtivas: MetricaDTO;
  usuariosCadastrados: MetricaDTO;
  pessoasCadastradas: MetricaDTO; // ⭐ NOVO
  fontesFinanciadoras: MetricaDTO;
  publicacoes: MetricaDTO;
  taxaConclusao: MetricaDTO;
}

export interface MetricaDTO {
  percentualCrescimento: number;
  valor: number;
  descricaoCrescimento: string;
}

export interface AtividadePorCategoriaDTO {
  categoria: string;
  quantidade: number;
}

export interface StatusPublicacaoDTO {
  publicadas: number;
  naoPublicadas: number;
  percentualPublicadas: number;
}

export interface DistribuicaoUsuarioDTO {
  tipo: string;
  quantidade: number;
}

export interface CursoDestaqueDTO {
  nome: string;
  quantidadeAtividades: number;
  quantidadeUsuarios: number;
}

export interface AtividadeRecenteDTO {
  tipo: string;
  descricao: string;
  dataHora: string; // ISO 8601
  tempoDecorrido: string;
}

export interface MetaProgressoDTO {
  nome: string;
  atual: number;
  meta: number;
  percentual: number;
}
```

### 4.3. Criar Componente de Dashboard

```typescript
// dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { DashboardDTO } from '../models/dashboard.dto';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  dashboard: DashboardDTO | null = null;
  loading = true;
  error: string | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.carregarDashboard();
  }

  carregarDashboard(): void {
    this.loading = true;
    this.error = null;

    this.dashboardService.obterDadosDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar dashboard';
        this.loading = false;
        console.error(err);
      }
    });
  }

  // Função auxiliar para formatar percentual de crescimento
  formatarPercentualCrescimento(percentual: number): string {
    if (percentual > 0) {
      return `+${percentual.toFixed(1)}%`;
    } else if (percentual < 0) {
      return `${percentual.toFixed(1)}%`;
    }
    return '0%';
  }

  // Verificar se é crescimento ou decréscimo
  ehCrescimento(percentual: number): boolean {
    return percentual > 0;
  }

  // Verificar se tem dados
  temDados(): boolean {
    return this.dashboard !== null && 
           this.dashboard.metricasGerais.totalCursos.valor > 0;
  }

  // Verificar se é admin (tem distribuição de usuários)
  ehAdmin(): boolean {
    return this.dashboard !== null && 
           this.dashboard.distribuicaoUsuarios.length > 0;
  }
}
```

### 4.4. Template HTML Exemplo

```html
<!-- dashboard.component.html -->
<div class="dashboard-container">
  <div *ngIf="loading" class="loading">
    <p>Carregando dashboard...</p>
  </div>

  <div *ngIf="error" class="error">
    <p>{{ error }}</p>
    <button (click)="carregarDashboard()">Tentar novamente</button>
  </div>

  <div *ngIf="!loading && !error && dashboard">
    <!-- Verificar se tem dados -->
    <div *ngIf="!temDados()" class="empty-state">
      <h2>Nenhum curso associado</h2>
      <p>Você não possui cursos associados. Entre em contato com o administrador.</p>
    </div>

    <div *ngIf="temDados()">
      <!-- Métricas Gerais -->
      <section class="metricas-gerais">
        <h2>Visão Geral do Sistema</h2>
        <div class="metricas-grid">
          <!-- Total de Cursos -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Total de Cursos</span>
              <span 
                class="metrica-percentual" 
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.totalCursos.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.totalCursos.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.totalCursos.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.totalCursos.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.totalCursos.descricaoCrescimento }}
            </div>
          </div>

          <!-- Atividades Ativas -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Atividades Ativas</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.atividadesAtivas.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.atividadesAtivas.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.atividadesAtivas.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.atividadesAtivas.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.atividadesAtivas.descricaoCrescimento }}
            </div>
          </div>

          <!-- Usuários Cadastrados -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Usuários Cadastrados</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.usuariosCadastrados.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.usuariosCadastrados.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.usuariosCadastrados.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.usuariosCadastrados.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.usuariosCadastrados.descricaoCrescimento }}
            </div>
          </div>

          <!-- Pessoas Cadastradas (NOVO) -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Pessoas Cadastradas</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.pessoasCadastradas.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.pessoasCadastradas.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.pessoasCadastradas.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.pessoasCadastradas.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.pessoasCadastradas.descricaoCrescimento }}
            </div>
          </div>

          <!-- Fontes Financiadoras -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Fontes Financiadoras</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.fontesFinanciadoras.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.fontesFinanciadoras.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.fontesFinanciadoras.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.fontesFinanciadoras.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.fontesFinanciadoras.descricaoCrescimento }}
            </div>
          </div>

          <!-- Publicações -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Publicações</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.publicacoes.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.publicacoes.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.publicacoes.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.publicacoes.valor }}</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.publicacoes.descricaoCrescimento }}
            </div>
          </div>

          <!-- Taxa de Conclusão -->
          <div class="metrica-card">
            <div class="metrica-header">
              <span class="metrica-titulo">Taxa de Conclusão</span>
              <span 
                class="metrica-percentual"
                [class.crescimento]="ehCrescimento(dashboard.metricasGerais.taxaConclusao.percentualCrescimento)"
                [class.decrescimento]="!ehCrescimento(dashboard.metricasGerais.taxaConclusao.percentualCrescimento)">
                {{ formatarPercentualCrescimento(dashboard.metricasGerais.taxaConclusao.percentualCrescimento) }}
              </span>
            </div>
            <div class="metrica-valor">{{ dashboard.metricasGerais.taxaConclusao.valor }}%</div>
            <div class="metrica-descricao">
              {{ dashboard.metricasGerais.taxaConclusao.descricaoCrescimento }}
            </div>
          </div>
        </div>
      </section>

      <!-- Atividades por Categoria -->
      <section class="atividades-categoria">
        <h2>Atividades por Categoria</h2>
        <div class="categoria-list">
          <div *ngFor="let item of dashboard.atividadesPorCategoria" class="categoria-item">
            <span class="categoria-nome">{{ item.categoria }}</span>
            <span class="categoria-quantidade">{{ item.quantidade }}</span>
          </div>
        </div>
      </section>

      <!-- Status de Publicação -->
      <section class="status-publicacao">
        <h2>Status de Publicação</h2>
        <div class="status-grid">
          <div class="status-item">
            <span class="status-label">Publicadas</span>
            <span class="status-valor">{{ dashboard.statusPublicacao.publicadas }}</span>
            <span class="status-percentual">{{ dashboard.statusPublicacao.percentualPublicadas.toFixed(0) }}%</span>
          </div>
          <div class="status-item">
            <span class="status-label">Não Publicadas</span>
            <span class="status-valor">{{ dashboard.statusPublicacao.naoPublicadas }}</span>
            <span class="status-percentual">
              {{ (100 - dashboard.statusPublicacao.percentualPublicadas).toFixed(0) }}%
            </span>
          </div>
        </div>
      </section>

      <!-- Distribuição de Usuários (apenas para admin) -->
      <section *ngIf="ehAdmin()" class="distribuicao-usuarios">
        <h2>Distribuição de Usuários</h2>
        <div class="distribuicao-list">
          <div *ngFor="let item of dashboard.distribuicaoUsuarios" class="distribuicao-item">
            <span class="distribuicao-tipo">{{ item.tipo }}</span>
            <span class="distribuicao-quantidade">{{ item.quantidade }}</span>
          </div>
        </div>
      </section>

      <!-- Cursos em Destaque -->
      <section class="cursos-destaque">
        <h2>Cursos em Destaque</h2>
        <div class="cursos-grid">
          <div *ngFor="let curso of dashboard.cursosDestaque" class="curso-card">
            <h3>{{ curso.nome }}</h3>
            <div class="curso-stats">
              <span>{{ curso.quantidadeAtividades }} atividades</span>
              <span>{{ curso.quantidadeUsuarios }} usuários</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Atividades Recentes -->
      <section class="atividades-recentes">
        <h2>Atividades Recentes</h2>
        <div class="recentes-list">
          <div *ngFor="let item of dashboard.atividadesRecentes" class="recente-item">
            <div class="recente-tipo">{{ item.tipo }}</div>
            <div class="recente-descricao">{{ item.descricao }}</div>
            <div class="recente-tempo">{{ item.tempoDecorrido }}</div>
          </div>
        </div>
      </section>

      <!-- Metas e Progresso -->
      <section class="metas-progresso">
        <h2>Metas e Progresso</h2>
        <div class="metas-grid">
          <div *ngFor="let meta of dashboard.metasProgresso" class="meta-card">
            <div class="meta-header">
              <span class="meta-nome">{{ meta.nome }}</span>
              <span class="meta-percentual">{{ meta.percentual.toFixed(0) }}%</span>
            </div>
            <div class="meta-progresso-bar">
              <div 
                class="meta-progresso-fill" 
                [style.width.%]="Math.min(meta.percentual, 100)">
              </div>
            </div>
            <div class="meta-stats">
              <span>{{ meta.atual }} / {{ meta.meta }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</div>
```

### 4.5. Tratamento de Erros

```typescript
// dashboard.component.ts - Tratamento de erros
carregarDashboard(): void {
  this.loading = true;
  this.error = null;

  this.dashboardService.obterDadosDashboard().subscribe({
    next: (data) => {
      this.dashboard = data;
      this.loading = false;
    },
    error: (err) => {
      if (err.status === 401) {
        this.error = 'Não autorizado. Faça login novamente.';
        // Redirecionar para login
      } else if (err.status === 403) {
        this.error = 'Você não tem permissão para acessar o dashboard.';
      } else if (err.status === 500) {
        this.error = 'Erro no servidor. Tente novamente mais tarde.';
      } else {
        this.error = 'Erro ao carregar dashboard. Tente novamente.';
      }
      this.loading = false;
      console.error('Erro ao carregar dashboard:', err);
    }
  });
}
```

---

## ✅ 5. Checklist de Implementação

### Interface/Model
- [ ] Criar interface `DashboardDTO`
- [ ] Criar interface `MetricasGeraisDTO` com campo `pessoasCadastradas` (novo)
- [ ] Criar interface `MetricaDTO`
- [ ] Criar interfaces `AtividadePorCategoriaDTO`, `StatusPublicacaoDTO`, etc.
- [ ] Criar interface `DistribuicaoUsuarioDTO` (pode estar vazio para não-admin)

### Service/API Client
- [ ] Criar `DashboardService` com método `obterDadosDashboard()`
- [ ] Configurar interceptors para incluir token JWT
- [ ] Implementar tratamento de erros HTTP

### Componente
- [ ] Criar componente `DashboardComponent`
- [ ] Implementar método `ngOnInit()` para carregar dados
- [ ] Implementar funções auxiliares:
  - [ ] `formatarPercentualCrescimento()`
  - [ ] `ehCrescimento()`
  - [ ] `temDados()` - verifica se tem dados (não é dashboard vazio)
  - [ ] `ehAdmin()` - verifica se é admin (tem distribuição de usuários)

### Template/UI
- [ ] Criar template HTML com todas as seções:
  - [ ] Métricas Gerais (7 métricas, incluindo Pessoas Cadastradas)
  - [ ] Atividades por Categoria
  - [ ] Status de Publicação
  - [ ] Distribuição de Usuários (condicional: só para admin)
  - [ ] Cursos em Destaque
  - [ ] Atividades Recentes
  - [ ] Metas e Progresso
- [ ] Exibir estado de loading
- [ ] Exibir estado de erro
- [ ] Exibir estado vazio (sem cursos associados)

### Estilização
- [ ] Estilizar cards de métricas
- [ ] Estilizar percentuais de crescimento (verde para positivo, vermelho para negativo)
- [ ] Estilizar barras de progresso das metas
- [ ] Responsividade para mobile/tablet
- [ ] Cores diferenciadas para diferentes tipos de métricas

### Testes
- [ ] Testar como ADMINISTRADOR (deve ver todos os dados)
- [ ] Testar como GERENTE com cursos associados
- [ ] Testar como GERENTE sem cursos associados (dashboard vazio)
- [ ] Testar como SECRETARIO
- [ ] Testar como COORDENADOR_ATIVIDADE
- [ ] Testar tratamento de erro 401 (não autorizado)
- [ ] Testar tratamento de erro 403 (sem permissão)
- [ ] Testar tratamento de erro 500 (erro do servidor)

---

## 🎨 6. Sugestões de UI/UX

### 6.1. Indicadores Visuais

- **Percentual de Crescimento**:
  - Verde (↑) para valores positivos
  - Vermelho (↓) para valores negativos
  - Cinza para zero

- **Cards de Métricas**:
  - Usar ícones diferentes para cada métrica
  - Destacar o valor principal
  - Mostrar percentual de forma discreta

- **Barras de Progresso**:
  - Usar cores diferentes baseadas no percentual:
    - Verde: > 80%
    - Amarelo: 50-80%
    - Vermelho: < 50%

### 6.2. Responsividade

- Grid de métricas: 3 colunas (desktop), 2 colunas (tablet), 1 coluna (mobile)
- Cursos em destaque: 4 colunas (desktop), 2 colunas (tablet), 1 coluna (mobile)

### 6.3. Performance

- Implementar cache (opcional) para evitar múltiplas requisições
- Loading skeleton para melhor UX durante carregamento
- Lazy loading de gráficos (se houver)

---

## ⚠️ 7. Pontos Importantes

### 7.1. Filtro por Usuário
- ✅ O backend **automaticamente** filtra os dados baseado no usuário logado
- ✅ O frontend **não precisa** enviar parâmetros adicionais
- ✅ O token JWT é usado para identificar o usuário

### 7.2. Dashboard Vazio
- ⚠️ Se um usuário não-admin não tiver cursos associados, receberá um dashboard vazio
- ✅ Exibir mensagem amigável: "Você não possui cursos associados. Entre em contato com o administrador."

### 7.3. Distribuição de Usuários
- ⚠️ Esta seção **só aparece para ADMINISTRADOR**
- ✅ Para outros roles, o array `distribuicaoUsuarios` estará **vazio**
- ✅ Usar `*ngIf` para mostrar/ocultar a seção

### 7.4. Percentuais de Crescimento
- ✅ Pode ser positivo, negativo ou zero
- ✅ Formatá-los com sinal `+` ou `-` e símbolo `%`
- ✅ Usar cores diferentes para crescimento/decrescimento

### 7.5. Novos Campos
- ✅ `pessoasCadastradas` foi adicionado em `MetricasGeraisDTO`
- ✅ Garantir que todas as 7 métricas sejam exibidas

---

## 📚 8. Exemplo de Uso Completo

```typescript
// app.module.ts ou app-routing.module.ts
import { DashboardComponent } from './dashboard/dashboard.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard] // Proteger rota
  }
];
```

```typescript
// auth.guard.ts - Verificar roles
canActivate(): boolean {
  const userRoles = this.authService.getUserRoles();
  const rolesPermitidas = [
    'ROLE_ADMINISTRADOR',
    'ROLE_GERENTE',
    'ROLE_SECRETARIO',
    'ROLE_COORDENADOR_ATIVIDADE'
  ];
  
  return userRoles.some(role => rolesPermitidas.includes(role));
}
```

---

**✅ Implementação concluída quando todas as tarefas do checklist estiverem completas e testadas!**

