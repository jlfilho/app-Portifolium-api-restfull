# Guia de Execução de Testes

Este documento explica como executar os testes automatizados da API REST.

## 🚀 Executar Todos os Testes (Ordem Correta)

### Opção 1: Usar o Script PowerShell (Recomendado)

O script `run-all-tests.ps1` executa todos os testes na ordem correta:

```powershell
.\run-all-tests.ps1
```

**Ordem de execução:**
1. `TipoCursoControllerIT` (primeiro)
2. `AuthenticationControllerIT`
3. `PasswordRecoveryControllerIT`
4. `UsuarioControllerIT` (último - pode alterar senhas)

### Opção 2: Comandos Manuais Sequenciais

Execute na ordem:

```powershell
# 1. TipoCursoControllerIT primeiro
.\mvnw.cmd test -Dtest=TipoCursoControllerIT

# 2. AuthenticationControllerIT
.\mvnw.cmd test -Dtest=AuthenticationControllerIT

# 3. PasswordRecoveryControllerIT
.\mvnw.cmd test -Dtest=PasswordRecoveryControllerIT

# 4. UsuarioControllerIT por último (altera senhas)
.\mvnw.cmd test -Dtest=UsuarioControllerIT
```

## 📋 Executar Testes Individuais

### Teste de Autenticação
```powershell
.\mvnw.cmd test -Dtest=AuthenticationControllerIT
```

### Teste de Recuperação de Senha
```powershell
.\mvnw.cmd test -Dtest=PasswordRecoveryControllerIT
```

### Teste de Tipo de Curso
```powershell
.\mvnw.cmd test -Dtest=TipoCursoControllerIT
```

### Teste de Usuário
```powershell
.\mvnw.cmd test -Dtest=UsuarioControllerIT
```

## ⚠️ Importante: Ordem de Execução

**Por que a ordem importa?**

O teste `UsuarioControllerIT` inclui um teste que altera a senha do usuário admin (`deveAlterarSenhaDoUsuario`). Embora o teste tente restaurar a senha original, quando todos os testes são executados juntos, outros testes podem falhar se tentarem fazer login antes que a senha seja restaurada.

**Solução:**
- Execute `TipoCursoControllerIT` **primeiro** (antes de qualquer alteração de senha)
- Execute `UsuarioControllerIT` **por último** (depois de todos os outros)

## 🔧 Opções Avançadas

### Executar com Clean (limpar antes)
```powershell
.\mvnw.cmd clean test -Dtest=NomeDoTeste
```

### Executar todos os testes do projeto (sem ordem garantida)
```powershell
.\mvnw.cmd test
```

### Executar apenas testes de integração
```powershell
.\mvnw.cmd test -Dtest="*ControllerIT"
```
⚠️ **Atenção:** Isso não garante a ordem, use o script `run-all-tests.ps1` se precisar da ordem correta.

## 📊 Estatísticas dos Testes

- **AuthenticationControllerIT**: 2 testes
- **PasswordRecoveryControllerIT**: 6 testes
- **TipoCursoControllerIT**: 16 testes
- **UsuarioControllerIT**: 15 testes

**Total**: 39 testes de integração

## 🐛 Troubleshooting

### Problema: "Expected status code <200> but was <401>"

Isso geralmente acontece quando:
1. A senha foi alterada por outro teste
2. O ApplicationContext não foi limpo corretamente

**Solução:**
1. Execute com `clean`: `.\mvnw.cmd clean test -Dtest=NomeDoTeste`
2. Use o script `run-all-tests.ps1` que executa na ordem correta

### Problema: ApplicationContext failure

**Solução:**
```powershell
.\mvnw.cmd clean test
```

## 📝 Notas

- Todos os testes usam o perfil `jwt` (`@ActiveProfiles("jwt")`)
- Os testes são executados em uma porta aleatória (`@LocalServerPort`)
- O `@DirtiesContext` é usado para garantir isolamento entre classes de teste

