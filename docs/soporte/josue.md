# Josue - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-03-02 | Nuevo catalogo CatTipoOrigenRecepcionSodimac

**Contexto**: Ivan y Josue solicitaron un nuevo catalogo de tipos de origen de recepcion especifico para Sodimac, con 5 entradas y external keys.
**Problema**: No existia el catalogo. Ya existia `CatTipoOrigenRecepcion` (id=20) con 4 entradas genericas, pero necesitaban uno con datos especificos de Sodimac.
**Solucion**: Se creo script portable `seed_CatTipoOrigenRecepcionSodimac.sql` con header + 5 detalles + traducciones (ES/EN/PT). Probado en local con curl.
**Datos del catalogo**:
- TOS001: Mercancia / Proveedor SLI / external=SLI / internalStatus=1
- TOS002: Transporte / Proveedor TRA / external=TRA / internalStatus=2
- TOS003: Indirectos / Proveedor IND / external=IND / internalStatus=3
- TOS004: Servicios / Proveedor SOT / external=SOT / internalStatus=4
- TOS005: Mercancia / Proveedor ODMBS / external=Blanco / internalStatus=1
**Nota**: TOS001 y TOS005 comparten internalStatus=1 (confirmado como correcto).
**Endpoint**: `GET /CatTipoOrigenRecepcionSodimac/details?lang=1`
**Archivos**: `docs/db/catalogs/seed_CatTipoOrigenRecepcionSodimac.sql`
**Jira**: -
**Estado**: Resuelto en local. Pendiente ejecutar script en Sodimac DEV via DBeaver.

---
