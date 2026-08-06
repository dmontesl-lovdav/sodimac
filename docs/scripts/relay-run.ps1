# ============================================================================
# relay-run.ps1  —  CORRER EN LA PC DE SODIMAC
# SOLO baja del mirror lo último y MUESTRA el archivo compartido para que lo revises.
# NO ejecuta nada. Si lo ves bien, tú lo corres aparte (ver comando al final).
# Uso:  powershell -File docs\scripts\relay-run.ps1
# ============================================================================
$ErrorActionPreference = "Stop"

$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root
Write-Host "Repo: $root" -ForegroundColor Cyan

Write-Host "== git pull origin dmontes ==" -ForegroundColor Cyan
git pull origin dmontes
if ($LASTEXITCODE -ne 0) { throw "git pull fallo" }

$runFile = Join-Path $root "docs/scripts/_relay/sodimac-run.ps1"
if (-not (Test-Path $runFile)) { throw "No existe $runFile" }

Write-Host "`n===================== sodimac-run.ps1 (REVISAR, NO ejecutado) =====================" -ForegroundColor Green
Get-Content $runFile
Write-Host "==================================================================================" -ForegroundColor Green
Write-Host "`nSi lo ves bien, ejecutalo tu:" -ForegroundColor Yellow
Write-Host "   powershell -File docs\scripts\_relay\sodimac-run.ps1" -ForegroundColor Yellow
