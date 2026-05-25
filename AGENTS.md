# Backend Agent Instructions

Responda em portugues do Brasil.

## Escopo

Este repositorio contem a API Spring Boot do app-Portifolium.

## Regras de Trabalho

- Preserve a organizacao em controller, service, repository, DTO, model e exceptions.
- Mantenha regras de negocio nos services.
- Use DTOs para contratos de entrada e saida.
- Use as exceptions de dominio existentes para falhas esperadas.
- Nao exponha segredos, tokens, credenciais, payloads completos ou arquivos em logs.
- Rode `./mvnw test` antes de finalizar alteracoes de backend.

## Fluxo Git Padrao

Este workspace tem dois repositorios Git separados:

- Backend/API: `app-Portifolium-api-restfull`
- Frontend: `app-Portifolium-frontend`

Siga este fluxo para ambos os projetos:

1. Identifique qual repositorio foi alterado.
2. Nunca misture alteracoes de backend e frontend no mesmo commit.
3. Trabalhe a partir de `main` atualizada, salvo quando a tarefa exigir uma PR empilhada.
4. Crie uma branch especifica para a tarefa quando a alteracao ainda nao estiver em `main`.
5. Use nomes de branch descritivos, por exemplo:
   - `codex-backend-ajuste-seguranca`
   - `codex-frontend-versionamento`
   - `codex-docs-organizacao`
6. Implemente apenas o escopo solicitado.
7. Rode a validacao adequada antes do commit:
   - Backend/API: `./mvnw test`
   - Frontend: `npm run build`
8. Faca commit somente dos arquivos alterados pela tarefa.
9. Use mensagens objetivas, por exemplo:
   - `fix: corrigir validacao de curso`
   - `feat: adicionar fluxo de relatorio`
   - `docs: reorganizar documentacao`
   - `chore(release): vX.Y.Z`
10. Envie a branch para `origin`.
11. Abra PR para `main` quando a alteracao nao deve ir direto para `main`.
12. Se a tarefa pedir explicitamente para atualizar `main`, avance `main`, valide, faca push e informe o commit enviado.
13. Ao finalizar, informe repositorio, branch, commit, PR ou destino do push, e validacoes executadas.

## Release

Releases usam SemVer `x.y.z` e tag `vX.Y.Z`.

Use o script da raiz do workspace:

```powershell
.\scripts\release.ps1 patch
.\scripts\release.ps1 minor
.\scripts\release.ps1 major
.\scripts\release.ps1 1.4.2
```

Nao altere versoes manualmente quando o script puder ser usado.
