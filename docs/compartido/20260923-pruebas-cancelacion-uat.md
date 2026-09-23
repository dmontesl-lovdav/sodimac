# Pruebas UAT — cancelacion NC/factura (Ivan) — 2026-09-23

Ya desplegado en UAT (Pipeline #223 verde). Endpoint: `PUT /invoices/{fiscal_uuid}/status`
(el `PUT /invoices` da 403 por seguridad; usar el de `/status`). El `uuid` de la URL
es el **fiscal_uuid** (folio), NO el invoice_uuid interno.

Reglas a validar:
- Cancelar NC  -> factura relacionada Y sus NCs asociadas activas -> 2 (Recibido Parcial). SIEMPRE.
- Cancelar factura (Recibido Parcial) -> NCs relacionadas -> 20 (Cancelada) + recepcion -> 0 (Disponible) (+ guia -> 2 si transporte).

==========================================================================
1) Buscar candidatos (SQL en la BD de UAT)
==========================================================================
-- factura con >=2 NCs relacionadas activas
SELECT f.fiscal_uuid AS fac_fiscal, f.invoice_uuid AS fac_uuid, f.status AS fac_st,
       a.supplier_number AS prov,
       nc.fiscal_uuid AS nc_fiscal, nc.invoice_uuid AS nc_uuid, nc.status AS nc_st
FROM tenant_fiscal.related_cfdi rc
JOIN tenant_fiscal.invoice nc ON nc.invoice_uuid = rc.invoice_uuid
JOIN tenant_fiscal.invoice f  ON f.invoice_uuid  = rc.related_invoice_uuid
LEFT JOIN tenant_fiscal.addendum a ON a.invoice_uuid = nc.invoice_uuid
WHERE nc.document_type = 'E' AND nc.status <> 20
ORDER BY f.fiscal_uuid;

==========================================================================
2) PUNTO 1 - cancelar una NC (factura + demas NCs -> 2)
==========================================================================
:: (cmd) sustituir <NC_FISCAL_UUID>, <NC_STATUS_ACTUAL>, <PROV>
curl -s -X PUT "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/<NC_FISCAL_UUID>/status" -H "Content-Type: application/json" -d "{\"estatusOrigen\":<NC_STATUS_ACTUAL>,\"estatusDestino\":20,\"numeroProveedor\":<PROV>,\"idUsuarioActualizacion\":\"11111111-1111-1111-1111-111111111111\"}"

Esperado: NC -> 20, factura -> 2, las otras NCs -> 2.

==========================================================================
3) PUNTO 2 - cancelar la factura (NCs -> 20 + recepcion 0)
==========================================================================
:: la factura debe estar en 2 (Recibido Parcial); si no, ponerla:
::   UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='<FAC_FISCAL_UUID>';
curl -s -X PUT "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/<FAC_FISCAL_UUID>/status" -H "Content-Type: application/json" -d "{\"estatusOrigen\":2,\"estatusDestino\":20,\"numeroProveedor\":<PROV>,\"idUsuarioActualizacion\":\"11111111-1111-1111-1111-111111111111\"}"

Esperado: factura -> 20, todas las NCs -> 20, recepcion -> 0.

==========================================================================
4) Verificar (SQL)
==========================================================================
SELECT invoice_uuid, document_type, status FROM tenant_fiscal.invoice
WHERE invoice_uuid IN ('<FAC_UUID>', '<NC1_UUID>', '<NC2_UUID>');

-- recepcion:
SELECT r.status FROM tenant_fiscal.addendum a
JOIN tenant_finance.reception r ON TRIM(r.reception_number) = TRIM(a.reception_number)
WHERE a.invoice_uuid = '<FAC_UUID>';

==========================================================================
Mensaje para Ivan (cuando pase la validacion)
==========================================================================
Ivan, ya desplegado y probado en UAT los dos puntos de cancelacion:
- Cancelar NC -> la factura relacionada y sus NCs asociadas pasan a Recibido Parcial (siempre).
- Cancelar factura -> sus NCs relacionadas pasan a Cancelado y la recepcion se libera a Disponible
  (ademas de la guia a Pendiente de Facturar en transporte).
Quedo en UAT por si lo quieres validar de tu lado.
