# STM-973: Relacion de Descuento con Nota de Credito

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-973

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Finanzas API |
| **API** | mrch.backend.somx.finanzas-api |
| **Puerto** | 8091 |

---

## Descripcion

Implementacion de endpoint para relacionar descuentos comerciales (rebates) con notas de credito fiscales. Permite vincular un descuento con su comprobante fiscal correspondiente.

---

## API Endpoints

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/rebates/relate` | Relacionar descuento con Nota de Credito |

### Request Body

```json
{
  "rebateUuid": "550e8400-e29b-41d4-a716-446655440000",
  "invoiceFiscalUuid": "12345678-1234-1234-1234-123456789012",
  "xmlContent": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
}
```

### Validaciones

| Validacion | Descripcion |
|------------|-------------|
| Tipo de documento | Debe ser tipo E (Egreso/NC) |
| Monto | Monto del descuento debe coincidir con monto de NC |
| RFC | RFC del proveedor debe coincidir con emisor de NC |

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-973 - Relacion Descuento con NC.postman_collection.json](../../../postman/STM-973%20-%20Relacion%20Descuento%20con%20NC.postman_collection.json) | Coleccion Postman |

---

## Notas Tecnicas

- API: finanzas-api (Puerto 8091)
- UUID fiscal: Identificador del TimbreFiscalDigital del SAT
- El xmlContent es opcional para validaciones adicionales
