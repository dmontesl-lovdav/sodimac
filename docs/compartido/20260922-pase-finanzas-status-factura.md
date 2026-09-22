# Pase finanzas-api — status de factura en listReception (Josue) — 2026-09-22

**Qué:** expone `status` de la factura en el nodo `invoice` del `GET /purchase-orders/listReception(V2)`.
**Módulo:** `APP03022-mrch.backend.somx.finanzas-api`
**Archivo:** `src/entities/tenant_fiscal.invoice.entity.ts` (agrega `@Column status`)
**SQL:** ninguno (la columna `status` ya existe en `tenant_fiscal.invoice`).
**Commit mirror:** `f540770d`

---

## Correr en la PC de Sodimac (PowerShell)

```powershell
# --- 1. bajar mirror ---
cd C:\local
git checkout dmontes
git pull origin dmontes

# --- 2. copiar mirror -> repo real ---
$proj = "APP03022-mrch.backend.somx.finanzas-api"
robocopy "C:\local\$proj" "C:\workspace-fbc-github\$proj" /MIR /XD .git node_modules dist build coverage .idea /XF *.log

# --- 3. commit a develop ---
cd "C:\workspace-fbc-github\$proj"
git checkout develop
git pull origin develop
git status   # esperado: SOLO src/entities/tenant_fiscal.invoice.entity.ts
git add src/entities/tenant_fiscal.invoice.entity.ts
git commit -m "feat(finanzas): expone status de la factura en el nodo invoice de listReception(V2)"
git push origin develop

# --- 4. promover develop -> uat (dispara redeploy) ---
git checkout uat
git pull origin uat
git merge develop --no-ff -m "merge: status de factura en listReception (Josue)"
git push origin uat
```

---

## Notas
- En el paso 3, `git status` debe mostrar **solo** `tenant_fiscal.invoice.entity.ts`. Si aparece más (ej. `docs/`, otros archivos de finanzas), NO lo agregues — solo ese archivo.
- **Sin SQL** en UAT: la columna `status` ya existe en la tabla; solo faltaba mapearla en el entity.
- Si el pipeline de finanzas falla por el error de Artifact Registry (mismo del build de fiscal-api), es infra — re-correr / avisar a Bonelli. No es este cambio.

## Validar en UAT (cuando despliegue)

GET (sin body → sin problemas de comillas en cmd). Ajusta las fechas a un rango con recepciones que tengan addenda con factura.

```bash
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/purchase-orders/listReceptionV2?receptionDateAtInitial=2026-09-03&receptionDateAtEnd=2026-09-03&pageNumber=1&pageSize=15"
```

Con `jq` (para ubicar el campo rápido):
```bash
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/purchase-orders/listReceptionV2?receptionDateAtInitial=2026-09-03&receptionDateAtEnd=2026-09-03&pageNumber=1&pageSize=15" | jq ".[].listAddendum[].invoice | {fiscalUuid, folio, status}"
```

**Esperado:** cada `listAddendum[].invoice` debe incluir `"status": <n>` junto a `fiscalUuid`/`folio`/etc. Antes el nodo terminaba en `certificationDate`; ahora trae también `status` (estatus del tren de la factura).
