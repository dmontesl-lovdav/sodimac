# STM-875: Busqueda Avanzada de Descuentos Comerciales

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-875

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Finanzas API |
| **API** | mrch.backend.somx.finanzas-api |
| **Puerto** | 8091 |

---

## Descripcion

Implementacion de sistema de busqueda avanzada y exportacion para descuentos comerciales (rebates). Incluye filtros multiples, paginacion y exportacion a CSV.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/rebates/published` | Obtener descuentos publicados (status=1) |
| POST | `/api/rebates/filter` | Busqueda avanzada con filtros |
| GET | `/api/rebates/vendor/:vendorNumber` | Descuentos por proveedor |
| GET | `/api/rebates/export/csv` | Exportar filtrados a CSV |
| GET | `/api/rebates/published/export/csv` | Exportar publicados a CSV |

### Filtros de Busqueda Avanzada

| Parametro | Tipo | Descripcion |
|-----------|------|-------------|
| `vendorNumber` | number | Numero de proveedor |
| `startDate` | date | Fecha inicio |
| `endDate` | date | Fecha fin |
| `status` | number | Estado (1=Publicado, 0=Borrador) |
| `minAmount` | decimal | Monto minimo |
| `maxAmount` | decimal | Monto maximo |
| `page` | number | Numero de pagina |
| `size` | number | Tamano de pagina |

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-875 - Busqueda Avanzada de Descuentos.postman_collection.json](../../../postman/STM-875%20-%20Busqueda%20Avanzada%20de%20Descuentos.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: finanzas-api (Puerto 8091)
- Exportacion CSV con encoding UTF-8
- Status 1 = Publicado/Activo, Status 0 = Borrador/Inactivo
