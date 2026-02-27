# STM-305: Analisis Funcional y Tecnico

## Resumen Ejecutivo

**JIRA:** STM-305 - Creacion del MER del estado de cuenta y vistas de consulta
**Fecha de Analisis:** 2026-02-06
**Estado:** En Analisis

---

## 1. Descripcion Funcional

### 1.1 Necesidad del Negocio

El sistema requiere generar **estados de cuenta mensuales por proveedor** que consoliden toda la informacion financiera del periodo:

- Facturas pendientes y pagadas
- Notas de credito aplicadas
- Descuentos comerciales
- Pagos realizados
- Ordenes de compra
- Recepciones de mercancia

**Objetivo principal:** Permitir a los proveedores consultar su situacion financiera con la empresa, con trazabilidad completa y soporte para reprocesos.

### 1.2 Conceptos Clave

#### a) Estado de Cuenta Mensual

| Concepto | Descripcion |
|----------|-------------|
| **Periodo** | Un estado de cuenta cubre exactamente un mes calendario |
| **Proveedor** | Cada estado de cuenta pertenece a un unico proveedor |
| **Version** | Permite multiples versiones del mismo periodo (reprocesos) |
| **Estatus** | Ciclo de vida: Generado -> Publicado -> Revisado/Rechazado/Reprocesado |

#### b) Calculo del Saldo

La formula del saldo final es:

```
SaldoFinal = SaldoInicial + SumaFacturas - (SumaPagos + SumaNotasCredito + SumaDescuentos)
```

**Encadenamiento de saldos:**
- El `SaldoInicial` del mes N = `SaldoFinal` de la ultima version publicada del mes N-1
- Esto garantiza continuidad entre periodos

#### c) Conversion de Moneda

Todas las tablas de detalle almacenan:
- **MonedaOrigen / MontoOrigen**: Valores en la moneda original del documento
- **TasaConversion**: Tipo de cambio utilizado al cierre del mes
- **MonedaConversion / MontoConversion**: Valores convertidos a moneda base (MXN)

Esto permite:
1. Trazabilidad de la moneda original
2. Consultas rapidas en moneda base
3. Reproducibilidad del calculo con la tasa historica

### 1.3 Casos de Uso

| Actor | Necesidad | Funcionalidad |
|-------|-----------|---------------|
| Proveedor | Consultar su estado de cuenta del mes | Ver PDF/detalle del estado de cuenta publicado |
| Proveedor | Marcar como revisado | Cambiar estatus a "Revisado" |
| Proveedor | Reportar discrepancia | Cambiar estatus a "Rechazado" con comentario |
| Finanzas | Generar estados de cuenta | Proceso batch de fin de mes |
| Finanzas | Reprocesar un estado | Crear nueva version sin alterar la anterior |
| Finanzas | Publicar estados | Cambiar estatus de "Generado" a "Publicado" |
| Auditoria | Revisar historico | Consultar versiones anteriores de un periodo |

### 1.4 Ciclo de Vida del Estado de Cuenta

```
[Generado] --> [Publicado] --> [Revisado]
                    |
                    +--> [Rechazado] --> [Reprocesado] --> [Generado v2]
```

| Estatus | Valor | Descripcion |
|---------|-------|-------------|
| Generado | 1 | Recien creado, pendiente de publicar |
| Publicado | 2 | Visible para el proveedor |
| Revisado | 3 | El proveedor confirma que esta correcto |
| Rechazado | 4 | El proveedor reporta discrepancia |
| Reprocesado | 5 | Se creo una nueva version que lo reemplaza |

---

## 2. Modelo de Datos

### 2.1 Diagrama Entidad-Relacion

```
                         ┌───────────────────────────┐
                         │      EstadoCuenta         │
                         │   (Tabla Principal)       │
                         ├───────────────────────────┤
                         │ PK IdEstadoCuenta         │
                         │    NumeroProveedor        │
                         │    Anio, Mes, Version     │
                         │    Estatus                │
                         │    MontoInicial           │
                         │    MontoFinal             │
                         │    FechaProceso           │
                         │    FechaRevision          │
                         │    FechaEmision           │
                         │    FechaInicioPeriodo     │
                         │ FK IdEstadoCuentaAnterior │──┐
                         └───────────────────────────┘  │
                                      │                  │
         ┌────────────────────────────┼──────────────────┘
         │                            │ 1:N
         │    ┌───────────────────────┼───────────────────────┐
         │    │           │           │           │           │
         │    ▼           ▼           ▼           ▼           ▼
         │ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
         │ │Facturas│ │Desctos │ │NotasCr │ │ Pagos  │ │  OC    │
         │ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘
         │                                                  │
         │                                                  ▼
         │                                            ┌────────┐
         └────────────────────────────────────────────│Recepc. │
           (Autoref: version anterior)                └────────┘
```

### 2.2 Tablas Principales

| # | Tabla | Proposito | FK Principal |
|---|-------|-----------|--------------|
| 1 | `EstadoCuenta` | Control, versionado y saldos | - |
| 2 | `EstadoCuentaFacturas` | Facturas pendientes y pagadas | IdEstadoCuenta |
| 3 | `EstadoCuentaDescuentos` | Descuentos comerciales | IdEstadoCuenta |
| 4 | `EstadoCuentaNotasCredito` | Notas de credito | IdEstadoCuenta |
| 5 | `EstadoCuentaPagos` | Pagos realizados | IdEstadoCuenta |
| 6 | `EstadoCuentaOrdenesCompra` | Ordenes de compra | IdEstadoCuenta |
| 7 | `EstadoCuentaRecepciones` | Recepciones de mercancia | IdEstadoCuenta |

### 2.3 Esquema de Base de Datos

Segun la arquitectura del proyecto, estas tablas se crearan en el esquema:

```
Base de datos: b2b_portal
Esquema: tenant_fiscal (Facturacion y Pagos)
```

---

## 3. Especificacion de Tablas

### 3.1 EstadoCuenta (Tabla Control)

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdEstadoCuenta | BIGINT IDENTITY | PK | Identificador unico del estado de cuenta |
| NumeroProveedor | BIGINT | | Id/Numero del proveedor (ref. logica a CatProveedor) |
| Anio | INT | | Ano del estado de cuenta (ej: 2026) |
| Mes | TINYINT | | Mes (1-12) |
| Version | INT | | Version del estado para reprocesos (1, 2, 3...) |
| Estatus | INT | | Catalogo (1=Generado, 2=Publicado, 3=Revisado, 4=Rechazado, 5=Reprocesado) |
| MontoInicial | DECIMAL(18,2) | | Saldo inicial del periodo (viene del mes anterior) |
| MontoFinal | DECIMAL(18,2) | | Saldo final calculado |
| FechaProceso | DATETIME | | Fecha/hora de generacion |
| FechaRevision | DATETIME NULL | | Fecha/hora en que el proveedor lo marco como revisado |
| FechaEmision | DATETIME | | Fecha de emision para el PDF |
| FechaInicioPeriodo | DATE | | Inicio del periodo consolidado |
| IdEstadoCuentaAnterior | BIGINT NULL | FK (self) | Referencia a la version anterior (para encadenamiento) |

**Indices sugeridos:**
- `IX_EstadoCuenta_Proveedor_Periodo`: (NumeroProveedor, Anio, Mes, Version)
- `IX_EstadoCuenta_Estatus`: (Estatus)

**Constraint unico:**
- `UQ_EstadoCuenta`: (NumeroProveedor, Anio, Mes, Version)

### 3.2 EstadoCuentaFacturas

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| TipoFactura | VARCHAR(20) | | PENDIENTE / PAGADA |
| Serie | VARCHAR(50) | | Serie CFDI |
| Folio | VARCHAR(50) | | Folio CFDI |
| UUID | VARCHAR(64) | | UUID de la factura (trazabilidad SAT) |
| FechaTimbrado | DATETIME | | Fecha de timbrado |
| FechaContabilizacion | DATETIME NULL | | Fecha contable |
| FechaPago | DATETIME NULL | | Solo aplica si PAGADA |
| MonedaOrigen | VARCHAR(10) | | Moneda del CFDI (MXN, USD, EUR) |
| MontoOrigen | DECIMAL(18,2) | | Monto total CFDI en moneda origen |
| TasaConversion | DECIMAL(18,6) | | Tasa utilizada al cierre |
| MonedaConversion | VARCHAR(10) | | Moneda base (MXN) |
| MontoConversion | DECIMAL(18,2) | | Monto convertido a base |
| Estatus | VARCHAR(50) | | Estatus de la factura |
| IdPago | BIGINT NULL | | Referencia logica a Pagos.idPago si aplica |

**Indice requerido:**
- `IX_EstadoCuentaFacturas_Estado`: (IdEstadoCuenta)

### 3.3 EstadoCuentaDescuentos

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| NumeroDocumento | VARCHAR(50) | | Numero de documento |
| ReferenciaDocumento | VARCHAR(50) NULL | | Referencia |
| Serie | VARCHAR(50) NULL | | Si aplica |
| Folio | VARCHAR(50) NULL | | Si aplica |
| UUID | VARCHAR(64) NULL | | Si aplica |
| Importe | DECIMAL(18,2) | | Importe del descuento |
| FechaDescuento | DATETIME NULL | | Fecha de descuento |
| FechaContabilizacion | DATETIME NULL | | Fecha contable |
| MonedaOrigen | VARCHAR(10) | | |
| TasaConversion | DECIMAL(18,6) | | |
| MonedaConversion | VARCHAR(10) | | |
| ImporteConversion | DECIMAL(18,2) | | |
| Estatus | VARCHAR(50) | | |

**Indice requerido:**
- `IX_EstadoCuentaDescuentos_Estado`: (IdEstadoCuenta)

### 3.4 EstadoCuentaNotasCredito

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| NumeroDocumento | VARCHAR(50) NULL | | Numero de documento |
| Serie | VARCHAR(50) NULL | | Serie |
| Folio | VARCHAR(50) NULL | | Folio |
| UUID | VARCHAR(64) | | UUID de la nota |
| Monto | DECIMAL(18,2) | | Importe |
| FechaDescuento | DATETIME NULL | | Fecha de emision o descuento |
| FechaContabilizacion | DATETIME NULL | | Fecha contable |
| MonedaOrigen | VARCHAR(10) | | |
| TasaConversion | DECIMAL(18,6) | | |
| MonedaConversion | VARCHAR(10) | | |
| MontoConversion | DECIMAL(18,2) | | |
| Estatus | VARCHAR(50) | | |

**Indice requerido:**
- `IX_EstadoCuentaNotasCredito_Estado`: (IdEstadoCuenta)

### 3.5 EstadoCuentaPagos

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| IdPago | BIGINT NULL | | Referencia logica a Pagos.idPago |
| NumeroDocumento | VARCHAR(50) NULL | | |
| ReferenciaDocumento | VARCHAR(50) NULL | | |
| MonedaOrigen | VARCHAR(10) | | |
| ImporteOrigen | DECIMAL(18,2) | | |
| TasaConversion | DECIMAL(18,6) | | |
| MonedaConversion | VARCHAR(10) | | |
| ImporteConversion | DECIMAL(18,2) | | |
| FechaPago | DATETIME NULL | | |
| Estatus | VARCHAR(50) | | |

**Indice requerido:**
- `IX_EstadoCuentaPagos_Estado`: (IdEstadoCuenta)

### 3.6 EstadoCuentaOrdenesCompra

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| NumeroOC | BIGINT | | OrdenCompra.ordenCompra |
| FechaDocumento | DATE NULL | | OrdenCompra.fechaOrdenCompra |
| FechaVencimiento | DATE NULL | | Politica de negocio |
| MonedaOrigen | VARCHAR(10) | | |
| MontoOrigen | DECIMAL(18,2) | | OrdenCompra.importe |
| TasaConversion | DECIMAL(18,6) | | |
| MonedaConversion | VARCHAR(10) | | |
| MontoConversion | DECIMAL(18,2) | | |
| Estatus | VARCHAR(50) | | OrdenCompra.estatus |
| OrigenOC_Id | BIGINT NULL | | Trazabilidad |

**Indice requerido:**
- `IX_EstadoCuentaOrdenesCompra_Estado`: (IdEstadoCuenta)

### 3.7 EstadoCuentaRecepciones

| Atributo | Tipo | PK/FK | Descripcion |
|----------|------|-------|-------------|
| IdDetalle | BIGINT IDENTITY | PK | Identificador del detalle |
| IdEstadoCuenta | BIGINT | FK | Relacion con el estado |
| NumeroRecepcion | BIGINT | | Recepcion.recepcion |
| NumeroOC | BIGINT | | Recepcion.ordenCompra |
| FechaDocumento | DATE NULL | | Si aplica |
| FechaRecepcion | DATE NULL | | Recepcion.fechaRecepcion |
| FechaVencimiento | DATE NULL | | Politica de negocio |
| MonedaOrigen | VARCHAR(10) | | |
| MontoOrigen | DECIMAL(18,2) | | Recepcion.importe |
| TasaConversion | DECIMAL(18,6) | | |
| MonedaConversion | VARCHAR(10) | | |
| MontoConversion | DECIMAL(18,2) | | |
| Estatus | VARCHAR(50) | | Recepcion.estatus |
| OrigenRecep_Id | BIGINT NULL | | Trazabilidad |

**Indice requerido:**
- `IX_EstadoCuentaRecepciones_Estado`: (IdEstadoCuenta)

---

## 4. Reglas de Negocio

### 4.1 Versionamiento

```
1. Al generar un estado de cuenta:
   - Buscar la version mas alta para (Proveedor, Anio, Mes)
   - Nueva version = version_max + 1 (o 1 si no existe)
   - Nunca modificar versiones anteriores

2. Al reprocesar:
   - Marcar la version actual como "Reprocesado" (5)
   - Crear nueva version con estatus "Generado" (1)
   - Establecer IdEstadoCuentaAnterior para trazabilidad
```

### 4.2 Encadenamiento de Saldos

```sql
-- Obtener saldo inicial para el mes actual
SELECT MontoFinal
FROM EstadoCuenta
WHERE NumeroProveedor = @proveedor
  AND Anio = @anioAnterior OR (Anio = @anio AND Mes = @mesAnterior)
  AND Estatus = 2  -- Solo versiones publicadas
ORDER BY Version DESC
LIMIT 1;
```

### 4.3 Calculo del Saldo Final

```sql
-- Pseudocodigo del calculo
SET @saldoFinal = @montoInicial
                + SUM(facturas.MontoConversion WHERE TipoFactura = 'PENDIENTE')
                - SUM(pagos.ImporteConversion)
                - SUM(notasCredito.MontoConversion)
                - SUM(descuentos.ImporteConversion);
```

### 4.4 Inmutabilidad

> **Regla critica:** Una vez que un estado de cuenta esta en estatus "Publicado" o superior, sus detalles NO se pueden modificar. Para corregir, se debe crear una nueva version.

---

## 5. Consideraciones Tecnicas

### 5.1 Tipo de Base de Datos

El JIRA menciona tipos como `BIGINT IDENTITY` y `TINYINT`, que son sintaxis de **SQL Server**. Sin embargo, el proyecto usa **PostgreSQL**.

**Mapeo de tipos SQL Server -> PostgreSQL:**

| SQL Server | PostgreSQL |
|------------|------------|
| BIGINT IDENTITY | BIGSERIAL |
| INT | INTEGER |
| TINYINT | SMALLINT |
| DATETIME | TIMESTAMP |
| DECIMAL(18,2) | NUMERIC(18,2) |
| VARCHAR(n) | VARCHAR(n) |

### 5.2 Nomenclatura

El JIRA usa **PascalCase** para nombres de tablas y columnas (ej: `EstadoCuenta`, `NumeroProveedor`). Verificar si el proyecto sigue:
- **snake_case**: `estado_cuenta`, `numero_proveedor` (convencion PostgreSQL)
- **PascalCase**: `EstadoCuenta`, `NumeroProveedor` (convencion SQL Server)

**Recomendacion:** Revisar tablas existentes en `tenant_fiscal` para mantener consistencia.

### 5.3 Auditoria

Considerar agregar campos de auditoria estandar:
- `created_by`: Usuario que creo el registro
- `created_at`: Fecha de creacion
- `updated_by`: Usuario que modifico
- `updated_at`: Fecha de modificacion

### 5.4 Performance

El JIRA menciona "consultas rapidas a mes vencido". Considerar:
1. **Indices compuestos** en las tablas de detalle por `IdEstadoCuenta`
2. **Particionamiento** por Anio si el volumen es alto
3. **Vistas materializadas** para reportes frecuentes

---

## 6. Preguntas Pendientes

### 6.1 Clarificaciones Necesarias

| # | Pregunta | Impacto |
|---|----------|---------|
| 1 | Cual es la convencion de nomenclatura del esquema `tenant_fiscal`? (snake_case o PascalCase) | Nombres de tablas y columnas |
| 2 | Existen tablas de catalogo para el campo `Estatus`? O se usa un ENUM/CHECK constraint? | Script DDL |
| 3 | El `NumeroProveedor` es FK a alguna tabla existente o es referencia logica externa? | Relaciones |
| 4 | Que tabla contiene las tasas de cambio historicas para la conversion? | Logica de calculo |
| 5 | Se requieren triggers para validar inmutabilidad de versiones publicadas? | Logica de negocio |
| 6 | El campo `IdPago` en Facturas/Pagos es FK a que tabla? | Relaciones |
| 7 | Se necesitan vistas de consulta ademas de las tablas? (El titulo menciona "vistas") | Alcance |

### 6.2 Suposiciones Actuales

- Esquema: `tenant_fiscal`
- Nomenclatura: Por confirmar (el JIRA usa PascalCase)
- Moneda base: MXN
- No hay FKs estrictas a tablas externas (OrdenCompra, Recepcion, Pago son referencias logicas)

---

## 7. Entregables Esperados

Basado en el analisis, los entregables para STM-305 serian:

| # | Entregable | Descripcion |
|---|------------|-------------|
| 1 | Script DDL | `01_STM-305_create_tables.sql` - Creacion de las 7 tablas |
| 2 | Script Indices | `02_STM-305_create_indexes.sql` - Indices de performance |
| 3 | Script Constraints | `03_STM-305_constraints.sql` - FKs y checks |
| 4 | Script Datos Catalogo | `04_STM-305_catalog_data.sql` - Valores de estatus |
| 5 | Datos de Prueba | `05_STM-305_test_data.sql` - Datos para validacion |
| 6 | Vistas (si aplica) | `06_STM-305_views.sql` - Vistas de consulta |
| 7 | Diagrama MER | Diagrama visual actualizado |

---

## 8. Referencias

- **JIRA:** [STM-305](https://jira.falabella.tech/browse/STM-305)
- **Esquema destino:** `tenant_fiscal`
- **Base de datos:** PostgreSQL (b2b_portal)

---

## Proximos Pasos

1. **Resolver preguntas pendientes** (seccion 6.1)
2. **Revisar nomenclatura** de tablas existentes en `tenant_fiscal`
3. **Crear scripts DDL** una vez confirmada la nomenclatura
4. **Revisar con el equipo** antes de ejecutar

