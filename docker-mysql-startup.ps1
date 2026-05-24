# Script PowerShell para iniciar a aplicacao com MySQL via Docker Compose
# Uso: .\docker-mysql-startup.ps1 [up|down|logs|restart|build|clean]

param(
    [Parameter(Position=0)]
    [ValidateSet("up", "down", "logs", "restart", "build", "clean")]
    [string]$Command = "up"
)

$ComposeFile = "docker-compose.mysql.yml"

switch ($Command) {
    "up" {
        Write-Host "[INICIANDO] Aplicacao com MySQL..." -ForegroundColor Green
        docker-compose -f $ComposeFile up -d
        Write-Host ""
        Write-Host "[AGUARDANDO] Servicos iniciarem (10 segundos)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        Write-Host ""
        Write-Host "[OK] Servicos iniciados!" -ForegroundColor Green
        Write-Host ""
        Write-Host "[STATUS] Verificando status dos servicos:" -ForegroundColor Cyan
        docker-compose -f $ComposeFile ps
        Write-Host ""
        Write-Host "[LOGS] Para ver os logs:" -ForegroundColor Cyan
        Write-Host "   docker-compose -f $ComposeFile logs -f app"
        Write-Host ""
        Write-Host "[HEALTH] Para verificar health check:" -ForegroundColor Cyan
        Write-Host "   curl http://localhost:8080/actuator/health"
        Write-Host ""
        Write-Host "[MYSQL] Para conectar ao MySQL:" -ForegroundColor Cyan
        Write-Host "   docker exec -it portifolium-mysql mysql -u portifolium_user -pportifolium123 portifolium"
    }
    
    "down" {
        Write-Host "[PARANDO] Aplicacao..." -ForegroundColor Yellow
        docker-compose -f $ComposeFile down
        Write-Host "[OK] Aplicacao parada!" -ForegroundColor Green
    }
    
    "restart" {
        Write-Host "[REINICIANDO] Aplicacao..." -ForegroundColor Yellow
        docker-compose -f $ComposeFile restart app
        Write-Host "[OK] Aplicacao reiniciada!" -ForegroundColor Green
    }
    
    "logs" {
        docker-compose -f $ComposeFile logs -f app
    }
    
    "build" {
        Write-Host "[BUILD] Construindo imagem da aplicacao..." -ForegroundColor Cyan
        docker-compose -f $ComposeFile build
        Write-Host "[OK] Imagem construida!" -ForegroundColor Green
    }
    
    "clean" {
        Write-Host "[LIMPEZA] Limpando containers, volumes e imagens..." -ForegroundColor Yellow
        docker-compose -f $ComposeFile down -v --rmi local
        Write-Host "[OK] Limpeza concluida!" -ForegroundColor Green
    }
    
    default {
        Write-Host "[ERRO] Comando invalido: $Command" -ForegroundColor Red
        Write-Host ""
        Write-Host "Uso: .\docker-mysql-startup.ps1 [up|down|logs|restart|build|clean]"
        Write-Host ""
        Write-Host "Comandos:"
        Write-Host "  up       - Inicia todos os servicos"
        Write-Host "  down     - Para todos os servicos"
        Write-Host "  restart  - Reinicia a aplicacao"
        Write-Host "  logs     - Mostra logs da aplicacao"
        Write-Host "  build    - Constrói a imagem da aplicacao"
        Write-Host "  clean    - Remove containers, volumes e imagens"
        exit 1
    }
}
