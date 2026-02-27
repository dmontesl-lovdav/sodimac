# CORRECCIONES DE ESQUEMA - Migraciones Flyway

## Fecha: 2025-11-11
## Archivos Modificados: V1__create_fiscal_tables.sql, V10__insert_fiscal_data.sql

---

## PROBLEMA IDENTIFICADO

Las migraciones V1 y V10 **NO incluían el prefijo de esquema `tenant_fiscal.`** en las sentencias SQL, lo que causaba que las tablas se crearan e insertaran datos en el esquema público por defecto en lugar del esquema tenant_fiscal.

### Impacto:

- Las tablas se creaban en el esquema incorrecto
- Los datos se insertaban en el esquema incorrecto
- Inconsistencia con V13 y scripts auxiliares que SÍ usan tenant_fiscal
- Conflictos potenciales con entidades JPA que esperan tenant_fiscal
- Problemas de búsqueda y acceso a datos

---

## ANÁLISIS PREVIO

### Archivos Analizados:

1. **V1__create_fiscal_tables.sql** - ❌ Requiere corrección
   - 16 CREATE TABLE sin esquema
   - 6 ALTER TABLE sin esquema
   - 56 CREATE INDEX sin esquema
   - ~16 COMMENT ON TABLE sin esquema
   - ~100+ COMMENT ON COLUMN sin esquema
   - ~20 FOREIGN KEY REFERENCES sin esquema

2. **V10__insert_fiscal_data.sql** - ❌ Requiere corrección
   - 24 INSERT INTO sin esquema
   - ~30 subconsultas WHERE NOT EXISTS sin esquema
   - 1 UPDATE sin esquema

3. **V13__create_tax_tables.sql** - ✅ Ya correcto
   - Todas las sentencias usan tenant_fiscal.

4. **generate_1000_payments.sql** - ✅ Ya correcto
   - Todas las sentencias usan tenant_fiscal.

5. **delete_1000_payments.sql** - ✅ Ya correcto
   - Todas las sentencias usan tenant_fiscal.

---

## CORRECCIONES APLICADAS

### 1. V1__create_fiscal_tables.sql (78 líneas corregidas)

#### Backup Creado:
```
c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql.backup
```

#### Tipos de Correcciones:

**A. CREATE TABLE (16 tablas):**
```sql
-- ANTES
CREATE TABLE pac_catalog (

-- DESPUÉS
CREATE TABLE tenant_fiscal.pac_catalog (
```

Tablas corregidas:
1. pac_catalog
2. version_catalog
3. issuer
4. receiver
5. authorized_receiver_catalog
6. invoice
7. addendum
8. related_cfdi
9. related_documents
10. equivalence_dr
11. payment
12. payments
13. totals
14. log
15. payment_response_catalog
16. payment_file_registry

**B. ALTER TABLE (6 statements):**
```sql
-- ANTES
ALTER TABLE payment ADD CONSTRAINT fk_payment_payments

-- DESPUÉS
ALTER TABLE tenant_fiscal.payment ADD CONSTRAINT fk_payment_payments
```

Líneas: 475, 479, 721, 729, 733, 738

**C. CREATE INDEX (56 índices):**
```sql
-- ANTES
CREATE INDEX idx_invoice_issuer_uuid ON invoice(issuer_uuid);

-- DESPUÉS
CREATE INDEX idx_invoice_issuer_uuid ON tenant_fiscal.invoice(issuer_uuid);
```

Líneas: 571-755

**D. COMMENT ON TABLE (16 statements):**
```sql
-- ANTES
COMMENT ON TABLE invoice IS 'Comprobantes fiscales digitales (CFDI)';

-- DESPUÉS
COMMENT ON TABLE tenant_fiscal.invoice IS 'Comprobantes fiscales digitales (CFDI)';
```

**E. COMMENT ON COLUMN (~100+ statements):**
```sql
-- ANTES
COMMENT ON COLUMN invoice.fiscal_uuid IS 'UUID del TimbreFiscalDigital del SAT';

-- DESPUÉS
COMMENT ON COLUMN tenant_fiscal.invoice.fiscal_uuid IS 'UUID del TimbreFiscalDigital del SAT';
```

**F. FOREIGN KEY REFERENCES (~20 statements):**
```sql
-- ANTES
FOREIGN KEY (issuer_uuid) REFERENCES issuer(issuer_uuid)

-- DESPUÉS
FOREIGN KEY (issuer_uuid) REFERENCES tenant_fiscal.issuer(issuer_uuid)
```

---

### 2. V10__insert_fiscal_data.sql (~55 líneas corregidas)

#### Backup Creado:
```
c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql.backup
```

#### Tipos de Correcciones:

**A. INSERT INTO (24 statements):**
```sql
-- ANTES
INSERT INTO issuer (
    issuer_uuid,
    name,
    rfc,
    tax_regime,
    created_by
)

-- DESPUÉS
INSERT INTO tenant_fiscal.issuer (
    issuer_uuid,
    name,
    rfc,
    tax_regime,
    created_by
)
```

Tablas afectadas:
- issuer (2 inserts)
- receiver (2 inserts)
- invoice (2 inserts)
- addendum (1 insert)
- related_cfdi (1 insert)
- payments (1 insert)
- payment (1 insert)
- related_documents (1 insert)
- equivalence_dr (1 insert)
- totals (1 insert)
- log (múltiples inserts)

**B. WHERE NOT EXISTS Subqueries (~30 occurrences):**
```sql
-- ANTES
WHERE NOT EXISTS (SELECT 1 FROM issuer WHERE issuer_uuid = ...)

-- DESPUÉS
WHERE NOT EXISTS (SELECT 1 FROM tenant_fiscal.issuer WHERE issuer_uuid = ...)
```

Tablas en subconsultas:
- issuer
- receiver
- invoice
- addendum
- related_cfdi
- payments
- payment
- related_documents
- equivalence_dr
- totals
- log

**C. UPDATE Statements (1 statement):**
```sql
-- ANTES
UPDATE invoice SET ...

-- DESPUÉS
UPDATE tenant_fiscal.invoice SET ...
```

---

## SCRIPTS DE CORRECCIÓN CREADOS

### 1. fix_v1_schema.ps1
PowerShell script que aplica todas las correcciones a V1 automáticamente usando expresiones regulares.

**Ubicación:** `c:\workspace-fbc\fix_v1_schema.ps1`

**Ejecución:**
```powershell
powershell -ExecutionPolicy Bypass -File "c:\workspace-fbc\fix_v1_schema.ps1"
```

### 2. fix_v10_schema.ps1
PowerShell script que aplica todas las correcciones a V10 automáticamente.

**Ubicación:** `c:\workspace-fbc\fix_v10_schema.ps1`

**Ejecución:**
```powershell
powershell -ExecutionPolicy Bypass -File "c:\workspace-fbc\fix_v10_schema.ps1"
```

---

## VERIFICACIÓN POST-CORRECCIÓN

### Verificar que V1 tiene esquemas correctos:

```bash
# Buscar CREATE TABLE sin esquema
grep -n "CREATE TABLE [^t]" c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql

# Buscar INSERT INTO sin esquema
grep -n "INSERT INTO [^t]" c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql

# Verificar que todos usan tenant_fiscal
grep -c "tenant_fiscal\." c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql
```

**Resultado Esperado:**
- 0 CREATE TABLE sin esquema (excepto línea 8: CREATE EXTENSION IF NOT EXISTS)
- Más de 200 ocurrencias de "tenant_fiscal."

### Verificar que V10 tiene esquemas correctos:

```bash
# Buscar INSERT INTO sin esquema
grep -n "INSERT INTO [^t]" c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql

# Buscar FROM sin esquema en subconsultas
grep -n "FROM [^t]" c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql

# Verificar que todos usan tenant_fiscal
grep -c "tenant_fiscal\." c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql
```

**Resultado Esperado:**
- 0 INSERT INTO sin esquema
- 0 FROM sin esquema (en WHERE NOT EXISTS)
- Más de 50 ocurrencias de "tenant_fiscal."

---

## CÓMO APLICAR ESTAS CORRECCIONES

### Opción 1: Si Flyway NO se ha ejecutado aún

Si las migraciones aún no se han aplicado a la base de datos:

1. ✅ Los archivos ya están corregidos
2. Ejecutar Flyway normalmente
3. Las tablas se crearán en tenant_fiscal correctamente

### Opción 2: Si Flyway YA ejecutó V1 y V10 (checksum mismatch)

Si Flyway ya aplicó las versiones originales, tienes 3 opciones:

#### Opción 2A: Reparar Flyway y aplicar correcciones (RECOMENDADO para DEV)

```sql
-- 1. Eliminar registros de Flyway
DELETE FROM public.flyway_schema_history WHERE version IN ('1', '10');

-- 2. Eliminar esquema y tablas (SOLO EN DESARROLLO)
DROP SCHEMA IF EXISTS tenant_fiscal CASCADE;
CREATE SCHEMA tenant_fiscal;

-- 3. Re-ejecutar Flyway (aplicará V1, V10, V13 corregidos)
```

#### Opción 2B: Migrar datos del esquema public a tenant_fiscal

```sql
-- 1. Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS tenant_fiscal;

-- 2. Mover tablas de public a tenant_fiscal
ALTER TABLE public.pac_catalog SET SCHEMA tenant_fiscal;
ALTER TABLE public.version_catalog SET SCHEMA tenant_fiscal;
ALTER TABLE public.issuer SET SCHEMA tenant_fiscal;
ALTER TABLE public.receiver SET SCHEMA tenant_fiscal;
ALTER TABLE public.authorized_receiver_catalog SET SCHEMA tenant_fiscal;
ALTER TABLE public.invoice SET SCHEMA tenant_fiscal;
ALTER TABLE public.addendum SET SCHEMA tenant_fiscal;
ALTER TABLE public.related_cfdi SET SCHEMA tenant_fiscal;
ALTER TABLE public.related_documents SET SCHEMA tenant_fiscal;
ALTER TABLE public.equivalence_dr SET SCHEMA tenant_fiscal;
ALTER TABLE public.payment SET SCHEMA tenant_fiscal;
ALTER TABLE public.payments SET SCHEMA tenant_fiscal;
ALTER TABLE public.totals SET SCHEMA tenant_fiscal;
ALTER TABLE public.log SET SCHEMA tenant_fiscal;
ALTER TABLE public.payment_response_catalog SET SCHEMA tenant_fiscal;
ALTER TABLE public.payment_file_registry SET SCHEMA tenant_fiscal;

-- 3. Actualizar checksums de Flyway para V1 y V10
UPDATE public.flyway_schema_history
SET checksum = (SELECT checksum FROM (VALUES (1, NULL), (10, NULL)) AS t(version, checksum) WHERE t.version = flyway_schema_history.version::int)
WHERE version IN ('1', '10');
```

#### Opción 2C: Usar repair de Flyway (solo actualiza checksums)

```bash
# Ejecutar repair de Flyway
mvn flyway:repair

# Esto actualizará los checksums pero NO moverá las tablas
# Necesitarás mover las tablas manualmente con Opción 2B
```

---

## RESUMEN DE CAMBIOS

| Archivo | Total Líneas Modificadas | Tipos de Cambios |
|---------|-------------------------|------------------|
| V1__create_fiscal_tables.sql | 78+ | CREATE TABLE (16), ALTER TABLE (6), CREATE INDEX (56), COMMENT ON TABLE (16), COMMENT ON COLUMN (100+), FK REFERENCES (20) |
| V10__insert_fiscal_data.sql | ~55 | INSERT INTO (24), WHERE NOT EXISTS (30+), UPDATE (1) |

**Total:** ~133+ líneas corregidas

---

## ARCHIVOS DE RESPALDO

Antes de aplicar las correcciones, se crearon backups:

1. `V1__create_fiscal_tables.sql.backup`
2. `V10__insert_fiscal_data.sql.backup`

Para restaurar las versiones originales (si es necesario):

```bash
# Restaurar V1
cp c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql.backup c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V1__create_fiscal_tables.sql

# Restaurar V10
cp c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql.backup c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\db\migration\V10__insert_fiscal_data.sql
```

---

## ESTADO FINAL DE LAS MIGRACIONES

| Archivo | Estado | Esquema Correcto |
|---------|--------|------------------|
| V1__create_fiscal_tables.sql | ✅ CORREGIDO | tenant_fiscal |
| V10__insert_fiscal_data.sql | ✅ CORREGIDO | tenant_fiscal |
| V13__create_tax_tables.sql | ✅ YA CORRECTO | tenant_fiscal |
| generate_1000_payments.sql | ✅ YA CORRECTO | tenant_fiscal |
| delete_1000_payments.sql | ✅ YA CORRECTO | tenant_fiscal |

---

## NOTAS ADICIONALES

1. **JPA Entities:** Las entidades Java ya están configuradas para usar tenant_fiscal a través de application.properties o @Table annotations. Estas correcciones alinean las migraciones con las entidades.

2. **Consistencia:** Ahora todas las migraciones y scripts auxiliares usan el mismo esquema (tenant_fiscal).

3. **Flyway Checksums:** Estas correcciones cambiarán los checksums de V1 y V10. Si Flyway ya ejecutó estas migraciones, necesitarás usar repair o limpiar la historia.

4. **Testing:** Después de aplicar las correcciones, verificar que:
   - Todas las tablas existen en tenant_fiscal
   - Los datos de prueba se insertaron correctamente
   - Las relaciones FK funcionan correctamente
   - Los endpoints de la API funcionan

5. **search_path de PostgreSQL:** Asegurarse de que el search_path incluye tenant_fiscal:
   ```sql
   SHOW search_path;
   -- Debe mostrar: tenant_fiscal, public (o similar)
   ```

---

**Fin del Changelog de Correcciones de Esquema**
