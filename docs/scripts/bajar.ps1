# bajar.ps1
# Baja lo último del mirror (git pull). NO ejecuta nada. Corre en cualquiera de las 2 PCs.
# Uso:  powershell -File docs\scripts\bajar.ps1
$ErrorActionPreference = "Stop"

$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root

git pull origin dmontes | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Host "error en git pull" -ForegroundColor Red; exit 1 }
Write-Host "repositorio actualizado" -ForegroundColor Green
