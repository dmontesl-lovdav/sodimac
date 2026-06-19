<#
.SYNOPSIS
  Dirección A (BAJAR): repos reales Sodimac -> mirror -> repo compartido.

  Actualiza los repos reales a develop, los copia al mirror (robocopy /MIR) y
  sube el resultado al mirror (rama dmontes). Pensado para correr en la PC Sodimac.

.DESCRIPTION
  Pasos:
    1. Por cada proyecto: git fetch + checkout develop + pull.
    2. Mirror a rama dmontes + pull, y robocopy real -> mirror de cada proyecto.
    3. git add -A en el mirror, muestra el status, pide confirmación y hace commit + push.

  Detalle del flujo: docs/SINCRONIZACION-MIRROR-SODIMAC.md (Dirección A).

.PARAMETER Projects
  Lista de carpetas de proyecto a sincronizar. Default: fiscal-api + util-api.

.PARAMETER Yes
  Omite la confirmación antes del commit/push (modo desatendido).

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
    "APP03022-mrch-bff-somx-ppsomx-util"   # OJO: bff util usa GUIONES, no puntos
  ),
  [string] $RealRoot   = "C:\workspace-fbc-github",
  [string] $Mirror     = "C:\local",
  [string] $MirrorBranch = "dmontes",
  [switch] $Yes
)

$ErrorActionPreference = "Stop"

function Write-Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "  OK  $msg" -ForegroundColor Green }
function Write-Warn2($msg){ Write-Host "  !!  $msg" -ForegroundColor Yellow }

# robocopy: exit codes 0-7 = éxito; >=8 = error real
function Invoke-Robocopy($src, $dst) {
  robocopy $src $dst /MIR /XD .git node_modules dist target build .idea /XF *.log | Out-Null
  if ($LASTEXITCODE -ge 8) { throw "robocopy falló ($src -> $dst), exit=$LASTEXITCODE" }
}

Write-Host "Proyectos a sincronizar:" -ForegroundColor White
$Projects | ForEach-Object { Write-Host "  - $_" }

# ---------- PASO 1: repos reales -> develop ----------
Write-Step "PASO 1 — actualizar repos reales a develop"
foreach ($p in $Projects) {
  $path = Join-Path $RealRoot $p
  if (-not (Test-Path (Join-Path $path ".git"))) {
    Write-Warn2 "$p no es repo git en $RealRoot — se omite"
    continue
  }
  Write-Host ">>> $p" -ForegroundColor Cyan
  Push-Location $path
  try {
    git fetch origin
    git checkout develop
    git pull origin develop
    Write-Ok "$p en develop al día"
  } finally { Pop-Location }
}

# ---------- PASO 2: mirror + robocopy ----------
Write-Step "PASO 2 — mirror a $MirrorBranch + robocopy real -> mirror"
Push-Location $Mirror
try {
  git checkout $MirrorBranch
  git pull origin $MirrorBranch
} finally { Pop-Location }

foreach ($p in $Projects) {
  $src = Join-Path $RealRoot $p
  $dst = Join-Path $Mirror $p
  if (-not (Test-Path $src)) { Write-Warn2 "origen no existe: $src — se omite"; continue }
  Write-Host ">>> robocopy $p" -ForegroundColor Cyan
  Invoke-Robocopy $src $dst
  Write-Ok "$p copiado al mirror"
}

# ---------- PASO 3: commit + push al mirror ----------
Write-Step "PASO 3 — commit + push al mirror ($MirrorBranch)"
Push-Location $Mirror
try {
  git add -A
  $status = git status --porcelain
  if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Ok "Sin cambios — el mirror ya estaba al día. Nada que subir."
    return
  }

  Write-Host "`nCambios staged:" -ForegroundColor White
  git status --short

  Write-Warn2 "Revisa que NO aparezcan revertidos cambios propios aún no subidos a Sodimac."

  if (-not $Yes) {
    $resp = Read-Host "`n¿Commit + push al mirror? (s/N)"
    if ($resp -notin @("s","S","y","Y")) { Write-Warn2 "Cancelado por el usuario. Cambios quedan staged."; return }
  }

  $fecha = (Get-Date).ToString("yyyy-MM-dd")
  $lista = ($Projects -join ", ")
  git commit -m "sync: develop real -> mirror ($lista) $fecha"
  git push origin $MirrorBranch
  Write-Ok "Push al mirror completado."
} finally { Pop-Location }

Write-Host "`nListo. En la PC personal: git pull origin $MirrorBranch" -ForegroundColor Green
