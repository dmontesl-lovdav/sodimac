# deploy-to-uat.ps1  (CORRER EN LA PC DE SODIMAC)
# Automatiza el pase mirror -> repo real -> develop -> uat, sin teclear los git a mano.
# Hace: pull del mirror (C:\local) -> robocopy a C:\workspace-fbc-github -> por cada
# proyecto: commit a develop + merge a uat + push.
#
# Uso (en la PC de Sodimac):
#   powershell -File docs\scripts\deploy-to-uat.ps1 -Message "feat: ..." -Projects "APP03022-mrch.backend.somx.fiscal-api"
#
# OJO: usa 'git add -A' del proyecto (robocopy /MIR ya dejo el arbol identico al mirror).
#      Revisa el 'git status' que imprime antes de confirmar cada push si quieres control fino.

param(
    [Parameter(Mandatory = $true)][string]$Message,
    [string[]]$Projects = @("APP03022-mrch.backend.somx.fiscal-api"),
    [string]$Mirror = "C:\local",
    [string]$Real   = "C:\workspace-fbc-github"
)
$ErrorActionPreference = "Stop"

function Invoke-Git($args) {
    & git @args
    if ($LASTEXITCODE -ne 0) { throw "git $($args -join ' ') fallo (exit $LASTEXITCODE)" }
}

Write-Host "== Pull del mirror ==" -ForegroundColor Cyan
Set-Location $Mirror
Invoke-Git @("pull", "origin", "dmontes")

foreach ($p in $Projects) {
    Write-Host "`n== Proyecto: $p ==" -ForegroundColor Cyan
    $src = Join-Path $Mirror $p
    $dst = Join-Path $Real   $p
    if (-not (Test-Path $src)) { throw "No existe origen: $src" }
    if (-not (Test-Path $dst)) { throw "No existe destino (repo real): $dst" }

    # robocopy: exit code < 8 = OK (0=sin cambios, 1=copiados, etc.)
    robocopy $src $dst /MIR /XD .git node_modules dist target build .idea /XF *.log | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "robocopy fallo (exit $LASTEXITCODE) en $p" }
    $global:LASTEXITCODE = 0

    Set-Location $dst
    Invoke-Git @("checkout", "develop")
    Invoke-Git @("pull", "origin", "develop")
    Write-Host "-- git status (develop) --" -ForegroundColor Yellow
    git status --short
    git add -A
    $pending = git status --porcelain
    if ($pending) {
        Invoke-Git @("commit", "-m", $Message)
        Invoke-Git @("push", "origin", "develop")
    } else {
        Write-Host "develop sin cambios para $p (ya estaba al dia)" -ForegroundColor Yellow
    }

    Invoke-Git @("checkout", "uat")
    Invoke-Git @("pull", "origin", "uat")
    Invoke-Git @("merge", "develop", "--no-ff", "-m", "merge: $Message")
    Invoke-Git @("push", "origin", "uat")
    Write-Host "OK -> $p desplegado a uat" -ForegroundColor Green
}

Write-Host "`n== Listo. El pipeline de GitHub Actions despliega uat. ==" -ForegroundColor Green
