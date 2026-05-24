#!/bin/bash

# Script de Deploy para Produção - Portifolium
# Uso: ./scripts/deploy-production.sh [--skip-build] [--backup]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Flags
SKIP_BUILD=false
BACKUP_DB=false

# Parse arguments
for arg in "$@"; do
    case $arg in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --backup)
            BACKUP_DB=true
            shift
            ;;
        *)
            ;;
    esac
done

# Função para imprimir mensagens
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    error "Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Verificar se o docker-compose.production.yml existe
if [ ! -f "docker-compose.production.yml" ]; then
    error "docker-compose.production.yml não encontrado!"
    exit 1
fi

# Validar variáveis de ambiente obrigatórias
info "🔍 Validando variáveis de ambiente..."

REQUIRED_VARS=(
    "JWT_SECRET_KEY"
    "MYSQL_PASSWORD"
    "MYSQL_ROOT_PASSWORD"
)

MISSING_VARS=()
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    error "As seguintes variáveis de ambiente são obrigatórias e não estão definidas:"
    for var in "${MISSING_VARS[@]}"; do
        error "  - $var"
    done
    error "Defina essas variáveis antes de continuar o deploy."
    exit 1
fi

info "✅ Todas as variáveis obrigatórias estão definidas."

# Obter versão
VERSION=$(git describe --tags --always --dirty 2>/dev/null || echo "production")
info "📦 Versão: $VERSION"

# Backup do banco de dados (se solicitado)
if [ "$BACKUP_DB" = true ]; then
    info "💾 Fazendo backup do banco de dados..."
    BACKUP_DIR="./backups"
    mkdir -p "$BACKUP_DIR"
    BACKUP_FILE="${BACKUP_DIR}/backup-$(date +%Y%m%d-%H%M%S).sql"
    
    # Verificar se o container MySQL está rodando
    if docker ps | grep -q portifolium-mysql; then
        docker exec portifolium-mysql mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE:-portifolium}" > "$BACKUP_FILE" 2>/dev/null || {
            warn "Não foi possível fazer backup automático. Continuando..."
        }
        if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
            info "✅ Backup criado: $BACKUP_FILE"
        fi
    else
        warn "Container MySQL não está rodando. Pulando backup."
    fi
fi

# Build da imagem (se não for pulado)
if [ "$SKIP_BUILD" = false ]; then
    info "🔨 Construindo imagem Docker..."
    ./scripts/build-production.sh "$VERSION"
else
    info "⏭️  Pulando build da imagem (--skip-build)"
fi

# Parar containers existentes
info "🛑 Parando containers existentes..."
docker-compose -f docker-compose.production.yml down || true

# Limpar recursos não utilizados (opcional)
info "🧹 Limpando recursos não utilizados..."
docker system prune -f --volumes || true

# Iniciar ambiente de produção
info "🚀 Iniciando ambiente de produção..."
docker-compose -f docker-compose.production.yml up -d

# Aguardar serviços ficarem prontos
info "⏳ Aguardando serviços ficarem prontos..."
sleep 10

# Verificar saúde dos serviços
info "🏥 Verificando saúde dos serviços..."

MAX_RETRIES=30
RETRY_COUNT=0
HEALTHY=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if docker ps | grep -q portifolium-app && \
       docker ps | grep -q portifolium-mysql && \
       docker ps | grep -q portifolium-redis; then
        # Verificar health check da aplicação
        APP_HEALTH=$(docker inspect --format='{{.State.Health.Status}}' portifolium-app 2>/dev/null || echo "none")
        if [ "$APP_HEALTH" = "healthy" ] || [ "$APP_HEALTH" = "starting" ]; then
            HEALTHY=true
            break
        fi
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 2
done

if [ "$HEALTHY" = true ]; then
    info "✅ Serviços estão rodando!"
    
    # Mostrar status dos containers
    info "📊 Status dos containers:"
    docker-compose -f docker-compose.production.yml ps
    
    # Mostrar logs recentes
    info "📋 Últimas linhas de log da aplicação:"
    docker logs --tail 20 portifolium-app 2>/dev/null || true
    
    info "🎉 Deploy concluído com sucesso!"
    info "🌐 Aplicação disponível em: http://localhost:${APP_PORT:-8080}"
    info "📊 Health check: http://localhost:${APP_PORT:-8080}/actuator/health"
else
    error "❌ Serviços não ficaram prontos a tempo!"
    error "📋 Logs da aplicação:"
    docker logs --tail 50 portifolium-app 2>/dev/null || true
    error "📋 Logs do MySQL:"
    docker logs --tail 50 portifolium-mysql 2>/dev/null || true
    exit 1
fi

