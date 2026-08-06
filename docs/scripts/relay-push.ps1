# relay-push.ps1
# Sube al mirror (rama dmontes) lo que escribas de ESTE lado.
# Uso: escribe/edita archivos (incluido docs/scripts/_relay/RELAY.md) y luego:
#   powershell -File docs\scripts\relay-push.ps1 -Message "lo que sea"
# Funciona en la PC personal (c:\workspace-sodimac) y en la de Sodimac (C:\local).

param(
    [Parameter(Mandatory = $true)][string]$Message
)
$ErrorActionPreference = "Stop"

$root = (git rev-parse --show-toplevel).Trim()
Set-Location $root
Write-Host "Repo: $root" -ForegroundColor Cyan

git add -A
$pending = git status --porcelain
if (-not $pending) {
    Write-Host "Nada que subir (sin cambios)." -ForegroundColor Yellow
    exit 0
}
git commit -m $Message
git push origin dmontes
Write-Host "OK -> pushed a origin/dmontes: $Message" -ForegroundColor Green
