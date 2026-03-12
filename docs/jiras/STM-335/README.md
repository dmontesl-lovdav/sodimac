# STM-335 - Ajustar servicio de facturación y notas de crédito

- **Tipo**: Story
- **Prioridad**: Media
- **Story Points**: 3
- **Epic**: STM-815
- **Asignado**: g_dco018
- **Reporter**: Iván Saúl Cortés Zamora
- **Estado**: Implementado
- **Sprint**: FBC - Sprint 5 - 2026

---

## Objetivo

Permitir que los servicios expongan información clara sobre la relación factura–nota de crédito:
- Visualizar el número de NCs por factura
- Consultar NCs filtradas por factura específica
- Controlar cancelación de NCs por estatus

---

## Reglas de negocio

### 1. Agregar conteo de NCs en servicio de facturación

Agregar una nueva propiedad en la respuesta del servicio de facturación (`POST /invoices/search` con `tipoDocumento=I`) que indique cuántas Notas de Crédito están relacionadas con cada factura.

### 2. Filtrar NCs por factura específica

Modificar el servicio de Notas de Crédito para que permita filtrar y devolver únicamente las NCs vinculadas a una factura específica, usando el identificador de factura como parámetro.

### 3. Cancelación de NCs - Validación de estatus

Ajustar el servicio de cambio de estatus de NC. Solo se permite cancelar NCs que tengan estos estatus:

| Estatus permitido | Código enum | Descripción |
|---|---|---|
| Pendiente de Contabilizar | **3** | NC registrada pendiente de contabilizar en SAP |
| Rechazo Contable | **11** | NC rechazada por temas contables |

> **Nota**: El jira original indica estatus 2 y 9, pero esos son códigos del catálogo front (EFA). Los códigos reales del enum `CreditNoteStatus` son 3 y 11.

Para cualquier otro estatus, mostrar mensaje de advertencia:
- **IdMensaje**: `WRN7023`
- **Mensaje**: "La nota de crédito no puede cancelarse porque ya cuenta con una afectación contable."

Una NC cancelada se registra con estatus:
- **10 – Cancelada**

---

## Implementación realizada

### Tarea 1: Conteo de NCs por factura

**Endpoint**: `POST /invoices/search` (tipoDocumento=I)

**Cambios**:
- `InvoiceSearchResponse.java` — agregado campo `creditNotesCount` (Integer)
- `InvoiceServiceImpl.java` — poblado con `notasCreditoRelacionadas.size()` en el builder (~línea 1277)

**Response nuevo** (campo agregado):
```json
{
  "invoiceUuid": "41c5d355-...",
  "creditNotesCount": 1,
  "notasCreditoRelacionadas": [...]
}
```

### Tarea 2: Filtrar NCs por factura

**Endpoint**: `POST /invoices/search` (tipoDocumento=E)

**Cambios**:
- `InvoiceSearchRequest.java` — agregado campo opcional `relatedInvoiceUuid` (UUID)
- `InvoiceSpecification.java` — agregado predicado #13 con subquery a `related_cfdi`

**Lógica**: Subquery en JPA Criteria API:
```sql
WHERE i.invoice_uuid IN (
    SELECT rc.invoice_uuid FROM related_cfdi rc
    WHERE rc.related_invoice_uuid = :relatedInvoiceUuid
)
```

### Tarea 3: Validación de cancelación de NC

**Endpoint**: `PUT /invoices/{uuid}/status`

**Cambios**:
- `CreditNoteStatus.java` — agregado `CANCELADA(10, ...)` con transiciones desde estatus 3 y 11
- `FiscalMessageCode.java` — agregado `WRN7023`
- `InvoiceServiceImpl.java` — validación usando `puedeTransicionarA()` del enum (sin estatus hardcodeados)

**Lógica**: Si `puedeTransicionarA()` retorna `false`:
- Si el destino es `CANCELADA` → lanza `WRN7023`
- Si es otro destino → lanza `BUS051` (transición no permitida)

**BD**: Se requiere agregar las transiciones al tren de estatus:
```sql
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (2, 3, 10, 1), (2, 11, 10, 1);
```

---

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `InvoiceSearchResponse.java` | Campo `creditNotesCount` |
| `InvoiceSearchRequest.java` | Campo `relatedInvoiceUuid` |
| `InvoiceSpecification.java` | Predicado #13 subquery `related_cfdi` |
| `InvoiceServiceImpl.java` | Poblar `creditNotesCount` + validación cancelación |
| `CreditNoteStatus.java` | Enum `CANCELADA(10)` + transiciones |
| `FiscalMessageCode.java` | Mensaje `WRN7023` |

---

## Datos de prueba en BD local

### Facturas con NCs relacionadas
| Factura UUID | Serie/Folio | Total | NCs |
|---|---|---|---|
| `41c5d355-4089-...` | FA/ETL001 | 11,600 | 1 |
| `f0000010-0010-...` | FA/10002 | 95,000 | 1 |
| `f0000011-0011-...` | MC/10003 | 42,000 | 1 |
| `f0000012-0012-...` | FA/10004 | 200,000 | 1 |

### NCs para pruebas de cancelación
| NC UUID | Folio | Estatus | Puede cancelar? |
|---|---|---|---|
| `e0000001-0001-...` | 50001 | 3 (Pend. Contabilizar) | SI |
| `e0000002-0002-...` | 50002 | 7 (Aplicado) | NO → WRN7023 |
| `e0000003-0003-...` | 50003 | 10 (Cancelada) | NO → ya cancelada |

---

## Archivos de soporte

- `queries-validacion.sql` — Consultas SQL validadas contra BD local
- `STM-335-NC-Facturas.postman_collection.json` — Colección Postman (sin variables, directo a localhost:8082)
- `test-stm-335.bat` — Script de pruebas con curl
- `fix-status-train.sql` — INSERT para transiciones faltantes en `status_train`
