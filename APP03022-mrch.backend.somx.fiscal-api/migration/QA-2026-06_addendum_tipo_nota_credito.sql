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
ADD COLUMN IF NOT EXISTS tipo_nota_credito VARCHAR(10);

COMMENT ON COLUMN tenant_fiscal.addendum.tipo_nota_credito IS 'Tipo de NC (CatTipoNotaCredito): 1=Ajuste por Recepcion, 2=Descuento Comercial. Solo NC. QA 2026-06';

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
