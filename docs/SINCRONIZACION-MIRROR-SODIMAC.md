# Sincronización Mirror ↔ Sodimac — Runbook

> Cómo traer cambios de los repos reales de Sodimac al workspace donde corre Claude, y viceversa.
> Cuando el usuario diga **"hay que sincronizar con el repositorio de Sodimac"**, este es el procedimiento.
> Última actualización: 2026-06-02.

---

## Por qué existe esto (el problema)

Claude está **bloqueado** en la PC de Sodimac. El desarrollo se hace en la **PC personal** (con Claude), pero el código real de Sodimac vive en repos a los que esta PC **no tiene acceso directo** (org `falabella-stores-and-merchandise`, requieren SSH key corporativa).

Solución: un **repo espejo (mirror)** en GitHub personal que actúa de puente.

```
┌─────────────────────────┐         ┌──────────────────────────┐
│  PC PERSONAL (Claude)   │         │  PC SODIMAC (sin Claude) │
│                         │         │                          │
│  c:\workspace-sodimac   │  push   │  C:\local                │
│  = clone del mirror     │◄───────►│  = clone del mirror      │
│  (1 repo, 6+ carpetas)  │  pull   │  (mismo mirror)          │
│                         │ github  │                          │
│  remote: dmontesl-lovdav│ personal│  + C:\workspace-fbc-github│
│  /sodimac  rama dmontes │         │    = N repos REALES       │
└─────────────────────────┘         │      (falabella org)      │
                                     │      cada uno su .git     │
                                     └──────────────────────────┘
```

- **Mirror**: `https://github.com/dmontesl-lovdav/sodimac`, **única rama `dmontes`**. Es un solo repo git que contiene una carpeta por proyecto.
- **Repos reales Sodimac**: `git@github.com:falabella-stores-and-merchandise/<proyecto>.git`, ramas `feature/* → develop → uat → main`. **DEV ya no se usa**, deploy directo a UAT.
- En la PC personal, las carpetas `APP03022-*` **NO son repos git independientes** — todo lo trackea el repo único `workspace-sodimac`.

**Regla de oro: Sodimac `develop` manda.** El mirror puede ir atrás. Nunca sobrescribir develop real con el mirror sin verificar.

---

## Dirección A — BAJAR (real Sodimac → mirror → workspace Claude)

Es la dirección más común: "tengo cambios nuevos en Sodimac, quiero que Claude los vea".

### Paso 1 (PC Sodimac) — actualizar repos reales a `develop`

```powershell
$reales = @(
  "APP03022-mrch.backend.somx.fiscal-api",
  "APP03022-mrch.backend.somx.finanzas-api",
  "APP03022-mrch.backend.somx.util-api",
  "APP03022-mrch.bff.somx.ppsomx.fiscal",
  "APP03022-mrch.bff.somx.ppsomx.finanzas",
  "APP03022-mrch-bff-somx-ppsomx-util"     # OJO: bff util usa GUIONES, no puntos
)
foreach ($p in $reales) {
  Write-Host "=== $p ===" -ForegroundColor Cyan
  Push-Location "C:\workspace-fbc-github\$p"
  git fetch origin
  git checkout develop
  git pull origin develop
  Pop-Location
}
```

### Paso 2 (PC Sodimac) — robocopy real → mirror + push

```powershell
$mirror = "C:\local"

# mirror en rama correcta y al día
cd $mirror
git checkout dmontes
git pull origin dmontes

# copiar cada repo real a su carpeta en el mirror
foreach ($p in $reales) {
  Write-Host "=== $p ===" -ForegroundColor Cyan
  robocopy "C:\workspace-fbc-github\$p" "$mirror\$p" /MIR /XD .git node_modules dist target build .idea /XF *.log
}

# commit + push al mirror
cd $mirror
git add -A
git commit -m "sync: develop real -> mirror (<modulos>) <fecha>"
git push origin dmontes
```

### Paso 3 (PC personal / Claude) — pull

```bash
cd c:\workspace-sodimac
git pull origin dmontes
```

Listo: Claude ya ve el código fresco.

---

## Dirección B — SUBIR (workspace Claude → mirror → real Sodimac)

Cuando Claude generó cambios y hay que llevarlos al repo real.

### Paso 1 (PC personal) — commit + push al mirror
```bash
cd c:\workspace-sodimac
git add -A
git commit -m "feat/fix: descripción"
git push origin dmontes
```

### Paso 2 (PC Sodimac) — pull mirror + robocopy mirror → real
```powershell
cd C:\local
git pull origin dmontes

# copiar SOLO el proyecto tocado, mirror -> real
robocopy "C:\local\<proyecto>" "C:\workspace-fbc-github\<proyecto>" /MIR /XD .git node_modules dist target build .idea /XF *.log
```

### Paso 3 (PC Sodimac) — branch + PR en el repo real
```powershell
cd C:\workspace-fbc-github\<proyecto>
git checkout develop
git pull origin develop
git checkout -b feature/STM-XXXX
git add -A
git commit -m "feat: descripción"
git push origin feature/STM-XXXX
# luego PR: feature/STM-XXXX -> develop -> uat -> main
```

---

## Verificación de divergencia UAT (antes de sincronizar)

Para detectar hotfixes aplicados directo a UAT que no estén en develop (patrón Bonelli):

```powershell
Get-ChildItem C:\workspace-fbc-github -Directory -Filter "APP03022*" | ForEach-Object {
  $g = Join-Path $_.FullName ".git"; if (-not (Test-Path $g)) { return }
  Push-Location $_.FullName
  git fetch origin --quiet 2>$null
  if (git rev-parse --verify --quiet origin/uat) {
    $uatAheadDev = git rev-list --count origin/develop..origin/uat 2>$null
    Write-Host ("{0,-46} uat>develop={1}" -f $_.Name, $uatAheadDev) -ForegroundColor Cyan
  }
  Pop-Location
}
```

Si `uat>develop` > 0, confirmar si son commits reales o solo merges:
```powershell
cd C:\workspace-fbc-github\<proyecto>
git log --oneline --no-merges origin/develop..origin/uat   # vacío = solo merges, nada que rescatar
git diff --stat origin/develop..origin/uat                 # vacío = árbol idéntico
```
- `--no-merges` **vacío** = uat solo recibió promociones de develop, **no hay hotfixes huérfanos**. Nada que reverse-mergear.
- Si aparecen commits → reverse-merge a develop: `git merge origin/uat --no-ff`.

---

## Trampas conocidas

- **bff util = GUIONES**: `APP03022-mrch-bff-somx-ppsomx-util` (no `.bff.somx.ppsomx.util`). La variante con puntos en `C:\workspace-fbc-github` **no tiene `.git`**, es basura — ignorar.
- **`robocopy /MIR`** borra en destino lo que no esté en origen (mantiene idéntico). `/XD .git` protege el `.git` del destino (vive en la raíz del mirror, no en la subcarpeta).
- **`/XF *.log`** no excluye `*.log.gz` — pueden colarse logs comprimidos. Inofensivo, pero se pueden limpiar.
- **`git push`** al mirror puede pedir credenciales del token personal `dmontesl-lovdav`.
- **No** hacer push directo a repos reales desde la PC personal (sin acceso). **No** curl a `uat.fbusinesscenter.com` ni `kubectl` desde la PC personal (sin red/credenciales corporativas).
- Si `git checkout develop` truena por cambios locales en un repo real → no forzar, resolver primero.

---

## Repos sincronizados típicamente (módulos David)

| Proyecto | Carpeta |
|---|---|
| fiscal-api | `APP03022-mrch.backend.somx.fiscal-api` |
| finanzas-api | `APP03022-mrch.backend.somx.finanzas-api` |
| util-api | `APP03022-mrch.backend.somx.util-api` |
| bff fiscal | `APP03022-mrch.bff.somx.ppsomx.fiscal` |
| bff finanzas | `APP03022-mrch.bff.somx.ppsomx.finanzas` |
| bff util | `APP03022-mrch-bff-somx-ppsomx-util` (guiones) |
