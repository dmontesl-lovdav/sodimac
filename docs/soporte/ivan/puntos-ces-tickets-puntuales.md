# Puntos CES — Reproceso por tickets puntuales

**Fecha**: 2026-05-05
**Pedido por**: Ivan
**Proyecto**: `soporte/bctfacturacion`
**Commits**: `3e568d8` (feature), rama `dmontes`

---

## Contexto

Ivan compartio 126 tickets de tipo **asignacion** (`TRANSACTIONTYPE='sale'`) que requieren ser enlazados manualmente — replicar `AdminPuntosCes` + `VentaCab` + `VentaDetImpuesto` para esos tickets especificos, sin depender del rango de fechas del job.

Lista completa en `sesiones/soporte/20260505-puntos-ces.txt`.

## Implementacion

### Modo exclusivo nuevo en `ReplicarPuntosJob`

`application.properties`:

```properties
puntos.ces.tickets.activo=true
puntos.ces.tickets.lista=2025121820380039279,2025121820300054032,...
```

Cuando `activo=true`:
- Ignora `puntos.ces.rango.fechas` y `puntos.ces.dias`
- Procesa solo los tickets en `lista`
- Solo `TRANSACTIONTYPE = 'sale'`
- **Sin filtro de fecha** (`FECHA_TRX > 2025-01-01` eliminado en este modo)
- Control CES con clave `MANUAL-yyyyMMdd` (idempotente)

### Archivos modificados

| Archivo | Cambio |
|---|---|
| `application.properties` | + 2 props |
| `TrxPointsRepository.java` | + `findPuntosCesByTickets(List<String>)` |
| `ITrxPointsService.java` + impl | + `obtenerPuntosCesPorTickets` |
| `ReplicarPuntosJob.java` | + `sincronizarPorTickets`, branch en `sincroniza()` |

## Query ejecutada

```sql
select hdr.NUM_TRX ticket, TO_CHAR(hdr.FECHA_TRX,'YYYY-MM-DD') fechaVenta,
       hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR, hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO,
       SUM(((SELECT distinct ext.f_conv FROM SW_CEM.trx_points_ext ext WHERE ext.id = p.ID) * TO_NUMBER(p.POINTS))) montoPuntos,
       p.ID idPuntos, p.TRANSACTIONTYPE tipoTransaccionCes, p.POINTS puntos
  from TRX_HDR hdr, sw_cem.TRX_POINTS p
 where (TO_CHAR("DATE",'YYYYMMDD') || p.BRANCH || lpad(p.POS,3,'0') || lpad(p.SEQUENCE,4,'0')) = hdr.NUM_TRX
   AND hdr.NUM_TRX IN (:pTickets)
   AND p.IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH(p.IDENTIFICATIONNUMBER) = 10
   AND NVL(p.POINTS,0) != 0
   AND p.TRANSACTIONTYPE = 'sale'
 group by hdr.NUM_TRX, hdr.FECHA_TRX, hdr.NUM_TIENDA, hdr.TIPO_TRX, hdr.MNT_TOTAL_A_PAGAR,
          hdr.MNT_TOT_SN_IMPTOS, hdr.MNT_REDONDEO, p.ID, p.TRANSACTIONTYPE, p.POINTS
 ORDER BY 1;
```

## Diagnostico cuando query devuelve 0 filas

Ejecutar paso a paso para aislar el filtro problematico:

### 1. Existen los tickets en TRX_HDR?

```sql
SELECT NUM_TRX, FECHA_TRX, NUM_TIENDA, TIPO_TRX
  FROM TRX_HDR
 WHERE NUM_TRX IN ('ticket1','ticket2',...);
```

Si 0 filas → tickets no estan en BCT. Verificar ambiente (test vs prod) o que tickets sean validos.

### 2. Existen TRX_POINTS para esos tickets (sin filtros)?

```sql
SELECT p.*
  FROM sw_cem.TRX_POINTS p, TRX_HDR hdr
 WHERE (TO_CHAR("DATE",'YYYYMMDD') || p.BRANCH || lpad(p.POS,3,'0') || lpad(p.SEQUENCE,4,'0')) = hdr.NUM_TRX
   AND hdr.NUM_TRX IN ('ticket1','ticket2',...);
```

Si 0 filas → no se generaron puntos para esos tickets. Cliente probablemente no tenia tarjeta CES.

### 3. Que TRANSACTIONTYPE tienen?

```sql
SELECT p.TRANSACTIONTYPE, COUNT(*)
  FROM sw_cem.TRX_POINTS p, TRX_HDR hdr
 WHERE (TO_CHAR("DATE",'YYYYMMDD') || p.BRANCH || lpad(p.POS,3,'0') || lpad(p.SEQUENCE,4,'0')) = hdr.NUM_TRX
   AND hdr.NUM_TRX IN ('ticket1','ticket2',...)
 GROUP BY p.TRANSACTIONTYPE;
```

Si NO hay `sale` → los tickets no son de asignacion. Ivan tendria que confirmar.

### 4. IDENTIFICATIONNUMBER cumple filtro?

```sql
SELECT p.IDENTIFICATIONNUMBER, LENGTH(p.IDENTIFICATIONNUMBER), p.POINTS, p.TRANSACTIONTYPE
  FROM sw_cem.TRX_POINTS p, TRX_HDR hdr
 WHERE (TO_CHAR("DATE",'YYYYMMDD') || p.BRANCH || lpad(p.POS,3,'0') || lpad(p.SEQUENCE,4,'0')) = hdr.NUM_TRX
   AND hdr.NUM_TRX IN ('ticket1','ticket2',...);
```

Si IDENTIFICATIONNUMBER no empieza con `1000` o no tiene 10 chars → filtro `LIKE '1000%' AND LENGTH=10` lo mata.

## Filtros sospechosos en orden de probabilidad

| Filtro | Por que mata el resultado |
|---|---|
| `p.TRANSACTIONTYPE = 'sale'` | Tickets no son asignacion sino devolucion/cancelacion/expirados |
| `IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH=10` | Cliente sin tarjeta CES o formato distinto |
| `NVL(p.POINTS,0) != 0` | Tickets con 0 puntos asignados |
| Join compuesto fecha+branch+pos+sequence | Formato distinto entre tickets |

## Ver tambien

- Proceso: `docs/wiki/procesos/10-puntos-ces.md`
- Conexiones BD: `docs/BASE-DE-DATOS-BCT.md`
- Sesion original: `sesiones/soporte/20260505-puntos-ces.txt`
