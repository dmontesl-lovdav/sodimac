# 📋 Análisis Construcción de fiscal-api

## 🔍 Resumen Ejecutivo

**fiscal-api** es un microservicio Java Spring Boot que gestiona la facturación electrónica CFDI para cumplimiento SAT en México.

### Diferencias Clave vs finanzas-api

| Aspecto | finanzas-api | fiscal-api |
|---------|--------------|------------|
| **Lenguaje** | Node.js + TypeScript | Java 17 + Spring Boot |
| **ORM** | TypeORM | JPA/Hibernate |
| **Migraciones** | TypeORM Migrations (.ts) | Flyway (.sql) |
| **Base de Datos** | PostgreSQL (tenant_finance) | PostgreSQL (tenant_fiscal) |
| **Propósito** | Operacional (OC, recepciones, pagos) | Fiscal (CFDI, timbrado SAT) |
| **DDL Auto** | Manual (migrations) | `spring.jpa.hibernate.ddl-auto=none` |

---

## 🗂️ Estructura del Proyecto fiscal-api

```
backend/mrch.backend.somx.fiscal-api/
├── src/main/
│   ├── java/                           # Código fuente Java
│   └── resources/
│       ├── application.properties      # Configuración principal ⚙️
│       └── db/migration/               # Migraciones Flyway 📦
│           ├── V0__drop_all_tables.sql
│           ├── V1__create_fiscal_tables.sql (757 líneas)
│           └── V10__insert_fiscal_data.sql
├── pom.xml                             # Dependencias Maven
└── target/                             # Binarios compilados
```

---

## ⚙️ Configuración de Base de Datos

### 📄 application.properties (Configuración Principal)

```properties
# JPA/HIBERNATE CONFIGURATION
spring.jpa.hibernate.ddl-auto=none
# ⚠️ IMPORTANTE: ddl-auto=none significa que Hibernate NO crea tablas automáticamente
# Las tablas deben crearse mediante Flyway migrations

# DATABASE CONNECTION
spring.datasource.url=${DATASOURCE_URL:jdbc:postgresql://10.138.153.10:5432/userapp}
spring.datasource.username=${DATASOURCE_USERNAME:postgres}
spring.datasource.password=${DATASOURCE_PASSWORD:Sodim@cP0str3s}
spring.datasource.driver-class-name=org.postgresql.Driver

# FLYWAY CONFIGURATION
spring.flyway.enabled=${FLYWAY_ENABLED:false}
# ⚠️ CRÍTICO: Flyway está DESHABILITADO por defecto
# Para habilitar: export FLYWAY_ENABLED=true (Linux/Mac) o set FLYWAY_ENABLED=true (Windows)

spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
```

### 🔑 Puntos Clave de Configuración

1. **Base de Datos:**
   - Servidor: `10.138.153.10:5432`
   - Base de datos: `userapp`
   - Schema: `tenant_fiscal` (asumido según patrón)

2. **Flyway DESHABILITADO por defecto:**
   ```bash
   # Para habilitar migraciones:

   # Linux/Mac:
   export FLYWAY_ENABLED=true
   mvn spring-boot:run

   # Windows:
   set FLYWAY_ENABLED=true
   mvn spring-boot:run

   # O en application.properties:
   spring.flyway.enabled=true
   ```

3. **Hibernate NO crea tablas:**
   - `ddl-auto=none` significa que el schema debe existir antes
   - Responsabilidad de Flyway crear el schema completo

---

## 🚀 ¿Cómo se Crea la Base de Datos?

### Flujo de Inicialización

```
┌──────────────────────────────────────────────────────────────┐
│ 1. INICIO DE APLICACIÓN (mvn spring-boot:run)               │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. SPRING BOOT VERIFICA: spring.flyway.enabled               │
│    ├─ false (default) → NO ejecuta migraciones              │
│    └─ true → Continúa con Flyway                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼ (si enabled=true)
┌──────────────────────────────────────────────────────────────┐
│ 3. FLYWAY VERIFICA TABLA flyway_schema_history               │
│    ├─ No existe → Crea tabla de control                     │
│    └─ Existe → Lee versiones aplicadas                      │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. FLYWAY EJECUTA MIGRACIONES PENDIENTES (orden V0 → V1 → V10)│
│    V0__drop_all_tables.sql        (limpia si existen)       │
│    V1__create_fiscal_tables.sql   (crea 16 tablas + indexes)│
│    V10__insert_fiscal_data.sql    (inserta datos iniciales) │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. HIBERNATE VALIDA ENTIDADES vs SCHEMA (ddl-auto=none)     │
│    ├─ Tablas coinciden → OK                                 │
│    └─ Tablas no coinciden → ERROR                           │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. APLICACIÓN LISTA (Controllers expuestos en puerto 8080)  │
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 Migraciones Flyway

### V0__drop_all_tables.sql (33 líneas)

**Propósito:** Script de limpieza para desarrollo

```sql
-- Elimina todas las tablas en orden inverso de dependencias
DROP TABLE IF EXISTS payment_file_registry CASCADE;
DROP TABLE IF EXISTS payment_response_catalog CASCADE;
DROP TABLE IF EXISTS log CASCADE;
DROP TABLE IF EXISTS totals CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS equivalence_dr CASCADE;
DROP TABLE IF EXISTS related_documents CASCADE;
DROP TABLE IF EXISTS related_cfdi CASCADE;
DROP TABLE IF EXISTS addendum CASCADE;
DROP TABLE IF EXISTS invoice CASCADE;
DROP TABLE IF EXISTS authorized_receiver_catalog CASCADE;
DROP TABLE IF EXISTS version_catalog CASCADE;
DROP TABLE IF EXISTS receiver CASCADE;
DROP TABLE IF EXISTS issuer CASCADE;
DROP TABLE IF EXISTS pac_catalog CASCADE;
```

⚠️ **Advertencia:** Este script elimina TODO el schema fiscal. Solo para desarrollo.

---

### V1__create_fiscal_tables.sql (757 líneas)

**Propósito:** Crea el schema completo del módulo fiscal CFDI

#### Tablas Creadas (16 tablas)

| # | Tabla | Descripción | Tipo PK | FKs |
|---|-------|-------------|---------|-----|
| 1 | `pac_catalog` | Proveedores Autorizados de Certificación (PAC) | SERIAL | - |
| 2 | `version_catalog` | Versiones de CFDI soportadas (4.0) | SERIAL | pac_id |
| 3 | `issuer` | Emisores de CFDI (Sodimac, Falabella) | UUID | - |
| 4 | `receiver` | Receptores de CFDI (proveedores) | UUID | - |
| 5 | `authorized_receiver_catalog` | Catálogo de receptores autorizados | SERIAL | receiver_uuid |
| 6 | `invoice` | Facturas CFDI (tipo I, E, T, N, P) | UUID | issuer_uuid, receiver_uuid |
| 7 | `addendum` | Addendas de facturas (OC, guía, recepción) | UUID | invoice_uuid, payments_uuid |
| 8 | `related_cfdi` | Relaciones entre CFDIs | UUID | invoice_uuid, related_invoice_uuid |
| 9 | `related_documents` | Documentos relacionados en pagos | UUID | payment_uuid, document_uuid |
| 10 | `equivalence_dr` | Equivalencias en moneda extranjera | UUID | related_document_uuid |
| 11 | `payment` | Pagos individuales (dentro de complemento) | UUID | payments_uuid |
| 12 | `payments` | Complementos de pago CFDI (tipo P) | UUID | issuer_uuid, receiver_uuid |
| 13 | `totals` | Totales de impuestos en complementos | UUID | payments_uuid |
| 14 | `log` | Bitácora de operaciones de timbrado | UUID | pac_id, version_id, cfdi_uuid |
| 15 | `payment_response_catalog` | Códigos de respuesta SAT | SERIAL | - |
| 16 | `payment_file_registry` | Registro de archivos XML procesados | SERIAL | payments_uuid |

#### Características Técnicas

**1. Extensión UUID:**
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- Habilita gen_random_uuid() para PKs de negocio
```

**2. Primary Keys:**
- **SERIAL:** Catálogos (pac_catalog, version_catalog, payment_response_catalog)
- **UUID:** Entidades de negocio (invoice, payments, payment, issuer, receiver)

**3. Foreign Keys:**
- Todas las FKs con nombres explícitos (`fk_tabla_referencia`)
- Uso de `ON DELETE CASCADE` en tablas dependientes
- Uso de `ON DELETE SET NULL` en referencias opcionales

**4. Constraints:**
```sql
-- Validación RFC (12 o 13 caracteres)
CONSTRAINT chk_issuer_rfc_format
    CHECK (rfc ~ '^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$')

-- Validación de cálculos
CONSTRAINT chk_invoice_total_calculation
    CHECK (total = subtotal - discount)

-- Validación de estados
CONSTRAINT chk_invoice_status
    CHECK (status IN (0, 1, 2, 3))
-- 0=cancelado, 1=vigente, 2=pendiente, 3=rechazado

-- Validación de fechas
CONSTRAINT chk_payment_date_not_future
    CHECK (payment_date <= CURRENT_DATE)
```

**5. Indexes Creados (48 indexes):**
```sql
-- Búsqueda rápida por RFC
CREATE UNIQUE INDEX idx_issuer_rfc ON issuer(rfc);
CREATE UNIQUE INDEX idx_receiver_rfc ON receiver(rfc);

-- Búsqueda de facturas
CREATE UNIQUE INDEX idx_invoice_series_folio
    ON invoice(series, folio)
    WHERE series IS NOT NULL AND folio IS NOT NULL;

-- Optimización temporal
CREATE INDEX idx_invoice_issue_date ON invoice(issue_date);
CREATE INDEX idx_log_transaction_date ON log(transaction_date);

-- Optimización de FKs
CREATE INDEX idx_invoice_issuer_uuid ON invoice(issuer_uuid);
CREATE INDEX idx_invoice_receiver_uuid ON invoice(receiver_uuid);
```

**6. Comentarios en Schema:**
Todas las tablas y columnas tienen `COMMENT ON` explicando su propósito.

---

### V10__insert_fiscal_data.sql (234 líneas)

**Propósito:** Inserta datos iniciales para pruebas y catálogos

#### Datos Insertados

**1. Catálogos PAC:**
```sql
INSERT INTO pac_catalog (pac_id, name, description, status)
VALUES (1, 'PAC_DEMO', 'PAC de demostracion para pruebas', 1);

INSERT INTO pac_catalog (pac_id, name, description, status)
VALUES (2, 'FINKOK', 'Finkok - Proveedor Autorizado de Certificacion', 1);
```

**2. Versiones CFDI:**
```sql
-- CFDI 4.0 Ingreso
INSERT INTO version_catalog (version_id, name, version, document_type, pac_id)
VALUES (1, 'CFDI 4.0 Ingreso', 4.000, 'I', 1);

-- CFDI 4.0 Pago (estructura base)
INSERT INTO version_catalog (version_id, name, version, document_type, pac_id)
VALUES (2, 'CFDI 4.0 Pago', 4.000, 'P', 1);

-- Complemento Pagos 2.0 (nodo Pagos20)
INSERT INTO version_catalog (version_id, name, version, document_type, pac_id, structure_url)
VALUES (3, 'Complemento Pagos 2.0', 2.000, 'P', 1,
    'http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos20.xsd');
```

**3. Emisores (Sodimac/Falabella):**
```sql
INSERT INTO issuer (issuer_uuid, name, rfc, tax_regime)
VALUES ('11111111-1111-1111-1111-111111111111'::uuid,
    'SODIMAC MEXICO S.A. DE C.V.', 'SOD970101ABC', '601');

-- RFC real de Sodimac
INSERT INTO receiver (receiver_uuid, name, rfc, tax_regime)
VALUES ('55555555-5555-5555-5555-555555555555'::uuid,
    'SODIMAC MEXICO S.A. DE C.V.', 'LAN7008173R5', '601');
```

**4. Catálogo de Respuestas SAT (15 códigos):**
```sql
-- Éxito
'200' → 'Complemento de pago validado exitosamente'
'0' → 'Validación exitosa SAT'

-- Errores SAT
'301' → 'CFDI no encontrado en SAT'
'302' → 'CFDI cancelado'
'303' → 'RFC emisor no válido'
'304' → 'RFC receptor no válido'
'305' → 'Sello digital inválido'

-- Errores Estructura XML
'401' → 'XML mal formado'
'402' → 'XSD no cumplido'
'403' → 'Nodo requerido faltante'
'404' → 'Atributo requerido faltante'

-- Errores Validación
'501' → 'Receptor no autorizado'
'502' → 'Versión no vigente'
'503' → 'Documento relacionado no existe'
'504' → 'Documento relacionado no pagado'
'505' → 'Complemento duplicado'

-- Errores Negocio
'601' → 'Proveedor no registrado'
'602' → 'Usuario no autorizado'
'700' → 'Error general'
```

**5. Datos de Prueba:**
- 2 facturas de prueba (invoice)
- 1 complemento de pago (payments)
- 1 pago individual (payment)
- 1 documento relacionado (related_documents)
- 1 registro de totales (totals)
- 1 addenda de ejemplo
- 1 registro en log

---

## 📊 Comparación de Migraciones

### finanzas-api (TypeORM)

```typescript
// src/migrations/1700000000001-CreateTablesAndIndexes.ts
export class CreateTablesAndIndexes1700000000001 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`
            CREATE TABLE purchase_order (
                purchase_order_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                order_number VARCHAR(20) UNIQUE,
                ...
            )
        `);

        await queryRunner.query(`
            CREATE INDEX idx_purchase_order_status
            ON purchase_order(status)
        `);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`DROP TABLE IF EXISTS purchase_order CASCADE`);
    }
}
```

**Características:**
- Programático (TypeScript)
- Control fino con QueryRunner
- Rollback explícito en down()
- Ejecutado por npm run migration:run

### fiscal-api (Flyway)

```sql
-- src/main/resources/db/migration/V1__create_fiscal_tables.sql
CREATE TABLE invoice (
    invoice_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_uuid UUID,
    ...
);

CREATE INDEX idx_invoice_issue_date ON invoice(issue_date);
```

**Características:**
- Declarativo (SQL puro)
- Sin código Java requerido
- Rollback manual (crear VX_revert.sql)
- Ejecutado automáticamente al iniciar app (si enabled=true)

---

## 🔄 Orden de Ejecución de Migraciones

Flyway ejecuta en orden **lexicográfico** basado en el prefijo de versión:

```
V0__drop_all_tables.sql          (versión 0)
    ↓
V1__create_fiscal_tables.sql     (versión 1)
    ↓
V10__insert_fiscal_data.sql      (versión 10)
```

**Registro en Base de Datos:**

Flyway crea la tabla `flyway_schema_history`:

```sql
SELECT * FROM flyway_schema_history;

┌──────────┬───────────────────────────────┬──────────┬─────────────────────┐
│ version  │ description                    │ type     │ installed_on        │
├──────────┼───────────────────────────────┼──────────┼─────────────────────┤
│ 0        │ drop all tables               │ SQL      │ 2025-11-10 09:00:00 │
│ 1        │ create fiscal tables          │ SQL      │ 2025-11-10 09:00:01 │
│ 10       │ insert fiscal data            │ SQL      │ 2025-11-10 09:00:02 │
└──────────┴───────────────────────────────┴──────────┴─────────────────────┘
```

---

## 🛠️ Cómo Levantar fiscal-api con Migraciones

### Opción 1: Habilitar Flyway via Variables de Entorno

```bash
# Linux/Mac
cd backend/mrch.backend.somx.fiscal-api
export FLYWAY_ENABLED=true
mvn clean compile
mvn spring-boot:run

# Windows CMD
cd backend\mrch.backend.somx.fiscal-api
set FLYWAY_ENABLED=true
mvn clean compile
mvn spring-boot:run
```

### Opción 2: Modificar application.properties

```properties
# Cambiar de:
spring.flyway.enabled=${FLYWAY_ENABLED:false}

# A:
spring.flyway.enabled=true
```

Luego:
```bash
mvn clean compile
mvn spring-boot:run
```

### Opción 3: Ejecutar Migraciones Manualmente

```bash
# Ejecutar migraciones sin iniciar la app
mvn flyway:migrate

# Ver estado de migraciones
mvn flyway:info

# Limpiar base de datos (⚠️ PELIGROSO)
mvn flyway:clean
```

---

## 📋 Checklist de Verificación

Después de levantar fiscal-api, verifica:

```sql
-- 1. Verificar que las 16 tablas existen
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 2. Verificar migraciones aplicadas
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY version;

-- 3. Verificar datos iniciales
SELECT COUNT(*) FROM pac_catalog;        -- Debe ser 2
SELECT COUNT(*) FROM version_catalog;    -- Debe ser 3
SELECT COUNT(*) FROM issuer;             -- Debe ser 2
SELECT COUNT(*) FROM receiver;           -- Debe ser 3
SELECT COUNT(*) FROM invoice;            -- Debe ser 2
SELECT COUNT(*) FROM payment_response_catalog;  -- Debe ser 15

-- 4. Verificar constraints
SELECT conname, contype
FROM pg_constraint
WHERE conrelid = 'invoice'::regclass;

-- 5. Verificar indexes
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'invoice';
```

---

## ⚠️ Problemas Comunes y Soluciones

### Problema 1: "Flyway no ejecuta migraciones"

**Causa:** `spring.flyway.enabled=false` (default)

**Solución:**
```bash
export FLYWAY_ENABLED=true
mvn spring-boot:run
```

---

### Problema 2: "Error: relation 'invoice' does not exist"

**Causa:** Hibernate intenta acceder a tablas que no existen porque Flyway no ejecutó

**Solución:**
1. Habilitar Flyway
2. Reiniciar aplicación
3. Verificar logs: `Migrating schema 'public' to version '1 - create fiscal tables'`

---

### Problema 3: "FlywayException: Found non-empty schema without schema history table"

**Causa:** La base de datos tiene tablas pero no tiene `flyway_schema_history`

**Solución:**
```properties
spring.flyway.baseline-on-migrate=true  # Ya configurado
```

O manualmente:
```bash
mvn flyway:baseline
mvn flyway:migrate
```

---

### Problema 4: "Checksum mismatch for migration version 1"

**Causa:** Se modificó V1__create_fiscal_tables.sql después de ejecutarlo

**Solución:**
```sql
-- Opción 1: Reparar (ignora checksum)
mvn flyway:repair

-- Opción 2: Limpiar y re-migrar (⚠️ PIERDE DATOS)
mvn flyway:clean
mvn flyway:migrate
```

---

## 🎯 Conclusiones

### ¿Cómo se crea la base de datos en fiscal-api?

1. **NO se crea automáticamente** (`ddl-auto=none`)
2. **Flyway es el responsable** (pero está deshabilitado por defecto)
3. **Debe habilitarse explícitamente** con `FLYWAY_ENABLED=true`
4. **Ejecuta 3 migraciones SQL** en orden: V0 → V1 → V10

### ¿A qué base de datos apunta?

- **Servidor:** 10.138.153.10:5432
- **Base de datos:** `userapp`
- **Schema:** `public` (o `tenant_fiscal` según configuración)
- **Misma instancia que finanzas-api** pero schemas diferentes

### ¿Qué scripts usa?

1. **V0__drop_all_tables.sql** - Limpia tablas existentes (desarrollo)
2. **V1__create_fiscal_tables.sql** - Crea 16 tablas + 48 indexes + constraints
3. **V10__insert_fiscal_data.sql** - Inserta datos iniciales (catálogos, pruebas)

### Patrón de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    MICROSERVICIOS FBC                       │
├─────────────────────────────┬───────────────────────────────┤
│      finanzas-api           │         fiscal-api            │
│  (Operacional)              │  (Cumplimiento Fiscal)        │
├─────────────────────────────┼───────────────────────────────┤
│ • Node.js + TypeScript      │ • Java + Spring Boot          │
│ • TypeORM migrations        │ • Flyway migrations           │
│ • Schema: tenant_finance    │ • Schema: tenant_fiscal       │
│ • 10 tablas operacionales   │ • 16 tablas CFDI              │
│                             │                               │
│ Tablas:                     │ Tablas:                       │
│ - purchase_order            │ - invoice (CFDI tipo I/E/T)   │
│ - reception                 │ - payments (CFDI tipo P)      │
│ - reception_sku             │ - payment (pagos individuales)│
│ - finanzas_payments         │ - issuer / receiver           │
│ - shipping_guide            │ - pac_catalog                 │
│ - stamped_rebate / rebate   │ - log (timbrado)              │
│ - sap_document              │ - addendum                    │
└─────────────────────────────┴───────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │   PostgreSQL Database Server  │
              │   10.138.153.10:5432          │
              │   Database: userapp           │
              └───────────────────────────────┘
```

---

**Generado el:** 2025-11-10
**Analista:** Claude Code
**Proyectos analizados:** finanzas-api + fiscal-api
