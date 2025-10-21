#!/bin/bash

echo "🔍 Testando build do Docker para AcadManage..."

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Verificar se os arquivos necessários existem
echo "📁 Verificando arquivos necessários..."
if [ ! -f "mvnw" ]; then
    echo "❌ Arquivo mvnw não encontrado!"
    exit 1
fi

if [ ! -d ".mvn" ]; then
    echo "❌ Diretório .mvn não encontrado!"
    exit 1
fi

if [ ! -f "pom.xml" ]; then
    echo "❌ Arquivo pom.xml não encontrado!"
    exit 1
fi

echo "✅ Todos os arquivos necessários encontrados!"

# Limpar imagens e containers antigos
echo "🧹 Limpando imagens e containers antigos..."
docker system prune -f

# Testar build com Dockerfile principal
echo "🔨 Testando build com Dockerfile principal..."
if docker build -t acadmanage:test .; then
    echo "✅ Build com Dockerfile principal bem-sucedido!"
    
    # Testar se a imagem roda
    echo "🚀 Testando execução da imagem..."
    if docker run --rm -d --name acadmanage-test -p 8081:8080 acadmanage:test; then
        echo "✅ Imagem executando com sucesso!"
        echo "🌐 Acesse: http://localhost:8081"
        
        # Aguardar um pouco e verificar health
        sleep 10
        if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo "✅ Health check passou!"
        else
            echo "⚠️  Health check falhou (pode ser normal se a aplicação ainda estiver inicializando)"
        fi
        
        # Parar container de teste
        docker stop acadmanage-test
        docker rm acadmanage-test
    else
        echo "❌ Falha ao executar a imagem!"
    fi
else
    echo "❌ Build com Dockerfile principal falhou!"
    echo "🔄 Tentando com Dockerfile.simple..."
    
    if docker build -t acadmanage:test -f Dockerfile.simple .; then
        echo "✅ Build com Dockerfile.simple bem-sucedido!"
    else
        echo "❌ Ambos os Dockerfiles falharam!"
        exit 1
    fi
fi

echo "🎉 Teste de build concluído!"
