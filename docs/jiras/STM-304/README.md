# STM-304: Filtrado y Exportacion CSV de Guias de Envio

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-304

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Fiscal API |
| **API** | mrch.backend.somx.fiscal-api |
| **Puerto** | 8082 |

---

## Descripcion

Implementacion de endpoints para filtrado avanzado y exportacion a CSV de guias de envio (Carta Porte). Permite buscar guias por diferentes criterios y exportar los resultados.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/shipping-guides` | Listar guias con paginacion |
| POST | `/api/shipping-guides/filter` | Busqueda avanzada con filtros |
| GET | `/api/shipping-guides/export/csv` | Exportar guias filtradas a CSV |

### Parametros de Filtrado

- `issuerRfc`: RFC del emisor
- `startDate` / `endDate`: Rango de fechas
- `page` / `size`: Paginacion

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-304 - Filtrado y CSV de Guias de Envio.postman_collection.json](../../../postman/STM-304%20-%20Filtrado%20y%20CSV%20de%20Guias%20de%20Envio.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: fiscal-api (Puerto 8082)
- Formato de exportacion: CSV con encoding UTF-8
