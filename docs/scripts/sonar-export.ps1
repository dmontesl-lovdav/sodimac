<#
.SYNOPSIS
  Exporta el detalle de issues de SonarQube (corp Falabella) a CSV/JSON + descripciones de reglas.
  CORRER EN LA PC SODIMAC (red corporativa + token corporativo). El output viaja por el mirror.

.DESCRIPTION
  El xlsx resumen solo trae conteos. Este script baja el detalle real:
  archivo + linea + regla + mensaje + severidad + impacto, mas la explicacion
  de cada regla (como corregir). Todo a docs/analisis/sonar/ (tracked -> viaja al PC personal).

.PARAMETER ProjectKey
  Key exacto del proyecto en Sonar (el ?id= de la URL del dashboard).
  Si se omite, lista proyectos que hagan match con -Filter y termina.

.PARAMETER Filter
  Texto para buscar el projectKey cuando no lo pasas (default: fiscal).

.PARAMETER Branch
  Rama analizada (default: uat).

.EXAMPLE
  $env:SONAR_TOKEN = "squ_..."          # pegar token, NO dejarlo en archivo
  .\sonar-export.ps1                     # descubre keys que matcheen "fiscal"
  .\sonar-export.ps1 -ProjectKey <key>   # baja el detalle
#>
param(
  [string]$ProjectKey,
  [string]$Filter = "fiscal",
  [string]$Branch = "uat",
  [string]$BaseUrl = "https://sonarqube-corp.falabella.tech",
  [string]$OutDir  = "$PSScriptRoot\..\analisis\sonar"
)

$ErrorActionPreference = "Stop"

if (-not $env:SONAR_TOKEN) {
  Write-Error "Falta `$env:SONAR_TOKEN. Ponlo antes de correr:  `$env:SONAR_TOKEN = 'squ_...'"
  return
}

# Auth basica: token como usuario, password vacio
$pair    = "$($env:SONAR_TOKEN):"
$b64     = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))
$headers = @{ Authorization = "Basic $b64" }

function Invoke-Sonar($path) {
  Invoke-RestMethod -Headers $headers -Uri "$BaseUrl$path" -Method Get
}

# Validar token
$val = Invoke-Sonar "/api/authentication/validate"
if (-not $val.valid) { Write-Error "Token invalido"; return }
Write-Host "Token OK" -ForegroundColor Green

# --- Descubrir projectKey si no se paso ---
if (-not $ProjectKey) {
  Write-Host "Proyectos que matchean '$Filter':" -ForegroundColor Cyan
  $enc = [Uri]::EscapeDataString("query = `"$Filter`"")
  $res = Invoke-Sonar "/api/components/search_projects?filter=$enc&ps=100"
  $res.components | Select-Object key, name | Format-Table -AutoSize
  Write-Host "`nRe-ejecuta con:  .\sonar-export.ps1 -ProjectKey <key>" -ForegroundColor Yellow
  return
}

if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Path $OutDir -Force | Out-Null }

# --- Paginar todos los issues abiertos del proyecto ---
Write-Host "Bajando issues de $ProjectKey (branch=$Branch)..." -ForegroundColor Cyan
$all  = @()
$page = 1
do {
  $q = "/api/issues/search?componentKeys=$ProjectKey&branch=$Branch" +
       "&resolved=false&ps=500&p=$page" +
       "&additionalFields=rules&s=FILE_LINE&asc=true"
  $r = Invoke-Sonar $q
  $all += $r.issues
  $total = $r.total
  Write-Host ("  page {0}: {1}/{2}" -f $page, $all.Count, $total)
  $page++
} while ($all.Count -lt $total -and $r.issues.Count -gt 0)

Write-Host "Total issues: $($all.Count)" -ForegroundColor Green

# --- Aplanar a CSV ---
$rows = $all | ForEach-Object {
  $comp = $_.component -replace "^$([regex]::Escape($ProjectKey)):", ""   # quitar prefijo projectKey:
  $imp  = ($_.impacts | ForEach-Object { "$($_.softwareQuality)=$($_.severity)" }) -join "; "
  [pscustomobject]@{
    file        = $comp
    line        = $_.line
    rule        = $_.rule
    severity    = $_.severity        # deprecated pero util
    type        = $_.type            # BUG / VULNERABILITY / CODE_SMELL
    impacts     = $imp               # MQR mode: SECURITY/RELIABILITY/MAINTAINABILITY = HIGH/MEDIUM/LOW
    effort      = $_.effort
    message     = $_.message
    status      = $_.status
    key         = $_.key
  }
}

$stamp   = Get-Date -Format "yyyyMMdd"
$safeKey = $ProjectKey -replace "[:/\\]", "_"
$csvPath = Join-Path $OutDir "$stamp`_$safeKey`_issues.csv"
$jsonPath= Join-Path $OutDir "$stamp`_$safeKey`_issues.json"

$rows | Sort-Object file, line | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8
$all  | ConvertTo-Json -Depth 6 | Out-File -FilePath $jsonPath -Encoding UTF8
Write-Host "CSV : $csvPath"  -ForegroundColor Green
Write-Host "JSON: $jsonPath" -ForegroundColor Green

# --- Descripcion de cada regla unica (como corregir) ---
$rules   = $all.rule | Sort-Object -Unique
Write-Host "Bajando descripcion de $($rules.Count) reglas..." -ForegroundColor Cyan
$ruleDocs = foreach ($rk in $rules) {
  try {
    $rd = Invoke-Sonar "/api/rules/show?key=$([Uri]::EscapeDataString($rk))"
    $howto = ($rd.rule.descriptionSections | Where-Object { $_.key -eq "how_to_fix" }).content
    [pscustomobject]@{
      rule    = $rk
      name    = $rd.rule.name
      count   = ($all | Where-Object { $_.rule -eq $rk }).Count
      htmlDesc= $rd.rule.htmlDesc
      howToFix= $howto
    }
  } catch {
    [pscustomobject]@{ rule = $rk; name = "(error)"; count = 0; htmlDesc = "$_"; howToFix = "" }
  }
}
$rulesPath = Join-Path $OutDir "$stamp`_$safeKey`_rules.json"
$ruleDocs | ConvertTo-Json -Depth 6 | Out-File -FilePath $rulesPath -Encoding UTF8
Write-Host "RULES: $rulesPath" -ForegroundColor Green

# --- Resumen por regla (para priorizar) ---
Write-Host "`nTop reglas por cantidad:" -ForegroundColor Cyan
$rows | Group-Object rule | Sort-Object Count -Descending |
  Select-Object -First 20 Count, Name | Format-Table -AutoSize
