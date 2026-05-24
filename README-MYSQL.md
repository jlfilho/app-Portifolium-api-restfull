# 🐬 Executando a Aplicação com MySQL via Docker

Este guia explica como executar a aplicação **Portifolium API** usando **MySQL** como banco de dados através do Docker Compose.

## 📋 Pré-requisitos

- Docker Desktop instalado e rodando
- Docker Compose v2.0+
- Porta 8080, 3306, 6379, 9090 e 3000 disponíveis

## 🚀 Início Rápido

### Opção 1: Usando Scripts Auxiliares (Recomendado)

#### Windows (PowerShell):
```powershell
# Iniciar todos os serviços
.\docker-mysql-startup.ps1 up

# Ver logs
.\docker-mysql-startup.ps1 logs

# Parar serviços
.\docker-mysql-startup.ps1 down

# Reiniciar aplicação
.\docker-mysql-startup.ps1 restart

# Reconstruir imagem
.\docker-mysql-startup.ps1 build

# Limpar tudo (containers, volumes, imagens)
.\docker-mysql-startup.ps1 clean
```

#### Linux/Mac (Bash):
```bash
# Dar permissão de execução
chmod +x docker-mysql-startup.sh

# Iniciar todos os serviços
./docker-mysql-startup.sh up

# Ver logs
./docker-mysql-startup.sh logs

# Parar serviços
./docker-mysql-startup.sh down

# Reiniciar aplicação
./docker-mysql-startup.sh restart

# Reconstruir imagem
./docker-mysql-startup.sh build

# Limpar tudo
./docker-mysql-startup.sh clean
```

### Opção 2: Usando Docker Compose Diretamente

```bash
# Iniciar todos os serviços
docker-compose -f docker-compose.mysql.yml up -d

# Ver logs em tempo real
docker-compose -f docker-compose.mysql.yml logs -f app

# Verificar status dos serviços
docker-compose -f docker-compose.mysql.yml ps

# Parar todos os serviços
docker-compose -f docker-compose.mysql.yml down

# Parar e remover volumes (⚠️ APAGA DADOS)
docker-compose -f docker-compose.mysql.yml down -v
```

## 🗄️ Serviços Incluídos

O `docker-compose.mysql.yml` inclui os seguintes serviços:

1. **app** - Aplicação Spring Boot (porta 8080)
2. **mysql** - Banco de dados MySQL 8.0 (porta 3306)
3. **redis** - Cache Redis (porta 6379) - opcional
4. **prometheus** - Monitoramento de métricas (porta 9090)
5. **grafana** - Dashboard de visualização (porta 3000)

## 📊 Acessar os Serviços

### Aplicação Spring Boot
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Swagger/OpenAPI**: http://localhost:8080/swagger-ui.html

### MySQL Database
- **Host**: localhost
- **Porta**: 3306
- **Database**: portifolium
- **Usuário**: portifolium_user
- **Senha**: portifolium123
- **Root Password**: rootpassword123

**Conectar via terminal:**
```bash
docker exec -it portifolium-mysql mysql -u portifolium_user -pportifolium123 portifolium
```

**Conectar via cliente MySQL (ex: MySQL Workbench):**
- Host: localhost
- Port: 3306
- Username: portifolium_user
- Password: portifolium123
- Database: portifolium

### Prometheus
- **URL**: http://localhost:9090

### Grafana
- **URL**: http://localhost:3000
- **Usuário**: admin
- **Senha**: admin

### Redis
- **Host**: localhost
- **Porta**: 6379

## ⚙️ Configurações

### Variáveis de Ambiente Principais

As configurações podem ser alteradas no arquivo `docker-compose.mysql.yml`:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=mysql,jwt
  - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/portifolium?...
  - SPRING_DATASOURCE_USERNAME=portifolium_user
  - SPRING_DATASOURCE_PASSWORD=portifolium123
  - SPRING_JPA_HIBERNATE_DDL_AUTO=create  # create|update|validate
```

### Perfis Spring Boot

A aplicação usa os seguintes profiles:
- **mysql**: Ativa configurações específicas do MySQL
- **jwt**: Ativa autenticação via JWT

### Estratégias de DDL

- **`create`**: Cria as tabelas do zero (apaga dados existentes)
- **`update`**: Atualiza schema mantendo dados
- **`validate`**: Apenas valida o schema (produção)

**⚠️ ATENÇÃO**: Na primeira execução, use `create`. Depois, mude para `update` ou `validate`.

## 🔍 Troubleshooting

### Verificar Logs

```bash
# Logs da aplicação
docker-compose -f docker-compose.mysql.yml logs -f app

# Logs do MySQL
docker-compose -f docker-compose.mysql.yml logs -f mysql

# Logs de todos os serviços
docker-compose -f docker-compose.mysql.yml logs -f
```

### Verificar Status dos Containers

```bash
docker-compose -f docker-compose.mysql.yml ps
```

### Verificar Health Check

```bash
# Health check da aplicação
curl http://localhost:8080/actuator/health

# Health check do MySQL
docker exec -it portifolium-mysql mysqladmin ping -h localhost -u root -prootpassword123
```

### Problema: Porta já em uso

Se alguma porta já estiver em uso, edite o arquivo `docker-compose.mysql.yml` e altere:

```yaml
ports:
  - "8081:8080"  # Altera porta externa de 8080 para 8081
```

### Problema: Banco de dados não conecta

1. Verifique se o MySQL está rodando:
```bash
docker-compose -f docker-compose.mysql.yml ps mysql
```

2. Verifique os logs do MySQL:
```bash
docker-compose -f docker-compose.mysql.yml logs mysql
```

3. Aguarde o health check do MySQL (pode levar até 30 segundos)

### Problema: Aplicação não inicia

1. Verifique os logs da aplicação:
```bash
docker-compose -f docker-compose.mysql.yml logs app
```

2. Verifique se o MySQL está pronto antes da aplicação tentar conectar
3. Aguarde até 2 minutos na primeira inicialização (build + criação de tabelas + população de dados)

### Limpar e Reiniciar Tudo

```bash
# Parar e remover tudo (incluindo volumes)
docker-compose -f docker-compose.mysql.yml down -v

# Reconstruir e iniciar
docker-compose -f docker-compose.mysql.yml up -d --build
```

## 📁 Estrutura de Dados

### Scripts SQL

- **`data-mysql.sql`**: Script de inicialização com dados iniciais (executado automaticamente)

### Volumes Persistentes

- **`mysql_data`**: Dados do MySQL (persistem entre restarts)
- **`redis_data`**: Dados do Redis
- **`prometheus_data`**: Métricas do Prometheus
- **`grafana_data`**: Dashboards e configurações do Grafana
- **`./portifolium-files`**: Arquivos da aplicação (fotos, evidências)

## 🔐 Segurança

### Credenciais Padrão

⚠️ **IMPORTANTE**: As credenciais abaixo são apenas para desenvolvimento. Em produção, altere todas as senhas!

- MySQL Root: `rootpassword123`
- MySQL User: `portifolium_user` / `portifolium123`
- Grafana Admin: `admin` / `admin`

### Alterar Credenciais

Edite o arquivo `docker-compose.mysql.yml` e altere as variáveis de ambiente:

```yaml
environment:
  - MYSQL_ROOT_PASSWORD=sua_senha_root_aqui
  - MYSQL_PASSWORD=sua_senha_usuario_aqui
  - SPRING_DATASOURCE_PASSWORD=sua_senha_usuario_aqui
```

## 📚 Recursos Adicionais

- **Documentação da API**: http://localhost:8080/swagger-ui.html
- **Métricas Prometheus**: http://localhost:9090
- **Dashboards Grafana**: http://localhost:3000

## 🆚 MySQL vs PostgreSQL

Esta configuração usa **MySQL** ao invés de PostgreSQL para:
- Evitar problemas de compatibilidade de tipos (ex: `bytea` vs `VARCHAR`)
- Melhor suporte a ordenação case-insensitive nativo
- Performance similar com configurações adequadas

Para voltar ao PostgreSQL, use:
```bash
docker-compose -f docker-compose.yml up -d
```

## ✅ Checklist de Verificação

Após iniciar, verifique:

- [ ] Aplicação responde em http://localhost:8080/actuator/health
- [ ] MySQL está acessível na porta 3306
- [ ] Consegue conectar ao MySQL com as credenciais fornecidas
- [ ] Swagger UI está acessível
- [ ] Logs não mostram erros críticos

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `docker-compose -f docker-compose.mysql.yml logs -f`
2. Consulte este README
3. Verifique a documentação principal em `README.md`

