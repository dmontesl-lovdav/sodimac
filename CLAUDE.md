# Sodimac B2B Portal — Claude Code instructions

> Mapa para agentes Claude. Punteros a documentacion en `docs/`. Versionado en git, viaja con cualquier clone.

## Contexto proyecto

Portal B2B de Sodimac Mexico — facturacion / finanzas / acuerdos comerciales con proveedores.

- Cliente: Sodimac (subsidiaria Falabella MX)
- Rama trabajo: `dmontes`
- 12 modulos: `APP03022-mrch.*` (backend / bff / frontend / batch)
- Workflow: desarrollo desde PC personal (Claude bloqueado en Sodimac), sync via GitHub

---

## Punto de entrada negocio

→ **[docs/wiki/README.md](docs/wiki/README.md)** — mapa procesos + glosario + donde-vive-que

Hermano operativo: [docs/INDEX.md](docs/INDEX.md) — indice maestro.

---

## Modulos activos

| Modulo | Puerto local | Tech | Doc |
|---|---|---|---|
| fiscal-api | 8082 | Spring Boot 3.4.2 / Java 17 | [wiki/modulos/fiscal.md](docs/wiki/modulos/fiscal.md) |
| finanzas-api | 3001 | Node.js / TypeScript / TypeORM | [wiki/modulos/finanzas.md](docs/wiki/modulos/finanzas.md) |
| util-api | 3712 | Node.js / TS — absorbio auditoria + catalogos | — |
| aclaraciones-api | 8082 | Spring Boot 3.4.2 / Java 17 | — |
| batch.fiscal-download | — | Java 8 / Spring Boot 2.7 / SQL Server | — |

BFFs (Express, proxy transparente, JWT deshabilitado local):
- `bff.fiscal` :3000 → fiscal-api
- `bff.finanzas` :3000 → finanzas-api  (en UAT `/ppsomx/backend-finanzas/`)
- `bff.ppsomx.util` → util-api
- `bff.ppsomx` generico

Frontends (React 18 / TS / Webpack / single-spa):
- `aclaraciones-spa` :3701
- `finanzas-spa` :3702
- `fiscal.spa` :3703
- `util.spa` :3700+

Deprecated:
- ~~auditoria-api~~ → migrado a util-api
- ~~catalogos-api~~ → migrado a util-api

---

## Donde buscar X

| Pregunta | Doc |
|---|---|
| Endpoints UAT + hosts | [docs/ENTORNO-UAT.md](docs/ENTORNO-UAT.md) |
| Curls validados UAT | [docs/CURLS-VALIDADOS-UAT.md](docs/CURLS-VALIDADOS-UAT.md) |
| Conexion BD local + UAT | [docs/BASE-DE-DATOS.md](docs/BASE-DE-DATOS.md) |
| Setup local (Docker + servicios) | [docs/ENTORNO-LOCAL.md](docs/ENTORNO-LOCAL.md) |
| Comandos git utiles | [docs/GIT-COMANDOS.md](docs/GIT-COMANDOS.md) |
| BFF fiscal guia tecnica | [docs/GUIA-TECNICA-BFF-FISCAL.md](docs/GUIA-TECNICA-BFF-FISCAL.md) |
| Bugs activos por modulo | `docs/wiki/modulos/<modulo>.md` → seccion "Puntos criticos" |
| Errores conocidos | [docs/errores/](docs/errores/) |
| Troubleshooting Envoy / gateway | [docs/wiki/troubleshooting-envoy.md](docs/wiki/troubleshooting-envoy.md) |
| Pipeline GitHub Actions | [docs/wiki/pipeline-actions.md](docs/wiki/pipeline-actions.md) |
| JIRAs | [docs/jiras/INDEX.md](docs/jiras/INDEX.md) → `STM-XXX/README.md` |
| Soporte por persona | `docs/soporte/<nombre>.md` (fer, ivan, jose-luis, josue, bonelli, eli) |
| Procesos negocio (CFDI, 3WM, pagos, etc) | [docs/wiki/procesos/](docs/wiki/procesos/) |
| Analisis tecnicos | [docs/analisis/](docs/analisis/) |
| Arquitectura + ER | [docs/arquitectura/](docs/arquitectura/) |
| Planes migracion / pruebas | [docs/planes/](docs/planes/) |
| Asset SQL / payload sesion | `sesiones/` (temporal, no permanente) |
| Prompts unificar KB | [docs/_prompts/](docs/_prompts/) |

---

## Convenciones

### JIRAs
- Cada JIRA vive en `docs/jiras/STM-XXX/`
- `README.md` = transcripcion fiel del XML (no analizar dentro)
- Analisis aparte en archivos hermanos
- Template: `docs/jiras/_TEMPLATE/`
- Indice: `docs/jiras/INDEX.md`

### Documentar bugs
- Bug activo → `docs/wiki/modulos/<modulo>.md` seccion "Puntos criticos"
- Bug resuelto → mismo lugar con fecha + commit fix

### Soporte
- Cada persona tiene archivo `docs/soporte/<nombre>.md`
- Historial cronologico (mas reciente arriba)

### Commits
- Rama: `dmontes`
- Mensaje: convencional commits (`feat:`, `fix:`, `docs:`)
- Co-Authored-By: Claude cuando agente contribuyo

### Naming env vars
- k8s: `DATASOURCE_URL`, `UTILS_API_URL` (mayusculas, plural)
- Local: `DB_HOST`, `UTIL_API_URL` (mas corto)
- Fallback encadenado en `application.properties` base

---

## Hosts UAT (Sodimac)

| Servicio | URL |
|---|---|
| BFF fiscal | `https://uat.fbusinesscenter.com/ppsomx/fiscal/` |
| BFF finanzas | `https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/` |
| BFF util | `https://uat.fbusinesscenter.com/ppsomx/backend-util/` |

`uat.vendor.fbusinesscenter.com` existe en ingress k8s pero requiere VPN/cert — usar siempre `uat.fbusinesscenter.com`.

Trampa: paths inexistentes responden 200 con HTML (SPA catchall). Validar `Content-Type` siempre.

---

## Bases de datos

### Postgres local
- Container: `sodimac-pg` (Docker)
- Puerto: 5434
- DB: `b2b_portal` | User: `wwwb2bportal`
- Schemas: `tenant_fiscal`, `tenant_finance`, `shared_catalogs`, `core_audit`, `core_utils`, `public`

### SQL Server local
- Container: `sodimac-mssql`
- Puerto: 1433
- DB: `SODIMAC_BATCH_DEV` | User: `SA`

### Sodimac UAT (real)
- Postgres: `10.100.64.102:5432` (IAP SSH)
- SQL Server: `10.138.150.124:5319`

Detalle: [docs/BASE-DE-DATOS.md](docs/BASE-DE-DATOS.md).

---

## Documentos al dia

| Doc | Ultima actualizacion |
|---|---|
| docs/wiki/modulos/finanzas.md | 2026-05-12 (bugs rutas + query params) |
| docs/ENTORNO-UAT.md | 2026-05-12 |
| docs/wiki/troubleshooting-envoy.md | 2026-05-12 |
| docs/CURLS-VALIDADOS-UAT.md | 2026-05-12 |

---

## Restricciones de cambios

- NO tocar `docs/wiki/` estructura (esta bien armada)
- NO tocar `docs/jiras/_TEMPLATE/`
- NO tocar `docs/soporte/` (historial por persona, append-only)
- `sesiones/` → no eliminar, solo agregar
