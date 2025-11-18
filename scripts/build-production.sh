#!/bin/bash

# Script de Build para Produção - Portifolium
# Uso: ./scripts/build-production.sh [tag]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

# Verificar se o Dockerfile.production existe
if [ ! -f "Dockerfile.production" ]; then
    error "Dockerfile.production não encontrado!"
    exit 1
fi

# Obter tag da versão (opcional)
TAG=${1:-$(git describe --tags --always --dirty 2>/dev/null || echo "latest")}
IMAGE_NAME="portifolium"
FULL_IMAGE_NAME="${IMAGE_NAME}:${TAG}"

info "🚀 Iniciando build da imagem de produção..."
info "📦 Imagem: ${FULL_IMAGE_NAME}"

# Validar variáveis de ambiente obrigatórias (apenas aviso, não bloqueia)
warn "Verificando variáveis de ambiente..."
MISSING_VARS=()

if [ -z "$JWT_SECRET_KEY" ]; then
    MISSING_VARS+=("JWT_SECRET_KEY")
fi

if [ -z "$MYSQL_PASSWORD" ]; then
    MISSING_VARS+=("MYSQL_PASSWORD")
fi

if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
    MISSING_VARS+=("MYSQL_ROOT_PASSWORD")
fi

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    warn "As seguintes variáveis de ambiente não estão definidas:"
    for var in "${MISSING_VARS[@]}"; do
        warn "  - $var"
    done
    warn "Certifique-se de defini-las antes do deploy!"
fi

# Build da imagem
info "🔨 Construindo imagem Docker..."
docker build \
    -f Dockerfile.production \
    -t "${FULL_IMAGE_NAME}" \
    -t "${IMAGE_NAME}:production" \
    -t "${IMAGE_NAME}:latest" \
    .

if [ $? -eq 0 ]; then
    info "✅ Build concluído com sucesso!"
    info "📋 Imagens criadas:"
    info "   - ${FULL_IMAGE_NAME}"
    info "   - ${IMAGE_NAME}:production"
    info "   - ${IMAGE_NAME}:latest"
    
    # Mostrar tamanho da imagem
    info "📊 Tamanho da imagem:"
    docker images "${IMAGE_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep -E "REPOSITORY|${IMAGE_NAME}"
    
    # Verificar vulnerabilidades (se trivy estiver disponível)
    if command -v trivy &> /dev/null; then
        info "🔍 Verificando vulnerabilidades com Trivy..."
        trivy image --severity HIGH,CRITICAL "${FULL_IMAGE_NAME}" || warn "Trivy encontrou vulnerabilidades ou não está configurado corretamente"
    else
        warn "Trivy não encontrado. Considere instalar para verificação de vulnerabilidades."
    fi
else
    error "❌ Falha no build da imagem!"
    exit 1
fi

info "🎉 Processo de build finalizado!"

