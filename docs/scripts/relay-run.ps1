# ============================================================================
# relay-run.ps1  —  CORRER EN LA PC DE SODIMAC
# Baja del mirror lo último y EJECUTA el archivo compartido docs/scripts/_relay/sodimac-run.ps1
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

Write-Host "== Ejecutando sodimac-run.ps1 ==" -ForegroundColor Green
& $runFile
Write-Host "== Fin sodimac-run.ps1 ==" -ForegroundColor Green
