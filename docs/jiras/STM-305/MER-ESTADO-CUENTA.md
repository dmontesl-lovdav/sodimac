# STM-305: Modelo Entidad-Relacion (MER)

## Diagrama General

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    ESTADO DE CUENTA                                          │
│                                   Esquema: tenant_fiscal                                     │
└─────────────────────────────────────────────────────────────────────────────────────────────┘

                                 ┌──────────────────────────────┐
                                 │       EstadoCuenta           │
                                 │      (Tabla Control)         │
                                 ├──────────────────────────────┤
                                 │ PK IdEstadoCuenta    BIGINT  │
                                 │    NumeroProveedor   BIGINT  │
                                 │    Anio              INT     │
                                 │    Mes               SMALLINT│
                                 │    Version           INT     │
                                 │    Estatus           INT     │
                                 │    MontoInicial      DECIMAL │
                                 │    MontoFinal        DECIMAL │
                                 │    FechaProceso      DATETIME│
                                 │    FechaRevision     DATETIME│
                                 │    FechaEmision      DATETIME│
                                 │    FechaInicioPeriodo DATE   │
                                 │ FK IdEstadoCuentaAnterior    │────┐ (Self-reference)
                                 └──────────────────────────────┘    │
                                               │                      │
                                               │ 1                    │
                                               │                      │
          ┌────────────────────────────────────┼──────────────────────┘
          │                                    │
          │            ┌───────────────────────┼───────────────────────┐
          │            │                       │                       │
          │            │ N                     │ N                     │ N
          │            ▼                       ▼                       ▼
          │  ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
          │  │EstadoCuentaFacturas │ │EstadoCuentaDesctos  │ │EstadoCuentaNotasCr  │
          │  ├─────────────────────┤ ├─────────────────────┤ ├─────────────────────┤
          │  │PK IdDetalle         │ │PK IdDetalle         │ │PK IdDetalle         │
          │  │FK IdEstadoCuenta    │ │FK IdEstadoCuenta    │ │FK IdEstadoCuenta    │
          │  │   TipoFactura       │ │   NumeroDocumento   │ │   NumeroDocumento   │
          │  │   Serie/Folio/UUID  │ │   Referencia        │ │   Serie/Folio/UUID  │
          │  │   FechaTimbrado     │ │   Serie/Folio/UUID  │ │   Monto             │
          │  │   FechaContabil.    │ │   Importe           │ │   FechaDescuento    │
          │  │   FechaPago         │ │   FechaDescuento    │ │   FechaContabil.    │
          │  │   MonedaOrigen      │ │   FechaContabil.    │ │   MonedaOrigen      │
          │  │   MontoOrigen       │ │   MonedaOrigen      │ │   TasaConversion    │
          │  │   TasaConversion    │ │   TasaConversion    │ │   MontoConversion   │
          │  │   MontoConversion   │ │   ImporteConversion │ │   Estatus           │
          │  │   Estatus           │ │   Estatus           │ └─────────────────────┘
          │  │   IdPago            │ └─────────────────────┘
          │  └─────────────────────┘
          │
          │  ┌───────────────────────────────────────────────────────────────────┐
          │  │                                                                   │
          │  │ N                              N                              N   │
          │  ▼                                ▼                              ▼   │
          │  ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
          │  │ EstadoCuentaPagos   │ │EstadoCuentaOC       │ │EstadoCuentaRecepc.  │
          │  ├─────────────────────┤ ├─────────────────────┤ ├─────────────────────┤
          │  │PK IdDetalle         │ │PK IdDetalle         │ │PK IdDetalle         │
          │  │FK IdEstadoCuenta    │ │FK IdEstadoCuenta    │ │FK IdEstadoCuenta    │
          │  │   IdPago            │ │   NumeroOC          │ │   NumeroRecepcion   │
          │  │   NumeroDocumento   │ │   FechaDocumento    │ │   NumeroOC          │
          │  │   Referencia        │ │   FechaVencimiento  │ │   FechaDocumento    │
          │  │   MonedaOrigen      │ │   MonedaOrigen      │ │   FechaRecepcion    │
          │  │   ImporteOrigen     │ │   MontoOrigen       │ │   FechaVencimiento  │
          │  │   TasaConversion    │ │   TasaConversion    │ │   MonedaOrigen      │
          │  │   ImporteConversion │ │   MontoConversion   │ │   MontoOrigen       │
          │  │   FechaPago         │ │   Estatus           │ │   TasaConversion    │
          │  │   Estatus           │ │   OrigenOC_Id       │ │   MontoConversion   │
          │  └─────────────────────┘ └─────────────────────┘ │   Estatus           │
          │                                                   │   OrigenRecep_Id    │
          │                                                   └─────────────────────┘
          │
          └────────────────────────────────────────────────────────────────────────────
              (Self-reference: IdEstadoCuentaAnterior para encadenamiento de versiones)
```

---

## Relaciones

### Tabla Principal -> Tablas Detalle

| Relacion | Cardinalidad | Descripcion |
|----------|--------------|-------------|
| EstadoCuenta -> EstadoCuentaFacturas | 1:N | Un estado tiene multiples facturas |
| EstadoCuenta -> EstadoCuentaDescuentos | 1:N | Un estado tiene multiples descuentos |
| EstadoCuenta -> EstadoCuentaNotasCredito | 1:N | Un estado tiene multiples notas de credito |
| EstadoCuenta -> EstadoCuentaPagos | 1:N | Un estado tiene multiples pagos |
| EstadoCuenta -> EstadoCuentaOrdenesCompra | 1:N | Un estado tiene multiples OC |
| EstadoCuenta -> EstadoCuentaRecepciones | 1:N | Un estado tiene multiples recepciones |

### Auto-referencia

| Relacion | Cardinalidad | Descripcion |
|----------|--------------|-------------|
| EstadoCuenta -> EstadoCuenta | 1:1 (opcional) | Referencia a la version anterior del mismo periodo |

---

## Flujo de Datos

```
                              ┌─────────────────┐
                              │  PROVEEDOR      │
                              │  (CatProveedor) │
                              └────────┬────────┘
                                       │
                                       │ NumeroProveedor
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ESTADO DE CUENTA                                    │
│                                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐       │
│  │  Facturas   │    │  Descuentos │    │Notas Credito│    │   Pagos     │       │
│  │  Pendientes │    │             │    │             │    │             │       │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘    └──────┬──────┘       │
│         │                  │                  │                  │              │
│         │ (+)              │ (-)              │ (-)              │ (-)          │
│         ▼                  ▼                  ▼                  ▼              │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │                                                                          │   │
│  │  SaldoFinal = SaldoInicial + Facturas - (Pagos + NotasCredito + Desctos) │   │
│  │                                                                          │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                  │
│  ┌─────────────┐    ┌─────────────┐                                             │
│  │  Ordenes    │    │ Recepciones │    (Informativos, no afectan saldo)        │
│  │  de Compra  │    │             │                                             │
│  └─────────────┘    └─────────────┘                                             │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
                              ┌─────────────────┐
                              │      PDF        │
                              │ Estado de Cuenta│
                              └─────────────────┘
```

---

## Ciclo de Vida de Versiones

```
  Mes N-1 (Enero 2026)                    Mes N (Febrero 2026)
  ─────────────────────                   ─────────────────────

  ┌─────────────────────┐                 ┌─────────────────────┐
  │ EstadoCuenta        │                 │ EstadoCuenta        │
  │ Version: 1          │                 │ Version: 1          │
  │ Estatus: Publicado  │                 │ Estatus: Generado   │
  │ MontoFinal: $50,000 │ ──────────────▶ │ MontoInicial: $50,000│
  └─────────────────────┘                 └─────────────────────┘
          │
          │ (Si hay reproceso)
          ▼
  ┌─────────────────────┐
  │ EstadoCuenta        │
  │ Version: 2          │
  │ Estatus: Publicado  │
  │ IdEstadoCuentaAnt: 1│
  │ MontoFinal: $48,000 │ (correccion)
  └─────────────────────┘
          │
          ▼
  La Version 1 cambia a
  Estatus: Reprocesado
```

---

## Conversion de Moneda

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DOCUMENTO ORIGEN                              │
│                                                                      │
│    Factura USD                                                       │
│    ├── MonedaOrigen: USD                                            │
│    └── MontoOrigen: $1,000.00 USD                                   │
│                                                                      │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ Conversion al cierre del mes
                                 │ TasaConversion: 17.50
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    REGISTRO EN ESTADO DE CUENTA                      │
│                                                                      │
│    EstadoCuentaFacturas                                             │
│    ├── MonedaOrigen: USD                                            │
│    ├── MontoOrigen: $1,000.00                                       │
│    ├── TasaConversion: 17.50                                        │
│    ├── MonedaConversion: MXN                                        │
│    └── MontoConversion: $17,500.00 MXN                              │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Indices Requeridos

```sql
-- Tabla principal
CREATE INDEX IX_EstadoCuenta_Proveedor_Periodo
    ON EstadoCuenta (NumeroProveedor, Anio, Mes, Version);

CREATE INDEX IX_EstadoCuenta_Estatus
    ON EstadoCuenta (Estatus);

-- Tablas detalle (todas requieren indice por IdEstadoCuenta)
CREATE INDEX IX_EstadoCuentaFacturas_Estado
    ON EstadoCuentaFacturas (IdEstadoCuenta);

CREATE INDEX IX_EstadoCuentaDescuentos_Estado
    ON EstadoCuentaDescuentos (IdEstadoCuenta);

CREATE INDEX IX_EstadoCuentaNotasCredito_Estado
    ON EstadoCuentaNotasCredito (IdEstadoCuenta);

CREATE INDEX IX_EstadoCuentaPagos_Estado
    ON EstadoCuentaPagos (IdEstadoCuenta);

CREATE INDEX IX_EstadoCuentaOrdenesCompra_Estado
    ON EstadoCuentaOrdenesCompra (IdEstadoCuenta);

CREATE INDEX IX_EstadoCuentaRecepciones_Estado
    ON EstadoCuentaRecepciones (IdEstadoCuenta);
```

---

## Resumen de Tablas

| Tabla | Columnas | Proposito |
|-------|----------|-----------|
| EstadoCuenta | 12 | Control, versionado y saldos |
| EstadoCuentaFacturas | 16 | Facturas PENDIENTE/PAGADA |
| EstadoCuentaDescuentos | 14 | Descuentos comerciales |
| EstadoCuentaNotasCredito | 13 | Notas de credito |
| EstadoCuentaPagos | 11 | Pagos realizados |
| EstadoCuentaOrdenesCompra | 12 | Ordenes de compra |
| EstadoCuentaRecepciones | 14 | Recepciones de mercancia |
| **TOTAL** | **92** | **7 tablas** |

