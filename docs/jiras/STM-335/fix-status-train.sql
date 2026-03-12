-- =============================================================================
-- STM-335: Agregar transiciones de cancelacion al tren de estatus de NC
-- Tabla: shared_catalogs.status_train
-- option_id = 2 (Nota de Credito)
-- Fecha: 2026-03-11
-- =============================================================================

-- Verificar estado actual (antes)
SELECT source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 2
ORDER BY source_status, target_status;

-- Agregar transiciones para cancelacion (estatus 10)
-- Solo estatus 3 (Pendiente de Contabilizar) y 11 (Rechazo Contable) pueden cancelar
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES
    (2, 3, 10, 1),   -- Pendiente de Contabilizar -> Cancelada
    (2, 11, 10, 1)   -- Rechazo Contable -> Cancelada
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;

-- Verificar estado final (despues)
SELECT
    st.source_status,
    CASE st.source_status
        WHEN 1 THEN 'Pendiente Addenda'
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pend. Contabilizar'
        WHEN 4 THEN 'Proceso descarga'
        WHEN 5 THEN 'Desglose NC'
        WHEN 6 THEN 'Pend. envio contab.'
        WHEN 7 THEN 'Aplicado'
        WHEN 11 THEN 'Rechazo Contable'
    END AS source_nombre,
    st.target_status,
    CASE st.target_status
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pend. Contabilizar'
        WHEN 4 THEN 'Proceso descarga'
        WHEN 5 THEN 'Desglose NC'
        WHEN 6 THEN 'Pend. envio contab.'
        WHEN 7 THEN 'Aplicado'
        WHEN 8 THEN 'Completado'
        WHEN 10 THEN 'Cancelada'
        WHEN 11 THEN 'Rechazo Contable'
    END AS target_nombre
FROM shared_catalogs.status_train st
WHERE st.option_id = 2
ORDER BY st.source_status, st.target_status;
