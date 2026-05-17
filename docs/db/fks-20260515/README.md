# Agregar FKs faltantes a tenant_fiscal + tenant_finance

> Fecha: 2026-05-15
> Validado en: BD local (`b2b_portal` PG 16 puerto 5434)
> Estado UAT: PENDIENTE (requiere autorización Bonelli/equipo)

## Contexto

ER del proyecto declara múltiples relaciones entre tablas. BD `b2b_portal` tenía solo 16 FKs declaradas (todas en `tenant_finance`); **`tenant_fiscal` tenía 0 FKs**. Integridad referencial dependía 100% del código TypeORM.

Riesgos:
- Inserts directos / ETL pueden dejar huérfanos sin error.
- DBeaver/pgAdmin no muestran relaciones visualmente.
- Optimizador PG menos eficiente.

Este script normaliza la BD para reflejar lo que el ER declara.

## Ejecución (orden estricto)

Todos los scripts son transaccionales (BEGIN/COMMIT). En caso de error, ROLLBACK automático.

### 1. Auditoría — conteo de huérfanos

```bash
psql -U postgres -d b2b_portal -f 01-audit-fiscal.sql
psql -U postgres -d b2b_portal -f 02-audit-finanzas.sql
```

Resultado esperado: filas con `huerfanos = 0`. Si hay valores >0, pasa al paso 2.

**Resultado en local (referencia)**:
- fiscal: 2 en `tax→invoice`, 2 en `related_documents→invoice`
- finanzas: 4 en `stamped_rebate→fiscal.invoice`

### 2. Identificar huérfanos específicos

```bash
psql -U postgres -d b2b_portal -f 03-identify-orphans.sql
```

Lista los registros concretos con su PK + referencia rota + fecha. Insumo para decidir DELETE vs SET NULL.

### 3. Limpieza huérfanos

```bash
psql -U postgres -d b2b_portal -v ON_ERROR_STOP=1 -f 04-cleanup-orphans.sql
```

Estrategia:
- **`tax` y `related_documents`**: columna FK es NOT NULL → DELETE (con cascada manual a tablas hijas).
- **`stamped_rebate.invoice_fiscal_uuid`**: columna NULLABLE → SET NULL (preserva registro, solo rompe vínculo a CFDI inexistente — estado válido "aún no timbrado").

Crea 3 tablas de backup con sufijo `_orphan_backup_20260515` antes de borrar. Si necesitas revertir:
```sql
INSERT INTO tenant_fiscal.tax SELECT * FROM tenant_fiscal.tax_orphan_backup_20260515;
INSERT INTO tenant_fiscal.related_documents SELECT * FROM tenant_fiscal.related_documents_orphan_backup_20260515;
-- stamped_rebate: solo restaurar valor de columna
UPDATE tenant_finance.stamped_rebate sr
SET invoice_fiscal_uuid = b.invoice_fiscal_uuid
FROM tenant_finance.stamped_rebate_orphan_backup_20260515 b
WHERE sr.stamped_rebate_uuid = b.stamped_rebate_uuid;
```

### 4. Agregar FKs

```bash
psql -U postgres -d b2b_portal -v ON_ERROR_STOP=1 -f 05-add-fks.sql
```

**Idempotente**: usa bloque PL/pgSQL con `IF NOT EXISTS` → solo crea las FKs faltantes. Puede correrse múltiples veces sin error.

Crea 25 FKs (21 en fiscal, 4 faltantes en finanzas).

## Estado final (validado local)

| Schema | FKs antes | FKs después |
|---|---|---|
| `tenant_fiscal` | 0 | **21** |
| `tenant_finance` | 16 | **20** |
| **Total** | **16** | **41** |

UNIQUE constraints requeridos (la BD ya los tenía):
- `tenant_fiscal.invoice (fiscal_uuid)` → `uq_invoice_fiscal_uuid`
- `tenant_finance.stamped_rebate (document_number)` → `uq_stamped_rebate_doc`

## Permisos requeridos

`postgres` (superuser). El usuario de app `wwwb2bportal` no es owner de las tablas → no puede hacer ALTER. Si el equipo de BD tiene políticas distintas, ajustar usuario antes de correr.

## Para correr en UAT

Mismo orden. **Antes de empezar**:

1. ✅ Autorización de Bonelli + equipo BD.
2. ✅ Backup completo BD UAT (no solo las 3 tablas de huérfanos).
3. ✅ Ventana de mantenimiento (los DELETE pueden bloquear queries breves).
4. ✅ Verificar que UAT tenga los UNIQUE constraints en `invoice.fiscal_uuid` y `stamped_rebate.document_number`. Si no, agregar primero.
5. ✅ Correr `01-audit-*.sql` para saber qué huérfanos existen en UAT — pueden ser distintos a local.

**Si en UAT hay huérfanos diferentes**: ajustar `04-cleanup-orphans.sql` según hallazgos. NO ejecutar a ciegas.

## Riesgos / consideraciones

- **Performance**: agregar FK en tabla grande puede tomar minutos (validación de toda la data existente). Ejecutar en ventana baja carga.
- **Cascade**: el script NO usa `ON DELETE CASCADE`. Eliminar un padre con hijos fallará — comportamiento intencional para no destruir data por accidente.
- **Cross-schema FK** (`stamped_rebate.invoice_fiscal_uuid → tenant_fiscal.invoice.fiscal_uuid` y `addendum_manual.invoice_uuid → tenant_fiscal.invoice.invoice_uuid`): PostgreSQL las soporta nativamente. Asegurar que ambos schemas residan en la misma DB.
- **Cliente de app (TypeORM)**: el código no necesita cambios. Las entities ya declaran las relaciones; solo se sincroniza BD con código.

## Referencias

- ER fuente: diagrama Modelo Fiscal + Modelo Financiero (PNG entregado por equipo).
- Wiki: [Módulo Fiscal](../../wiki/modulos/fiscal.md), [Módulo Finanzas](../../wiki/modulos/finanzas.md).
- JIRA relacionado: ninguno específico — esto es deuda histórica de migraciones.
