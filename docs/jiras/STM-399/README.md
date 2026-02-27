# STM-399: Ajuste al MER Financiero para registrar el pago a proveedores

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-399

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :yellow_circle: En Analisis |
| **Asignado** | [Nombre] |
| **Sprint** | Sprint XX |
| **Fecha Inicio** | 2026-02-24 |
| **Fecha Fin** | - |
| **Proyecto** | finanzas-api |

---

## Descripcion

Como usuario de negocio se requiere ajustar el MER del pago a proveedor para conocer el deposito total que se hizo a cada proveedor. Se necesita crear una estructura cabecera-detalle donde la cabecera represente el pago agrupado (deposito total) y el detalle las lineas de pago aplicadas a facturas individuales.

---

## Analisis Tecnico

### Esquema: `tenant_finance` (Base de datos: `b2b_portal`)

### Tablas de Pagos Existentes en el Sistema

El sistema actualmente tiene **9 objetos** relacionados con pagos distribuidos en 3 esquemas:

| Esquema | Tabla | Registros | Proposito |
|---------|-------|-----------|-----------|
| `tenant_finance` | `finanzas_payments` | 3 | **Pagos financieros (candidata a renombrar)** |
| `tenant_finance` | `fiscal_payments` | 0 | Pagos fiscales |
| `tenant_finance` | `account_statement_payment` | 2 | Pagos en estado de cuenta |
| `tenant_finance` | `vw_fiscal_payments` | - | Vista de pagos fiscales |
| `tenant_fiscal` | `payments` | 15 | Complemento de pagos CFDI (SAT) |
| `tenant_fiscal` | `payment` | 20 | Detalle de pago dentro del complemento |
| `tenant_fiscal` | `payment_file_registry` | 0 | Registro de archivos de pago |
| `tenant_fiscal` | `payment_response_catalog` | 19 | Catalogo de respuestas de pago |
| `shared_catalogs` | `payment_condition` | 6 | Condiciones de pago de proveedores |

### Identificacion de la Tabla "Pagos" del JIRA

La tabla que el JIRA solicita renombrar es **`tenant_finance.finanzas_payments`**, ya que:
- Esta en el esquema financiero (`tenant_finance`)
- Contiene los campos que coinciden con el contexto del JIRA: empresa (`company`), numero de proveedor (`vendor_number`), moneda (`currency`), importe (`amount`), fecha de pago (`payment_date`)
- Representa lineas de pago aplicadas a documentos (facturas)

> **IMPORTANTE**: Las tablas de `tenant_fiscal` (`payments`, `payment`) son del complemento de pagos SAT (CFDI tipo P) y NO deben confundirse con estas. Son de un modulo completamente diferente.

---

## 1. Tabla Actual: `finanzas_payments` (a renombrar como `payment_detail`)

### Estructura Actual (15 columnas)

| Columna | Tipo | Nullable | Default | Descripcion |
|---------|------|----------|---------|-------------|
| `finanzas_payment_uuid` | uuid | NO | gen_random_uuid() | PK |
| `company` | integer | NO | - | Empresa SAP |
| `document_number` | varchar(100) | NO | - | Numero de documento |
| `document_reference` | varchar(100) | NO | - | Referencia del documento |
| `vendor_number` | integer | NO | - | Numero de proveedor |
| `amount` | numeric(15,2) | NO | - | Monto del pago |
| `currency` | varchar(3) | NO | 'MXN' | Moneda |
| `document_type` | varchar(5) | NO | - | Tipo de documento |
| `sap_document` | varchar(50) | NO | - | Documento SAP |
| `payment_date` | date | NO | - | Fecha de pago |
| `status` | integer | NO | 1 | Estado |
| `created_by` | bigint | YES | - | Usuario creador |
| `created_at` | timestamp | NO | - | Fecha creacion |
| `updated_by` | bigint | YES | - | Usuario actualizador |
| `updated_at` | timestamp | YES | - | Fecha actualizacion |

### Campos a Agregar segun JIRA

| Campo | Tipo Sugerido | Descripcion |
|-------|---------------|-------------|
| `uuid` | uuid | Identificador unico (ya existe como `finanzas_payment_uuid`) |
| `payment_header_uuid` | uuid (FK) | Clave foranea que vincula con la nueva tabla cabecera |

### Entity TypeORM Actual

**Archivo**: `finanzas-api/src/entities/FinanzasPayment.entities.ts`

```typescript
@Entity({ name: 'finanzas_payments' })
export class FinanzasPayment {
    @PrimaryGeneratedColumn('uuid', { name: 'finanzas_payment_uuid' })
    finanzasPaymentUuid!: string;
    // ... company, documentNumber, documentReference, vendorNumber, amount, currency, etc.
}
```

### Archivos Asociados en finanzas-api

| Tipo | Archivo |
|------|---------|
| Entity | `src/entities/FinanzasPayment.entities.ts` |
| Repository | `src/repositories/FinanzasPayment.repo.ts` |
| Service | `src/services/finanzasPayment.service.ts` |
| Controller | `src/controllers/finanzasPayment.controller.ts` |
| Routes | `src/routes/finanzasPayments.routes.ts` |
| Schema | `src/schemas/finanzasPayment.schema.ts` |
| Docs | `src/docs/paths/finanzasPayments.ts` |

---

## 2. Nueva Tabla: `payment_header` (CabeceraPago)

### Estructura Propuesta

Basada en los campos solicitados por el JIRA, mapeados a convenciones del proyecto:

| Columna JIRA | Columna BD (propuesta) | Tipo | Nullable | Descripcion |
|-------------|------------------------|------|----------|-------------|
| `idReferenciaPago` | `payment_header_uuid` | uuid | NO (PK) | Identificador unico del pago agrupado |
| `empresa` | `company` | integer | NO | Empresa SAP de origen |
| `anio` | `year` | integer | NO | Anio de la operacion |
| `numeroProveedor` | `vendor_number` | integer | NO | Identificador del proveedor |
| `moneda` | `currency` | varchar(3) | NO | Moneda del pago (default MXN) |
| `importe` | `total_amount` | numeric(15,2) | NO | Monto total pagado al proveedor |
| `fechaPago` | `payment_date` | date | NO | Fecha de pago real |
| `estatus` | `status` | integer | NO | Estado del pago |
| `fechaRegistro` | `created_at` | timestamp | NO | Fecha de registro en el sistema |
| `fechaActualizacion` | `updated_at` | timestamp | YES | Fecha de ultima actualizacion |
| *(auditoria)* | `created_by` | bigint | YES | Usuario que creo el registro |
| *(auditoria)* | `updated_by` | bigint | YES | Usuario que actualizo el registro |

### Relacion Cabecera-Detalle

```
payment_header (1) ────── (N) payment_detail (ex finanzas_payments)
     │                              │
     │ payment_header_uuid (PK)     │ payment_header_uuid (FK)
     │ company                      │ finanzas_payment_uuid (PK)
     │ year                         │ company
     │ vendor_number                │ document_number
     │ currency                     │ document_reference
     │ total_amount                 │ vendor_number
     │ payment_date                 │ amount
     │ status                       │ currency
     │ created_at                   │ payment_date
     │ updated_at                   │ ...
```

Un `payment_header` agrupa todas las lineas de pago (`payment_detail`) que corresponden a un mismo deposito/transferencia al proveedor.

---

## 3. Tabla Relacionada: `fiscal_payments`

### Estructura Actual (19 columnas)

| Columna | Tipo | Nullable | Descripcion |
|---------|------|----------|-------------|
| `fiscal_payment_uuid` | uuid | NO (PK) | PK |
| `payment_number` | varchar(50) | NO | Numero de pago (unique) |
| `company` | integer | NO | Empresa SAP |
| `document_number` | varchar(100) | NO | Numero de documento |
| `reference_number` | varchar(100) | NO | Numero de referencia |
| `vendor_number` | integer | NO | Numero de proveedor |
| `amount` | numeric(15,2) | NO | Monto |
| `currency` | varchar(3) | NO | Moneda |
| `document_type` | varchar(5) | NO | Tipo de documento |
| `sap_document` | varchar(50) | YES | Documento SAP |
| `payment_date` | date | NO | Fecha de pago |
| `status` | integer | NO | Estado |
| `payment_method` | varchar(10) | YES | Metodo de pago |
| `bank_account` | varchar(50) | YES | Cuenta bancaria |
| `reference_payment` | varchar(100) | YES | Referencia de pago |
| + campos de auditoria | | | |

### Archivos Asociados en finanzas-api

| Tipo | Archivo |
|------|---------|
| Entity | `src/entities/FiscalPayment.entity.ts` |
| Repository | `src/repositories/fiscalPayment.repo.ts` |
| Service | `src/services/fiscalPayment.service.ts` |
| Controller | `src/controllers/fiscalPayment.controller.ts` |
| Routes | `src/routes/fiscalPayment.routes.ts` |
| Schema | `src/schemas/fiscalPayment.schema.ts` |
| Docs | `src/docs/components/fiscalPayment.ts` |

> **PREGUNTA**: El JIRA solo menciona la tabla "Pagos" → `finanzas_payments`. La tabla `fiscal_payments` tambien necesita vincularse a la nueva cabecera? Ambas tablas tienen estructura similar y comparten los campos `company`, `vendor_number`, `amount`, `currency`, `payment_date`.

---

## 4. Foreign Keys del Esquema tenant_finance

| Tabla Origen | Columna | Tabla Destino | Constraint |
|-------------|---------|---------------|------------|
| `account_statement_payment` | `account_statement_uuid` | `account_statement` | `fk_account_statement_payment_statement` |

> **Nota**: Actualmente `finanzas_payments` NO tiene FKs entrantes ni salientes. Esto facilita el renombramiento.

---

## 5. Resumen de Acciones DDL

### 5.1 Crear tabla `payment_header`

```sql
CREATE TABLE tenant_finance.payment_header (
    payment_header_uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    company             INTEGER NOT NULL,
    year                INTEGER NOT NULL,
    vendor_number       INTEGER NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'MXN',
    total_amount        NUMERIC(15, 2) NOT NULL,
    payment_date        DATE NOT NULL,
    status              INTEGER NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    updated_at          TIMESTAMP,
    CONSTRAINT pk_payment_header PRIMARY KEY (payment_header_uuid)
);

CREATE INDEX idx_payment_header_vendor ON tenant_finance.payment_header (vendor_number);
CREATE INDEX idx_payment_header_company_year ON tenant_finance.payment_header (company, year);
CREATE INDEX idx_payment_header_payment_date ON tenant_finance.payment_header (payment_date);
CREATE INDEX idx_payment_header_status ON tenant_finance.payment_header (status);
```

### 5.2 Renombrar tabla y agregar FK

```sql
-- Renombrar finanzas_payments → payment_detail
ALTER TABLE tenant_finance.finanzas_payments RENAME TO payment_detail;

-- Agregar columna FK hacia payment_header
ALTER TABLE tenant_finance.payment_detail
    ADD COLUMN payment_header_uuid UUID;

-- Crear FK
ALTER TABLE tenant_finance.payment_detail
    ADD CONSTRAINT fk_payment_detail_header
    FOREIGN KEY (payment_header_uuid)
    REFERENCES tenant_finance.payment_header (payment_header_uuid);

-- Indice para la FK
CREATE INDEX idx_payment_detail_header ON tenant_finance.payment_detail (payment_header_uuid);
```

---

## 6. Impacto en Codigo (finanzas-api)

### Archivos a Modificar

| Archivo | Cambio |
|---------|--------|
| `src/entities/FinanzasPayment.entities.ts` | Renombrar tabla a `payment_detail`, agregar relacion `@ManyToOne` con PaymentHeader |
| `src/repositories/FinanzasPayment.repo.ts` | Actualizar nombre de tabla/entity |
| `src/services/finanzasPayment.service.ts` | Actualizar referencias |
| `src/controllers/finanzasPayment.controller.ts` | Actualizar referencias |
| `src/routes/finanzasPayments.routes.ts` | Actualizar rutas si es necesario |
| `src/schemas/finanzasPayment.schema.ts` | Actualizar schema de validacion |
| `src/docs/paths/finanzasPayments.ts` | Actualizar documentacion OpenAPI |

### Archivos a Crear

| Archivo | Descripcion |
|---------|-------------|
| `src/entities/PaymentHeader.entity.ts` | Nueva entity para `payment_header` |
| `src/repositories/paymentHeader.repo.ts` | Nuevo repository |
| `src/services/paymentHeader.service.ts` | Nuevo service con logica de agrupamiento |
| `src/controllers/paymentHeader.controller.ts` | Nuevo controller |
| `src/routes/paymentHeader.routes.ts` | Nuevas rutas |
| `src/schemas/paymentHeader.schema.ts` | Schema de validacion |

### Impacto en BFF

Verificar si el BFF de finanzas expone endpoints de `finanzas_payments`:

| BFF | Archivo | Verificar |
|-----|---------|-----------|
| `ppsomx.finanzas` | `api.yml` | Endpoints de pagos financieros |

---

## 7. Datos Existentes en `finanzas_payments` (consultado en BD)

| UUID | Company | Doc Number | Doc Ref | Vendor | Amount | Currency | Type | SAP Doc | Payment Date | Status |
|------|---------|-----------|---------|--------|--------|----------|------|---------|-------------|--------|
| `123f8f1e...` | 1 | PAY-2024-0002 | AP-REF-002 | 1002 | 25,000.50 | MXN | PP | SAP-PAY-002 | 2024-02-20 | 1 |
| `37537301...` | 1 | PAY-2024-0003 | AP-REF-003 | 1003 | 8,500.75 | MXN | PP | SAP-PAY-003 | 2024-02-25 | 2 |
| `e666490f...` | 1 | PAY-2024-0001 | AP-REF-001 | 1001 | 15,000.00 | MXN | PP | SAP-PAY-001 | 2024-03-15 | 1 |

### Analisis de Agrupamiento

| Vendor | Payment Date | Currency | Lineas | Total |
|--------|-------------|----------|--------|-------|
| 1001 | 2024-03-15 | MXN | 1 | 15,000.00 |
| 1002 | 2024-02-20 | MXN | 1 | 25,000.50 |
| 1003 | 2024-02-25 | MXN | 1 | 8,500.75 |

> **Observacion**: Los 3 registros actuales son de **proveedores distintos** en **fechas distintas**, cada uno con 1 sola linea. No hay patron de agrupamiento aun (cada pago es individual). Son datos de prueba.

### Otras Tablas de Pagos

| Tabla | Registros | Observacion |
|-------|-----------|-------------|
| `fiscal_payments` | 0 | Vacia, sin datos |
| `account_statement_payment` | 2 | Pagos de estado de cuenta (PAG-001: $8,000 MXN, PAG-002: $2,000 MXN). Ambos con status "Aplicado" |

---

## 8. Impacto en BFF Finanzas

El BFF de finanzas (`ppsomx.finanzas/cloud-endpoint/openapi.yaml`) expone endpoints de `fiscal-payments` pero **NO expone endpoints de `finanzas_payments`**.

| Endpoint BFF | Tabla Backend | Impacto |
|-------------|---------------|---------|
| `GET /fiscal-payments` | `fiscal_payments` | Sin impacto directo (tabla diferente) |
| `POST /fiscal-payments` | `fiscal_payments` | Sin impacto directo |
| `GET /fiscal-payments/{uuid}` | `fiscal_payments` | Sin impacto directo |
| `PUT /fiscal-payments/{uuid}` | `fiscal_payments` | Sin impacto directo |
| `DELETE /fiscal-payments/{uuid}` | `fiscal_payments` | Sin impacto directo |
| *(no existe)* | `finanzas_payments` | **Se debe crear nuevo endpoint en BFF para payment_header y payment_detail** |

> **Conclusion**: El renombramiento de `finanzas_payments` → `payment_detail` no rompe el BFF actual ya que este no expone esa tabla. Pero se deberan crear nuevos endpoints para la estructura cabecera-detalle.

---

## Preguntas Abiertas

| # | Pregunta | Estado |
|---|----------|--------|
| 1 | **Tabla "Pagos"**: Se confirma que es `finanzas_payments` (esquema `tenant_finance`)? El sistema tiene multiples tablas de pagos en diferentes esquemas. | :yellow_circle: Pendiente |
| 2 | **`fiscal_payments`**: Esta tabla tambien debe vincularse a la nueva cabecera? Tiene estructura similar y 0 registros actualmente. | :yellow_circle: Pendiente |
| 3 | **Nombre de tabla renombrada**: El JIRA dice "DetallePagos". Se usa `payment_detail` (convencion snake_case del proyecto) o `detalle_pagos` (literal del JIRA)? | :yellow_circle: Pendiente |
| 4 | **Estatus del pago**: Que valores tendra el campo `status` en `payment_header`? Ej: 1=Registrado, 2=Aplicado, 3=Conciliado? Se usara el catalogo de catalogos o valores fijos? | :yellow_circle: Pendiente |
| 5 | **FK obligatoria**: La columna `payment_header_uuid` en `payment_detail` debe ser NOT NULL (todo detalle requiere cabecera) o permite NULL (para datos historicos sin cabecera)? | :yellow_circle: Pendiente |
| 6 | **Datos existentes**: Los 3 registros actuales en `finanzas_payments` necesitan migrarse a la nueva estructura (crear su cabecera)? | :yellow_circle: Pendiente |
| 7 | **`account_statement_payment`**: Esta tabla tambien se ve afectada por este cambio? Tiene una estructura similar de pagos. | :yellow_circle: Pendiente |

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos
- [x] Analisis tecnico completado (consulta directa a BD)
- [ ] Preguntas abiertas resueltas
- [ ] Diseno aprobado por el equipo

### Base de Datos
- [ ] Script DDL para crear `payment_header`
- [ ] Script DDL para renombrar `finanzas_payments` → `payment_detail`
- [ ] Script DDL para agregar FK `payment_header_uuid`
- [ ] Script de migracion de datos existentes (si aplica)
- [ ] Scripts ejecutados en BD local

### Backend (finanzas-api)
- [ ] Crear entity `PaymentHeader`
- [ ] Modificar entity `FinanzasPayment` (renombrar tabla, agregar relacion)
- [ ] Crear repository, service, controller para PaymentHeader
- [ ] Modificar service/controller de PaymentDetail
- [ ] Validaciones implementadas
- [ ] Manejo de errores

### BFF (finanzas)
- [ ] Actualizar api.yml con nuevos endpoints
- [ ] Actualizar DTOs

### Testing
- [ ] Pruebas unitarias
- [ ] Pruebas de integracion
- [ ] Pruebas en Postman
- [ ] QA aprobado

### Despliegue
- [ ] Code Review aprobado
- [ ] Merge a develop
- [ ] Desplegado en DEV
- [ ] Desplegado en QA
- [ ] Desplegado en PROD

### Documentacion
- [x] README de JIRA creado
- [ ] Swagger/OpenAPI actualizado
- [ ] Diagrama ER actualizado

---

## Notas y Decisiones

| Fecha | Decision | Razon |
|-------|----------|-------|
| 2026-02-24 | Tabla "Pagos" identificada como `tenant_finance.finanzas_payments` | Unica tabla financiera con estructura que coincide con el contexto del JIRA (empresa, proveedor, moneda, importe) |
| 2026-02-24 | Nombre propuesto `payment_detail` en lugar de `detalle_pagos` | Seguir convencion snake_case en ingles del proyecto existente |
| 2026-02-24 | Nombre propuesto `payment_header` en lugar de `cabecera_pago` | Misma convencion que el resto del esquema |
| 2026-02-24 | FK `payment_header_uuid` nullable inicialmente | Permite migracion gradual sin romper datos existentes |

---

## Referencias

- [Base de Datos](../../BASE-DE-DATOS.md) - Datos de conexion
- Esquema `tenant_finance` - Puerto 5434, BD `b2b_portal`
- finanzas-api: Node.js/TypeScript con TypeORM
