# Prompt B — Memory audit (dry-run, read-only)

WORKSPACE: `C:\workspace-sodimac\`

## Objetivo

Side-by-side memory vs docs equivalentes. Reporte para decidir qué reducir. NO modifica memory.

## Pasos

1. Lista memory: `ls C:\Users\dmont\.claude\projects\c--workspace-sodimac\memory\*.md` (excluir `MEMORY.md`)
2. Para cada entry, identificar doc candidato:

| Memory entry | Doc candidato |
|---|---|
| `project_architecture.md` | `docs/ENTORNO-LOCAL.md` |
| `database-setup.md` | `docs/BASE-DE-DATOS.md` |
| `reference_sodimac_dev_connections.md` | `docs/BASE-DE-DATOS.md` + `docs/ENTORNO-UAT.md` |
| `reference_study_plan_devops.md` | `docs/aprendizaje/devops-cicd.md` |
| `reference_uat_gcp_connection.md` | `docs/ENTORNO-UAT.md` |
| (resto) | candidato = ninguno |

3. Para cada par memory+doc:
   - Leer ambos completos
   - Extraer secciones (headers)
   - Comparar: % overlap, contenido único memory, contenido único doc
4. Generar reporte

## Output

`docs/_audit-memory-2026-05-12.md`

Formato por entry:

```markdown
## project_architecture.md vs docs/ENTORNO-LOCAL.md

**Overlap estimado**: 70%

**Secciones memory NO cubiertas en doc**:
- Estado actual servicios (texto: "...")
- Decisiones técnicas históricas

**Secciones doc NO cubiertas en memory**:
- Comandos docker-compose
- Vars entorno detalladas

**Recomendación**: REDUCIR / MANTENER / FUSIONAR
```

Cierre con tabla resumen:

| Memory entry | Doc candidato | Overlap | Recomendación |

## Restricciones

- NO editar memory ni docs
- NO tocar `feedback_*.md` (skip)
- NO tocar `user_role.md` (skip)
- NO tocar `project_*_epic` / `*_active` (skip si referencia jira activo)

## Commit

`docs: auditoría memory vs docs (reporte dry-run)`
