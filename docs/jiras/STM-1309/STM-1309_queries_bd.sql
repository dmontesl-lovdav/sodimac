-- STM-1309 — Queries de validación por escenario (del Panel de Queries del jira)
-- Sistemas: Sodimac SAP (SQL Server), SAPITO (Oracle), i213 (SQL Server AdmIF213ProdDB)
-- Reemplazar :proveedor / :uuid / :documento por valores reales.

------------------------------------------------------------
-- Escenario 1 — Estatus 6 → 7 (Pendiente registro en SAPITO)
-- Sistema: Sodimac SAP (SODIMAC_SAP_DEV). Si existe registro → cambiar a 7.
------------------------------------------------------------
SELECT COUNT(1)
FROM Envios_Ap
WHERE CODIGO_PROVEEDOR = :proveedor
  AND NUMERO_UUID = :uuid;

------------------------------------------------------------
-- Escenario 2 — Estatus 7 → 8 (Pendiente envío a i213)
-- Sistema: SAPITO (Oracle). FLAG_ENVIADO IN (0) pendiente → cambiar a 8.
------------------------------------------------------------
SELECT COUNT(1)
FROM Envios_Ap
WHERE CODIGO_PROVEEDOR = :proveedor
  AND NUMERO_UUID = :uuid
  AND FLAG_ENVIADO IN (0);

------------------------------------------------------------
-- Escenario 3 — Estatus 8 → 9 / 16 (Factura enviada a i213)
-- Sistema: SAPITO (Oracle). FLAG_ENVIADO IN (1) → 9; si no enviado → flag=2 → estatus 16.
------------------------------------------------------------
SELECT COUNT(1)
FROM Envios_Ap
WHERE CODIGO_PROVEEDOR = :proveedor
  AND NUMERO_UUID = :uuid
  AND FLAG_ENVIADO IN (1);

------------------------------------------------------------
-- Escenario 4 — Estatus 9 → 10 / 13 (Pendiente contabilizar en SAP)
-- Sistema: i213. SP devuelve CODE: 1=contabilizado → 10 ; 0=rechazo contable → 13.
------------------------------------------------------------
EXEC i123_Valida_Documento_AP :proveedor, :documento;

------------------------------------------------------------
-- Escenario 5 — Estatus 10 → 11 (Pendiente Pago)
-- Sistema: i213. SP devuelve CODE: 1=pagado → 11 ; 0=no cambiar.
------------------------------------------------------------
EXEC i213_Valida_Documento_Pagado_AP :proveedor, :documento;

------------------------------------------------------------
-- Control / trazabilidad (SODIMAC_BATCH_DEV) — según spec del jira
-- NOTA: el jira pide CtrlProcesoCab/Det/Elemento/ctrlLog.
--       invoice-sync (Robert) actualmente escribe en CtrlEnlace* (desviación, ver análisis).
------------------------------------------------------------
-- USE SODIMAC_BATCH_DEV;
-- SELECT TOP 10 * FROM CtrlProcesoCab ORDER BY idEjecucion DESC;
-- SELECT * FROM CtrlProcesoDet WHERE idEjecucion = :idEjecucion;
-- SELECT * FROM CtrlProcesoElemento WHERE idEjecucion = :idEjecucion;
-- SELECT * FROM ctrlLog WHERE idEjecucion = :idEjecucion;
