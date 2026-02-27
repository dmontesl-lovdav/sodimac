# STM-1252: Servicio de Catalogo de Bloqueo de Proveedores en BFF

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1252

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | Completado |
| **Modulo** | BFF Catalogos |
| **BFF** | mrch.bff.somx.ppsomx.catalogos (Puerto 3000) |
| **Backend** | mrch.backend.somx.catalogos-api (Puerto 8083) |
| **Fecha** | 2025-12-18 |
| **Probado** | Si |

---

## Descripcion

Implementar el servicio del catalogo de bloqueo de proveedores en el BFF de Catalogos.

> **Nota**: Esta funcionalidad ya fue implementada en el JIRA **STM-1224**. Ver documentacion completa en [STM-1224](../STM-1224/README.md).

---

## Endpoints Disponibles (9 endpoints)

| Metodo | Endpoint | Descripcion | Estado |
|--------|----------|-------------|--------|
| GET | `/supplier-blocks` | Listar todos los bloqueos | OK |
| POST | `/supplier-blocks` | Crear bloqueo | OK |
| GET | `/supplier-blocks/{id}` | Obtener bloqueo por ID | OK |
| PUT | `/supplier-blocks/{id}` | Actualizar bloqueo | OK |
| DELETE | `/supplier-blocks/{id}` | Eliminar bloqueo | OK |
| GET | `/supplier-blocks/supplier/{supplierNumber}` | Bloqueos de un proveedor | OK |
| GET | `/supplier-blocks/supplier/{supplierNumber}/active` | Bloqueos vigentes | OK |
| GET | `/supplier-blocks/supplier/{supplierNumber}/is-blocked` | Verificar si esta bloqueado | OK |
| GET | `/supplier-blocks/supplier/{supplierNumber}/at-date?date=yyyy-MM-dd` | Bloqueos a fecha especifica | OK |

---

## Como Probar (via BFF - Puerto 3000)

### Listar bloqueos
```bash
curl http://localhost:3000/supplier-blocks
```

### Obtener bloqueo por ID
```bash
curl http://localhost:3000/supplier-blocks/1
```

### Bloqueos de un proveedor
```bash
curl http://localhost:3000/supplier-blocks/supplier/PROV001
```

### Bloqueos activos de un proveedor
```bash
curl http://localhost:3000/supplier-blocks/supplier/PROV002/active
```

### Verificar si proveedor esta bloqueado
```bash
curl http://localhost:3000/supplier-blocks/supplier/PROV002/is-blocked
```
Respuesta: `{"supplierNumber":"PROV002","blocked":true}`

### Bloqueos a fecha especifica
```bash
curl "http://localhost:3000/supplier-blocks/supplier/PROV001/at-date?date=2025-02-15"
```

### Crear bloqueo
```bash
curl -X POST http://localhost:3000/supplier-blocks \
  -H "Content-Type: application/json" \
  -d '{
    "supplierNumber": "PROV005",
    "validFrom": "2025-12-20",
    "validTo": "2025-12-25",
    "blockReason": "Motivo del bloqueo"
  }'
```

### Actualizar bloqueo
```bash
curl -X PUT http://localhost:3000/supplier-blocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "blockReason": "Motivo actualizado"
  }'
```

### Eliminar bloqueo
```bash
curl -X DELETE http://localhost:3000/supplier-blocks/1
```

---

## Resultados de Pruebas

Fecha de prueba: 2025-12-18

| Endpoint | Resultado |
|----------|-----------|
| GET /supplier-blocks | OK - Retorna lista de bloqueos |
| GET /supplier-blocks/{id} | OK - Retorna bloqueo por ID |
| GET /supplier-blocks/supplier/{supplierNumber} | OK - Retorna bloqueos del proveedor |
| GET /supplier-blocks/supplier/{supplierNumber}/active | OK - Retorna bloqueos vigentes |
| GET /supplier-blocks/supplier/{supplierNumber}/is-blocked | OK - Retorna {blocked: true/false} |
| GET /supplier-blocks/supplier/{supplierNumber}/at-date | OK - Retorna bloqueos a fecha |
| POST /supplier-blocks | OK - Crea bloqueo |
| PUT /supplier-blocks/{id} | OK - Actualiza bloqueo |
| DELETE /supplier-blocks/{id} | OK - Elimina bloqueo (soft-delete) |

---

## Coleccion Postman

Archivo: `postman/STM-1252 - Supplier Blocks BFF.postman_collection.json`

Variable: `base_url = http://localhost:3000`

---

## Documentacion Relacionada

| JIRA | Descripcion |
|------|-------------|
| [STM-1224](../STM-1224/README.md) | Implementacion original de bloqueo de proveedores (Backend) |
| [STM-1225](../STM-1225/README.md) | API de catalogo de proveedores |

---

## Notas Tecnicas

- Backend: catalogos-api (Puerto 8083)
- BFF: ppsomx.catalogos (Puerto 3000)
- OpenAPI: api.yml (lineas 648-923)
- Validacion de solapamiento de fechas
- Soft-delete con campo status
- Tabla: shared_catalogs.supplier_block
