#!/bin/bash

echo "🔍 Testando build do Docker passo a passo..."

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Limpar imagens e containers antigos
echo "🧹 Limpando imagens e containers antigos..."
docker system prune -f

# Testar build com Dockerfile.simple primeiro
echo "🔨 Testando build com Dockerfile.simple..."
if docker build -t acadmanage:test -f Dockerfile.simple .; then
    echo "✅ Build com Dockerfile.simple bem-sucedido!"
    
    # Testar se a imagem roda
    echo "🚀 Testando execução da imagem..."
    if docker run --rm -d --name acadmanage-test -p 8081:8080 acadmanage:test; then
        echo "✅ Imagem executando com sucesso!"
        echo "🌐 Acesse: http://localhost:8081"
        
        # Aguardar um pouco e verificar health
        echo "⏳ Aguardando inicialização da aplicação..."
        sleep 15
        
        if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo "✅ Health check passou!"
        else
            echo "⚠️  Health check falhou - verificando logs..."
            docker logs acadmanage-test
        fi
        
        # Parar container de teste
        docker stop acadmanage-test
        docker rm acadmanage-test
    else
        echo "❌ Falha ao executar a imagem!"
        echo "📋 Logs do container:"
        docker logs acadmanage-test 2>/dev/null || echo "Container não foi criado"
    fi
else
    echo "❌ Build com Dockerfile.simple falhou!"
    echo "🔍 Verificando problemas..."
    
    # Tentar build com mais detalhes
    echo "🔍 Tentando build com mais detalhes..."
    docker build -t acadmanage:test -f Dockerfile.simple . --progress=plain --no-cache
fi

echo "🎉 Teste de build concluído!"

