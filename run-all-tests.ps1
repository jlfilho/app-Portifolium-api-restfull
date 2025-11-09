# Script para executar todos os testes na ordem desejada
# TipoCursoControllerIT primeiro, UsuarioControllerIT por último
# Isso evita problemas de senhas alteradas pelo UsuarioControllerIT

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Executando todos os testes na ordem:" -ForegroundColor Cyan
Write-Host "1. TipoCursoControllerIT (primeiro)" -ForegroundColor Yellow
Write-Host "2. AuthenticationControllerIT" -ForegroundColor Yellow
Write-Host "3. PasswordRecoveryControllerIT" -ForegroundColor Yellow
Write-Host "4. FonteFinanciadoraControllerIT" -ForegroundColor Yellow
Write-Host "5. CategoriaControllerIT" -ForegroundColor Yellow
Write-Host "6. CursoControllerIT" -ForegroundColor Yellow
Write-Host "7. AtividadePessoaPapelControllerIT" -ForegroundColor Yellow
Write-Host "8. AtividadeControllerIT" -ForegroundColor Yellow
Write-Host "9. EvidenciaControllerIT" -ForegroundColor Yellow
Write-Host "10. UsuarioControllerIT (por último - altera senhas)" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$totalFailures = 0
$totalErrors = 0
$startTime = Get-Date

# 1. TipoCursoControllerIT (primeiro)
Write-Host "[1/10] Executando TipoCursoControllerIT..." -ForegroundColor Green
$result1 = & .\mvnw.cmd test -Dtest=TipoCursoControllerIT 2>&1
$exitCode1 = $LASTEXITCODE
$result1 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode1 -ne 0) {
    $totalFailures++
    Write-Host "❌ TipoCursoControllerIT falhou! (Exit code: $exitCode1)" -ForegroundColor Red
} else {
    Write-Host "✅ TipoCursoControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 2. AuthenticationControllerIT
Write-Host "[2/10] Executando AuthenticationControllerIT..." -ForegroundColor Green
$result2 = & .\mvnw.cmd test -Dtest=AuthenticationControllerIT 2>&1
$exitCode2 = $LASTEXITCODE
$result2 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode2 -ne 0) {
    $totalFailures++
    Write-Host "❌ AuthenticationControllerIT falhou! (Exit code: $exitCode2)" -ForegroundColor Red
} else {
    Write-Host "✅ AuthenticationControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 3. PasswordRecoveryControllerIT
Write-Host "[3/10] Executando PasswordRecoveryControllerIT..." -ForegroundColor Green
$result3 = & .\mvnw.cmd test -Dtest=PasswordRecoveryControllerIT 2>&1
$exitCode3 = $LASTEXITCODE
$result3 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode3 -ne 0) {
    $totalFailures++
    Write-Host "❌ PasswordRecoveryControllerIT falhou! (Exit code: $exitCode3)" -ForegroundColor Red
} else {
    Write-Host "✅ PasswordRecoveryControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 4. FonteFinanciadoraControllerIT
Write-Host "[4/10] Executando FonteFinanciadoraControllerIT..." -ForegroundColor Green
$result4 = & .\mvnw.cmd test -Dtest=FonteFinanciadoraControllerIT 2>&1
$exitCode4 = $LASTEXITCODE
$result4 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode4 -ne 0) {
    $totalFailures++
    Write-Host "❌ FonteFinanciadoraControllerIT falhou! (Exit code: $exitCode4)" -ForegroundColor Red
} else {
    Write-Host "✅ FonteFinanciadoraControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 5. CategoriaControllerIT
Write-Host "[5/10] Executando CategoriaControllerIT..." -ForegroundColor Green
$result5 = & .\mvnw.cmd test -Dtest=CategoriaControllerIT 2>&1
$exitCode5 = $LASTEXITCODE
$result5 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode5 -ne 0) {
    $totalFailures++
    Write-Host "❌ CategoriaControllerIT falhou! (Exit code: $exitCode5)" -ForegroundColor Red
} else {
    Write-Host "✅ CategoriaControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 6. CursoControllerIT
Write-Host "[6/10] Executando CursoControllerIT..." -ForegroundColor Green
$result6 = & .\mvnw.cmd test -Dtest=CursoControllerIT 2>&1
$exitCode6 = $LASTEXITCODE
$result6 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode6 -ne 0) {
    $totalFailures++
    Write-Host "❌ CursoControllerIT falhou! (Exit code: $exitCode6)" -ForegroundColor Red
} else {
    Write-Host "✅ CursoControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 7. AtividadePessoaPapelControllerIT
Write-Host "[7/10] Executando AtividadePessoaPapelControllerIT..." -ForegroundColor Green
$result7 = & .\mvnw.cmd test -Dtest=AtividadePessoaPapelControllerIT 2>&1
$exitCode7 = $LASTEXITCODE
$result7 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode7 -ne 0) {
    $totalFailures++
    Write-Host "❌ AtividadePessoaPapelControllerIT falhou! (Exit code: $exitCode7)" -ForegroundColor Red
} else {
    Write-Host "✅ AtividadePessoaPapelControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 8. AtividadeControllerIT
Write-Host "[8/10] Executando AtividadeControllerIT..." -ForegroundColor Green
$result8 = & .\mvnw.cmd test -Dtest=AtividadeControllerIT 2>&1
$exitCode8 = $LASTEXITCODE
$result8 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode8 -ne 0) {
    $totalFailures++
    Write-Host "❌ AtividadeControllerIT falhou! (Exit code: $exitCode8)" -ForegroundColor Red
} else {
    Write-Host "✅ AtividadeControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 9. EvidenciaControllerIT
Write-Host "[9/10] Executando EvidenciaControllerIT..." -ForegroundColor Green
$result9 = & .\mvnw.cmd test -Dtest=EvidenciaControllerIT 2>&1
$exitCode9 = $LASTEXITCODE
$result9 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode9 -ne 0) {
    $totalFailures++
    Write-Host "❌ EvidenciaControllerIT falhou! (Exit code: $exitCode9)" -ForegroundColor Red
} else {
    Write-Host "✅ EvidenciaControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# 10. UsuarioControllerIT (último - altera senhas)
Write-Host "[10/10] Executando UsuarioControllerIT (último - pode alterar senhas)..." -ForegroundColor Green
$result10 = & .\mvnw.cmd test -Dtest=UsuarioControllerIT 2>&1
$exitCode10 = $LASTEXITCODE
$result10 | Select-String -Pattern "Tests run:|Failures:|Errors:" | ForEach-Object { Write-Host $_ -ForegroundColor White }
if ($exitCode10 -ne 0) {
    $totalFailures++
    Write-Host "❌ UsuarioControllerIT falhou! (Exit code: $exitCode10)" -ForegroundColor Red
} else {
    Write-Host "✅ UsuarioControllerIT passou!" -ForegroundColor Green
}
Write-Host ""

# Resumo final
$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "RESUMO FINAL" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Tempo total: $($duration.TotalSeconds.ToString('F2')) segundos" -ForegroundColor White
Write-Host ""

if ($totalFailures -eq 0) {
    Write-Host "✅ Todos os testes passaram!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ $totalFailures suite(s) de teste falharam!" -ForegroundColor Red
    exit 1
}

