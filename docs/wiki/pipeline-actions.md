# Pipeline CI Microservice — GitHub Actions Sodimac

Guía para entender y operar los workflows de GitHub Actions de los 6 proyectos Sodimac (fiscal-api, bff.fiscal, finanzas-api, bff.finanzas, util-api, bff-util).

---

## Workflows existentes por repo

| Workflow | Trigger | Propósito |
|----------|---------|-----------|
| **Pipeline CI Microservice** | `on: push` a develop / uat / main | Build → Docker → K8S deploy → Cloud Endpoint deploy |
| **DevSecOps Security Tools** | `on: pull_request` | SCA (Dependency-Check), Secret Scan, SAST, Container Scanning |
| **Dependabot Updates** | scheduled / on PR | Updates de deps con CVE |
| **Automatic Dependency Submission (Maven)** | `on: push` | Submit dependency graph a GitHub para Dependabot |

---

## Anatomía del Pipeline CI Microservice

Stages típicos en orden:

1. **build** — compile (mvn / npm)
2. **build_docker** — `docker build` 3 imágenes (DEV / UAT / PROD)
3. **deploy-cloudendpoint** (grupo)
   - Manual Approval Cloud Endpoint **DEV**
   - Manual Approval Cloud Endpoint **UAT**
   - Manual Approval Cloud Endpoint **PROD**
   - Deploy Cloud Endpoint DEV / UAT / PROD (uno corre según branch)
4. **deploy** (grupo)
   - Preview K8S DEV / UAT / PROD
   - Manual Approval GKE (en algunos repos)
   - Deploy GKE DEV / UAT / PROD

Pipeline muestra **los 3** stages env siempre, pero solo el que aplica al branch corre. Los otros aparecen `⊘ skipped`.

| Branch push | Stage relevante |
|-------------|-----------------|
| `develop` | deploy DEV |
| `uat` | deploy UAT |
| `main`/`master` | deploy PROD |

---

## Manual Approval Cloud Endpoint — patrón crítico

Cuando un job `Manual Approval Cloud Endpoint X` aparece pendiente, el workflow queda en estado **Queued** (o "Waiting for approval") hasta recibir aprobación humana.

### Cómo se implementa

Sodimac usa la action **trstringer/manual-approval** (o equivalente). Funciona así:

1. Bot abre **Issue** en el repo con título: `Manual Approval Required for <ENV> Deployment Cloud Endpoint - Commit <SHA>`
2. Body del issue lista approvers requeridos (ej: `@g-dco018_FTC`, `@jrcastillo_FTC`)
3. Workflow pausa esperando comentario clave en el issue (típicamente `approved`)
4. Approver comenta `approved` (o palabra config) → bot cierra issue → workflow continúa
5. Si nadie aprueba → workflow eventualmente se cancela por timeout

### Approvers conocidos por env

| Env | Approvers |
|-----|-----------|
| DEV | g_dco018 (David), jrcastillo (Jhonatan), Bonelli |
| UAT | g_dco018, jrcastillo, Bonelli |
| PROD | Solo líderes DevOps (NO aprobar sin runbook formal) |

### Self-approval

Sodimac NO tiene GitHub Environments protection nativa configurada — la regla "Prevent self-review" **no aplica**. Como usa action externa basada en issues, **el actor del push puede aprobarse a sí mismo**. Se valida con prueba: el botón está disponible o el comentario `approved` se acepta.

### Encontrar el issue de approval

URL filtrada:
```
https://github.com/falabella-stores-and-merchandise/<repo>/issues?q=is%3Aissue+is%3Aopen+author%3Aapp%2Fgithub-actions
```

Si los issues abiertos están vacíos pero workflow sigue Queued:
1. Click en el job `Manual Approval Cloud Endpoint X` en sidebar del run
2. Leer logs — referencia al issue específico
3. Si issue cerrado por timeout → workflow zombie, hacer **cancel + re-run**

### Re-run

Botón **Re-run jobs** aparece solo si run está en estado `Cancelled` / `Failure` / `Success`. Si está `Queued` o `In progress`, NO aparece.

Si no hay botón y necesitas re-disparar:
- Opción A: `git commit --allow-empty -m "chore: trigger pipeline"` + push
- Opción B: cancelar manualmente el run desde menú `...` → aparece Re-run

---

## Trampa: pods K8S sí desplegados, Cloud Endpoint NO

Lectura común errónea: `Manual Approval Cloud Endpoint UAT` no aprobado = nada desplegado. **Falso**.

El stage `deploy GKE` corre **antes** o **en paralelo** al Cloud Endpoint. Pods K8S quedan con código nuevo, pero gateway sigue OpenAPI viejo.

**Implicación**:
- Cambios internos pod-a-pod → ya funcionan ✓
- Endpoint nuevo expuesto externamente → gateway no lo enruta hasta aprobar Cloud Endpoint ❌
- Síntoma: `x-envoy-decorator-operation: ingress UnknownOperationName` + 404 (ver `troubleshooting-envoy.md`)

---

## Trampa: pipeline Queued >3 días

Patrón observado: si nadie aprueba el gate, workflow puede quedar Queued varios días sin moverse. Eventualmente cancela. No "se queda esperando para siempre".

Acción cuando lleva >24h Queued:
1. Verificar si existe issue de approval abierto
2. Ping aprobador directo (Slack/Teams/Email a Bonelli o Jhonatan)
3. Si tras 3 días no hay movimiento, escalar a líder DevOps

---

## DevSecOps Security Tools — falla SCA (Dependency-Check)

Si el workflow `DevSecOps Security Tools` falla:

| Stage rojo | Causa | Fix |
|------------|-------|-----|
| SCA / dependency-check | CVE en dependencia (OWASP / Snyk) | bump version dep en `package.json` o `pom.xml` |
| Secret Scan | secret commiteado en código | `git filter-branch` + rotar secret |
| SAST (Auto) | code smell de seguridad (CodeQL) | refactor según mensaje |
| Container Scanning | CVE en base image | bump base image en Dockerfile |

**Marca "Required"** en el workflow significa que es check obligatorio en branch protection. Si está rojo, normalmente bloquea merge — pero algunos repos Sodimac permiten override de admin. Históricamente Sodimac permite merge con DevSecOps rojo + registra deuda.

---

## Cómo diagnosticar un workflow run desde UI

URL Actions:
```
https://github.com/falabella-stores-and-merchandise/<repo>/actions
```

Estados:
- 🟢 Success — todo OK, pods + gateway actualizados
- 🟡 Queued — esperando runner o approval (revisar Manual Approval gates)
- 🔵 In progress — corriendo
- 🔴 Failure — algún stage falló, click para ver cuál
- ⊘ Cancelled — abortado (timeout approval, manual cancel, o stage cancelado)

Filtros útiles:
- `Branch: develop` → solo runs de develop
- `Status: Failure` → fallos recientes
- `Event: push` → triggered por push (no PR)

---

## Patrón sync develop ↔ uat

Bonelli (DevOps) suele pushear hotfixes directo a rama `uat`, saltándose develop. Tras `git pull origin uat` que muestre fast-forward de commits nuevos, hacer reverse-merge para mantener develop sincronizado:

```cmd
git checkout develop
git pull origin develop
git merge origin/uat --no-ff -m "merge: uat into develop (sync hotfixes Bonelli)"
git push origin develop
```

Detalle: ver memoria Claude `project_uat_hotfixes_pattern.md`.

---

## Glosario rápido

- **Cloud Endpoint** — GCP service que registra OpenAPI specs y configura Envoy gateway / ESPv2 para enrutar API requests
- **ESPv2 / Envoy** — proxy que valida requests contra OpenAPI antes pasarlas al backend
- **K8S deploy / Deploy GKE** — `kubectl apply` de manifests al cluster GKE (pods nuevos)
- **Manual Approval gate** — paso del pipeline que pausa esperando aprobación humana
- **trstringer/manual-approval** — action GitHub que implementa el gate via issues
- **DevSecOps** — workflow de scans de seguridad (SCA, SAST, Secret, Container)
