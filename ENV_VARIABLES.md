# Variáveis de Ambiente para Produção

Este documento lista todas as variáveis de ambiente necessárias para executar a aplicação em produção.

## Como Usar

Defina essas variáveis no sistema operacional ou use um arquivo `.env` (não commite o arquivo `.env` no repositório).

## Variáveis Obrigatórias

### Banco de Dados MySQL

```bash
MYSQL_ROOT_PASSWORD=your_secure_root_password_here
MYSQL_DATABASE=portifolium
MYSQL_USER=portifolium_user
MYSQL_PASSWORD=your_secure_password_here
MYSQL_PORT=3306
```

### Segurança JWT

```bash
# Gere uma chave secreta forte (mínimo 256 bits)
# Exemplo: openssl rand -hex 32
JWT_SECRET_KEY=your_jwt_secret_key_minimum_256_bits_here
JWT_EXPIRATION_TIME=3600000  # 1 hora em milissegundos
```

### Email

```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tecnocomp@uea.edu.br
MAIL_PASSWORD=your_email_password_here
```

## Variáveis Opcionais

### Aplicação

```bash
APP_PORT=8080
```

### Estratégia DDL do Hibernate

```bash
# Para PRIMEIRA inicialização em produção, use 'create' ou 'update'
# Após a primeira inicialização, mude para 'validate' para segurança
SPRING_JPA_HIBERNATE_DDL_AUTO=create  # Primeira vez
# SPRING_JPA_HIBERNATE_DDL_AUTO=validate  # Após primeira inicialização
```

### Redis (Cache)

```bash
REDIS_HOST=redis
REDIS_PORT=6379
```

## Exemplo de Arquivo .env

Crie um arquivo `.env` na raiz do projeto com o seguinte conteúdo:

```bash
# Configurações da Aplicação
APP_PORT=8080

# Configurações do Banco de Dados MySQL
MYSQL_ROOT_PASSWORD=your_secure_root_password_here
MYSQL_DATABASE=portifolium
MYSQL_USER=portifolium_user
MYSQL_PASSWORD=your_secure_password_here
MYSQL_PORT=3306

# Estratégia DDL
SPRING_JPA_HIBERNATE_DDL_AUTO=validate

# Configurações de Segurança JWT
JWT_SECRET_KEY=your_jwt_secret_key_minimum_256_bits_here
JWT_EXPIRATION_TIME=3600000

# Configurações de Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tecnocomp@uea.edu.br
MAIL_PASSWORD=your_email_password_here

# Configurações do Redis
REDIS_HOST=redis
REDIS_PORT=6379
```

## Notas Importantes

1. **Todas as senhas devem ser fortes e únicas**
2. **JWT_SECRET_KEY** deve ter pelo menos 256 bits (64 caracteres hexadecimais)
3. **Em produção, use sempre `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`** para evitar alterações acidentais no schema
4. **Mantenha as credenciais seguras** e nunca as compartilhe publicamente
5. **Use variáveis de ambiente do sistema** em vez de arquivo `.env` em servidores de produção para maior segurança

## Gerando Chave JWT Segura

Para gerar uma chave JWT segura, use:

```bash
openssl rand -hex 32
```

Ou use um gerador online de chaves seguras.

