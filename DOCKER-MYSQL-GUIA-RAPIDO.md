# 🐬 Guia Rápido - Docker com MySQL

## 🚀 Início Rápido

### Windows (PowerShell)
```powershell
# Iniciar tudo
.\docker-mysql-startup.ps1 up

# Ver logs
.\docker-mysql-startup.ps1 logs

# Parar tudo
.\docker-mysql-startup.ps1 down
```

### Linux/Mac (Bash)
```bash
# Dar permissão de execução (primeira vez)
chmod +x docker-mysql-startup.sh

# Iniciar tudo
./docker-mysql-startup.sh up

# Ver logs
./docker-mysql-startup.sh logs

# Parar tudo
./docker-mysql-startup.sh down
```

### Docker Compose Direto
```bash
# Iniciar todos os serviços
docker-compose -f docker-compose.mysql.yml up -d

# Ver logs em tempo real
docker-compose -f docker-compose.mysql.yml logs -f app

# Parar todos os serviços
docker-compose -f docker-compose.mysql.yml down
```

## 📊 Serviços Disponíveis

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Aplicação** | http://localhost:8080 | - |
| **Swagger** | http://localhost:8080/swagger-ui.html | - |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **MySQL** | localhost:3306 | `portifolium_user` / `portifolium123` |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | `admin` / `admin` |

## 🔐 Credenciais Padrão

### Usuário Admin da Aplicação
- **Email**: `admin@uea.edu.br`
- **Senha**: `admin123`

### MySQL
- **Database**: `portifolium`
- **Usuário**: `portifolium_user`
- **Senha**: `portifolium123`
- **Root Password**: `rootpassword123`

## 🔧 Comandos Úteis

### Verificar Status
```bash
docker-compose -f docker-compose.mysql.yml ps
```

### Conectar ao MySQL
```bash
docker exec -it portifolium-mysql mysql -u portifolium_user -pportifolium123 portifolium
```

### Ver Logs Específicos
```bash
# Aplicação
docker-compose -f docker-compose.mysql.yml logs -f app

# MySQL
docker-compose -f docker-compose.mysql.yml logs -f mysql
```

### Reiniciar Aplicação
```bash
docker-compose -f docker-compose.mysql.yml restart app
```

### Limpar Tudo (⚠️ Apaga Dados)
```bash
docker-compose -f docker-compose.mysql.yml down -v
```

## 🛠️ Troubleshooting

### Aplicação não inicia
1. Verifique os logs: `docker-compose -f docker-compose.mysql.yml logs app`
2. Aguarde 2 minutos na primeira inicialização (criação de tabelas + dados)
3. Verifique se o MySQL está pronto: `docker-compose -f docker-compose.mysql.yml ps mysql`

### Porta já em uso
Edite `docker-compose.mysql.yml` e altere a porta externa:
```yaml
ports:
  - "8081:8080"  # Muda porta externa para 8081
```

### Limpar e reiniciar do zero
```bash
# Parar e remover tudo (incluindo volumes)
docker-compose -f docker-compose.mysql.yml down -v

# Reconstruir e iniciar
docker-compose -f docker-compose.mysql.yml up -d --build
```

## 📝 Notas Importantes

1. **Primeira Execução**: Usa `SPRING_JPA_HIBERNATE_DDL_AUTO=create` para criar as tabelas
2. **Dados Iniciais**: Carregados automaticamente de `data-mysql.sql`
3. **Volumes**: Dados do MySQL persistem entre reinicializações (volume `mysql_data`)
4. **Logs SQL**: Ativados por padrão para debug (`DEBUG` e `TRACE`)

## ✅ Checklist Pós-Instalação

- [ ] Aplicação responde em http://localhost:8080/actuator/health
- [ ] Swagger UI acessível em http://localhost:8080/swagger-ui.html
- [ ] Consegue conectar ao MySQL
- [ ] Login funciona com `admin@uea.edu.br` / `admin123`

## 📚 Documentação Completa

Para mais detalhes, consulte: `README-MYSQL.md`

