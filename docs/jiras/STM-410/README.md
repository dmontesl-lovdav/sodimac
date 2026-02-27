# STM-410: Ajuste de Servicio de Facturacion y Deuda Tecnica

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-410

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :yellow_circle: Pendiente |
| **Asignado** | Por asignar |
| **Sprint** | Por definir |
| **Prioridad** | Alta |
| **Proyecto** | fiscal-api |
| **Dependencia** | STM-1166 (Tren de Estatus) |

---

## Descripcion

Ajustar el servicio de facturacion para ser consumido por procesos y paginas web, incluyendo:
- Nuevos metodos de consulta con estatus obligatorio
- Validacion de estatus contra catalogo (tren de estatus)
- Nuevo metodo de actualizacion de estatus individual
- Integracion con servicio de tren de estatus (STM-1166)

---

## Criterios de Aceptacion

### Escenario 1: Consulta valida de facturas por estatus y fechas
- **Dado** que el usuario ingresa un estatus valido y un rango de fechas correcto
- **Cuando** realiza la consulta de facturas
- **Entonces** el sistema debe mostrar el listado de facturas correspondiente
- **Y** permitir navegar los resultados por paginas
- **Y** respetar el numero de registros por pagina definido

### Escenario 2: Consulta con estatus no catalogado
- **Dado** que el usuario ingresa un estatus que no esta catalogado para la opcion
- **Cuando** intenta realizar la consulta
- **Entonces** el sistema debe mostrar el mensaje WRN7011
- **Y** no ejecutar la busqueda

### Escenario 3: Consulta filtrada por proveedor
- **Dado** que el usuario incluye el identificador de proveedor en la consulta
- **Cuando** ejecuta la busqueda
- **Entonces** el sistema debe mostrar unicamente las facturas asociadas a dicho proveedor

### Escenario 4: Actualizacion de estatus permitida
- **Dado** que una factura tiene un estatus origen valido
- **Y** el estatus destino esta permitido conforme al tren de estatus
- **Cuando** se solicita la actualizacion
- **Entonces** el sistema debe actualizar el estatus de la factura
- **Y** registrar la fecha de contabilizacion si fue proporcionada

### Escenario 5: Actualizacion de estatus no permitida
- **Dado** que el usuario intenta actualizar una factura a un estatus no permitido
- **Cuando** se valida el cambio contra el tren de estatus
- **Entonces** el sistema no debe actualizar la factura
- **Y** debe mostrar un mensaje de advertencia indicando que no fue posible realizar el cambio

---

## Analisis de Implementacion

### Lo que YA EXISTE en fiscal-api

| Componente | Ubicacion | Estado |
|------------|-----------|--------|
| InvoiceController | `controller/InvoiceController.java` | Existe |
| POST /invoices/search | Linea ~180 | Existe (estatus opcional) |
| PUT /invoices | Linea ~120 | Existe (actualiza addenda + estatus) |
| InvoiceSearchRequest | `model/dto/InvoiceSearchRequest.java` | Existe |
| InvoiceStatus enum | `model/enums/InvoiceStatus.java` | Existe (transiciones hardcodeadas) |
| validateStatusTransition() | `service/impl/InvoiceServiceImpl.java` | Existe (usa enums locales) |

### Lo que FALTA implementar

| Componente | Descripcion | Prioridad |
|------------|-------------|-----------|
| **Nuevo endpoint consulta** | POST /invoices/search/by-status (estatus obligatorio) | Alta |
| **Validacion WRN7011** | Validar estatus contra catalogo antes de buscar | Alta |
| **Nuevo endpoint actualizacion** | PUT /invoices/{uuid}/status (solo estatus) | Alta |
| **Integracion STM-1166** | Consumir servicio de tren de estatus | Alta |
| **Campo fecha contabilizacion** | Solo si destino = 12 (Pendiente de Pago) | Media |
| **StatusTrainApiService** | Cliente para consumir catalogos-api | Alta |

---

## Nuevos Endpoints a Implementar

### 1. Consulta de Facturas por Estatus (Nuevo)

```
POST /invoices/search/by-status
```

**Request Body:**
```json
{
  "estatus": 3,
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-06-30",
  "idProveedor": 1234567890,
  "tipoDocumento": "I",
  "page": 0,
  "size": 20
}
```

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| estatus | Integer | **SI** | Estatus de la factura |
| fechaInicioRecepcion | LocalDate | **SI** | Fecha inicio |
| fechaFinalRecepcion | LocalDate | **SI** | Fecha fin |
| idProveedor | BigDecimal | No | Numero de proveedor |
| tipoDocumento | String | **SI** | "I" o "E" |
| page | Integer | No | Pagina (default 0) |
| size | Integer | No | Registros por pagina (default 20) |

**Validaciones:**
1. Antes de ejecutar la busqueda, validar que el estatus origen esta catalogado
2. Si no existe -> retornar WRN7011

**Response:** Mismo formato que `/invoices/search` actual

---

### 2. Actualizacion de Estatus Individual (Nuevo)

```
PUT /invoices/{uuid}/status
```

**Path Parameter:**
- `uuid`: UUID fiscal de la factura

**Request Body:**
```json
{
  "idProveedor": 1234567890,
  "estatusOrigen": 3,
  "estatusDestino": 4,
  "fechaContabilizacion": "2025-01-15T10:30:00"
}
```

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| idProveedor | BigDecimal | **SI** | Numero de proveedor |
| estatusOrigen | Integer | **SI** | Estatus actual esperado |
| estatusDestino | Integer | **SI** | Nuevo estatus |
| fechaContabilizacion | LocalDateTime | No | Solo se guarda si destino = 12 |

**Validaciones:**
1. Validar que la factura existe (por UUID fiscal)
2. Validar que pertenece al proveedor
3. Validar que el estatus actual coincide con estatusOrigen
4. Consumir STM-1166 para validar transicion permitida
5. Si transicion no permitida -> retornar mensaje del servicio

**Response exitosa (200):**
```json
{
  "success": true,
  "code": "BUS3010",
  "message": "Estatus actualizado exitosamente",
  "invoiceUuid": "uuid-interno",
  "fiscalUuid": "uuid-fiscal",
  "estatusAnterior": 3,
  "estatusNuevo": 4,
  "estatusNuevoNombre": "Proceso de descarga",
  "fechaContabilizacion": null,
  "fechaActualizacion": "2025-01-12T15:30:00"
}
```

**Response - Transicion no permitida (400):**
```json
{
  "success": false,
  "code": "WRN7011",
  "message": "El estatus destino no existe catalogado. Por favor, valide la informacion antes de continuar."
}
```

---

## Integracion con STM-1166

### Nuevo Servicio: StatusTrainApiService

```java
public interface StatusTrainApiService {

    /**
     * Valida si una transicion de estatus es permitida.
     *
     * @param idOpcion ID de la opcion (1=Factura, 2=NC)
     * @param estatusOrigen Estatus actual
     * @param estatusDestino Estatus destino
     * @return StatusTrainValidationResponse
     */
    StatusTrainValidationResponse validateTransition(
        Integer idOpcion,
        Integer estatusOrigen,
        Integer estatusDestino
    );

    /**
     * Verifica si un estatus origen esta catalogado.
     *
     * @param idOpcion ID de la opcion
     * @param estatusOrigen Estatus a validar
     * @return true si existe, false si no
     */
    boolean isStatusCataloged(Integer idOpcion, Integer estatusOrigen);

    /**
     * Obtiene los estatus destino permitidos desde un origen.
     *
     * @param idOpcion ID de la opcion
     * @param estatusOrigen Estatus actual
     * @return Lista de estatus destino permitidos
     */
    List<StatusTrainDto> getAllowedDestinations(
        Integer idOpcion,
        Integer estatusOrigen
    );
}
```

### Configuracion

```properties
# application.properties
statusTrain.api.enabled=${STATUS_TRAIN_API_ENABLED:true}
statusTrain.api.url=${STATUS_TRAIN_API_URL:http://localhost:8083}
statusTrain.api.timeout-ms=${STATUS_TRAIN_API_TIMEOUT:5000}
```

---

## Informacion de Factura a Visualizar

Segun el JIRA, se deben visualizar todos los elementos de:

### Comprobante
- Serie, Folio, Version
- Fecha Emision, Fecha Certificacion
- Total, Subtotal, Descuento
- Moneda, Tipo de Cambio
- Metodo de Pago, Forma de Pago
- Condiciones de Pago
- Lugar de Expedicion
- UUID Fiscal
- Estatus, Nombre Estatus

### Emisor
- RFC, Nombre, Regimen Fiscal

### Receptor
- RFC, Nombre, Regimen Fiscal

### Addenda
- Tiene Addenda (boolean)
- Tipo Addenda, Nombre Tipo
- Numero Proveedor
- Numero Orden Compra
- Numero Recepcion
- Guia Entrega
- Tipo Proveedor

**Nota:** El `InvoiceSearchResponse` actual ya incluye todos estos campos.

---

## Mensajes de Catalogo

| Codigo | Tipo | Mensaje | Uso |
|--------|------|---------|-----|
| WRN7010 | Advertencia | El estatus origen no existe catalogado... | Consulta/Actualizacion |
| WRN7011 | Advertencia | El estatus destino no existe catalogado... | Consulta/Actualizacion |
| BUS3010 | Exito | Estatus actualizado exitosamente | Actualizacion |
| BUS3011 | Error | Transicion de estatus no permitida | Actualizacion |

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos
- [x] Analisis tecnico completado
- [ ] Diseno aprobado por el equipo

### Pre-requisitos
- [ ] **STM-1166 implementado y desplegado** (BLOQUEANTE)

### Backend (fiscal-api)
- [ ] StatusTrainApiService (interface)
- [ ] StatusTrainApiServiceImpl (cliente REST)
- [ ] InvoiceStatusSearchRequest (nuevo DTO)
- [ ] InvoiceStatusUpdateRequest (nuevo DTO)
- [ ] InvoiceStatusUpdateResponse (nuevo DTO)
- [ ] Nuevo endpoint POST /invoices/search/by-status
- [ ] Nuevo endpoint PUT /invoices/{uuid}/status
- [ ] Validacion WRN7011 antes de busqueda
- [ ] Integracion con servicio tren de estatus
- [ ] Logica fecha contabilizacion (solo destino = 12)
- [ ] Configuracion en application.properties

### Testing
- [ ] Pruebas unitarias
- [ ] Pruebas de integracion
- [ ] Coleccion Postman

### Documentacion
- [ ] README actualizado
- [ ] OpenAPI/Swagger actualizado
- [ ] BFF api.yml actualizado

---

## Archivos a Crear/Modificar

### Nuevos Archivos

```
src/main/java/com/sodimac/fiscal/api/
├── model/dto/
│   ├── InvoiceStatusSearchRequest.java
│   ├── InvoiceStatusUpdateRequest.java
│   └── InvoiceStatusUpdateResponse.java
├── service/
│   ├── StatusTrainApiService.java
│   └── impl/
│       └── StatusTrainApiServiceImpl.java
```

### Archivos a Modificar

```
src/main/java/com/sodimac/fiscal/api/
├── controller/
│   └── InvoiceController.java (nuevos endpoints)
├── service/
│   ├── InvoiceService.java (nuevos metodos)
│   └── impl/
│       └── InvoiceServiceImpl.java (implementacion)
├── model/enums/
│   └── FiscalMessageCode.java (nuevos codigos)
src/main/resources/
└── application.properties (config statusTrain.api)
```

---

## Orden de Implementacion Sugerido

1. **Implementar STM-1166 primero** (catalogos-api)
   - Crear tabla CAT_TREN_ESTATUS
   - Implementar endpoints de consulta/validacion
   - Migrar datos de enums actuales

2. **Implementar STM-410** (fiscal-api)
   - Crear StatusTrainApiService
   - Crear nuevos DTOs
   - Implementar endpoint POST /invoices/search/by-status
   - Implementar endpoint PUT /invoices/{uuid}/status
   - Agregar validaciones WRN7010, WRN7011

---

## Referencias

- [STM-1166 - Tren de Estatus](../STM-1166/README.md)
- [InvoiceController.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java)
- [InvoiceServiceImpl.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java)
- [InvoiceStatus.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/InvoiceStatus.java)
