# STM-771: Busqueda Avanzada de Facturas

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-771

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Fiscal API |
| **API** | mrch.backend.somx.fiscal-api |
| **Puerto** | 8082 |

---

## Descripcion

Implementacion de sistema de busqueda avanzada para facturas electronicas con multiples filtros y criterios de busqueda.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/invoices` | Listar facturas con paginacion |
| GET | `/api/invoices/:uuid` | Obtener factura por UUID interno |
| GET | `/api/invoices/fiscal/:fiscalUuid` | Obtener por UUID fiscal (SAT) |
| POST | `/api/invoices/search` | Busqueda avanzada con filtros |

### Filtros de Busqueda Avanzada

| Parametro | Tipo | Descripcion |
|-----------|------|-------------|
| `issuerRfc` | string | RFC del emisor |
| `receiverRfc` | string | RFC del receptor |
| `documentType` | string | Tipo documento (I/E/T/P) |
| `startDate` | date | Fecha inicio |
| `endDate` | date | Fecha fin |
| `page` | number | Numero de pagina (0-indexed) |
| `size` | number | Tamano de pagina |

### Tipos de Documento

| Tipo | Descripcion |
|------|-------------|
| I | Ingreso (Factura) |
| E | Egreso (Nota de Credito) |
| T | Traslado |
| P | Pago |

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-771 - Busqueda Avanzada de Facturas.postman_collection.json](../../../postman/STM-771%20-%20Busqueda%20Avanzada%20de%20Facturas.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: fiscal-api (Puerto 8082)
- UUID interno vs UUID fiscal: El UUID interno es el ID de la BD, el fiscal es el del SAT
