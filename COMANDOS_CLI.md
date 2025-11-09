# Guia de Comandos CLI - AcadManage API

Este documento apresenta todos os comandos disponíveis para executar o projeto **AcadManage** via linha de comando (CLI).

## 📋 Pré-requisitos

- **Java 17+** instalado
- **Maven** instalado (ou use o `mvnw` wrapper incluído no projeto)
- **Windows PowerShell** ou **Git Bash** (para scripts)

## 🚀 Executar o Projeto

### Opção 1: Usando Maven Wrapper (Recomendado)

#### Windows (PowerShell/CMD)
```powershell
# Executar a aplicação Spring Boot
.\mvnw.cmd spring-boot:run

# Ou usando Maven direto (se instalado)
mvn spring-boot:run
```

#### Linux/Mac
```bash
# Executar a aplicação Spring Boot
./mvnw spring-boot:run

# Ou usando Maven direto (se instalado)
mvn spring-boot:run
```

### Opção 2: Compilar e Executar JAR

```powershell
# Compilar o projeto
.\mvnw.cmd clean package

# Executar o JAR gerado
java -jar target/acadmanage-0.0.1-SNAPSHOT.jar
```

### Opção 3: Executar com Perfil Específico

```powershell
# Executar com perfil JWT (padrão)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=jwt

# Executar com perfil Docker
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker
```

## 🧪 Executar Testes

### Executar Todos os Testes
```powershell
.\mvnw.cmd test
```

### Executar Teste Específico
```powershell
# Exemplo: Executar apenas EvidenciaControllerIT
.\mvnw.cmd test -Dtest=EvidenciaControllerIT

# Executar múltiplos testes
.\mvnw.cmd test -Dtest=EvidenciaControllerIT,AtividadeControllerIT
```

### Executar Todos os Testes de Integração (Ordem Específica)
```powershell
# Usar o script PowerShell que executa na ordem correta
.\run-all-tests.ps1
```

### Ver Cobertura de Testes
```powershell
.\mvnw.cmd test jacoco:report
# Relatório será gerado em: target/site/jacoco/index.html
```

## 🛠️ Comandos Maven Úteis

### Compilar o Projeto
```powershell
# Limpar e compilar
.\mvnw.cmd clean compile

# Compilar ignorando testes
.\mvnw.cmd clean package -DskipTests

# Compilar e gerar JAR
.\mvnw.cmd clean package
```

### Limpar o Projeto
```powershell
# Remover arquivos compilados
.\mvnw.cmd clean

# Limpar incluindo logs e temporários
.\mvnw.cmd clean clean-all
```

### Instalar Dependências
```powershell
# Baixar e instalar dependências
.\mvnw.cmd dependency:resolve

# Ver dependências do projeto
.\mvnw.cmd dependency:tree

# Verificar atualizações de dependências
.\mvnw.cmd versions:display-dependency-updates
```

### Verificar o Projeto
```powershell
# Validar estrutura do projeto
.\mvnw.cmd validate

# Verificar plugins
.\mvnw.cmd help:effective-pom

# Ver informações do projeto
.\mvnw.cmd help:effective-settings
```

## 🐳 Executar com Docker

### Docker Compose (Aplicação + Monitoramento)
```powershell
# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down

# Reconstruir e iniciar
docker-compose up -d --build
```

### Docker Build e Run
```powershell
# Build da imagem
docker build -t acadmanage:latest .

# Executar container
docker run -p 8080:8080 acadmanage:latest

# Executar com variáveis de ambiente
docker run -p 8080:8080 -e EMAIL_PASSWORD=senha acadmanage:latest
```

## 🔧 Comandos de Desenvolvimento

### Gerar Documentação da API
```powershell
# A documentação Swagger é gerada automaticamente
# Acesse após iniciar: http://localhost:8080/swagger-ui.html
```

### Acessar Console H2 Database
```powershell
# Após iniciar a aplicação, acesse:
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./data/testdb
# Usuário: sa
# Senha: (vazio)
```

### Verificar Porta em Uso
```powershell
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Parar Aplicação em Execução
```powershell
# Encontrar processo Java na porta 8080 e encerrar
# Windows PowerShell
Get-NetTCPConnection -LocalPort 8080 | Select-Object -ExpandProperty OwningProcess | ForEach-Object { Stop-Process -Id $_ }

# Ou usar Ctrl+C no terminal onde está rodando
```

## 📊 Monitoramento e Métricas

### Health Check
```powershell
# Verificar saúde da aplicação (após iniciar)
curl http://localhost:8080/actuator/health

# Ou no navegador
# http://localhost:8080/actuator/health
```

### Ver Métricas
```powershell
# Métricas do Prometheus (se estiver rodando)
curl http://localhost:9090/metrics

# Acessar Grafana (após docker-compose)
# http://localhost:3000
# Usuário: admin
# Senha: admin
```

## 🔐 Configuração de Ambiente

### Variáveis de Ambiente
```powershell
# Windows PowerShell - Definir variável temporária
$env:EMAIL_PASSWORD="sua-senha-email"

# Windows CMD
set EMAIL_PASSWORD=sua-senha-email

# Linux/Mac
export EMAIL_PASSWORD=sua-senha-email
```

### Executar com Variáveis de Ambiente
```powershell
# Windows PowerShell
$env:EMAIL_PASSWORD="senha"; .\mvnw.cmd spring-boot:run

# Linux/Mac
EMAIL_PASSWORD=senha ./mvnw spring-boot:run
```

## 📝 Scripts Disponíveis

### Executar Todos os Testes (Ordem Correta)
```powershell
.\run-all-tests.ps1
```

### Build e Teste
```powershell
# Script bash (Linux/Mac)
.\scripts\build-and-test.sh

# Ou manualmente
.\mvnw.cmd clean test
```

### Deploy
```powershell
# Script de deploy (se disponível)
.\scripts\deploy.sh staging
.\scripts\deploy.sh production
```

## 🌐 Endpoints Principais

Após iniciar a aplicação, os principais endpoints disponíveis:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console
- **Health Check:** http://localhost:8080/actuator/health
- **API Base:** http://localhost:8080/api

### Exemplo de Teste com cURL

```powershell
# Login
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@uea.edu.br\",\"password\":\"admin123\"}"

# Listar cursos (com token)
curl -X GET http://localhost:8080/api/cursos ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## 🐛 Troubleshooting

### Erro: Porta já em uso
```powershell
# Encontrar processo e encerrar
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill
```

### Erro: Permissão negada no mvnw
```powershell
# Windows - Dar permissão de execução
icacls mvnw.cmd /grant Everyone:RX

# Linux/Mac
chmod +x mvnw
```

### Erro: Maven não encontrado
```powershell
# Use o wrapper incluído (mvnw.cmd ou ./mvnw)
# Não precisa ter Maven instalado globalmente
```

### Limpar Cache do Maven
```powershell
# Limpar cache local
.\mvnw.cmd dependency:purge-local-repository
```

## 📚 Comandos Avançados

### Executar com Debug
```powershell
# Habilitar debug remoto na porta 5005
.\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Ver Dependências com Vulnerabilidades
```powershell
# Verificar vulnerabilidades (se plugin estiver configurado)
.\mvnw.cmd org.owasp:dependency-check-maven:check
```

### Gerar Relatório de Testes
```powershell
# Relatório será gerado em: target/surefire-reports
.\mvnw.cmd test surefire-report:report
```

## 💡 Dicas Úteis

1. **Primeira Execução:** A primeira vez pode demorar mais devido ao download de dependências
2. **Modo Desenvolvimento:** O Spring Boot DevTools recarrega automaticamente mudanças
3. **Logs:** Configure nível de log no `application.properties`:
   ```
   logging.level.edu.uea.acadmanage=DEBUG
   ```
4. **Performance:** Use `-Xmx512m` para limitar memória:
   ```powershell
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"
   ```

## 📖 Mais Informações

- **Documentação Spring Boot:** https://spring.io/projects/spring-boot
- **Maven Documentation:** https://maven.apache.org/guides/
- **Projeto GitHub:** Ver README.md principal

---

**Nota:** Todos os comandos assumem que você está no diretório raiz do projeto (`d:\app-Portifolium-api-restfull`).

