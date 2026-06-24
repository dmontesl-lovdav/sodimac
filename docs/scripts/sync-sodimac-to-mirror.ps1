<#
.SYNOPSIS
  Direccion A (BAJAR): repos reales Sodimac -> mirror -> repo compartido.

  Actualiza los repos reales a develop, los copia al mirror (robocopy /MIR) y
  sube el resultado al mirror (rama dmontes). Pensado para correr en la PC Sodimac.

.DESCRIPTION
  Pasos:
    1. Por cada proyecto: git fetch + checkout develop + pull.
    2. Mirror a rama dmontes + pull, y robocopy real -> mirror de cada proyecto.
    3. git add -A en el mirror, muestra el status, pide confirmacion y hace commit + push.

  Detalle del flujo: docs/SINCRONIZACION-MIRROR-SODIMAC.md (Direccion A).

.PARAMETER Projects
  Lista de carpetas de proyecto a sincronizar. Default: los 9 (3 backends + 3 bff + 3 fronts SPA).
  OJO: el front se publica a UAT por su propio pipeline; sincronizarlo solo lo trae al mirror.

.PARAMETER Yes
  Omite la confirmacion antes del commit/push (modo desatendido).

.EXAMPLE
  .\sync-sodimac-to-mirror.ps1
  .\sync-sodimac-to-mirror.ps1 -Yes
  .\sync-sodimac-to-mirror.ps1 -Projects "APP03022-mrch.backend.somx.fiscal-api"
#>

param(
  [string[]] $Projects = @(
    # Backends
    "APP03022-mrch.backend.somx.fiscal-api",
    "APP03022-mrch.backend.somx.finanzas-api",
    "APP03022-mrch.backend.somx.util-api",
    # BFFs
    "APP03022-mrch.bff.somx.ppsomx.fiscal",
    "APP03022-mrch.bff.somx.ppsomx.finanzas",
    "APP03022-mrch-bff-somx-ppsomx-util",  # OJO: bff util usa GUIONES, no puntos
    # Fronts (SPA). OJO naming inconsistente: finanzas con GUION, fiscal/util con PUNTO.
    # Verificar el nombre real de la carpeta en C:\workspace-fbc-github antes de correr.
    "APP03022-mrch.frontend.somx.fiscal.spa",
    "APP03022-mrch.frontend.somx.finanzas-spa",
    "APP03022-mrch.frontend.somx.util.spa"
  ),
  [string] $RealRoot     = "C:\workspace-fbc-github",
  [string] $Mirror       = "C:\local",
  [string] $MirrorBranch = "dmontes",
  [switch] $Yes
)

$ErrorActionPreference = "Stop"

function Write-Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "  OK  $msg" -ForegroundColor Green }
function Write-Note($msg) { Write-Host "  !!  $msg" -ForegroundColor Yellow }

# robocopy: exit codes 0-7 = exito; >=8 = error real
function Invoke-Robocopy($src, $dst) {
  robocopy $src $dst /MIR /XD .git node_modules dist target build .idea /XF *.log | Out-Null
  if ($LASTEXITCODE -ge 8) { throw "robocopy fallo ($src -> $dst), exit=$LASTEXITCODE" }
}

Write-Host "Proyectos a sincronizar:" -ForegroundColor White
$Projects | ForEach-Object { Write-Host "  - $_" }

# ---------- PASO 1: repos reales -> uat y develop (queda en develop) ----------
Write-Step "PASO 1 - actualizar repos reales (uat + develop, termina en develop)"
foreach ($p in $Projects) {
  $path = Join-Path $RealRoot $p
  if (-not (Test-Path (Join-Path $path ".git"))) {
    Write-Note "$p no es repo git en $RealRoot - se omite"
    continue
  }
  Write-Host ">>> $p" -ForegroundColor Cyan
  Push-Location $path
  try {
    git fetch origin
    # Actualiza uat
    git checkout uat
    git pull origin uat
    # Termina en develop (es lo que se copia al mirror)
    git checkout develop
    git pull origin develop
    Write-Ok "${p}: uat y develop al dia (en develop)"
  } finally { Pop-Location }
}

# ---------- PASO 2: mirror + robocopy ----------
Write-Step "PASO 2 - mirror a $MirrorBranch + robocopy real -> mirror"
Push-Location $Mirror
try {
  git checkout $MirrorBranch
  git pull origin $MirrorBranch
} finally { Pop-Location }

foreach ($p in $Projects) {
  $src = Join-Path $RealRoot $p
  $dst = Join-Path $Mirror $p
  if (-not (Test-Path $src)) { Write-Note "origen no existe: $src - se omite"; continue }
  Write-Host ">>> robocopy $p" -ForegroundColor Cyan
  Invoke-Robocopy $src $dst
  Write-Ok "$p copiado al mirror"
}

# ---------- PASO 3: commit + push al mirror ----------
Write-Step "PASO 3 - commit + push al mirror ($MirrorBranch)"
Push-Location $Mirror
try {
  git add -A
  $status = git status --porcelain
  if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Ok "Sin cambios - el mirror ya estaba al dia. Nada que subir."
    return
  }

  Write-Host "`nCambios staged:" -ForegroundColor White
  git status --short

  Write-Note "Revisa que NO aparezcan revertidos cambios propios aun no subidos a Sodimac."

  if (-not $Yes) {
    $resp = Read-Host "`n.Commit + push al mirror? (s/N)"
    if ($resp -notin @("s","S","y","Y")) { Write-Note "Cancelado por el usuario. Cambios quedan staged."; return }
  }

  $fecha = (Get-Date).ToString("yyyy-MM-dd")
  $lista = ($Projects -join ", ")
  git commit -m "sync: develop real -> mirror ($lista) $fecha"
  git push origin $MirrorBranch
  Write-Ok "Push al mirror completado."
} finally { Pop-Location }

Write-Host "`nListo. En la PC personal: git pull origin $MirrorBranch" -ForegroundColor Green
