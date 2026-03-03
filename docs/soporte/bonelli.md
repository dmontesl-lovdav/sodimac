# Bonelli - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-03-02 | Alta de mensajes WRN7024 y WRN7025

**Contexto**: Bonelli solicito el alta de dos mensajes de advertencia en el catalogo `CatMsgAdvertencia` (`shared_catalogs`). Ya tiene el backend listo para recibirlos.
**Mensajes**:
- `WRN7024`: "El valor del pago total no es igual al desglose del pago, favor de validar."
- `WRN7025`: "El desglose del pago no contiene un ingreso, favor de validar."
**Solucion**: Se verifico que no existian. Se creo script `docs/db/catalogs/seed_WRN7024_WRN7025.sql`. Probado en local con 3 idiomas (es/en/pt), dict_id 8032-8033.
**Jira**: -
**Estado**: Resuelto en local. Pendiente ejecutar en Sodimac DEV.

---

## 2026-03-02 | Correccion naming tablas de pagos (STM-399)

**Contexto**: Bonelli desplego la estructura cabecera-detalle para pagos en tenant_finance (v2 y v3). Se revisaron las diferencias con nuestro diseno.
**Problema**: Naming inconsistente entre cabecera y detalle, columnas en español mezcladas con ingles, constraints/indices con nombres obsoletos.
**Lo que hizo bien (v3)**: Renombro `finanzas_payments` → `payment_detail`, agrego FK constraint, agrego UNIQUE en cabecera, agrego FKs en todo el esquema.
**Correcciones pendientes**:
- Tabla: `finanzas_payment_headers` → `payment_header`
- Columnas: `id_referencia_pago` → `payment_header_uuid`, `importe` → `total_amount`
- `anio` se mantiene (palabra reservada)
- Constraints/indices: `finanzas_payments_*` → `payment_detail_*`, `finanzas_payment_headers_*` → `payment_header_*`
- FK nullable (3 registros sin cabecera en prod)
**Script**: `docs/jiras/STM-399/STM-399_fix_naming.sql`
**Jira**: STM-399
**Estado**: Script listo y probado en local. Pendiente aplicar en Sodimac DEV.

---
