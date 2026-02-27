# STM-337: Registro de Facturas y Notas de Credito

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-337

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Fiscal API |
| **API** | mrch.backend.somx.fiscal-api |
| **Puerto** | 8082 |

---

## Descripcion

Implementacion de endpoints para registro y procesamiento de facturas electronicas y notas de credito (CFDI). Incluye subida de archivos XML y transformacion a formato JSON.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/xml-invoices/upload` | Subir XML de factura |
| POST | `/api/xml-invoices/upload-transform` | Subir y transformar XML a JSON |

### Funcionalidad

1. **Upload**: Sube archivo XML y lo almacena
2. **Upload-Transform**: Sube XML, lo parsea y transforma a estructura JSON normalizada

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-337 - Registro de Facturas y NC.postman_collection.json](../../../postman/STM-337%20-%20Registro%20de%20Facturas%20y%20NC.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: fiscal-api (Puerto 8082)
- Soporta CFDI 4.0
- Tipos de documento: I (Ingreso), E (Egreso/NC), T (Traslado), P (Pago)
