# STM-972: Conversion de XML a JSON

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-972

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Fiscal API |
| **API** | mrch.backend.somx.fiscal-api |
| **Puerto** | 8082 |

---

## Descripcion

Implementacion de utilidades para conversion de archivos XML fiscales a formato JSON. Incluye preservacion de estructura original y deteccion automatica de tipo de documento.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/xml-invoices/upload-convert` | Convertir XML a JSON (sin transformacion) |
| POST | `/api/xml-invoices/detect-document-type` | Detectar tipo de documento XML |

### Conversion XML a JSON

- Preserva la estructura exacta del XML original
- Convierte atributos y elementos a propiedades JSON
- Soporta namespaces CFDI

### Tipos de Documento Detectados

| Tipo | Descripcion |
|------|-------------|
| CFDI | Factura o Nota de Credito |
| Complemento de Pago | CFDI tipo P |
| Carta Porte | Complemento de traslado |
| Nomina | Complemento de nomina |

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-972 - Conversion XML a JSON.postman_collection.json](../../../postman/STM-972%20-%20Conversion%20XML%20a%20JSON.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: fiscal-api (Puerto 8082)
- Soporta CFDI 4.0 y complementos vigentes
- Encoding: UTF-8
