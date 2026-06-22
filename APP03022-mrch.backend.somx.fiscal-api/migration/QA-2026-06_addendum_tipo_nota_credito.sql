-- ============================================================================
-- QA junio-2026: Agregar columna tipo_nota_credito a tabla addendum
-- ============================================================================
-- Descripcion: Almacena el tipo de nota de credito que el front envia al publicar
--              una NC (catalogo CatTipoNotaCredito): 1 = Ajuste por Recepcion,
--              2 = Descuento Comercial. Solo aplica a notas de credito.
--
-- Autor: Sodimac Tech Team
-- Fecha: 2026-06-22
-- QA: Listado Issue Abiertos filas 16 / 43
-- ============================================================================

ALTER TABLE tenant_fiscal.addendum
ADD COLUMN IF NOT EXISTS tipo_nota_credito VARCHAR(10) DEFAULT '0';

COMMENT ON COLUMN tenant_fiscal.addendum.tipo_nota_credito IS 'Tipo de NC (CatTipoNotaCredito): 0=Factura (no aplica), 1=Ajuste por Recepcion, 2=Descuento Comercial. QA 2026-06';

-- Default 0 en facturas (decision Ivan 2026-06-22): la addenda aplica a facturas y NC; en
-- facturas el tipo de NC no aplica y queda en 0 (no null).
ALTER TABLE tenant_fiscal.addendum ALTER COLUMN tipo_nota_credito SET DEFAULT '0';

-- Backfill addendas existentes de facturas (document_type 'I') que quedaron en null -> 0.
UPDATE tenant_fiscal.addendum a
SET tipo_nota_credito = '0'
FROM tenant_fiscal.invoice i
WHERE a.invoice_uuid = i.invoice_uuid
  AND i.document_type = 'I'
  AND a.tipo_nota_credito IS NULL;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
