# Prompt A — Inventario JIRAs (cheap, read-only)

WORKSPACE: `C:\workspace-sodimac\`

## Objetivo

Generar inventario completo de `docs/jiras/STM-*/` sin clasificar ni mover. Output único para revisión humana.

## Pasos

1. `ls docs/jiras/` → lista todas carpetas STM-XXX (excluir `_TEMPLATE`, `devops`, `INDEX.md`, `*.txt`, `historicos`)
2. Para cada STM-XXX:
   - Leer primeras 30 líneas de `README.md` (o `MXSTM-*.md` si no hay README)
   - Extraer: estado declarado (Completado / En desarrollo / Pendiente / Análisis), módulo (fiscal-api / finanzas-api / util-api / batch / etc), fecha si aparece
   - `git log --oneline -1 -- docs/jiras/STM-XXX/` → último commit
   - `ls docs/jiras/STM-XXX/` → archivos presentes (postman, sql, curl, xml, retro)
3. Cruce: `ls sesiones/STM-XXX/` 2>/dev/null → marcar si tiene assets de sesión paralelos
4. Cruce memory: grep `memory/*.md` por `"STM-XXX"` → marcar si tiene entrada memory

## Output

Archivo único: `docs/jiras/_inventario-2026-05-12.md`

Formato tabla markdown:

| JIRA | Módulo | Estado README | Último commit | Archivos | Sesión? | Memory? |
|------|--------|---------------|---------------|----------|---------|---------|
| STM-272 | fiscal-api | Completado | 2026-03-27 | README, postman | - | - |
| STM-1403 | seguridad | En desarrollo | 2026-05-08 | README, xml | sesiones/security_schema.sql | project_stm1403_epic.md |

Cierre archivo con totales:
- Total JIRAs
- Breakdown por estado declarado
- Cuántos tienen sesión paralela
- Cuántos están en memory

## Restricciones

- READ-ONLY. No crear/mover/editar nada salvo `_inventario-2026-05-12.md`
- No interpretar — solo extraer lo que el README dice literal
- No tocar wiki ni memory

## Commit

`docs(jiras): inventario completo para triage`
