# Guia de Deploy em Produção com Docker e MySQL

Este guia explica como fazer o build e deploy da aplicação Portifolium em produção usando Docker e MySQL 8.0.

## Pré-requisitos

- Docker e Docker Compose instalados
- Variáveis de ambiente configuradas (veja `ENV_VARIABLES.md`)
- Acesso ao servidor de produção

## Estrutura de Arquivos

```
.
├── Dockerfile.production          # Dockerfile otimizado para produção
├── docker-compose.production.yml  # Configuração Docker Compose para produção
├── init-mysql.sql                 # Script de inicialização do MySQL
├── mysql-config/                  # Configurações do MySQL
│   └── my.cnf
├── scripts/
│   ├── build-production.sh        # Script de build
│   └── deploy-production.sh       # Script de deploy
└── ENV_VARIABLES.md               # Documentação das variáveis de ambiente
```

## Configuração Inicial

### 1. Configurar Variáveis de Ambiente

Defina as variáveis de ambiente obrigatórias no sistema:

**IMPORTANTE:** Para a primeira inicialização, configure `SPRING_JPA_HIBERNATE_DDL_AUTO=create` para criar as tabelas e inserir os dados iniciais. Após a primeira inicialização, mude para `validate` para segurança.

```bash
export JWT_SECRET_KEY="sua_chave_secreta_aqui"
export MYSQL_ROOT_PASSWORD="senha_root_mysql"
export MYSQL_PASSWORD="senha_usuario_mysql"
export MAIL_PASSWORD="senha_email"
```

Ou crie um arquivo `.env` (não commite no repositório):

```bash
cp ENV_VARIABLES.md .env
# Edite o arquivo .env com os valores reais
```

### 2. Gerar Chave JWT Segura

```bash
openssl rand -hex 32
```

### 3. Dados Iniciais

Na primeira inicialização com `SPRING_JPA_HIBERNATE_DDL_AUTO=create`, os seguintes dados serão inseridos automaticamente:

- **Categorias:** Ensino, Pesquisa, Extensão
- **Tipos de Curso:** Bacharelado, Licenciatura, Tecnólogo, Especialização, MBA, Mestrado, Doutorado
- **Fontes Financiadoras:** UEA, FAPEAM, CAPES, CNPq, Outros
- **Roles:** ROLE_ADMINISTRADOR, ROLE_GERENTE, ROLE_SECRETARIO, ROLE_COORDENADOR_ATIVIDADE
- **Usuário Administrador:**
  - Email: `admin@uea.edu.br`
  - Senha: `admin123`
  - Role: ROLE_ADMINISTRADOR

**Nota:** O script usa `INSERT IGNORE`, então mesmo se executar múltiplas vezes, não haverá duplicação de dados.

## Build da Imagem

### Opção 1: Usando o Script

```bash
./scripts/build-production.sh [tag]
```

Exemplo:
```bash
./scripts/build-production.sh v1.0.0
```

### Opção 2: Build Manual

```bash
docker build -f Dockerfile.production -t portifolium:production .
```

## Deploy

### Deploy Completo (Build + Deploy)

```bash
./scripts/deploy-production.sh
```

### Deploy sem Build (se a imagem já existe)

```bash
./scripts/deploy-production.sh --skip-build
```

### Deploy com Backup do Banco

```bash
./scripts/deploy-production.sh --backup
```

### Deploy Manual

```bash
# Parar containers existentes
docker-compose -f docker-compose.production.yml down

# Iniciar serviços
docker-compose -f docker-compose.production.yml up -d

# Verificar logs
docker-compose -f docker-compose.production.yml logs -f
```

## Verificação

### Verificar Status dos Containers

```bash
docker-compose -f docker-compose.production.yml ps
```

### Verificar Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Ver Logs

```bash
# Logs da aplicação
docker logs -f portifolium-app

# Logs do MySQL
docker logs -f portifolium-mysql

# Logs do Redis
docker logs -f portifolium-redis
```

## Manutenção

### Backup do Banco de Dados

```bash
docker exec portifolium-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} portifolium > backup-$(date +%Y%m%d).sql
```

### Restaurar Backup

```bash
docker exec -i portifolium-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} portifolium < backup-20241117.sql
```

### Atualizar Aplicação

```bash
# 1. Fazer backup (opcional)
./scripts/deploy-production.sh --backup

# 2. Fazer pull das mudanças
git pull

# 3. Rebuild e redeploy
./scripts/deploy-production.sh
```

### Parar Serviços

```bash
docker-compose -f docker-compose.production.yml down
```

### Parar e Remover Volumes (CUIDADO: apaga dados)

```bash
docker-compose -f docker-compose.production.yml down -v
```

## Troubleshooting

### Aplicação não inicia

1. Verificar logs:
   ```bash
   docker logs portifolium-app
   ```

2. Verificar se o MySQL está rodando:
   ```bash
   docker ps | grep mysql
   ```

3. Verificar variáveis de ambiente:
   ```bash
   docker exec portifolium-app env | grep -E "SPRING|JWT|MYSQL"
   ```

### Erro de conexão com MySQL

1. Verificar se o MySQL está saudável:
   ```bash
   docker exec portifolium-mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD}
   ```

2. Verificar logs do MySQL:
   ```bash
   docker logs portifolium-mysql
   ```

### Problemas de Permissão

Se houver problemas com permissões nos arquivos:

```bash
# Ajustar permissões do diretório de arquivos
docker exec portifolium-app chown -R appuser:appgroup /portifolium-files
```

## Segurança

### Checklist de Segurança

- [ ] Todas as senhas são fortes e únicas
- [ ] JWT_SECRET_KEY tem pelo menos 256 bits
- [ ] SPRING_JPA_HIBERNATE_DDL_AUTO está configurado como 'validate'
- [ ] Portas do MySQL e Redis não estão expostas publicamente
- [ ] Variáveis de ambiente não estão em arquivos versionados
- [ ] Backups são feitos regularmente
- [ ] Logs são monitorados

### Recomendações

1. Use um gerenciador de segredos (ex: HashiCorp Vault, AWS Secrets Manager)
2. Configure firewall para restringir acesso às portas
3. Use HTTPS com certificado válido
4. Configure rate limiting
5. Monitore logs e métricas regularmente

## Monitoramento

### Métricas Prometheus

Acesse: `http://localhost:8080/actuator/prometheus`

### Health Checks

- Aplicação: `http://localhost:8080/actuator/health`
- MySQL: Verificado automaticamente pelo Docker
- Redis: Verificado automaticamente pelo Docker

## Suporte

Para problemas ou dúvidas, consulte:
- Logs da aplicação
- Documentação do Spring Boot
- Documentação do Docker

