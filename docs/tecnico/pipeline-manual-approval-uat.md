# Pipeline CI/CD — Manual Approval gates UAT/PROD (BFFs Sodimac)

## Resumen

El workflow `pipeline.yml` de los BFFs Sodimac (`APP03022-mrch-bff-somx-ppsomx-<service>`) tiene **gates de aprobación manual** antes de desplegar a UAT y PROD. Sin la aprobación, el pipeline queda en estado `Cancelled`.

## Flujo del pipeline

```
push a develop / uat / main
  ↓
build (compile + tests)
  ↓
build_docker (imagen)
  ↓
deploy-cloudendpoint ⚠ (gate manual UAT y PROD)
  ↓
deploy
  ├─ Preview K8S DEV ✓ (auto)
  ├─ Deploy GKE DEV ✓ (auto)
  ├─ Manual Approval UAT ⏸ (espera click)
  ├─ Preview K8S UAT
  ├─ Deploy Cloud Endpoint UAT
  ├─ Deploy GKE UAT
  ├─ Manual Approval PROD ⏸ (espera click)
  ├─ Preview K8S PROD
  ├─ Deploy Cloud Endpoint PROD
  ├─ Deploy GKE PROD
  └─ Tag
```

## Comportamiento

Tras merge a `develop`/`uat`:
1. Build + Docker corren auto ✓
2. DEV deploy corre auto ✓
3. **Manual Approval UAT** queda en pending — alguien con permisos debe aprobar
4. Si no se aprueba en X minutos → pipeline timeout → status `Cancelled`
5. Sin aprobación → no se despliega openapi al gateway UAT → endpoints nuevos siguen dando 404 "request is not defined by this API"

## Cómo aprobar

Vista de un workflow run:
```
https://github.com/falabella-stores-and-merchandise/<repo>/actions/runs/<id>
```

1. Click en el job pendiente (ej. "Deploy Cloud Endpoint UAT")
2. Botón "Review pending deployments"
3. Seleccionar environment → "Approve and deploy"

Quién tiene permisos: Bonelli (DevOps), probablemente otros del equipo.

## Diagnóstico de pipeline Cancelled

Síntoma: PR mergeado a develop, endpoint nuevo en openapi, pero gateway UAT sigue respondiendo 404.

1. Ir a Actions del repo
2. Buscar workflow `pipeline.yml` del commit del merge
3. Si status = `Cancelled` + gate manual sin aprobar → ese es el problema
4. Re-run jobs O pedir aprobación a Bonelli

## Botón "Re-run jobs"

Top-right del workflow run. Re-ejecuta desde el principio o solo failed jobs. Si el gate manual se agotó por timeout, hay que re-run.

## Workflows que NO son deploy

Algunos workflows fallan pero **no bloquean el deploy** (no aplica el gate):

| Workflow | `on:` | Bloquea deploy? |
|---|---|---|
| `pipeline.yml` | `push` | SÍ — es el deploy |
| `security-suit.yml` | `pull_request` | No (corre en PR, no en merge) |
| `dependency-check` | parte de security-suit | No (informativo) |

Si `security-suit / dependency-check` falla en un PR por vulnerabilidades pre-existentes → no bloquea merge ni deploy.

## Referencias

- [STM-1525 PR #26 workflow run](https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util/actions) — ejemplo de pipeline.yml con gates manuales (mayo 2026)
- [docs/aprendizaje/devops-cicd.md](../aprendizaje/devops-cicd.md) — plan de estudio devops/cicd

## Flujo de promoción rama → ambiente

| Rama | Ambiente | Aprobación |
|---|---|---|
| `feature/*` | — | — |
| `develop` | DEV (auto) | No requerido |
| `uat` | UAT | Manual Approval |
| `main` | PROD | Manual Approval |

**Nota:** DEV environment está obsoleto en Sodimac MX. Por convención se promueve develop → uat directo via PR cuando hay batch listo para validar.
