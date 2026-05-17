# Ruta de estudio — DevOps / CI-CD

Plan personal de aprendizaje para entender CI/CD, deploys, GitHub Actions, Docker, K8s y la plataforma Sodimac (GCP Cloud Endpoints).

Origen: confusión en run #59 de `bff.finanzas` donde el workflow quedó esperando aprobación manual de Cloud Endpoint y se canceló. Pods K8S sí desplegaron, gateway no. Esa diferencia es el hueco de conocimiento a cerrar.

---

## Orden recomendado

Aprende en este orden — cada paso construye sobre el anterior.

### 1. Git profundo (base de todo)

- **Pro Git book** (gratis, https://git-scm.com/book) — capítulos 1-3 obligatorios, capítulo 5 (workflows) crítico.
- **Learn Git Branching** (https://learngitbranching.js.org) — interactivo, visual, ~1 tarde.
- Dominar: `rebase` vs `merge`, `cherry-pick`, `reflog`, `reset` (--soft/--mixed/--hard), `bisect`.

### 2. GitHub Actions específico

- **Docs oficiales**: https://docs.github.com/en/actions — empezar con "Quickstart" + "Workflow syntax".
- Conceptos clave:
  - `jobs`, `steps`, `needs`, `if`, `matrix`
  - `environments` + `secrets`
  - **Branch protection rules** + **Environment protection rules** (lo que bloqueó el deploy hoy)
- Práctica: forkear un repo público, agregar workflow `lint + test + deploy` a Pages.

### 3. CI/CD conceptual

- Libro: **"Continuous Delivery"** — Jez Humble (clásico, denso pero define el campo).
- Resumen rápido (15 min): https://martinfowler.com/articles/continuousIntegration.html (Martin Fowler).
- Modelo mental: pipeline = `build → test → package → deploy`. Cada paso es un gate.

### 4. Docker + Kubernetes (lo que despliega los pods)

- **Docker for Developers** — tutorial oficial Docker docs. Conceptos: Dockerfile, layers, multi-stage builds.
- **Kubernetes Up & Running** (O'Reilly) o **kube.academy** (gratis).
- Comandos mínimos:
  ```bash
  kubectl get pods
  kubectl logs <pod>
  kubectl describe pod <pod>
  kubectl rollout status deployment/<name>
  ```
- Diferenciar: `Deployment` vs `Pod` vs `Service` vs `Ingress`.

### 5. GCP Cloud Endpoints (el stack real de Sodimac)

- https://cloud.google.com/endpoints/docs — arquitectura "ESPv2 + Envoy".
- Flujo: OpenAPI spec → Envoy config → routing + auth.
- Por qué `Deploy Cloud Endpoint` es un job separado de los pods K8S (la confusión del run #59).

### 6. Plataforma interna Sodimac

- Preguntar a DevOps Sodimac: ¿wiki interna? ¿runbook de deploy? ¿contacto para approvals?
- Leer `pipeline.yml` en cada repo. **Es el mejor maestro** porque describe el pipeline exacto que tú usas.

---

## Ejercicios prácticos

1. **Repo propio en GitHub**: crear repo, agregar workflow que corra `npm test` en cada PR. Romper el test a propósito → ver el ✗.
2. **Branch protection**: configurar tu repo para que `main` requiera 1 approval + checks pasen. Intentar mergear sin → bloqueado.
3. **Docker local**: dockerizar un Hello World Node.js, correr `docker build` + `docker run`.
4. **Kind o Minikube**: levantar K8S local, desplegar el Hello World, acceder vía `port-forward`.
5. **Matrix builds**: workflow GitHub Actions que corra tests en Node 18/20/22 en paralelo.

---

## Cursos pagados (alternativa a libros)

- **KodeKloud** — labs prácticos K8S / Docker / CI-CD. ~$30/mes.
- **A Cloud Guru** — paths CKAD + Terraform.
- **Frontend Masters** — "Complete Intro to Containers" (Brian Holt, a veces gratis).

---

## Plan semanal sugerido

| Semana | Foco | Entregable |
|--------|------|------------|
| 1 | Pro Git cap 3 (branching) | Resumen propio en 1 página |
| 2 | GitHub Actions Quickstart + Environments | Workflow funcionando en repo personal |
| 3 | Leer `pipeline.yml` de bff.finanzas + fiscal-api | Diagrama de lo que hace cada stage |
| 4 | Docker básico | Hello World dockerizado |
| 5 | K8s básico | Hello World corriendo en Minikube |
| 6 | Cloud Endpoints + plataforma Sodimac | Diagrama: cliente → Cloudflare → Envoy → BFF → backend |

---

## Por qué este orden

- Git primero porque CI/CD vive sobre git workflows.
- GitHub Actions antes que Docker porque es el "qué" antes del "cómo". El workflow llama a docker; entender el workflow primero da contexto.
- Docker antes que K8s porque K8s orquesta contenedores; sin Docker no se entiende K8s.
- Cloud Endpoints al final porque es específico de GCP/Sodimac, no es transferible a otros trabajos.

---

## Recursos adicionales (para después)

- **Terraform** — infra como código. Si Sodimac usa Terraform para GCP, vale la pena.
- **Helm** — package manager K8s. Útil si los charts existen en el repo.
- **Observabilidad**: Prometheus + Grafana + OpenTelemetry — para entender métricas/logs/traces de los pods.
- **SRE Book** (Google, gratis online) — filosofía de operar sistemas en producción.

---

## Estado actual (2026-05-15)

- ✓ Git básico — usa diario, sólido.
- ⚠ GitHub Actions — usa pero no entiende workflows. **Empezar aquí.**
- ✗ Docker — usa imágenes que otros construyen, no construye propias.
- ✗ K8s — sabe que existe, no opera.
- ✗ Cloud Endpoints — no entiende qué hace ni por qué.
