-- ============================================================================
-- STM-393: Columna sent_date en tabla invoice
-- Servicio: fiscal-api
-- Fecha: 2025-01-06
-- Descripcion: Agregar campo de fecha de envio a documentos fiscales
-- ============================================================================

-- ============================================================================
-- VERIFICACION PREVIA
-- ============================================================================

-- Verificar estructura actual de la tabla invoice
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'tenant_fiscal'
  AND table_name = 'invoice'
ORDER BY ordinal_position;

-- Verificar si la columna ya existe
SELECT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'tenant_fiscal'
      AND table_name = 'invoice'
      AND column_name = 'sent_date'
) as columna_existe;

-- ============================================================================
-- DDL: Agregar columna sent_date
-- ============================================================================

-- Agregar columna (solo si no existe)
ALTER TABLE tenant_fiscal.invoice
ADD COLUMN IF NOT EXISTS sent_date TIMESTAMP;

-- Comentario de la columna
COMMENT ON COLUMN tenant_fiscal.invoice.sent_date
IS 'Fecha y hora de envio del documento a contabilizar (STM-393)';

-- ============================================================================
-- INDICE: Para consultas por fecha de envio
-- ============================================================================

-- Crear indice (solo si no existe)
CREATE INDEX IF NOT EXISTS idx_invoice_sent_date
ON tenant_fiscal.invoice (sent_date)
WHERE sent_date IS NOT NULL;

-- ============================================================================
-- MIGRACION DE DATOS EXISTENTES (Opcional)
-- ============================================================================

-- Opcion 1: Establecer sent_date = updated_at para facturas ya enviadas (status >= 6)
-- ADVERTENCIA: Ejecutar solo si se desea poblar datos historicos
-- Esta operacion puede tardar dependiendo del volumen de datos

-- Contar registros afectados
SELECT
    status,
    COUNT(*) as cantidad,
    COUNT(CASE WHEN sent_date IS NULL THEN 1 END) as sin_fecha_envio
FROM tenant_fiscal.invoice
WHERE status >= 6
GROUP BY status
ORDER BY status;

-- Actualizar registros (OPCIONAL - descomentar para ejecutar)
-- UPDATE tenant_fiscal.invoice
-- SET sent_date = COALESCE(updated_at, created_at)
-- WHERE status >= 6
--   AND sent_date IS NULL;

-- ============================================================================
-- VERIFICACION POST-DDL
-- ============================================================================

-- Verificar columna agregada
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'tenant_fiscal'
  AND table_name = 'invoice'
  AND column_name = 'sent_date';

-- Verificar indice creado
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'tenant_fiscal'
  AND tablename = 'invoice'
  AND indexname = 'idx_invoice_sent_date';

-- Verificar datos migrados (si se ejecuto la migracion)
SELECT
    CASE WHEN sent_date IS NULL THEN 'Sin fecha envio' ELSE 'Con fecha envio' END as estado,
    COUNT(*) as cantidad
FROM tenant_fiscal.invoice
WHERE status >= 6
GROUP BY CASE WHEN sent_date IS NULL THEN 'Sin fecha envio' ELSE 'Con fecha envio' END;

-- Muestra de registros con sent_date
SELECT
    invoice_uuid,
    fiscal_uuid,
    document_type,
    series,
    folio,
    status,
    created_at,
    updated_at,
    sent_date
FROM tenant_fiscal.invoice
WHERE sent_date IS NOT NULL
ORDER BY sent_date DESC
LIMIT 10;

-- ============================================================================
-- ROLLBACK (en caso de ser necesario)
-- ============================================================================

-- DROP INDEX IF EXISTS tenant_fiscal.idx_invoice_sent_date;
-- ALTER TABLE tenant_fiscal.invoice DROP COLUMN IF EXISTS sent_date;

-- ============================================================================
-- NOTAS DE IMPLEMENTACION
-- ============================================================================

-- 1. Actualizar InvoiceEntity.java:
--    @Column(name = "sent_date")
--    private LocalDateTime sentDate;

-- 2. Actualizar InvoiceSearchResponse.java:
--    @Schema(description = "Fecha y hora de envio del documento")
--    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
--    private LocalDateTime sentDate;

-- 3. Actualizar InvoiceServiceImpl.mapToSearchResponse():
--    .sentDate(invoice.getSentDate())

-- 4. Actualizar logica de cambio de estatus:
--    if (newStatus == 6 && invoice.getSentDate() == null) {
--        invoice.setSentDate(LocalDateTime.now());
--    }

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
