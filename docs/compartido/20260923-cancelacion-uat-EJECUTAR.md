# EJECUTAR en UAT — cancelacion NC/factura con datos reales — 2026-09-23

Factura 95d1a84a (prov 252338) con 2 NCs. Endpoint PUT /invoices/{fiscal_uuid}/status.
OJO: en el tren no existe 5->20, por eso se pone el origen cancelable antes
(NC desde 3, factura desde 2). cmd: comillas escapadas.

Datos:
  FAC  fiscal=b6828aef-75b9-4813-862b-3319cd7ea122  uuid=95d1a84a-14d9-487d-a9ef-7dad25334be5  prov=252338
  NC1  fiscal=0665d251-206f-4d1c-8b89-03c2087a62d5  uuid=02057fcc-5220-499e-b641-645eb1a4f2e9
  NC2  fiscal=a252062a-bd50-49c6-86c9-800c3bf3c0bf  uuid=1a2d6183-1650-4647-bf70-1775d903de5e

==========================================================================
PUNTO 1 - cancelar NC1 (factura + NC2 -> 2)
==========================================================================
-- SQL: origen cancelable para NC1
UPDATE tenant_fiscal.invoice SET status=3 WHERE invoice_uuid='02057fcc-5220-499e-b641-645eb1a4f2e9';

:: curl (cmd)
curl -s -X PUT "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/0665d251-206f-4d1c-8b89-03c2087a62d5/status" -H "Content-Type: application/json" -d "{\"estatusOrigen\":3,\"estatusDestino\":20,\"numeroProveedor\":252338,\"idUsuarioActualizacion\":\"11111111-1111-1111-1111-111111111111\"}"

-- verificar: NC1=20, factura=2, NC2=2
SELECT invoice_uuid, document_type, status FROM tenant_fiscal.invoice
WHERE invoice_uuid IN ('95d1a84a-14d9-487d-a9ef-7dad25334be5','02057fcc-5220-499e-b641-645eb1a4f2e9','1a2d6183-1650-4647-bf70-1775d903de5e');

==========================================================================
PUNTO 2 - cancelar la factura (NCs -> 20 + recepcion 0)
==========================================================================
-- SQL: reset factura en 2 (Recibido Parcial), NCs activas (3)
UPDATE tenant_fiscal.invoice SET status=2 WHERE invoice_uuid='95d1a84a-14d9-487d-a9ef-7dad25334be5';
UPDATE tenant_fiscal.invoice SET status=3 WHERE invoice_uuid IN ('02057fcc-5220-499e-b641-645eb1a4f2e9','1a2d6183-1650-4647-bf70-1775d903de5e');

:: curl (cmd)
curl -s -X PUT "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/b6828aef-75b9-4813-862b-3319cd7ea122/status" -H "Content-Type: application/json" -d "{\"estatusOrigen\":2,\"estatusDestino\":20,\"numeroProveedor\":252338,\"idUsuarioActualizacion\":\"11111111-1111-1111-1111-111111111111\"}"

-- verificar: factura=20, NC1=20, NC2=20
SELECT invoice_uuid, document_type, status FROM tenant_fiscal.invoice
WHERE invoice_uuid IN ('95d1a84a-14d9-487d-a9ef-7dad25334be5','02057fcc-5220-499e-b641-645eb1a4f2e9','1a2d6183-1650-4647-bf70-1775d903de5e');

-- recepcion -> 0
SELECT r.status FROM tenant_fiscal.addendum a
JOIN tenant_finance.reception r ON TRIM(r.reception_number)=TRIM(a.reception_number)
WHERE a.invoice_uuid='95d1a84a-14d9-487d-a9ef-7dad25334be5';

==========================================================================
Orden: Punto 1 -> verificar -> Punto 2 -> verificar.
Si un curl da BUS051, revisa que corriste el UPDATE de origen antes.
El uuid de la URL es el fiscal_uuid (folio), no el invoice_uuid.
