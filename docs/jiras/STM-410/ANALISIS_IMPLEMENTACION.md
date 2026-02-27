# STM-410 + STM-1166: Analisis de Implementacion

## Resumen Ejecutivo

### Dependencia Critica

```
STM-1166 (Tren de Estatus)  ──►  STM-410 (Ajuste Facturacion)
      catalogos-api                    fiscal-api
```

**STM-1166 DEBE implementarse ANTES que STM-410.**

---

## Comparativa: Estado Actual vs Requerido

### Consulta de Facturas

| Aspecto | Estado Actual | Requerido STM-410 |
|---------|---------------|-------------------|
| Endpoint | POST /invoices/search | POST /invoices/search/by-status |
| Estatus | Opcional | **Obligatorio** |
| Validacion estatus | No existe | Validar contra catalogo (WRN7011) |
| Proveedor | Opcional | Opcional |
| Paginacion | Si | Si |

### Actualizacion de Estatus

| Aspecto | Estado Actual | Requerido STM-410 |
|---------|---------------|-------------------|
| Endpoint | PUT /invoices | PUT /invoices/{uuid}/status |
| Parametros | uuid, proveedor, estatus, addenda | uuid, proveedor, estatusOrigen, estatusDestino, fechaContabilizacion |
| Validacion transicion | Enum local (hardcoded) | Servicio externo STM-1166 |
| Fecha contabilizacion | No existe | Solo si destino = 7 (Pendiente de Pago) |

### Validacion de Transiciones

| Aspecto | Estado Actual | Requerido |
|---------|---------------|-----------|
| Ubicacion | InvoiceStatus.java (enum) | CAT_TREN_ESTATUS (BD catalogos) |
| Modificacion | Requiere redespliegue | Dinamico via BD |
| Auditoria | No existe | Usuario + fechas |
| Consistencia | Por proyecto | Centralizado |

---

## Plan de Implementacion

### Fase 1: STM-1166 - Tren de Estatus (catalogos-api)

**Duracion estimada:** A definir por el equipo

#### 1.1 Base de Datos
```sql
-- Crear tabla en esquema core_catalogs
CREATE TABLE cat_tren_estatus (
    id_configuracion SERIAL PRIMARY KEY,
    id_opcion INTEGER NOT NULL,
    estatus_origen INTEGER NOT NULL,
    estatus_destino INTEGER NOT NULL,
    id_usuario_registro BIGINT NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    id_usuario_actualizacion BIGINT,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT uk_tren_estatus UNIQUE (id_opcion, estatus_origen, estatus_destino)
);

CREATE INDEX idx_tren_estatus_opcion_origen
ON cat_tren_estatus(id_opcion, estatus_origen);
```

#### 1.2 Migracion de Datos (desde enums actuales)
```sql
-- Facturas (id_opcion = 1)
-- Transiciones desde InvoiceStatus.java
INSERT INTO cat_tren_estatus (id_opcion, estatus_origen, estatus_destino, id_usuario_registro) VALUES
-- PENDIENTE_ADDENDA (1) -> 2, 3, 13
(1, 1, 2, 1), (1, 1, 3, 1), (1, 1, 13, 1),
-- RECIBIDO_PARCIAL (2) -> 3, 13
(1, 2, 3, 1), (1, 2, 13, 1),
-- PENDIENTE_CONTABILIZAR (3) -> 4
(1, 3, 4, 1),
-- PROCESO_DESCARGA (4) -> 5, 11
(1, 4, 5, 1), (1, 4, 11, 1),
-- DESGLOSE_FACTURA (5) -> 6, 11
(1, 5, 6, 1), (1, 5, 11, 1),
-- PENDIENTE_ENVIO_CONTABILIZAR (6) -> 7, 11
(1, 6, 7, 1), (1, 6, 11, 1),
-- PENDIENTE_PAGO (7) -> 8
(1, 7, 8, 1),
-- PAGADO (8) -> 9
(1, 8, 9, 1),
-- PENDIENTE_COMPLEMENTO (9) -> 10
(1, 9, 10, 1),
-- RECHAZO_CONTABLE (11) -> 7
(1, 11, 7, 1);

-- Notas de Credito (id_opcion = 2)
-- Similar para CreditNoteStatus.java
INSERT INTO cat_tren_estatus (id_opcion, estatus_origen, estatus_destino, id_usuario_registro) VALUES
(2, 1, 2, 1), (2, 1, 3, 1),
(2, 2, 3, 1),
(2, 3, 4, 1),
(2, 4, 5, 1), (2, 4, 11, 1),
(2, 5, 6, 1), (2, 5, 11, 1),
(2, 6, 7, 1), (2, 6, 11, 1),
(2, 7, 8, 1),
(2, 11, 7, 1);
```

#### 1.3 Backend (catalogos-api)
- [ ] StatusTrainEntity.java
- [ ] StatusTrainRepository.java
- [ ] StatusTrainDto.java
- [ ] StatusTrainService.java
- [ ] StatusTrainServiceImpl.java
- [ ] StatusTrainController.java
- [ ] Endpoints:
  - GET /api/status-train/validate
  - GET /api/status-train/allowed-destinations
  - POST /api/status-train
  - PUT /api/status-train/{id}

#### 1.4 Mensajes de Catalogo
- [ ] Agregar WRN7010 en CatMsgAdvertencia
- [ ] Agregar WRN7011 en CatMsgAdvertencia

---

### Fase 2: STM-410 - Ajuste Facturacion (fiscal-api)

**Duracion estimada:** A definir por el equipo
**Prerequisito:** STM-1166 desplegado

#### 2.1 Configuracion
```properties
# application.properties
statusTrain.api.enabled=${STATUS_TRAIN_API_ENABLED:true}
statusTrain.api.url=${STATUS_TRAIN_API_URL:http://localhost:8083}
statusTrain.api.timeout-ms=${STATUS_TRAIN_API_TIMEOUT:5000}
```

#### 2.2 Cliente para STM-1166
- [ ] StatusTrainApiService.java (interface)
- [ ] StatusTrainApiServiceImpl.java (RestTemplate)
- [ ] StatusTrainValidationResponse.java (DTO)

#### 2.3 Nuevos DTOs
- [ ] InvoiceStatusSearchRequest.java
- [ ] InvoiceStatusUpdateRequest.java
- [ ] InvoiceStatusUpdateResponse.java

#### 2.4 Nuevos Endpoints
- [ ] POST /invoices/search/by-status
- [ ] PUT /invoices/{uuid}/status

#### 2.5 Logica de Negocio
- [ ] Validar estatus catalogado antes de busqueda (WRN7011)
- [ ] Consumir STM-1166 para validar transiciones
- [ ] Guardar fechaContabilizacion solo si destino = 7 (PENDIENTE_PAGO)

#### 2.6 Actualizacion de Codigos
- [ ] Agregar BUS3010, BUS3011 en FiscalMessageCode.java

---

## Matriz de Riesgos

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| STM-1166 no disponible | Alto | STM-1166 es prerequisito obligatorio, no hay fallback |
| Latencia servicio externo | Medio | Configurar timeout adecuado |
| Datos inconsistentes en migracion | Alto | Script de validacion post-migracion |
| Cambios en transiciones durante desarrollo | Bajo | BD centralizada permite ajustes sin redespliegue |

---

## Decisiones Confirmadas

| Pregunta | Decision |
|----------|----------|
| **Fallback si STM-1166 no disponible** | NO hay fallback. STM-1166 debe estar disponible |
| **Cache de reglas** | NO cachear. Consultar siempre al servicio |
| **IDs de opciones** | 1=Factura (I), 2=NC (E), 3=Pagos (P), 4=CartaPorte (T) |
| **Estatus para fecha contabilizacion** | 7 "Pendiente de Pago" (según código InvoiceStatus.java, usar enum configurable) |

---

## Estimacion de Esfuerzo

### STM-1166 (catalogos-api)
| Componente | Esfuerzo |
|------------|----------|
| DDL + Migracion datos | Bajo |
| Entidad + Repository | Bajo |
| Service + Controller | Medio |
| Pruebas | Medio |
| **Total** | **Medio** |

### STM-410 (fiscal-api)
| Componente | Esfuerzo |
|------------|----------|
| StatusTrainApiService | Bajo |
| Nuevos DTOs | Bajo |
| Nuevos endpoints | Medio |
| Validaciones | Medio |
| Pruebas | Medio |
| **Total** | **Medio-Alto** |

---

## Conclusion

**Recomendacion:** Implementar STM-1166 primero ya que:
1. Es dependencia bloqueante de STM-410
2. Centraliza las reglas de transicion para todo el sistema
3. Permite modificar transiciones sin redespliegue
4. Agrega trazabilidad y auditoria

Una vez desplegado STM-1166, STM-410 puede implementarse consumiendo el servicio centralizado.
