# relay-run.ps1 — baja lo último del mirror. NO ejecuta nada. Corre en cualquier lado.
# Uso:  powershell -File docs\scripts\relay-run.ps1
$ErrorActionPreference = "Stop"
$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root
git pull origin dmontes | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Host "error en git pull" -ForegroundColor Red; exit 1 }
Write-Host "repositorio actualizado" -ForegroundColor Green
