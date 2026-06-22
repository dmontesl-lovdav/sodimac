# Ivan - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-06-22 | Retro: tipoProveedor en /search + nombre de estatus + bloqueo Transporte

**Contexto**: Ivan dio retro de 3 puntos tras validar UAT.
1. `/search`: tipoProveedor no reflejaba cambios en `CatTipoProveedor` (PARMEX Mercancía→Transporte seguía dando Mercancía).
2. `/register`: estatus salía "Recibida", esperaba "En proceso de envío" (notó que las descripciones no deben venir de código fijo).
3. Proveedor 308550 bloqueado por tipo Transporte (no sabía si era bug).

**Veredicto**: P1 y P2a válidos, P2b no es bug.
- **P1**: tipoProveedor se congelaba al registrar. Fix: resolver en vivo desde `supplier_number` (display en /search y /complementos-pago). Filtro sigue en valor guardado (pendiente).
- **P2a**: el nombre del estatus salía del enum `InvoiceStatus` hardcodeado. El catálogo `CatEstatusFactura` dice 3 = "En proceso de envió". Fix: leer descripción de la BD, no del enum.
- **P2b**: `CatBloqueoTipoProveedor` tiene Transporte con `status=1` = bloqueado. El bloqueo funciona bien; si no quieren que bloquee, ajustar catálogo.

**Commit**: `81deffa` (rama `dmontes`)
**Detalle**: [docs/analisis/RETRO-IVAN-2026-06-22-tipoproveedor-estatus.md](../analisis/RETRO-IVAN-2026-06-22-tipoproveedor-estatus.md)
**Estado**: Implementado, probado local, pendiente deploy UAT.

---

## 2026-05-05 | Reproceso puntos CES por tickets puntuales

**Contexto**: Ivan compartio 126 tickets de tipo asignacion (`TRANSACTIONTYPE='sale'`) que requieren replicarse manualmente al modelo fiscal (`AdminPuntosCes` + `VentaCab` + `VentaDetImpuesto`) sin depender del rango de fechas del job.
**Solucion**: Modo exclusivo nuevo en `ReplicarPuntosJob` controlado por dos props (`puntos.ces.tickets.activo`, `puntos.ces.tickets.lista`). Nuevo metodo de repo `findPuntosCesByTickets` filtra por `NUM_TRX IN (...)` y `TRANSACTIONTYPE='sale'`, sin filtro de fecha. Control CES con clave `MANUAL-yyyyMMdd`.
**Proyecto**: `soporte/bctfacturacion`
**Commit**: `3e568d8` (rama `dmontes`)
**Detalle**: [docs/soporte/ivan/puntos-ces-tickets-puntuales.md](ivan/puntos-ces-tickets-puntuales.md)
**Proceso completo**: [docs/wiki/procesos/10-puntos-ces.md](../wiki/procesos/10-puntos-ces.md)
**Tickets**: `sesiones/soporte/20260505-puntos-ces.txt`
**Estado**: Implementado y pusheado. Validacion en ejecucion — query devuelve 0 filas en algun ambiente, queries de diagnostico documentadas.

---

## 2026-03-18 | Candado 60 días autofacturador no bloquea timbrado

**Contexto**: Iván reporta que al timbrar desde el autofacturador, el sistema deja pasar tickets que deberían estar bloqueados por el candado de 60 días.
**Análisis**:
- El parámetro `Aplicacion.DiasPermitidosFacturar` está correctamente configurado en 60 (consultado vía WS de parámetros)
- La validación está en `autofacturador/TicketsServiceImpl.validarTicketWS()` líneas 138-157
- **Posible causa**: si el ticket no existe en BD Oracle BCT (`ticketRepository.findByTicket()` retorna `null`), el método devuelve "OK" en línea 143 y se salta el candado
- El WSFT (`/timbrarVersion`) NO tiene candado de días propio, solo `/retimbrarTicket` lo tiene
- Iván va a debuggear línea 142 de `TicketsServiceImpl.java` para verificar si `ticketBctHdrVal` llega como `null`
**Proyectos involucrados**: `soporte/autofacturador`, `soporte/sodimacfinanzaswsft`
**Estado**: En investigación por Iván

---

## 2026-03-02 | Nuevo catalogo CatTipoOrigenRecepcionSodimac

**Contexto**: Ivan y Josue solicitaron un nuevo catalogo de tipos de origen de recepcion especifico para Sodimac, con 5 entradas y external keys.
**Problema**: No existia el catalogo. Ya existia `CatTipoOrigenRecepcion` (id=20) pero necesitaban uno con datos especificos.
**Solucion**: Se creo script portable `seed_CatTipoOrigenRecepcionSodimac.sql`. Probado en local.
**Datos**: TOS001=SLI(Mercancia), TOS002=TRA(Transporte), TOS003=IND(Indirectos), TOS004=SOT(Servicios), TOS005=Blanco(Mercancia/ODMBS)
**Endpoint**: `GET /CatTipoOrigenRecepcionSodimac/details?lang=1`
**Archivos**: `docs/db/catalogs/seed_CatTipoOrigenRecepcionSodimac.sql`
**Jira**: -
**Estado**: Resuelto en local. Pendiente ejecutar en Sodimac DEV.

---

## 2026-03-02 | Estructura de tablas de pagos (payment_header / payment_detail)

**Contexto**: Ivan pidio la estructura y nombre de las tablas de pagos del esquema `tenant_finance`
**Problema**: Necesitaba conocer el modelo de datos de pagos para su desarrollo
**Solucion**: Se le compartio la estructura completa. Detalle a continuacion:
**Jira**: STM-399
**Estado**: Resuelto (informacion entregada)

### Estructura cabecera-detalle

```
payment_header (1) ──── (N) payment_detail
```

### `payment_header` — Cabecera (deposito total al proveedor)

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `payment_header_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `company` | INTEGER | NO | | Empresa SAP |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `total_amount` | NUMERIC(15,2) | NO | | Monto total del deposito |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `vendor_number`, `company`, `payment_date`, `status`

### `payment_detail` — Detalle (lineas de pago a facturas)

> Antes se llamaba `finanzas_payments`, renombrada por STM-399

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `finanzas_payment_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `payment_header_uuid` | UUID | NO | | **FK → payment_header** |
| `company` | INTEGER | NO | | Empresa SAP |
| `document_number` | VARCHAR(100) | NO | | Numero de documento |
| `document_reference` | VARCHAR(100) | NO | | Referencia del documento |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `amount` | NUMERIC(15,2) | NO | | Monto del pago |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `document_type` | VARCHAR(5) | NO | | Tipo de documento |
| `sap_document` | VARCHAR(50) | NO | | Documento SAP |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado (0-3) |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `document_number`, `vendor_number`, `sap_document`, `payment_date`, `status`, `payment_header_uuid`
Check constraints: `amount >= 0`, `status IN (0,1,2,3)`

---
