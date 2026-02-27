# STM-393 GAP 01: Campo Fecha de Envio (sentDate)

## Descripcion del GAP

El campo "Fecha de envio" es requerido en el grid de resultados y en el reporte XLSX, pero actualmente no existe en la tabla `invoice` ni en el response de busqueda.

## Impacto

- **Severidad**: Alta
- **Servicios afectados**: fiscal-api
- **Tablas afectadas**: tenant_fiscal.invoice

---

## Implementacion Requerida

### 1. Base de Datos

#### Script DDL

```sql
-- Archivo: STM-393_03_sent_date_column.sql
-- Agregar columna sent_date a la tabla invoice

ALTER TABLE tenant_fiscal.invoice
ADD COLUMN IF NOT EXISTS sent_date TIMESTAMP;

-- Comentario de la columna
COMMENT ON COLUMN tenant_fiscal.invoice.sent_date IS 'Fecha y hora de envio del documento a contabilizar';

-- Indice para consultas por fecha de envio
CREATE INDEX IF NOT EXISTS idx_invoice_sent_date
ON tenant_fiscal.invoice (sent_date);
```

#### Script de migracion (datos existentes)

```sql
-- Opcional: Establecer sent_date para registros existentes
-- Solo para facturas con estatus >= 6 (Pendiente de envio a contabilizar)
UPDATE tenant_fiscal.invoice
SET sent_date = updated_at
WHERE status >= 6
  AND sent_date IS NULL
  AND updated_at IS NOT NULL;
```

---

### 2. Backend (fiscal-api)

#### 2.1 Entidad InvoiceEntity.java

**Archivo:** `src/main/java/com/sodimac/fiscal/api/model/entity/InvoiceEntity.java`

**Agregar campo:**

```java
@Column(name = "sent_date")
private LocalDateTime sentDate;

// Getter y Setter
public LocalDateTime getSentDate() {
    return sentDate;
}

public void setSentDate(LocalDateTime sentDate) {
    this.sentDate = sentDate;
}
```

#### 2.2 DTO InvoiceSearchResponse.java

**Archivo:** `src/main/java/com/sodimac/fiscal/api/model/dto/InvoiceSearchResponse.java`

**Agregar campo:**

```java
@Schema(description = "Fecha y hora de envio del documento")
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime sentDate;

// En el builder
.sentDate(invoice.getSentDate())
```

#### 2.3 Servicio InvoiceServiceImpl.java

**Archivo:** `src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java`

**Modificar metodo mapToSearchResponse:**

```java
private InvoiceSearchResponse mapToSearchResponse(InvoiceEntity invoice,
        IssuerEntity issuer, ReceiverEntity receiver, AddendumEntity addenda) {

    return InvoiceSearchResponse.builder()
        // ... campos existentes ...
        .sentDate(invoice.getSentDate())  // <-- Agregar esta linea
        .build();
}
```

#### 2.4 Actualizacion de sentDate

**Cuando se envia a contabilizar (cambio de estatus a 6):**

```java
public void updateInvoiceStatus(UUID invoiceUuid, Integer newStatus, Integer userId) {
    InvoiceEntity invoice = invoiceRepository.findById(invoiceUuid)
        .orElseThrow(() -> new FiscalException(FiscalErrorCode.INVOICE_NOT_FOUND));

    // Si el nuevo estatus es 6 (Pendiente de envio) y no tiene fecha de envio
    if (newStatus == 6 && invoice.getSentDate() == null) {
        invoice.setSentDate(LocalDateTime.now());
    }

    invoice.setStatus(newStatus);
    invoice.setUpdatedAt(LocalDateTime.now());
    invoice.setUpdatedBy(userId);

    invoiceRepository.save(invoice);
}
```

---

### 3. OpenAPI / Swagger

**Actualizar documentacion en el schema de respuesta:**

```yaml
InvoiceSearchResponse:
  type: object
  properties:
    # ... campos existentes ...
    sentDate:
      type: string
      format: date-time
      description: Fecha y hora de envio del documento
      example: "2025-01-15T14:30:00"
```

---

## Formato de Fecha

| Contexto | Formato | Ejemplo |
|----------|---------|---------|
| API Response (JSON) | ISO 8601 | 2025-01-15T14:30:00 |
| Grid Frontend | dd/mm/yyyy hh24:mi:ss | 15/01/2025 14:30:00 |
| Reporte XLSX | dd/mm/yyyy hh24:mi:ss | 15/01/2025 14:30:00 |

---

## Consultas SQL de Validacion

```sql
-- Verificar columna agregada
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'tenant_fiscal'
  AND table_name = 'invoice'
  AND column_name = 'sent_date';

-- Verificar datos migrados
SELECT
    fiscal_uuid,
    status,
    created_at,
    updated_at,
    sent_date
FROM tenant_fiscal.invoice
WHERE status >= 6
ORDER BY created_at DESC
LIMIT 10;

-- Contar registros con/sin fecha de envio
SELECT
    CASE WHEN sent_date IS NULL THEN 'Sin fecha envio' ELSE 'Con fecha envio' END as estado,
    COUNT(*) as cantidad
FROM tenant_fiscal.invoice
WHERE status >= 6
GROUP BY CASE WHEN sent_date IS NULL THEN 'Sin fecha envio' ELSE 'Con fecha envio' END;
```

---

## Checklist de Implementacion

- [ ] Script SQL ejecutado en ambiente DEV
- [ ] Entidad InvoiceEntity actualizada
- [ ] DTO InvoiceSearchResponse actualizado
- [ ] Servicio InvoiceServiceImpl actualizado
- [ ] Pruebas unitarias actualizadas
- [ ] OpenAPI/Swagger actualizado
- [ ] Script SQL ejecutado en ambiente UAT
- [ ] Pruebas de integracion completadas

---

## Archivos a Modificar

| Archivo | Tipo | Descripcion |
|---------|------|-------------|
| `STM-393_03_sent_date_column.sql` | SQL | Script DDL |
| `InvoiceEntity.java` | Java | Agregar campo sentDate |
| `InvoiceSearchResponse.java` | Java | Agregar campo sentDate en DTO |
| `InvoiceServiceImpl.java` | Java | Mapear campo en response |
| `api.yml` | YAML | Actualizar OpenAPI |

---

**Esfuerzo estimado:** 4 horas
**Dependencias:** Ninguna
