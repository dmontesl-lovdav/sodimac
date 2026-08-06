# sincronizar.ps1
# Hace commit + push de la carpeta docs/compartido/ con el mensaje "sincronizado".
# Toma altas, modificaciones y borrados. Corre en cualquiera de las 2 PCs.
# Uso:  powershell -File docs\scripts\sincronizar.ps1
$ErrorActionPreference = "Stop"

$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root

git add -A docs/compartido
$pending = git status --porcelain docs/compartido
if (-not $pending) {
    Write-Host "sin cambios en docs/compartido" -ForegroundColor Yellow
    exit 0
}
git commit -m "sincronizado"
git push origin dmontes
if ($LASTEXITCODE -ne 0) { Write-Host "error en push" -ForegroundColor Red; exit 1 }
Write-Host "sincronizado" -ForegroundColor Green
