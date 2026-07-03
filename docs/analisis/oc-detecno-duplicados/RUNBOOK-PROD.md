# Runbook — Remediacion OrdenCompraProveedor en PROD

**BD:** `SODIMAC_SAP_PROD` (10.138.150.124:5319)
**Validado en DEV:** 2026-07-03 (dedup OK, indice OK, SP idempotente 57333->57333)
**Requiere:** respaldo hecho + ventana + aprobacion Ivan/Marco.

> Regla de oro: cada paso valida antes de avanzar. Ante cualquier resultado inesperado,
> DETENER y revisar. Nada se borra hasta validar en caliente varios dias.

---

## Pre-checks (antes de la ventana)

```sql
SELECT DB_NAME();  -- DEBE decir SODIMAC_SAP_PROD

-- 1) El rollback del incidente ya termino (fila vacia)
SELECT session_id, status, command, percent_complete
FROM sys.dm_exec_requests WHERE session_id = 98;

-- 2) Tamano actual (referencia)
SELECT COUNT(*) AS total FROM OrdenCompraProveedor WITH (NOLOCK);   -- ~53.4M esperado

-- 3) Ordenes distintas (objetivo de la limpieza)
SELECT COUNT(DISTINCT CONCAT(NumeroProveedor,'-',OrdenCompra,'-',Recepcion,'-',
       FechaRecepcion,'-',Estatus)) AS distintos
FROM OrdenCompraProveedor WITH (NOLOCK);   -- ~325,670 esperado

-- 4) Nadie usando la tabla durante la ventana (coordinar apagado del batch)
```

## Respaldos (ya versionados en git)
- SP original: `backup/uspRegistroOrdenCompraProveedor_20260703_PROD.sql`
- Vista original: `backup/vw_ordencompraproveedor_20260703_PROD.sql`
- Datos: el paso 1 del script 01 renombra la tabla a `_OLD_20260703` (no se borra).

---

## Secuencia de la ventana

### Paso 1 — Limpieza (script 01)
- Ejecutar `01-limpieza-tabla.sql` pasos 1 y 2 (crear `_Clean` + validar).
- **Checkpoint:** `total_limpio` debe ser ~325,670. Si no cuadra, DETENER.
- Ejecutar paso 3 (swap de nombres). Requiere que NADIE tenga la tabla abierta.

### Paso 2 — Indice (script 02)
- Verificar 0 duplicados (query del script). Debe dar 0 filas.
- Crear `UX_OrdenCompraProveedor_Negocio` (unique clustered, IGNORE_DUP_KEY).
- **Checkpoint:** `SELECT is_unique FROM sys.indexes WHERE name='UX_...'` = 1.

### Paso 3 — SP reescrito (script 03)
- Ejecutar `03-usp-rewrite.sql` (ALTER PROCEDURE).
- **Checkpoint:** compila sin error.

### Paso 4 — Prueba de humo en PROD (misma que DEV)
```sql
SELECT COUNT(*) AS antes FROM OrdenCompraProveedor;
TRUNCATE TABLE OrdenCompraProveedorTemp;
INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE());
EXEC uspRegistroOrdenCompraProveedor;
SELECT COUNT(*) AS tras_1 FROM OrdenCompraProveedor;   -- antes + 1
-- re-correr misma fila
INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE());
EXEC uspRegistroOrdenCompraProveedor;
SELECT COUNT(*) AS tras_2 FROM OrdenCompraProveedor;   -- DEBE seguir = tras_1
-- limpiar
DELETE FROM OrdenCompraProveedor WHERE Sucursal='TEST';
TRUNCATE TABLE OrdenCompraProveedorTemp;
```
- **Checkpoint:** `tras_2 = tras_1`. Si duplica, DETENER y revertir.

### Paso 5 — Backfill enero (batch Java)
- Encender el batch con `descarga.periodo.enabled=true`, rango enero, bloques de 7.
- Validar conteo en BD tras correr.
- Al terminar backfill: `descarga.periodo.enabled=false` + redeploy (modo diario normal).

---

## Rollback

### Si falla el SP (paso 3/4)
Restaurar el SP original desde `backup/uspRegistroOrdenCompraProveedor_20260703_PROD.sql`
(cambiar CREATE por ALTER).

### Si hay que revertir la tabla (paso 1/2)
```sql
-- borrar indice y deshacer swap
DROP INDEX UX_OrdenCompraProveedor_Negocio ON OrdenCompraProveedor;
BEGIN TRAN;
  EXEC sp_rename 'OrdenCompraProveedor',              'OrdenCompraProveedor_Clean';
  EXEC sp_rename 'OrdenCompraProveedor_OLD_20260703', 'OrdenCompraProveedor';
COMMIT;
```

### Limpieza final (SOLO tras validar en caliente varios dias)
```sql
DROP TABLE OrdenCompraProveedor_OLD_20260703;
DROP TABLE OrdenCompraProveedor_Clean;  -- si quedo huerfana tras rollback
```

---

## Notas
- La vista `vw_ordencompraproveedor` NO se modifica; el SP reescrito ya no la usa.
- El indice unico es el blindaje real: aunque el batch reintente, no vuelve a duplicar.
- Hardening opcional futuro: normalizar `FechaRecepcion` varchar -> datetime (entity Java + columna).
