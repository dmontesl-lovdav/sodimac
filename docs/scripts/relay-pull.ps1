# relay-pull.ps1
# Baja del mirror (rama dmontes) lo que haya subido el OTRO lado y muestra el mensaje del relay.
# Uso:
#   powershell -File docs\scripts\relay-pull.ps1
# Funciona en la PC personal (c:\workspace-sodimac) y en la de Sodimac (C:\local).

$ErrorActionPreference = "Stop"

$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root
Write-Host "Repo: $root" -ForegroundColor Cyan

git pull origin dmontes

$relay = Join-Path $root "docs/scripts/_relay/RELAY.md"
if (Test-Path $relay) {
    Write-Host "`n===================== RELAY.md =====================" -ForegroundColor Green
    Get-Content $relay
    Write-Host "====================================================" -ForegroundColor Green
} else {
    Write-Host "(no hay docs/scripts/_relay/RELAY.md)" -ForegroundColor Yellow
}
