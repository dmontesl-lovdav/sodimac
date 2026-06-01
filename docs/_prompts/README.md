# Prompts unificar KB

Plan ejecución secuencial. Cada prompt = 1 sesión con commit propio.

## Orden

1. **[A — Inventario JIRAs](unificar-kb-A-inventario-jiras.md)** — read-only, genera tabla 63 JIRAs
   - Output: `docs/jiras/_inventario-2026-05-12.md`
   - **Revisar humano antes de B**
2. **[B — Audit memory](unificar-kb-B-audit-memory.md)** — read-only, side-by-side memory vs docs
   - Output: `docs/_audit-memory-2026-05-12.md`
   - **Revisar humano antes de C**
3. **[C — Deprecated + INDEX](unificar-kb-C-deprecated-index.md)** — cirugía con verificación previa

## Pendiente (post A/B/C)

- Mover JIRAs cerrados a `historicos/` (decidir tras revisar inventario A)
- Reducir memory entries (decidir tras revisar audit B)
- Llenar 7 procesos wiki pendientes (alta proveedor, OC, recepción, REP, NC, estado cuenta, bloqueo)

## Restricciones globales

- NO tocar `docs/wiki/` estructura
- NO reducir `feedback_*.md` en memory
- NO tocar `docs/soporte/` (historial por persona)
- NO tocar `docs/jiras/_TEMPLATE/`
- 1 commit por prompt
