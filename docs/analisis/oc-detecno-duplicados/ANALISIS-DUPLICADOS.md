# Analisis — Duplicacion masiva OrdenCompraProveedor (OC Detecno)

**Fecha:** 2026-06-25
**BD:** `SODIMAC_SAP_PROD` (10.138.150.124:5319)
**Tabla:** `OrdenCompraProveedor`
**Proyecto batch:** `soporte/finanzas_descarga_oc_prov_detecno`

---

## Hechos medidos

| Metrica | Valor |
|---|---|
| Filas totales | 53,453,419 |
| Ordenes distintas (5 cols clave) | 325,670 |
| Factor duplicacion | ~164x |
| Basura | 99.4% |

Clave de negocio = `(NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus)`.

---

## Sintoma operativo

- El SP `uspRegistroOrdenCompraProveedor` no termina (O(n^2) sobre 53M).
- Un INSERT del SP corrio ~30 min / 24 min CPU / 22M IO antes de matarse.
- El rollback posterior tardo horas (habia insertado millones de filas).

---

## Causa raiz — CONFIRMADA (2026-06-25)

Query de verificacion arrojo: **cada clave de negocio aparece exactamente 3175 veces**,
uniforme, con las 5 columnas IDENTICAS. El `NOT EXISTS` no dedupea en absoluto.

Mecanismo confirmado:
1. **Java solo hace INSERT a la temp, nunca la trunca** (`OrdenCompraServiceImpl`). Solo el
   SP la trunca, al final, dentro de la transaccion. Si el SP falla (ROLLBACK en CATCH) o se
   corre varias veces antes de que el SP termine, la temp **acumula** filas entre corridas.
2. `NOT EXISTS` evalua contra la base ANTES del INSERT -> todas las filas repetidas dentro
   de la misma temp pasan el chequeo y se insertan de golpe. **No dedupea dentro del lote.**
3. **El INSERT #2 del SP es redundante**: el INSERT #1 ya inserta el cambio de estatus
   (clave con Estatus distinto); el #2 lo vuelve a insertar -> doble por cambio de estatus.

El factor uniforme (3175 en enero; ~164x promedio global) = re-insercion sistematica del
set completo. La columna `FechaRecepcion` NO es la culpable (el GROUP BY agrupa identicas).

Defectos de fondo que se combinan:

### 1. El dedup `NOT EXISTS` no esta matcheando → re-inserta TODO en cada corrida

```sql
INSERT INTO OrdenCompraProveedor
SELECT ... FROM OrdenCompraProveedorTemp A
WHERE NOT EXISTS (
    SELECT 1 FROM OrdenCompraProveedor B
    WHERE A.NumeroProveedor = B.NumeroProveedor
      AND A.OrdenCompra     = B.OrdenCompra
      AND A.Recepcion       = B.Recepcion
      AND A.FechaRecepcion  = B.FechaRecepcion   -- <-- sospechoso (tipo/formato)
      AND A.Estatus         = B.Estatus);
```

Si esta comparacion fallara en matchear filas que SI existen, cada ejecucion
re-inserta el set completo. El batch original corria con rango de **un anio entero**
(`2026/01/01`–`2027/01/01`) en cada ejecucion programada → cada corrida apila ~325k
filas. ~164 corridas → 53M. El patron (multiplo entero del set distinto) encaja con
**re-duplicacion total por corrida**, no con historia de estatus.

Sospechoso #1: **`FechaRecepcion`**. En la entity Java es `String` (`setString`), y se
guarda como `"2026-06-01T00:00:00"`. Si la columna base es `datetime` (o difiere el
formato/tipo entre temp y base), el `=` no matchea de forma consistente entre corridas
→ `NOT EXISTS` siempre verdadero → re-insert.

### 2. Subconsultas `MAX` correlacionadas O(n^2) (problema de performance)

```sql
SELECT * INTO #OrdenCompraActual_Temp
FROM vw_ordencompraproveedor A
WHERE A.FechaRegistro = (
   SELECT MAX(B.FechaRegistro) FROM vw_ordencompraproveedor B
   WHERE A.NumeroProveedor=B.NumeroProveedor
     AND A.OrdenCompra=B.OrdenCompra
     AND A.Recepcion=B.Recepcion);
```

`vw_ordencompraproveedor` se auto-une (la vista lee `OrdenCompraProveedor` + subquery a
`AdminCatalogo` por fila + LEFT JOIN `CatProveedor`). Con 53M filas esto es
53M x 53M efectivo → no escala. Igual el bloque de `Comprobante`+`Addenda` con otro
`MAX(Fecha)` correlacionado.

> La duplicacion (#1) infla la tabla; el O(n^2) (#2) hace que el SP sea imposible de
> correr sobre la tabla inflada. Se retroalimentan.

---

## Query de verificacion (confirmar cual columna rompe el match)

```sql
-- La (prov,oc,rec) mas duplicada y todas sus filas
;WITH top1 AS (
  SELECT TOP 1 NumeroProveedor, OrdenCompra, Recepcion
  FROM OrdenCompraProveedor WITH (NOLOCK)
  GROUP BY NumeroProveedor, OrdenCompra, Recepcion
  ORDER BY COUNT(*) DESC)
SELECT o.NumeroProveedor, o.OrdenCompra, o.Recepcion,
       o.FechaRecepcion, o.Estatus, o.FechaRegistro, o.Uuid
FROM OrdenCompraProveedor o WITH (NOLOCK)
JOIN top1 t ON o.NumeroProveedor=t.NumeroProveedor
           AND o.OrdenCompra=t.OrdenCompra
           AND o.Recepcion=t.Recepcion
ORDER BY o.FechaRecepcion, o.Estatus, o.FechaRegistro;
```

**Lectura:**
- Si las filas repiten **identico** `(FechaRecepcion, Estatus)` muchas veces con distinto
  `FechaRegistro` → el `NOT EXISTS` deberia haberlas bloqueado y no lo hizo →
  confirma **bug de match** (tipo/formato de columna).
- Si difieren en `Estatus`/`FechaRecepcion` → seria versionado legitimo (improbable a 164x).

Tambien util:
```sql
SELECT TOP 20 NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) copias
FROM OrdenCompraProveedor WITH (NOLOCK)
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
ORDER BY COUNT(*) DESC;
```
Si `copias` >> 1 con clave identica → `NOT EXISTS` roto, 100% confirmado.

---

## Plan de remediacion (REQUIERE aprobacion + backup + ventana)

### Fase 0 — Esperar rollback en curso
No tocar la tabla hasta que SPID del rollback desaparezca. No reiniciar SQL Server.

### Fase 1 — Limpiar duplicados (53M → ~325k)
Con tabla nueva (minimal log), NO con DELETE de 53M:
```sql
SELECT *
INTO OrdenCompraProveedor_Clean
FROM (
  SELECT *,
         ROW_NUMBER() OVER (
           PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
           ORDER BY CASE WHEN ISNULL(Uuid,'')<>'' THEN 0 ELSE 1 END, FechaRegistro DESC
         ) AS rn
  FROM OrdenCompraProveedor WITH (NOLOCK)
) t
WHERE rn = 1;
-- validar COUNT ~325,670, luego rename swap + reindex
```
Regla de conservacion: prioriza fila con `Uuid` lleno, luego `FechaRegistro` mas reciente.

### Fase 2 — Blindar contra re-duplicacion
- **Indice unico** en `(NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus)`.
- Normalizar el tipo/formato de `FechaRecepcion` (que temp y base sean el mismo tipo).
- Reescribir el SP:
  - Dedup de la temp antes de insertar.
  - INSERT idempotente via indice unico (`IGNORE_DUP_KEY=ON`) o `MERGE`.
  - Reemplazar los `MAX` correlacionados por `ROW_NUMBER() OVER(PARTITION BY...)`.

### Fase 3 — Backfill
Sobre tabla de 325k limpia + SP arreglado, el backfill por bloques (ya implementado en
`MainComponent`) corre en segundos. El SP una sola vez al final.

---

## Estado del batch (lado app, ya hecho)

- Descarga por periodos en bloques configurable (`descarga.periodo.*`) — resuelve el
  timeout de Detecno. Validado: enero = 5,852 ordenes en 5 bloques de 7 dias.
- SP se ejecuta **una sola vez** al final (no por bloque).

Pendiente = lado BD (este documento): limpieza + indice + reescritura SP.

---

## Archivos de referencia
- `vw_ordencompraproveedor.sql` — definicion vista (capturada 2026-06-25).
- `uspRegistroOrdenCompraProveedor.sql` — SP original con el bug (no re-desplegar tal cual).
