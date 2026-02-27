# STM-305: Hallazgos de Investigacion

## Fecha
2026-02-06

---

## 1. Esquema de Base de Datos

### 1.1 Esquemas Existentes

| Esquema | Tablas | Proposito |
|---------|--------|-----------|
| `tenant_finance` | 18 | Finanzas: OC, Recepciones, Pagos, CxP |
| `tenant_fiscal` | 22 | Fiscal: Facturas, Impuestos, CFDI |
| `tenant_catalogs` | 0 | Catalogos tenant (vacio actualmente) |
| `shared_catalogs` | 22+ | Catalogos compartidos, mensajes |

### 1.2 Tablas en tenant_finance

```
accounts_payable            finanzas_payments           rebate
activity_logs               fiscal_payments             reception
addendum_manual             migrations                  reception_sku
pac_catalog                 purchase_order              sap_document
shipping_guide              shipping_guide_document     stamped_rebate
shipping_guide_purchase_order                           vendor_block
version_catalog
```

### 1.3 Conclusion

> **Las tablas de Estado de Cuenta deben crearse en el esquema `tenant_finance`**, junto con las demas tablas financieras (purchase_order, reception, accounts_payable, etc.).

---

## 2. NumeroProveedor (vendor_number)

### 2.1 Patron Existente

| Tabla | Columna | Tipo | Nullable |
|-------|---------|------|----------|
| purchase_order | vendor_number | BIGINT | NOT NULL |
| accounts_payable | vendor_number | INTEGER | NOT NULL |

### 2.2 Entidad TypeORM (PurchaseOrder)

```typescript
@Column({ name: 'vendor_number', type: 'bigint', nullable: false })
supplierNumber!: number;
```

### 2.3 Relacion con Proveedor

- **No hay FK estricta** a tabla de proveedores
- Es una **referencia logica** al numero de proveedor
- El catalogo de proveedores (`cat_supplier`) esta definido en codigo pero la tabla no existe en BD

### 2.4 Conclusion

> `numero_proveedor` debe ser tipo **BIGINT** (no INTEGER) como referencia logica, sin FK estricta.

---

## 3. Catalogos de Estatus

### 3.1 Catalogos Existentes

| ID | Codigo | Nombre | Modulo |
|----|--------|--------|--------|
| 15 | CatEstatusFactura | Estatus Factura | fiscal |
| 17 | CatEstatusPago | Estatus Pago | fiscal |
| 3 | CatEstatusRegistro | Estatus Registro | general |
| 19 | CatEstatusComplemento | Estatus Complemento | - |

### 3.2 Patron de Elementos

Ejemplo de `CatEstatusFactura`:

| Key | Descripcion |
|-----|-------------|
| EFA001 | Rechazo Comercial |
| EFA002 | Pendiente Addenda |
| EFA003 | Recibido Parcial |
| EFA004 | Pendiente de Contabilizar |
| EFA005 | En proceso de descarga |
| ... | ... |
| EFA009 | Pagado |
| EFA011 | Completado |

### 3.3 Propuesta para Estado de Cuenta

Crear catalogo `CatEstatusEstadoCuenta` con elementos:

| Key | internal_status | Descripcion |
|-----|-----------------|-------------|
| EEC001 | 1 | Generado |
| EEC002 | 2 | Publicado |
| EEC003 | 3 | Revisado |
| EEC004 | 4 | Rechazado |
| EEC005 | 5 | Reprocesado |

### 3.4 Conclusion

> El campo `status` en las tablas debe referenciar los valores `internal_status` del catalogo (1, 2, 3, 4, 5), o alternativamente usar las claves (EEC001, EEC002, etc.) segun el patron existente.

---

## 4. Conversion de Moneda

### 4.1 Patron en accounts_payable

```typescript
@Column({ name: 'currency', type: 'varchar', length: 3, default: 'MXN' })
currency!: string;

@Column({ name: 'exchange_rate', type: 'numeric', precision: 18, scale: 6, default: 1 })
exchangeRate!: number;

@Column({ name: 'amount', type: 'numeric', precision: 15, scale: 2 })
amount!: number;
```

### 4.2 Diferencia con JIRA

| JIRA Propone | Patron Existente |
|--------------|------------------|
| MonedaOrigen + MontoOrigen | currency + amount |
| TasaConversion | exchange_rate |
| MonedaConversion + MontoConversion | (no existe, se calcula) |

### 4.3 Conclusion

> Se puede seguir el esquema del JIRA (guardar monto origen y monto convertido por separado) para **materializar** los valores y evitar calculos en tiempo de consulta. Esto es consistente con el requerimiento de "consultas rapidas a mes vencido".

---

## 5. Nomenclatura Final

### 5.1 Convenciones Confirmadas

| Elemento | Convencion | Ejemplo |
|----------|------------|---------|
| Tablas | snake_case | `estado_cuenta` |
| Columnas | snake_case | `numero_proveedor` |
| PK | `{tabla}_id` o `{tabla}_uuid` | `estado_cuenta_id` |
| FK | `{tabla_ref}_id` | `estado_cuenta_id` |
| Auditoria | 4 campos | created_by, created_at, updated_by, updated_at |
| Timestamps | TIMESTAMP | `default: () => 'CURRENT_TIMESTAMP'` |

### 5.2 Mapeo Final de Tablas

| JIRA (PascalCase) | PostgreSQL (snake_case) |
|-------------------|-------------------------|
| EstadoCuenta | `estado_cuenta` |
| EstadoCuentaFacturas | `estado_cuenta_factura` |
| EstadoCuentaDescuentos | `estado_cuenta_descuento` |
| EstadoCuentaNotasCredito | `estado_cuenta_nota_credito` |
| EstadoCuentaPagos | `estado_cuenta_pago` |
| EstadoCuentaOrdenesCompra | `estado_cuenta_orden_compra` |
| EstadoCuentaRecepciones | `estado_cuenta_recepcion` |

---

## 6. Resumen de Decisiones

| # | Pregunta | Respuesta |
|---|----------|-----------|
| 1 | Esquema? | `tenant_finance` |
| 2 | Nomenclatura? | snake_case |
| 3 | NumeroProveedor? | BIGINT, referencia logica (sin FK) |
| 4 | Catalogo estatus? | Crear `CatEstatusEstadoCuenta` en shared_catalogs |
| 5 | Conversion moneda? | Materializar ambos valores (origen + convertido) |
| 6 | Vistas SQL? | Pendiente confirmar con el equipo |

---

## 7. Proximos Pasos

1. [x] Investigar esquema correcto
2. [x] Investigar patron NumeroProveedor
3. [x] Investigar catalogos de estatus
4. [ ] Crear script DDL para tablas
5. [ ] Crear script para catalogo de estatus
6. [ ] Crear datos de prueba

