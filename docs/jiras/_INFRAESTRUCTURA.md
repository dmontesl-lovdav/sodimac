# Infraestructura y flujo de versionamiento

> **Fuente de la verdad:** repositorios oficiales Sodimac en la organización `falabella-stores-and-merchandise` de GitHub. Rama `develop`. Todo lo demás (mirror, copia local) es derivado y debe alinearse contra esa fuente.

## Mapa de entornos

### PC Personal (esta máquina, con Claude)

| Ruta | Qué es |
|------|--------|
| `C:\workspace-sodimac\` | Workspace de trabajo. Clon del mirror `dmontesl-lovdav/sodimac` rama `dmontes`. Contiene los proyectos Sodimac en subcarpetas (uno por servicio). |
| `https://github.com/dmontesl-lovdav/sodimac` | **Mirror personal en GitHub.** Solo rama `dmontes`. Sirve como puente entre PC Personal y PC Sodimac. NO es fuente de verdad. |

Convención de carpetas en workspace-sodimac:
- `APP03022-mrch.backend.somx.<nombre>` → backends (Node/Java)
- `APP03022-mrch.bff.somx.ppsomx.<nombre>` → BFFs con puntos
- `APP03022-mrch-bff-somx-ppsomx-<nombre>` → BFFs con guiones (variantes)
- `APP03022-mrch.frontend.somx.<nombre>` → frontends

### PC Sodimac (sin Claude, bloqueado por empresa)

| Ruta | Qué es |
|------|--------|
| `C:\local\` | Clon del mirror `dmontesl-lovdav/sodimac` rama `dmontes`. Recibe cambios de PC Personal vía `git pull`. |
| `C:\workspace-fbc-github\<repo>\` | **Clones de los repos REALES de Sodimac.** Uno por repo, organización `falabella-stores-and-merchandise`. Es donde se hacen commits/PRs oficiales. |

## Flujo de trabajo

```
[PC Personal con Claude]                    [PC Sodimac sin Claude]
─────────────────────────                  ────────────────────────

  C:\workspace-sodimac\                       C:\local\
        │                                          ↑
        │  1. git push origin dmontes              │ 3. git pull origin dmontes
        ↓                                          │
                                                   │
   github.com/dmontesl-lovdav/sodimac  ────────────┘
   (mirror personal — rama dmontes)
                                                   │
                                                   │ 4. robocopy de C:\local\<repo>\src
                                                   ↓    a C:\workspace-fbc-github\<repo>\src
                                                   
                                            C:\workspace-fbc-github\<repo>\
                                                   │
                                                   │ 5. git add + commit + push
                                                   ↓
                                            
                                       github.com/falabella-stores-and-merchandise/<repo>
                                       (FUENTE DE LA VERDAD — rama develop / uat / main)
```

### Pasos en orden

1. **PC Personal:** edito código en `C:\workspace-sodimac\<carpeta>`. Pruebo local (Docker + servicios).
2. **PC Personal:** commit + `git push origin dmontes` al mirror personal.
3. **PC Sodimac:** `cd C:\local && git pull origin dmontes` → trae los cambios al clon del mirror.
4. **PC Sodimac:** `robocopy C:\local\<repo>\<src> C:\workspace-fbc-github\<repo>\<src> [archivo]` → mueve archivos al repo institucional. Excluir siempre `.git`, `node_modules`, `dist`, `coverage`, `.vscode`, `target`.
5. **PC Sodimac:** en `C:\workspace-fbc-github\<repo>`: crear rama `feature/STM-XXXX`, `git add`, commit, `git push origin feature/STM-XXXX`, abrir PR develop.

### Comandos típicos por etapa

**Sync masivo todos los repos a develop fresh (PC Sodimac):**

```cmd
cd C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api && git fetch --all --prune && git checkout develop && git pull
REM repetir por cada repo relevante (ver lista completa en docs/jiras/INDEX.md sección "URLs pase a UAT")
```

Verificación masiva del estado:

```cmd
for %d in (APP03022-mrch.backend.somx.fiscal-api APP03022-mrch.backend.somx.finanzas-api APP03022-mrch.backend.somx.util-api APP03022-mrch.bff.somx.ppsomx.fiscal APP03022-mrch.bff.somx.ppsomx.finanzas APP03022-mrch-bff-somx-ppsomx-util) do @echo === %d === & cd /d "C:\workspace-fbc-github\%d" & git branch --show-current & git status -s
```

**Robocopy mirror → repo Sodimac (un archivo o carpeta puntual):**

```cmd
REM Un archivo:
robocopy C:\local\APP03022-mrch.bff.somx.ppsomx.finanzas\src C:\workspace-fbc-github\APP03022-mrch.bff.somx.ppsomx.finanzas\src App.js

REM Carpeta completa (espejo):
robocopy C:\workspace-fbc-github\<repo> C:\local\<repo> /MIR /XD .git node_modules dist coverage .vscode target
```

`/MIR` = espejo (borra en destino lo que no exista en source). Usar con cuidado.

**Reset destructivo (descartar cambios locales para alinear con develop):**

```cmd
git reset --hard
git clean -fd
git checkout develop
git pull origin develop
```

Esto **borra** todo cambio sin commit + archivos untracked. Antes asegurarse de tener backup de lo que valga.

**Push directo a develop (cuando develop NO está protegido, casos críticos):**

```cmd
git add <archivo>
git commit -m "feat: STM-XXXX descripción corta"
git push origin develop
```

## Reglas críticas

1. **Sodimac develop manda.** Si el mirror y Sodimac develop divergen, **siempre sincronizar trayendo Sodimac develop** primero, luego aplicar el delta encima. Nunca al revés.

2. **Ramas:** lo ideal es `feature/STM-XXXX` → PR → `develop`. En casos críticos (urgente, develop no protegido, sin reviewer disponible) se permite push directo a `develop`, pero el mensaje del commit debe mencionar `STM-XXXX` y justificación en el body.

3. **DEV está obsoleto.** Despliegue va directo a UAT. No usar la rama `dev` ni el ambiente DEV.

4. **No sobrescribir Sodimac develop con versiones viejas del mirror.** Antes de cualquier robocopy hacia `workspace-fbc-github`, verificar que la versión que se mueve es MÁS RECIENTE o EQUIVALENTE que la de develop.

5. **Backups antes de `robocopy /MIR` o `git reset --hard`.** Los hacemos en `c:\workspace-sodimac\sesiones\<feature>\backup-YYYYMMDD\` cuando hay cambios sin commit que pueden perderse.

6. **Mensajes de commit con `STM-XXXX`** para que `INDEX.md` pueda enlazar trabajo a jiras.

7. **Variables de ambiente:** cuando un cambio agrega vars (ej `UTIL_API_URL`, `SECURITY_ENABLED`), avisar a DevOps (Bonelli) con listado por proyecto + valores de referencia local.

## Cuándo es válido push directo a develop

- Develop NO está protegido en el repo (verificable con `git push origin develop` sin rebote).
- No hay reviewer disponible en el momento crítico (deploy bloqueado, error en UAT).
- Cambio es pequeño y autocontenido (un archivo, una refactorización menor).
- El commit incluye `STM-XXXX` y descripción clara.
- **NO aplicar a `main` ni `uat` directo** — esos siempre vía PR.

## Cuándo es OBLIGATORIO PR

- Cambios estructurales (arquitectura, schema DB, nuevas APIs públicas).
- Develop protegido en el repo (push directo rebota).
- Hay reviewer asignado al jira.
- Sub-jiras de un epic mayor (mejor un PR consolidado por epic).

---

## Debugging del gateway (Google Cloud Endpoints / Envoy / Cloudflare)

Headers de response que diagnostican dónde está el problema:

| Header | Significado |
|--------|-------------|
| `x-envoy-decorator-operation: ingress <OperationName>` | Envoy matcheó la ruta del openapi y forwardeó al backend. |
| `x-envoy-decorator-operation: ingress UnknownOperationName` + body `"The current request is not defined by this API."` | Openapi desplegado **NO** tiene la ruta. Falta deploy del cloud-endpoint o commit no llegó a gateway. |
| `x-envoy-upstream-service-time: <ms>` | Pod backend respondió en N ms. Si ausente, pod nunca respondió. |
| Sin `x-envoy-*` + `Server: cloudflare` + `error code: 504` | Cloudflare timeout. Pod cuelga (no responde a Envoy). Investigar logs del pod. |
| `via: 1.1 google` | Pasó por Google Cloud Endpoints (ESP). |
| `x-powered-by: Express` | El proceso Node respondió. |

Útil para distinguir capas:

```
Cliente → Cloudflare → Google Cloud Endpoints (ESP/Envoy) → BFF (Node proxy) → Backend
         ↑                ↑                                  ↑                  ↑
         CF-RAY           x-envoy-*                          (mismo Node)       x-envoy-upstream-service-time
```

## Logs Pino BFFs

Los BFFs Node (bff.finanzas, bff.fiscal, bff-util) usan `pino` con output JSON estructurado. Útil grep para debugging:

```
grep "Security context injected" → confirma que extractUserKey + fetchSecurityContext + buildSecurityHeaders corrieron OK
grep "util-api user-attributes non-ok" → util-api respondió error
grep "util-api user-attributes error" → fetch falló (timeout, network)
grep "No userKey" → no se pudo decodificar JWT (Bearer faltante o inválido)
grep "Failed to parse X-Endpoint-API-UserInfo" → header GCP malformado
grep "Failed to decode Bearer token" → Bearer no es JWT válido
```

## Squash merge — hash distinto

GitHub PR con "squash and merge" **regenera el commit** con un hash nuevo en `develop`. El commit original del feature branch NO aparece en `develop`. Para verificar si tu trabajo se mergeó:

- ❌ No confiar en buscar el hash del feature branch en `git log origin/develop`.
- ✓ Comparar archivos: `git diff origin/feature/STM-XXXX origin/develop -- <archivos>`. Si vacío o solo extra cosas de develop → tu trabajo está consolidado.
- ✓ Buscar por mensaje: `git log origin/develop --oneline --grep "STM-XXXX"`. El merge commit usualmente menciona el PR (`Merge pull request #N from .../feature/STM-XXXX`).

## Lecciones aprendidas

### Inconsistencia entre repos de un epic

**Síntoma:** epic STM-1403 mergeó código a fiscal-api + bff.fiscal pero **NO** a bff.finanzas. Resultado: backend finanzas-api leía headers `x-user-*` que nadie inyectaba.

**Lección:** al cerrar un epic multi-repo, verificar **archivo por archivo en cada repo** que el código existe en develop. Solo "git log con STM-XXXX" no es suficiente — un repo puede tener docs pero no código.

**Checklist para cerrar epic:**
- [ ] Por cada repo del epic: `grep -rl "<marker-code>" src/` confirma que el código del feature está presente.
- [ ] Por cada repo del epic: `git log origin/develop --grep "STM-XXXX"` muestra commit/merge.
- [ ] Verificar consistencia entre BFFs y backends que se hablan entre sí (si BFF inyecta headers, backend debe leerlos y viceversa).

### Diagnóstico 504 en UAT

**Síntoma:** POST a UAT cuelga, 504 Cloudflare, sin `x-envoy-upstream-service-time`.

**Causa típica:** versión vieja del pod corriendo. Tu commit puede estar en develop pero el pod no se redeployó.

**Diagnóstico en orden:**
1. ¿openapi del gateway tiene la ruta? Verifica `x-envoy-decorator-operation` con un GET simple. Si UnknownOperationName → falta deploy del cloud-endpoint.
2. ¿Pod backend está vivo? Healthcheck público sin auth (`/health`, `/actuator/health`).
3. ¿Pod responde rápido a errores? POST con body inválido debería dar 400 fast. Si cuelga → middleware/filter colgando (típico: fetch sin timeout).
4. Logs del pod (`kubectl logs` o GCP Cloud Logging) son la única forma de confirmar.

### PowerShell + curl + JSON body

`curl.exe` en PowerShell + `$body` here-string + `-d` falla por encoding UTF-16 BOM o `\r\n` raros → backend recibe JSON malformado → 400 SyntaxError.

Solución: archivo `body.json` ASCII sin BOM + `curl.exe ... --data-binary "@body.json"`. O usar `cmd.exe` que escapa más predecible.
