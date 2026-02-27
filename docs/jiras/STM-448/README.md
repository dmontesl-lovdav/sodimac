# STM-448: Consulta de Complementos de Pago

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-448

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Fiscal API |
| **API** | mrch.backend.somx.fiscal-api |
| **Puerto** | 8082 |

---

## Descripcion

Implementacion de endpoints para consulta y gestion de complementos de pago (CFDI tipo P). Permite obtener pagos por diferentes criterios incluyendo UUID fiscal.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/payment-complements` | Listar complementos con paginacion |
| GET | `/api/payment-complements/:id` | Obtener complemento por ID interno |
| GET | `/api/payment-complements/fiscal/:fiscalUuid` | Obtener por UUID fiscal |
| POST | `/api/payment-complements/search` | Busqueda avanzada con filtros |

### Filtros de Busqueda

- `issuerRfc`: RFC del emisor
- `receiverRfc`: RFC del receptor
- `startDate` / `endDate`: Rango de fechas
- `page` / `size`: Paginacion

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-448 - Consulta de Complementos de Pago.postman_collection.json](../../../postman/STM-448%20-%20Consulta%20de%20Complementos%20de%20Pago.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: fiscal-api (Puerto 8082)
- Complemento de Pago version 2.0
- UUID fiscal: Identificador unico del SAT (TimbreFiscalDigital)
