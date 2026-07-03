# Prueba UAT — Fila 122 (estatus de la NC acompaña a la factura)

> Runbook de validación en UAT del flujo completo de fila 104/122 tras el deploy (jar + catálogo E/F
> + migración + mensajes). Objetivo: ver la NC en **2 (Recibido Parcial)** mientras no cuadra, en
> **3 (En proceso de envío)** al cuadrar, y **11 (Cancelada)** en la cascada de rechazo.
> Hermano: [QA-104-NC-recalculo-tolerancia-estatus3.md](QA-104-NC-recalculo-tolerancia-estatus3.md).

## Datos base
- Factura de prueba: `fiscal_uuid = 31343515-7304-4d3e-a109-6a2102c69185` (Serie T122/CV, subtotal **9000**).
- Recepción: `reception_number = 999056`, **amount 5000**.
- Tolerancia UAT: 40 (monto). Emisor NC AIR130902MN1, receptor CSD161207R2A (autorizado), FormaPago 99, UsoCFDI G02.
- NC de prueba (referencian la factura por su fiscalUuid):
  - **NC-A** subtotal 2000 → neto 9000−2000 = 7000 (> 5000, fuera tol) → **sigue 2**.
  - **NC-B** subtotal 1980 → Σ 3980, neto 5020 (diff 20 ≤ 40) → **cuadra → 3**.
  - **NC-C** subtotal 4200 → neto 4800 (< 5000, fuera tol) → **cascada rechazo**.
- Host: `https://uat.fbusinesscenter.com/ppsomx/fiscal`. Headers: `x-tenant-id: 1`, `x-user-rfc: THE791105HP2`.

## Paso 0 — Prep (SQL): limpiar NC previas + factura a 2
```sql
DO $$ DECLARE r RECORD; BEGIN
 FOR r IN SELECT i.invoice_uuid FROM tenant_fiscal.invoice i
   JOIN tenant_fiscal.related_cfdi rc ON rc.invoice_uuid=i.invoice_uuid
   WHERE rc.related_invoice_uuid=(SELECT invoice_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185')
 LOOP
   DELETE FROM tenant_fiscal.related_cfdi WHERE invoice_uuid=r.invoice_uuid OR related_invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax_withholding WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice_status_history WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid=r.invoice_uuid;
 END LOOP; END $$;
UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
UPDATE tenant_finance.reception SET status=0 WHERE reception_number='999056';
```
XML: ver Paso 1 del runbook (PowerShell here-strings → `C:\log\test\ncA.xml`, `ncB.xml`, `ncC.xml`).

## Escenario 1 — parcial → cuadra

**1.A Registrar NC-A** (esperado: factura 2, NC-A **2 Recibido Parcial**)
```
curl.exe -s -S "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/register" -H "x-tenant-id: 1" -H "x-user-rfc: THE791105HP2" -F "file=@C:\log\test\ncA.xml" -F "idTransaccion=t122a" -F "supplierNumber=380204" -F "tipoNotaCredito=1" -w "`nHTTP %{http_code}`n"
```
**1.B Registrar NC-B** (esperado: factura **3**, NC-A y NC-B **3 En proceso de envío**)
```
curl.exe -s -S "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/register" -H "x-tenant-id: 1" -H "x-user-rfc: THE791105HP2" -F "file=@C:\log\test\ncB.xml" -F "idTransaccion=t122b" -F "supplierNumber=380204" -F "tipoNotaCredito=1" -w "`nHTTP %{http_code}`n"
```
**Verificar (SQL):**
```sql
SELECT document_type, folio, status FROM tenant_fiscal.invoice
WHERE fiscal_uuid IN ('31343515-7304-4d3e-a109-6a2102c69185',
 'd1230000-0000-0000-0000-0000000000a0','d1230000-0000-0000-0000-0000000000b0') ORDER BY document_type, folio;
```
Tras 1.A → factura I=2, NC 123A=2. Tras 1.B → factura I=3, NC 123A=3, 123B=3.

## Escenario 2 — cascada (sobre-corrige)

**2.0 Reset** (borrar NC-A/B + factura a 2):
```sql
DO $$ DECLARE r RECORD; BEGIN
 FOR r IN SELECT invoice_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid IN
   ('d1230000-0000-0000-0000-0000000000a0','d1230000-0000-0000-0000-0000000000b0') LOOP
   DELETE FROM tenant_fiscal.related_cfdi WHERE invoice_uuid=r.invoice_uuid OR related_invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax_withholding WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice_status_history WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid=r.invoice_uuid;
 END LOOP; END $$;
UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
UPDATE tenant_finance.reception SET status=0 WHERE reception_number='999056';
```
**2.A Sin confirmar** (esperado: HTTP 400 **WRN7034**, no registra, factura sigue 2)
```
curl.exe -s -S "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/register" -H "x-tenant-id: 1" -H "x-user-rfc: THE791105HP2" -F "file=@C:\log\test\ncC.xml" -F "idTransaccion=t122c1" -F "supplierNumber=380204" -F "tipoNotaCredito=1" -w "`nHTTP %{http_code}`n"
```
**2.B Confirmando** (esperado: factura **1**, NC-C **11**, recepción **0**)
```
curl.exe -s -S "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/register" -H "x-tenant-id: 1" -H "x-user-rfc: THE791105HP2" -F "file=@C:\log\test\ncC.xml" -F "idTransaccion=t122c2" -F "supplierNumber=380204" -F "tipoNotaCredito=1" -F "confirmarCancelacionNc=true" -w "`nHTTP %{http_code}`n"
```
**Verificar (SQL):**
```sql
SELECT document_type, folio, status FROM tenant_fiscal.invoice
WHERE fiscal_uuid IN ('31343515-7304-4d3e-a109-6a2102c69185','d1230000-0000-0000-0000-0000000000c0') ORDER BY document_type;
SELECT reception_number, status FROM tenant_finance.reception WHERE reception_number='999056';
```

## Verificación de labels (E/F) — opcional
Búsqueda por UUID de la NC → el `statusName` debe mostrar el label E/F ("Recibida Parcial" /
"En proceso de envio" / "Cancelada"):
```
curl.exe -s -S "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" -H "x-tenant-id: 1" -H "x-user-rfc: THE791105HP2" -H "Content-Type: application/json" -d "{\"rfcEmisor\":\"AIR130902MN1\",\"tipoDocumento\":\"E\",\"uuid\":\"d1230000-0000-0000-0000-0000000000a0\"}"
```

## Cleanup final
```sql
DO $$ DECLARE r RECORD; BEGIN
 FOR r IN SELECT invoice_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid IN
  ('d1230000-0000-0000-0000-0000000000a0','d1230000-0000-0000-0000-0000000000b0','d1230000-0000-0000-0000-0000000000c0') LOOP
   DELETE FROM tenant_fiscal.related_cfdi WHERE invoice_uuid=r.invoice_uuid OR related_invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax_withholding WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice_status_history WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid=r.invoice_uuid;
 END LOOP; END $$;
UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
```

## Estatus de la prueba — VALIDADO UAT 2026-07-03 ✅
| Paso | Escenario | Esperado | Estatus |
|---|---|---|---|
| 1.A | NC-A (2000) | factura 2, NC-A **2** | ✅ OK |
| 1.B | NC-B (1980) | factura **3**, NC-A/B **3** | ✅ OK |
| 2.A | NC-C sin confirmar | **WRN7034** 400, no registra | ✅ OK |
| 2.B | NC-C confirmando | factura **1**, NC **11**, recep **0** | ✅ OK |

Todo el flujo fila 104/122 con catálogo E/F quedó validado en UAT con datos reales. El mensaje
WRN7034 salió desde `core_utils.cat_message` (sembrado). Deploy completo: jar + catálogo E/F +
migración +2 + mensajes.

### Contexto del deploy (2026-07-03)
- BD UAT: catálogo `CatEstatusNotaCredito` E/F (1-12) ✔, migración +2 NC ✔, mensajes en
  `core_utils.cat_message` (WRN7030-7034, BUS3103) ✔.
- Jar fila 122: desplegado ✔.
- Pendiente aparte (avisado a Ivan): tren del PUT manual de NC (`shared_catalogs.status_train`
  option_id=2 + enum `CreditNoteStatus`) sigue desalineado — revisar por separado.
