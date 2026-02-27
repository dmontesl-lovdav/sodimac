# STM-829: Desarrollo de Pantalla de Pagos

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-829

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado (Backend) |
| **Modulo** | Fiscal API / Portal Proveedores |
| **BFF** | https://dev.fbusinesscenter.com/ppsomx/fiscal |
| **API Backend** | mrch.backend.somx.fiscal-api (Puerto 8082) |
| **Desarrollador Backend** | David Montes |
| **Solicitante** | Jose Luis |
| **Fecha** | Enero 2025 |

---

## Descripcion

> Como usuario del modulo financiero, quiero consultar y visualizar los pagos realizados a las facturas publicadas por los proveedores, para contar con visibilidad, trazabilidad y control de los pagos efectuados, asi como facilitar la consulta y seguimiento de la informacion financiera.

---

## Criterios de Aceptacion

### CA-01 - Acceso a la tarjeta Pagos
```
Dado que el usuario ingresa al modulo financiero
Cuando seleccione la tarjeta "Pagos"
Entonces el sistema debe mostrar la pantalla de consulta de pagos.
```
> **Backend:** No aplica - Implementacion frontend

### CA-02 - Validacion de filtros obligatorios
```
Dado que el usuario intenta realizar una busqueda
Cuando no capture la fecha inicio o fecha fin
Entonces el sistema no debe permitir la consulta
Y debera indicar que los campos son obligatorios.
```
> **Backend:** Validacion debe hacerse en frontend. El servicio acepta los parametros `fechaPagoInicio` y `fechaPagoFin`.

### CA-03 - Visualizacion por perfil
```
Dado que el usuario inicia sesion
Cuando el perfil sea administrador
Entonces debe visualizar el filtro Numero de proveedor.

Cuando el perfil no sea administrador
Entonces el filtro no debe mostrarse.
```
> **Backend:** El parametro `numeroProveedor` esta disponible. El frontend controla su visibilidad segun perfil.

### CA-04 - Visualizacion de resultados
```
Dado que existen pagos registrados
Cuando se ejecute la busqueda
Entonces el sistema debe mostrar el grid con paginacion de 10 registros
Y con los campos definidos.
```
> **Backend:** Usar `?page=0&size=10` para paginacion de 10 registros.

### CA-05 - Navegacion a factura
```
Dado que el usuario selecciona el boton Ver factura
Cuando se realice la accion
Entonces el sistema debe redirigir a la pantalla de facturacion
Y filtrar la informacion usando el numero de documento y el id del proveedor.
```
> **Backend:** Usar endpoint `/invoices/search` con folio y RFC del emisor.

### CA-06 - Exportacion de informacion
```
Dado que el usuario tiene resultados en pantalla
Cuando seleccione descargar
Entonces el sistema debe generar un archivo CSV con los registros visibles.
```
> **Backend:** El frontend puede usar los datos de la respuesta para generar CSV.

### CA-07 - Limpieza de filtros
```
Dado que el usuario ha aplicado filtros
Cuando seleccione Limpiar filtros
Entonces el sistema debe restablecer los campos a su estado inicial.
```
> **Backend:** No aplica - Implementacion frontend

---

## Reglas de Negocio

### Estructura y Navegacion
- Crear una nueva tarjeta dentro del modulo financiero con el nombre "Pagos"
- El desarrollo debera estar versionado dentro del modulo financiero
- Agregar un boton Regresar, que lleve a la pantalla principal de la tarjeta financiera

### Filtros de Busqueda
| Filtro | Obligatorio | Visible |
|--------|-------------|---------|
| Fecha inicio de pago | **SI** | Siempre |
| Fecha fin de pago | **SI** | Siempre |
| Numero de proveedor | No | Solo administrador |
| Numero de documento | No | Siempre |
| Numero de referencia | No | Siempre |

### Grid de Resultados
Paginacion de **10 registros** con los siguientes campos:

| # | Campo | Descripcion | Campo en API |
|---|-------|-------------|--------------|
| 1 | idPago | UUID del pago | `paymentUuid` |
| 2 | Numero documento | Folio del complemento | `folio` (de payments) |
| 3 | Referencia documento | Serie del complemento | `series` (de payments) |
| 4 | Numero proveedor | RFC del emisor | `issuerRfc` |
| 5 | Nombre proveedor | Nombre del emisor | `issuerName` |
| 6 | Moneda | MXN, USD, etc. | `currency` |
| 7 | Importe | Monto del pago | `amount` |
| 8 | Tipo de documento | Fijo | "Complemento de Pago" |
| 9 | Documento SAP | Numero de operacion | `operationNumber` |
| 10 | Fecha de pago | Fecha del pago | `paymentDate` |
| 11 | Estatus | Estado del pago | `status` / `statusDescription` |
| 12 | Fecha de registro | Fecha creacion | `createdAt` |
| 13 | Fecha de actualizacion | Fecha modificacion | `updatedAt` |

---

## Endpoints Disponibles

### URL Base BFF DEV
```
https://dev.fbusinesscenter.com/ppsomx/fiscal
```

### 1. Listar Pagos
```http
GET /payments
GET /payments?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "paymentUuid": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
      "paymentsUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
      "paymentDate": "2025-11-20",
      "paymentMethod": "03",
      "currency": "MXN",
      "amount": 1800.00,
      "operationNumber": "OP-001-2024",
      "exchangeRate": 1.000000
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Obtener Pago por UUID
```http
GET /payments/{paymentUuid}
```

**Response:**
```json
{
  "paymentUuid": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
  "paymentsUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "paymentDate": "2025-11-20",
  "paymentMethod": "03",
  "currency": "MXN",
  "amount": 1800.00,
  "operationNumber": "OP-001-2024"
}
```

### 3. Listar Documentos de Pago (Complementos)
```http
GET /payment-documents
GET /payment-documents?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "paymentsUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
      "version": 2.0,
      "paymentDate": "2025-11-20",
      "folio": "001",
      "series": "P",
      "status": 1,
      "xmlContent": "<cfdi:Comprobante>...</cfdi:Comprobante>"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 4. Buscar Facturas (para navegacion CA-05)
```http
POST /invoices/search
Content-Type: application/json
```

**Request:**
```json
{
  "folio": "001",
  "rfcEmisor": "SOD970101ABC",
  "fechaInicioRecepcion": "2024-01-01",
  "fechaFinalRecepcion": "2025-12-31",
  "tipoDocumento": "I",
  "page": 0,
  "size": 10
}
```

---

## Como Probar

### 1. Listar todos los pagos
```bash
curl -s "https://dev.fbusinesscenter.com/ppsomx/fiscal/payments" \
  -H "Accept: application/json"
```

### 2. Listar pagos con paginacion (10 registros - CA-04)
```bash
curl -s "https://dev.fbusinesscenter.com/ppsomx/fiscal/payments?page=0&size=10" \
  -H "Accept: application/json"
```

### 3. Obtener pago por UUID
```bash
curl -s "https://dev.fbusinesscenter.com/ppsomx/fiscal/payments/bbbbbbbb-cccc-dddd-eeee-ffffffffffff" \
  -H "Accept: application/json"
```

### 4. Listar documentos de pago
```bash
curl -s "https://dev.fbusinesscenter.com/ppsomx/fiscal/payment-documents?page=0&size=10" \
  -H "Accept: application/json"
```

---

## Codigos de Estatus de Pago

| Codigo | Descripcion |
|--------|-------------|
| 0 | Cancelado |
| 1 | Vigente |
| 2 | Pendiente |
| 3 | Rechazado |

---

## Modelo de Datos

### Tablas Principales
```
+------------------+       +------------------+
|     payments     |       |     payment      |
+------------------+       +------------------+
| payments_uuid PK |<------| payment_uuid PK  |
| fiscal_uuid      |       | payments_uuid FK |
| version          |       | payment_date     |
| payment_date     |       | payment_method   |
| issuer_uuid FK   |       | currency         |
| receiver_uuid FK |       | amount           |
| folio            |       | operation_number |
| series           |       | exchange_rate    |
| xml_content      |       +------------------+
| status           |
+------------------+
        |
+------------------+
|     issuer       |
+------------------+
| issuer_uuid PK   |
| name             |
| rfc              |
| tax_regime       |
+------------------+
```

### Relaciones
- Un **complemento** (payments) puede tener multiples **pagos** (payment)
- `payments.issuer_uuid` → `issuer.issuer_uuid` (Proveedor/Emisor)
- `payments.receiver_uuid` → `receiver.receiver_uuid` (Sodimac/Receptor)
- `payment.payments_uuid` → `payments.payments_uuid`

---

## Datos de Prueba en DEV

| Campo | Valor |
|-------|-------|
| paymentUuid | `bbbbbbbb-cccc-dddd-eeee-ffffffffffff` |
| paymentsUuid | `aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee` |
| paymentDate | `2025-11-20` |
| currency | `MXN` |
| amount | `1800.00` |
| operationNumber | `OP-001-2024` |
| folio | `001` |
| series | `P` |
| status | `1` (Vigente) |
| issuerRfc | `SOD970101ABC` |
| issuerName | `SODIMAC MEXICO S.A. DE C.V.` |

---

## Archivos Relacionados

### Colecciones Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-829_Payments_Screen_BFF-DEV.postman_collection.json](./STM-829_Payments_Screen_BFF-DEV.postman_collection.json) | Coleccion BFF DEV (URLs reales) |

### Consultas SQL
| Archivo | Descripcion |
|---------|-------------|
| [queries-validacion.sql](./queries-validacion.sql) | Consultas para validar datos en BD |

---

## Conexion a Base de Datos

```
Host: 10.138.153.10
Puerto: 5432
Base de datos: userapp
Schema: tenant_fiscal
Usuario: postgres
Password: Sodim@cP0str3s
```

---

## Notas para Frontend (Jose Luis)

1. **Fechas obligatorias (CA-02):** Validar en frontend que `fechaPagoInicio` y `fechaPagoFin` esten informados antes de llamar al servicio.

2. **Perfil administrador (CA-03):** El parametro `numeroProveedor` siempre esta disponible en el endpoint. Controlar visibilidad en frontend segun perfil.

3. **Paginacion (CA-04):** Usar `?page=0&size=10` para obtener 10 registros por pagina.

4. **Navegacion a factura (CA-05):** Al hacer clic en "Ver factura", llamar a `/invoices/search` con el `folio` y `rfcEmisor` del pago seleccionado.

5. **Exportacion CSV (CA-06):** El servicio retorna datos paginados. Para exportar:
   - Hacer multiples llamadas incrementando `page`
   - O usar un `size` mayor para obtener mas registros

6. **Limpiar filtros (CA-07):** Restablecer campos en frontend y llamar al servicio sin filtros (solo con paginacion).

---

## Checklist de Validacion

### Backend
- [x] Endpoint `/payments` responde correctamente
- [x] Endpoint `/payments/{uuid}` responde correctamente
- [x] Endpoint `/payment-documents` responde correctamente
- [x] Paginacion funciona con `page` y `size`
- [x] Datos coinciden con base de datos

### Frontend (Jose Luis)
- [ ] Tarjeta "Pagos" creada en modulo financiero
- [ ] Filtros de fecha obligatorios validados
- [ ] Filtro numero proveedor visible solo para admin
- [ ] Grid muestra 10 registros por pagina
- [ ] Boton "Ver factura" navega correctamente
- [ ] Exportacion CSV funciona
- [ ] Boton "Limpiar filtros" funciona

---

## Contacto

**Desarrollador Backend:** David Montes
**Proyecto:** APP03022-mrch.backend.somx.fiscal-api
