# STM-305: Creacion del MER del Estado de Cuenta

## Resumen

| Campo | Valor |
|-------|-------|
| **JIRA** | STM-305 |
| **Titulo** | Creacion del MER del estado de cuenta y vistas de consulta |
| **Estado** | **COMPLETADO** |
| **Esquema DB** | `tenant_finance` |
| **Base de Datos** | PostgreSQL (b2b_portal) |

---

## Objetivo

Crear el modelo de datos para el **Estado de Cuenta de Proveedores**, que permite:

1. Generar estados de cuenta mensuales por proveedor
2. Consolidar facturas, pagos, notas de credito y descuentos
3. Soportar versionamiento para reprocesos
4. Generar PDFs para consulta de proveedores

---

## Tablas a Crear (en tenant_finance)

| # | Tabla | Descripcion |
|---|-------|-------------|
| 1 | `account_statement` | Tabla principal - Control y versionado |
| 2 | `account_statement_invoice` | Facturas pendientes y pagadas |
| 3 | `account_statement_discount` | Descuentos comerciales |
| 4 | `account_statement_credit_note` | Notas de credito |
| 5 | `account_statement_payment` | Pagos realizados |
| 6 | `account_statement_purchase_order` | Ordenes de compra |
| 7 | `account_statement_reception` | Recepciones de mercancia |

---

## Formula del Saldo

```
SaldoFinal = SaldoInicial + Facturas - (Pagos + NotasCredito + Descuentos)
```

Donde:
- `SaldoInicial` = SaldoFinal del mes anterior (ultima version publicada)

---

## Documentacion

| Archivo | Contenido |
|---------|-----------|
| **[GUIA-INTEGRACION-ESTADO-CUENTA.md](./GUIA-INTEGRACION-ESTADO-CUENTA.md)** | **Guia para equipo de integracion** |
| [ANALISIS-STM-305.md](./ANALISIS-STM-305.md) | Analisis funcional y tecnico completo |
| [MER-ESTADO-CUENTA.md](./MER-ESTADO-CUENTA.md) | Diagrama Entidad-Relacion visual |
| [NOMENCLATURA-TENANT-FISCAL.md](./NOMENCLATURA-TENANT-FISCAL.md) | Convenciones de nomenclatura |
| [HALLAZGOS-INVESTIGACION.md](./HALLAZGOS-INVESTIGACION.md) | Resultados de investigacion en BD |

---

## Scripts SQL

| # | Script | Descripcion |
|---|--------|-------------|
| 1 | [01_STM-305_create_tables.sql](./01_STM-305_create_tables.sql) | Creacion de las 7 tablas |
| 2 | [02_STM-305_create_indexes.sql](./02_STM-305_create_indexes.sql) | Indices de performance |
| 3 | [03_STM-305_catalog_status.sql](./03_STM-305_catalog_status.sql) | Catalogo CatEstatusEstadoCuenta |
| 4 | [04_STM-305_test_data.sql](./04_STM-305_test_data.sql) | Datos de prueba |
| 5 | [05_STM-305_create_views.sql](./05_STM-305_create_views.sql) | 10 vistas de consulta |
| 6 | [06_STM-305_validate.sql](./06_STM-305_validate.sql) | Script de validacion |

**Orden de ejecucion:**
```bash
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 01_STM-305_create_tables.sql
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 02_STM-305_create_indexes.sql
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 03_STM-305_catalog_status.sql
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 04_STM-305_test_data.sql
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 05_STM-305_create_views.sql
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -f 06_STM-305_validate.sql
```

---

## Estimacion de Horas

| Escenario | Horas | Descripcion |
|-----------|-------|-------------|
| Minimo | 8.5 | Solo tablas + indices |
| **Normal** | **10.5** | Tablas + indices + vistas |
| Maximo | 13.0 | Todo + buffer 25% |

**Recomendacion:** Asignar **10-12 horas**

---

## Nomenclatura (Resuelta)

Revision realizada el 2026-02-06 contra tablas existentes en `tenant_finance`.

| Elemento | Convencion |
|----------|------------|
| Tablas | Ingles, snake_case, singular |
| Columnas | Ingles, snake_case |
| PK | `{tabla}_uuid` UUID con `gen_random_uuid()` |
| FK | `{tabla_ref}_uuid` |
| Auditoria | created_by, created_at, updated_by, updated_at |
| Comentarios | Espanol |

Ver detalle en: [NOMENCLATURA-TENANT-FISCAL.md](./NOMENCLATURA-TENANT-FISCAL.md)

---

## Preguntas Resueltas

| # | Pregunta | Respuesta |
|---|----------|-----------|
| 1 | Convencion de nomenclatura? | **Ingles, snake_case** (tablas y columnas) |
| 2 | Existe catalogo para `Estatus`? | **Crear** `CatEstatusEstadoCuenta` (EEC001-EEC005) |
| 3 | `vendor_number` es FK o referencia logica? | **Referencia logica** (BIGINT, sin FK) |
| 4 | Donde estan las tasas de cambio historicas? | **Materializar** en cada registro (currency, amount, exchange_rate, base_amount) |
| 5 | Se requieren vistas de consulta SQL? | **Si** - 10 vistas creadas |

Ver detalles en: [HALLAZGOS-INVESTIGACION.md](./HALLAZGOS-INVESTIGACION.md)

---

## Proximos Pasos

- [x] Analisis inicial
- [x] Revisar nomenclatura de tablas existentes
- [x] Resolver preguntas pendientes
- [x] Crear scripts DDL (7 tablas + indices)
- [x] Crear catalogo de estatus (CatEstatusEstadoCuenta)
- [x] Crear datos de prueba
- [x] Ejecutar scripts en BD de desarrollo (2026-02-09)
- [x] Crear guia de integracion
- [x] Crear vistas de consulta (10 vistas)
- [x] Validacion completa (34 pruebas PASSED)

---

## Referencias

- [JIRA STM-305](https://jira.falabella.tech/browse/STM-305)
- [Base de Datos](../../BASE-DE-DATOS.md)

