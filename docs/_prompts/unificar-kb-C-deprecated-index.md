# Prompt C — Deprecated + INDEX (cirugía precisa)

WORKSPACE: `C:\workspace-sodimac\`

## Objetivo

Marcar `auditoria-api` / `catalogos-api` como deprecated SOLO donde verificado. Actualizar `docs/INDEX.md` con módulos + estado.

## Pasos

### 1. Verificar deprecación real

- `ls APP03022-mrch.backend.somx.auditoria-api/` → ¿existe carpeta?
- `ls APP03022-mrch.backend.somx.catalogos-api/` → ¿existe carpeta?
- `grep -r "auditoria-api" APP03022-mrch.backend.somx.util-api/src/` → ¿util-api absorbió?
- `grep -r "catalogos-api" APP03022-mrch.backend.somx.util-api/src/` → idem

Si carpeta existe Y código activo → NO deprecated. Reportar y abortar paso 2.

### 2. Si deprecación confirmada

Agregar advertencia en archivos:
- `docs/INDEX.md`
- `docs/ENTORNO-LOCAL.md`
- `docs/BASE-DE-DATOS.md`
- `docs/arquitectura/` (cualquier `.md` que mencione)

Formato: `~~auditoria-api~~ → migrado a util-api (commit XXX)` con referencia commit real.

### 3. Actualizar `docs/INDEX.md`

Sección "Módulos (estado actual)" — tabla módulo / puerto / estado / repo:

```markdown
## Módulos (estado actual)

| Módulo | Puerto | Estado | Repo |
|--------|--------|--------|------|
| fiscal-api | 8082 | ✅ Activo | APP03022-mrch.backend.somx.fiscal-api |
| util-api | 3712 | ✅ Activo (absorbió auditoria + catalogos) | APP03022-mrch.backend.somx.util-api |
| finanzas-api | 3001 | ✅ Activo | APP03022-mrch.backend.somx.finanzas-api |
| ~~auditoria-api~~ | 8091 | ❌ DEPRECATED | migrado a util-api |
| ~~catalogos-api~~ | 8083 | ❌ DEPRECATED | migrado a util-api |
| batch.fiscal-download | — | ✅ Activo (Java 8) | APP03022-mrch.batch.somx.fiscal-download |
```

### 4. Nota sesiones

Agregar en `docs/INDEX.md`:

```markdown
## Sesiones de trabajo

`sesiones/` — assets temporales (SQL ad-hoc, payloads, capturas). No es doc permanente.
Para info perdurable: `docs/jiras/STM-XXX/`.
```

## Restricciones

- NO marcar deprecated sin verificación en paso 1
- NO tocar `wiki/`
- NO mover archivos
- 1 commit único al final con resumen de cambios

## Output

Resumen ejecutivo en respuesta:
- ¿Deprecación confirmada? sí/no + evidencia
- Archivos modificados (lista)
- INDEX.md secciones agregadas

## Commit

`docs: marcar módulos deprecated + actualizar INDEX con módulos y sesiones`
