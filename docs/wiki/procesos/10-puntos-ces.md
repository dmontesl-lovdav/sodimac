# Proceso: Puntos CES

> Replicacion del flujo de puntos CES desde Oracle BCT (transacciones de tienda) hacia SQL Server FISCAL (contabilidad). Ejecutado por el batch `bctfacturacion`, job `ReplicarPuntosJob`.

## Que es

CES (Circulo de Especialistas Sodimac) = programa de fidelidad. Cuando un cliente con tarjeta CES compra en tienda, se le **asignan puntos**. Esos puntos se contabilizan como pasivo y se materializan al canjearlos.

El batch lee los movimientos de puntos del POS (Oracle) y los replica a la BD fiscal para amarrarlos contra la venta correspondiente.

## 4 tipos de movimiento CES

`sw_cem.TRX_POINTS.TRANSACTIONTYPE` define el tipo. El job ejecuta **4 queries union** en `TrxPointsRepository.findPuntosCes`:

| UNION | TRANSACTIONTYPE | Significado |
|---|---|---|
| 1 | `cn` | Devolucion / cancelacion de asignacion |
| 2 | (sin filtro de tipo) | **Asignacion** de puntos por venta (rows con `TIPO_FORMA_PAGO != 2`) |
| 3 | (sin filtro de tipo) | Cancelacion de provisiones (`TIPO_FORMA_PAGO = 2`) |
| 4 | `exp_ces` | Puntos expirados (rango fecha-380 a fecha-365) |

## Flujo end-to-end

```
LEER (Oracle BCT)
   TRX_HDR + sw_cem.TRX_POINTS + TRX_POINTS_EXT + TRX_POINTS_SKU
   Join compuesto: TO_CHAR(DATE,'YYYYMMDD') || BRANCH || lpad(POS,3) || lpad(SEQUENCE,4) = NUM_TRX
   Filtros: IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH=10, POINTS!=0, FECHA_TRX > 2025-01-01

GUARDAR (SQL Server FISCAL)
   AdminPuntosCes      ← siempre (1 por ticket+tipoTransaccionCes)
   |__ VentaCab        ← solo si tipo in uspObtieneTransCesPermitidos()
       |__ VentaDetImpuesto  ← puntos prorrateados por SKU

CONTROL (MariaDB inHouse)
   ControlVentaCes (id, fecha, total, estatus)
   Idempotente: si fecha existe con estatus CORRECTO -> skip
```

**Enlace clave**: `idPuntosCes` (PK de `sw_cem.TRX_POINTS.ID`) une los 3 niveles → `AdminPuntosCes` ↔ `VentaCab` ↔ `VentaDetImpuesto`.

## Calculo puntos por SKU

```
factor = adminPuntos.montoPuntos / adminPuntos.puntos
ventaDetImp.montoPuntos = puntosSku.puntos * factor
```

Donde `factor` viene de `TRX_POINTS_EXT.f_conv` (conversion puntos → pesos).

## Configuracion (`application.properties`)

### Modo rango / dias atras (default)

```properties
puntos.ces.rango.fechas=true           # true=rango, false=dias atras
puntos.ces.fecha.inicio=2025-01-01
puntos.ces.fecha.fin=2025-02-23
puntos.ces.dias=15                     # si rango=false, retrocede N dias desde ayer
```

### Modo tickets puntuales (exclusivo)

Cuando un PM/soporte pide reprocesar una lista especifica de tickets:

```properties
puntos.ces.tickets.activo=true
puntos.ces.tickets.lista=2025121820380039279,2025121820300054032,...
```

- **Exclusivo**: cuando `activo=true` ignora rango y dias.
- **Solo `TRANSACTIONTYPE='sale'`** (asignacion).
- **Sin filtro de fecha** — corre con cualquier ticket historico.
- Control CES se registra con clave `MANUAL-yyyyMMdd`.
- Implementado en commit `3e568d8` (PR rama `dmontes`).

## Tabla BKP — pendiente

`sw_cem.TRX_POINTS_BKP` existe pero NO esta referenciada en las queries del job. Ivan reporto que las cancelaciones se mueven a esta tabla — pendiente confirmar si hay que agregar query adicional. Ver `docs/conversacion/ivan.txt`.

## Diagnostico: query devuelve 0 filas

Filtros que mas suelen matar el resultado:

| Filtro | Riesgo |
|---|---|
| `p.TRANSACTIONTYPE = 'sale'` | Tickets de devolucion/cancelacion no aparecen |
| `IDENTIFICATIONNUMBER LIKE '1000%' AND LENGTH=10` | Cliente sin tarjeta CES → 0 |
| `NVL(p.POINTS,0) != 0` | Tickets con 0 puntos asignados → 0 |
| Join `TO_CHAR(DATE)\|\|BRANCH\|\|POS\|\|SEQUENCE = NUM_TRX` | Formato diferente entre tickets → 0 |

Queries de validacion paso a paso en `docs/soporte/ivan/puntos-ces-tickets-puntuales.md`.

## Tablas

Ver `docs/BASE-DE-DATOS-BCT.md` para credenciales y URLs.

| BD | Tabla | Rol |
|---|---|---|
| Oracle BCT | `TRX_HDR` | Cabecera ticket |
| Oracle BCT | `TRX_DET` / `TRX_DET_IMPUESTO` | Detalle + impuestos |
| Oracle BCT | `TRX_FRM_PAGO` | Formas de pago |
| Oracle BCT | `sw_cem.TRX_POINTS` | Movimiento puntos |
| Oracle BCT | `sw_cem.TRX_POINTS_EXT` | Factor conversion `f_conv` |
| Oracle BCT | `sw_cem.TRX_POINTS_SKU` | Puntos por SKU |
| SQL Server FISCAL | `AdminPuntosCes` | Cabecera puntos replicada |
| SQL Server FISCAL | `VentaCab` | Cabecera venta replicada |
| SQL Server FISCAL | `VentaDetImpuesto` | Detalle replicado |
| MariaDB inHouse | `ControlVentaCes` | Control de ejecucion |

## Archivos clave

- [ReplicarPuntosJob.java](../../../soporte/bctfacturacion/src/main/java/com/sodimac/bctfacturacion/job/impl/ReplicarPuntosJob.java) — orquestacion
- [TrxPointsRepository.java](../../../soporte/bctfacturacion/src/main/java/com/sodimac/bctfacturacion/repository/bct/TrxPointsRepository.java) — queries Oracle
- [AdminPuntosCesRepository.java](../../../soporte/bctfacturacion/src/main/java/com/sodimac/bctfacturacion/repository/ces/AdminPuntosCesRepository.java) — escritura FISCAL
- [ControVentaCesRepository.java](../../../soporte/bctfacturacion/src/main/java/com/sodimac/bctfacturacion/repository/ces/ControVentaCesRepository.java) — SP control
