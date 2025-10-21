# 🎨 Paleta de Cores - AcadManage

## 📋 Visão Geral

Paleta de cores profissional e moderna para o sistema acadêmico AcadManage, utilizando tons de azul e teal (verde-azulado) que transmitem confiança, profissionalismo e educação.

---

## 🎨 Cores Principais

### **Cores Primárias (Azul Acadêmico)**

#### `--primary-color: #0D47A1`
- **Nome:** Azul Escuro Acadêmico
- **RGB:** rgb(13, 71, 161)
- **Uso:** Títulos, headings, elementos importantes
- **Significado:** Confiança, profissionalismo, estabilidade

#### `--primary-light: #1976D2`
- **Nome:** Azul Médio
- **RGB:** rgb(25, 118, 210)
- **Uso:** Botões primários, links, destaques
- **Significado:** Tecnologia, inovação

#### `--primary-dark: #01579B`
- **Nome:** Azul Profundo
- **RGB:** rgb(1, 87, 155)
- **Uso:** Hover states, elementos em foco
- **Significado:** Seriedade, autoridade

---

### **Cores de Destaque (Teal/Verde-Azulado)**

#### `--accent-color: #00897B`
- **Nome:** Teal Principal
- **RGB:** rgb(0, 137, 123)
- **Uso:** Call-to-actions secundários, badges, acentos
- **Significado:** Crescimento, educação, equilíbrio

#### `--accent-light: #4DB6AC`
- **Nome:** Teal Claro
- **RGB:** rgb(77, 182, 172)
- **Uso:** Backgrounds sutis, highlights suaves
- **Significado:** Frescor, vitalidade

---

### **Cores de Feedback**

#### `--error-color: #D32F2F`
- **Nome:** Vermelho de Erro
- **RGB:** rgb(211, 47, 47)
- **Uso:** Mensagens de erro, validações falhas
- **Background:** #FFEBEE

#### `--success-color: #388E3C`
- **Nome:** Verde de Sucesso
- **RGB:** rgb(56, 142, 60)
- **Uso:** Mensagens de sucesso, confirmações
- **Background:** #E8F5E9

---

### **Cores Neutras**

#### `--text-primary: #212121`
- **RGB:** rgb(33, 33, 33)
- **Uso:** Texto principal, conteúdo importante

#### `--text-secondary: #757575`
- **RGB:** rgb(117, 117, 117)
- **Uso:** Texto secundário, legendas, labels

#### `--white: #FFFFFF`
- **RGB:** rgb(255, 255, 255)
- **Uso:** Backgrounds, cards, formulários

#### `--gray-light: #F5F5F5`
- **RGB:** rgb(245, 245, 245)
- **Uso:** Backgrounds sutis, campos de input

#### `--gray-medium: #E0E0E0`
- **RGB:** rgb(224, 224, 224)
- **Uso:** Bordas, separadores

---

## 🌈 Gradientes

### **Gradiente Principal (Background)**
```css
background: linear-gradient(135deg, #0D47A1, #00695C);
```
- De: Azul Escuro → Para: Teal Escuro
- **Uso:** Background do login, páginas de entrada

### **Gradiente de Botão**
```css
background: linear-gradient(135deg, #1976D2, #00897B);
```
- De: Azul Médio → Para: Teal
- **Uso:** Botões primários, CTAs

---

## 📐 Aplicação na Tela de Login

### **Elementos Estilizados:**

1. **Background**
   - Gradiente azul → teal
   - Transmite profissionalismo e modernidade

2. **Card do Formulário**
   - Fundo branco (#FFFFFF)
   - Sombra suave para elevação
   - Bordas arredondadas (12px)

3. **Título (H1)**
   - Cor: `--primary-color` (#0D47A1)
   - Sublinhado decorativo em `--accent-color`

4. **Inputs**
   - Background: `--gray-light` (#F5F5F5)
   - Borda: `--gray-medium` (#E0E0E0)
   - Focus: `--primary-light` (#1976D2)

5. **Botão de Login**
   - Gradiente: Azul → Teal
   - Hover: Versão mais escura + elevação
   - Sombra azul para profundidade

6. **Mensagens**
   - Erro: Fundo rosa claro + borda vermelha
   - Sucesso: Fundo verde claro + borda verde

7. **Ícone**
   - Emoji de graduação (🎓)
   - Reforça contexto acadêmico

---

## 💡 Guia de Uso

### **Quando usar cada cor:**

#### **Azul Primário (#0D47A1)**
✅ Headers e títulos importantes  
✅ Navegação principal  
✅ Elementos de identidade da marca  
❌ Fundos grandes (muito escuro)

#### **Azul Médio (#1976D2)**
✅ Botões primários  
✅ Links principais  
✅ Elementos interativos  
✅ Ícones importantes

#### **Teal (#00897B)**
✅ Botões secundários  
✅ Badges e tags  
✅ Destaques de informação  
✅ Elementos de progresso

#### **Cinza Claro (#F5F5F5)**
✅ Backgrounds de cards  
✅ Campos de formulário  
✅ Seções alternadas  
❌ Texto (baixo contraste)

---

## 🎯 Acessibilidade

### **Contraste de Texto:**

| Combinação | Contraste | WCAG AA | WCAG AAA |
|------------|-----------|---------|----------|
| `#0D47A1` em `#FFFFFF` | 8.59:1 | ✅ Pass | ✅ Pass |
| `#1976D2` em `#FFFFFF` | 4.92:1 | ✅ Pass | ❌ Fail |
| `#00897B` em `#FFFFFF` | 3.44:1 | ⚠️ Large Text | ❌ Fail |
| `#212121` em `#FFFFFF` | 16.10:1 | ✅ Pass | ✅ Pass |
| `#757575` em `#FFFFFF` | 4.69:1 | ✅ Pass | ❌ Fail |

**Recomendações:**
- Use `--primary-color` (#0D47A1) para texto em fundos claros
- Use `--text-primary` (#212121) para corpo de texto
- Use `--text-secondary` (#757575) apenas para textos grandes ou não-críticos

---

## 🔧 Implementação em CSS

### **Uso de Variáveis CSS:**

```css
/* Definir variáveis no :root */
:root {
    --primary-color: #0D47A1;
    --primary-light: #1976D2;
    --accent-color: #00897B;
    /* ... */
}

/* Usar em qualquer lugar */
.button-primary {
    background-color: var(--primary-light);
    color: var(--white);
}

.button-primary:hover {
    background-color: var(--primary-dark);
}
```

---

## 📱 Responsividade

### **Mobile (< 600px):**
- Mantém paleta de cores
- Ajusta tamanhos e espaçamentos
- Garante áreas de toque adequadas (min 44px)

### **Tablet (600px - 900px):**
- Cores permanecem consistentes
- Layout adapta-se ao espaço disponível

### **Desktop (> 900px):**
- Experiência completa
- Efeitos de hover mais pronunciados

---

## 🎨 Exportação para Design

### **Para Figma/Adobe XD:**
```
Primárias:
#0D47A1 - Primary Dark
#1976D2 - Primary
#01579B - Primary Darker

Accent:
#00897B - Accent
#4DB6AC - Accent Light

Feedback:
#D32F2F - Error
#388E3C - Success

Neutras:
#212121 - Text Primary
#757575 - Text Secondary
#F5F5F5 - Background Light
#E0E0E0 - Borders
```

---

## ✨ Melhorias Visuais Implementadas

### **1. Animações**
- ✅ Fade in do formulário ao carregar
- ✅ Transições suaves em hover
- ✅ Elevação do botão ao passar o mouse

### **2. Sombras**
- ✅ Sombra do card: `0 8px 24px rgba(0, 0, 0, 0.15)`
- ✅ Sombra do botão: `0 4px 12px rgba(13, 71, 161, 0.3)`
- ✅ Sombra de foco: `0 0 0 3px rgba(25, 118, 210, 0.1)`

### **3. Bordas Arredondadas**
- Card: 12px
- Inputs: 8px
- Botão: 8px
- Mensagens: 6px

### **4. Tipografia**
- Fonte: Segoe UI (sistema operacional)
- Fallbacks: Tahoma, Geneva, Verdana, sans-serif
- Pesos: 400 (normal), 500 (medium), 600 (semi-bold)

---

## 🚀 Próximos Passos

Para manter a consistência visual em toda a aplicação:

1. ✅ **Criar arquivo CSS global** com variáveis
2. ⏳ **Aplicar em todos os componentes** do frontend
3. ⏳ **Documentar componentes** reutilizáveis
4. ⏳ **Criar guia de estilo** completo
5. ⏳ **Design system** com Storybook (opcional)

---

## 📄 Referências

- **Material Design:** Inspiração para paleta azul/teal
- **WCAG 2.1:** Diretrizes de acessibilidade
- **Google Material:** Referências de cores acadêmicas

---

**Criado em:** 19/10/2025  
**Versão:** 1.0  
**Aplicação:** AcadManage - Sistema de Gestão Acadêmica

