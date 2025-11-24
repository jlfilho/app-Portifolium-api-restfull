#!/bin/bash

# Script para iniciar a aplicação com MySQL via Docker Compose
# Uso: ./docker-mysql-startup.sh [up|down|logs|restart]

set -e

COMPOSE_FILE="docker-compose.mysql.yml"

case "$1" in
  up|start)
    echo "🚀 Iniciando aplicação com MySQL..."
    docker-compose -f $COMPOSE_FILE up -d
    echo ""
    echo "⏳ Aguardando serviços iniciarem..."
    sleep 10
    echo ""
    echo "✅ Serviços iniciados!"
    echo ""
    echo "📊 Verificando status dos serviços:"
    docker-compose -f $COMPOSE_FILE ps
    echo ""
    echo "📝 Para ver os logs:"
    echo "   docker-compose -f $COMPOSE_FILE logs -f app"
    echo ""
    echo "🔍 Para verificar health check:"
    echo "   curl http://localhost:8080/actuator/health"
    echo ""
    echo "🗄️  Para conectar ao MySQL:"
    echo "   docker exec -it portifolium-mysql mysql -u portifolium_user -pportifolium123 portifolium"
    ;;
    
  down|stop)
    echo "🛑 Parando aplicação..."
    docker-compose -f $COMPOSE_FILE down
    echo "✅ Aplicação parada!"
    ;;
    
  restart)
    echo "🔄 Reiniciando aplicação..."
    docker-compose -f $COMPOSE_FILE restart
    echo "✅ Aplicação reiniciada!"
    ;;
    
  logs)
    docker-compose -f $COMPOSE_FILE logs -f app
    ;;
    
  build)
    echo "🔨 Construindo imagem da aplicação..."
    docker-compose -f $COMPOSE_FILE build
    echo "✅ Imagem construída!"
    ;;
    
  clean)
    echo "🧹 Limpando containers, volumes e imagens..."
    docker-compose -f $COMPOSE_FILE down -v --rmi local
    echo "✅ Limpeza concluída!"
    ;;
    
  *)
    echo "Uso: $0 {up|down|restart|logs|build|clean}"
    echo ""
    echo "Comandos:"
    echo "  up       - Inicia todos os serviços"
    echo "  down     - Para todos os serviços"
    echo "  restart  - Reinicia a aplicação"
    echo "  logs     - Mostra logs da aplicação"
    echo "  build    - Constrói a imagem da aplicação"
    echo "  clean    - Remove containers, volumes e imagens"
    exit 1
    ;;
esac

