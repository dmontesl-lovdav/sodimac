-- ============================================================
-- Fila 122 - Migración de estatus de NC al modelo nuevo E/F (+2)
-- Mapea la numeración vieja de CatEstatusNotaCredito a la nueva:
--   viejo 1 (En proceso envío)   -> 3
--   viejo 2 (Pendiente Contab)   -> 4  ... viejo 9 (Cancelada) -> 11, viejo 10 (Borrada) -> 12
-- ⚠️ CORRER ANTES/JUNTO con el deploy del código fila 122 (el código nuevo ya escribe 2/3/11;
--    si se corre el +2 sobre NC ya nuevas, se corrompen).
-- ============================================================
UPDATE tenant_fiscal.invoice
SET status = status + 2
WHERE document_type = 'E' AND status BETWEEN 1 AND 10;
