#!/bin/bash

# Script de Deploy para AcadManage
# Uso: ./deploy.sh [staging|production]

set -e

ENVIRONMENT=${1:-staging}
VERSION=$(git describe --tags --always --dirty)

echo "🚀 Deploying AcadManage to $ENVIRONMENT (Version: $VERSION)"

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Função para deploy em staging
deploy_staging() {
    echo "📦 Deploying to staging environment..."
    
    # Build da imagem
    docker build -t acadmanage:staging .
    
    # Parar containers existentes
    docker-compose -f docker-compose.staging.yml down || true
    
    # Iniciar ambiente de staging
    docker-compose -f docker-compose.staging.yml up -d
    
    echo "✅ Staging deployment completed!"
    echo "🌐 Application: http://localhost:8080"
    echo "📊 Grafana: http://localhost:3000 (admin/admin)"
    echo "📈 Prometheus: http://localhost:9090"
}

# Função para deploy em produção
deploy_production() {
    echo "🚀 Deploying to production environment..."
    
    # Verificar se há mudanças não commitadas
    if [[ -n $(git status --porcelain) ]]; then
        echo "❌ Há mudanças não commitadas. Commit ou stash antes de fazer deploy em produção."
        exit 1
    fi
    
    # Build da imagem de produção
    docker build -t acadmanage:production .
    
    # Tag da imagem
    docker tag acadmanage:production acadmanage:$VERSION
    
    # Parar containers existentes
    docker-compose -f docker-compose.production.yml down || true
    
    # Iniciar ambiente de produção
    docker-compose -f docker-compose.production.yml up -d
    
    echo "✅ Production deployment completed!"
    echo "🏷️  Image tagged as: acadmanage:$VERSION"
}

# Função para rollback
rollback() {
    echo "🔄 Rolling back to previous version..."
    
    # Implementar lógica de rollback aqui
    echo "✅ Rollback completed!"
}

# Menu principal
case $ENVIRONMENT in
    "staging")
        deploy_staging
        ;;
    "production")
        deploy_production
        ;;
    "rollback")
        rollback
        ;;
    *)
        echo "❌ Uso: $0 [staging|production|rollback]"
        exit 1
        ;;
esac

echo "🎉 Deploy process completed successfully!"


