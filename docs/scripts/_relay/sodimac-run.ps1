# ============================================================================
# sodimac-run.ps1  —  ARCHIVO COMPARTIDO (el "buzón ejecutable")
# ----------------------------------------------------------------------------
# Lado PERSONAL: editas este archivo con lo que quieras que corra en Sodimac,
#                luego:  powershell -File docs\scripts\relay-push.ps1 -Message "..."
# Lado SODIMAC:  powershell -File docs\scripts\relay-run.ps1
#                (hace git pull y EJECUTA este archivo)
#
# Todo lo que pongas debajo se ejecuta EN la PC de Sodimac.
# ============================================================================

$ErrorActionPreference = "Stop"

# --- Parametros de este pase ---
$Message = "feat(fiscal): unificar identidad de usuario a UUID (created_by/updated_by/user_id/changed_by)"
$proj    = "APP03022-mrch.backend.somx.fiscal-api"
$mirror  = "C:\local"
$real    = "C:\workspace-fbc-github"

function Run-Git { param([string[]]$a) & git @a; if ($LASTEXITCODE -ne 0) { throw "git $($a -join ' ') fallo ($LASTEXITCODE)" } }

Write-Host "== robocopy $proj (mirror -> real) ==" -ForegroundColor Cyan
robocopy "$mirror\$proj" "$real\$proj" /MIR /XD .git node_modules dist target build .idea /XF *.log | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy fallo ($LASTEXITCODE)" }
$global:LASTEXITCODE = 0

Set-Location "$real\$proj"
Run-Git @("checkout","develop")
Run-Git @("pull","origin","develop")
Write-Host "-- git status (revisa que sean los archivos esperados) --" -ForegroundColor Yellow
git status --short
Run-Git @("add","-A","src/main/java/com/sodimac/fiscal/api/","src/test/java/com/sodimac/fiscal/api/","migration/QA-2026-08-06-identidad-usuario-uuid.sql")
Run-Git @("commit","-m",$Message)
Run-Git @("push","origin","develop")

Run-Git @("checkout","uat")
Run-Git @("pull","origin","uat")
Run-Git @("merge","develop","--no-ff","-m","merge: identidad de usuario UUID")
Run-Git @("push","origin","uat")

Write-Host "`n== OK: codigo desplegado a uat (el pipeline lo aplica). ==" -ForegroundColor Green
Write-Host "== FALTA el ALTER (manual, tunel IAP):" -ForegroundColor Yellow
Write-Host "   psql -h 127.0.0.1 -p <puerto> -U <user> -d b2b_portal -f `"$real\$proj\migration\QA-2026-08-06-identidad-usuario-uuid.sql`""
