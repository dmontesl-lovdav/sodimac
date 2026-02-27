# STM-1212: Creacion del Modelo Entidad-Relacion en BD (Herramientas y Utilerias)

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1212

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Desarrollo Completado |
| **Modulo** | Herramientas y Utilerias |
| **Fecha Inicio** | 2025-12-08 |
| **Stack** | Node.js + Express + TypeScript + TypeORM |
| **BD** | PostgreSQL |
| **Proyecto** | mrch.backend.somx.utils-api |

---

## Descripcion

Crear las tablas del modulo **Herramientas y Utilerias** en el esquema de base de datos para persistir informacion de catalogos y parametros de configuracion del sistema.

---

## Modelo de Referencia

Ver imagen del modelo: [modelo-bd-utilerias.png](../../arquitectura/modelos-bd/imagenes/modelo-bd-utilerias.png)

### Tablas del Modelo

| # | Modelo Visual (ES) | BD (snake_case) | TypeScript | Estado |
|---|-------------------|-----------------|------------|--------|
| 1 | CatParametro | cat_parameter | CatParameter | **Completado** |
| 2 | CatModulo | cat_module | CatModule | **Completado** |
| 3 | CatMensaje | cat_message | CatMessage | **Completado** |
| 4 | MsgAplicativo | application_msg | ApplicationMsg | **Completado** |
| 5 | CatProceso | cat_process | CatProcess | **Completado** |
| 6 | CatTipoElemento | cat_item_type | CatItemType | **Completado** |
| 7 | CatElemento | cat_item | CatItem | **Completado** |

---

## Convencion de Nomenclatura

> **Basado en**: mrch.backend.somx.finanzas-api

| Aspecto | Convencion | Ejemplo |
|---------|------------|---------|
| **Tablas BD** | snake_case | `cat_parameter` |
| **Columnas BD** | snake_case | `id_parameter`, `created_at` |
| **Clases TypeScript** | PascalCase | `CatParameter` |
| **Propiedades TS** | camelCase | `idParameter`, `createdAt` |
| **Comentarios SQL** | Espanol | `-- Catalogo de parametros` |

---

## Criterios de Aceptacion

### cat_parameter
- [x] **CA-01**: Tabla creada con todas las columnas
- [x] **CA-02**: PK con SERIAL
- [x] **CA-03**: Indices: idx_cat_parameter_name, idx_cat_parameter_id_module
- [x] **CA-04**: Constraint UNIQUE (name, version)

### cat_module
- [x] **CA-05**: Tabla creada con columnas name, description
- [x] **CA-06**: Indice: idx_cat_module_name

### cat_message
- [x] **CA-07**: Tabla creada con message_code, id_message_type
- [x] **CA-08**: Indices: idx_cat_message_code, idx_cat_message_type
- [x] **CA-09**: Constraint UNIQUE (message_code)

### application_msg
- [x] **CA-10**: Tabla creada con id_message, id_application
- [x] **CA-11**: Indices: idx_application_msg_app, idx_application_msg_message
- [x] **CA-12**: Constraint UNIQUE (id_message, id_application)

### cat_process
- [x] **CA-13**: Tabla creada con name, description
- [x] **CA-14**: Indice: idx_cat_process_name

### cat_item_type
- [x] **CA-15**: Tabla creada con name, description
- [x] **CA-16**: Indice: idx_cat_item_type_name

### cat_item
- [x] **CA-17**: Tabla creada con name, description
- [x] **CA-18**: Indice: idx_cat_item_name

---

## Checklist de Desarrollo

### Base de Datos
- [x] Script DDL para cat_parameter
- [x] Migracion TypeORM con todas las tablas
- [ ] Script ejecutado en ambiente DEV
- [ ] Prueba de humo exitosa

### Backend (TypeORM)
- [x] Entidad CatParameter.entity.ts
- [x] Entidad CatModule.entity.ts
- [x] Entidad CatMessage.entity.ts
- [x] Entidad ApplicationMsg.entity.ts
- [x] Entidad CatProcess.entity.ts
- [x] Entidad CatItemType.entity.ts
- [x] Entidad CatItem.entity.ts
- [x] Todas las entidades registradas en datasource

### API REST
- [x] Controllers para todas las entidades
- [x] Services para todas las entidades
- [x] Repositories para todas las entidades
- [x] Routes configuradas
- [x] Documentacion OpenAPI

### Documentacion
- [x] README del JIRA completado
- [x] Coleccion Postman actualizada

---

## API Endpoints

### Parameters (/api/parameters)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/parameters | Listar (filtros: idModule, idType, name, status) |
| GET | /api/parameters/:id | Obtener por ID |
| POST | /api/parameters | Crear |
| PATCH | /api/parameters/:id | Actualizar |
| DELETE | /api/parameters/:id | Eliminar |

### Modules (/api/modules)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/modules | Listar (filtro: name) |
| GET | /api/modules/:id | Obtener por ID |
| POST | /api/modules | Crear |
| PATCH | /api/modules/:id | Actualizar |
| DELETE | /api/modules/:id | Eliminar |

### Messages (/api/messages)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/messages | Listar (filtros: messageCode, idMessageType) |
| GET | /api/messages/code/:code | Obtener por codigo |
| GET | /api/messages/:id | Obtener por ID |
| POST | /api/messages | Crear |
| PATCH | /api/messages/:id | Actualizar |
| DELETE | /api/messages/:id | Eliminar |

### Application Messages (/api/application-messages)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/application-messages | Listar (filtros: idApplication, idMessage) |
| GET | /api/application-messages/:id | Obtener por ID |
| POST | /api/application-messages | Crear |
| DELETE | /api/application-messages/:id | Eliminar |

### Processes (/api/processes)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/processes | Listar (filtro: name) |
| GET | /api/processes/:id | Obtener por ID |
| POST | /api/processes | Crear |
| PATCH | /api/processes/:id | Actualizar |
| DELETE | /api/processes/:id | Eliminar |

### Item Types (/api/item-types)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/item-types | Listar (filtro: name) |
| GET | /api/item-types/:id | Obtener por ID |
| POST | /api/item-types | Crear |
| PATCH | /api/item-types/:id | Actualizar |
| DELETE | /api/item-types/:id | Eliminar |

### Items (/api/items)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/items | Listar (filtro: name) |
| GET | /api/items/:id | Obtener por ID |
| POST | /api/items | Crear |
| PATCH | /api/items/:id | Actualizar |
| DELETE | /api/items/:id | Eliminar |

---

## Archivos Relacionados

### Scripts SQL
| Archivo | Descripcion |
|---------|-------------|
| [STM-1212_cat_parameter.sql](./scripts/STM-1212_cat_parameter.sql) | DDL cat_parameter |
| [STM-1212_smoke_test.sql](./scripts/STM-1212_smoke_test.sql) | Prueba de humo |
| [STM-1212_initial_data.sql](./scripts/STM-1212_initial_data.sql) | Carga inicial de datos |
| [STM-1212_delete_data.sql](./scripts/STM-1212_delete_data.sql) | Borrado de datos |
| [STM-1212_drop_tables.sql](./scripts/STM-1212_drop_tables.sql) | Eliminacion de tablas |
| [07_utils_api_mensajes.sql](../../../backend/mrch.backend.somx.catalogos-api/src/main/resources/db/07_utils_api_mensajes.sql) | Mensajes en catalogos-api |

### Migraciones
| Archivo | Descripcion |
|---------|-------------|
| [1733673600000-CreateCatParameter.ts](../../../backend/mrch.backend.somx.utils-api/src/migrations/1733673600000-CreateCatParameter.ts) | Migracion cat_parameter |
| [1733673700000-CreateUtilsTables.ts](../../../backend/mrch.backend.somx.utils-api/src/migrations/1733673700000-CreateUtilsTables.ts) | Migracion todas las tablas |

### Entidades
| Archivo | Tabla |
|---------|-------|
| [CatParameter.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatParameter.entity.ts) | cat_parameter |
| [CatModule.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatModule.entity.ts) | cat_module |
| [CatMessage.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatMessage.entity.ts) | cat_message |
| [ApplicationMsg.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/ApplicationMsg.entity.ts) | application_msg |
| [CatProcess.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatProcess.entity.ts) | cat_process |
| [CatItemType.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatItemType.entity.ts) | cat_item_type |
| [CatItem.entity.ts](../../../backend/mrch.backend.somx.utils-api/src/entities/CatItem.entity.ts) | cat_item |

### Controllers, Services, Repositories
| Entidad | Controller | Service | Repository | Routes |
|---------|------------|---------|------------|--------|
| Parameter | parameter.controller.ts | parameter.service.ts | parameter.repo.ts | parameter.routes.ts |
| Module | module.controller.ts | module.service.ts | module.repo.ts | module.routes.ts |
| Message | message.controller.ts | message.service.ts | message.repo.ts | message.routes.ts |
| ApplicationMsg | applicationMsg.controller.ts | applicationMsg.service.ts | applicationMsg.repo.ts | applicationMsg.routes.ts |
| Process | process.controller.ts | process.service.ts | process.repo.ts | process.routes.ts |
| ItemType | itemType.controller.ts | itemType.service.ts | itemType.repo.ts | itemType.routes.ts |
| Item | item.controller.ts | item.service.ts | item.repo.ts | item.routes.ts |

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-1212 - Catalogos Utils API.postman_collection.json](../../../postman/STM-1212%20-%20Catalogos%20Utils%20API.postman_collection.json) | Coleccion Postman |
| [Utils-API-BFF.postman_environment.json](../../../postman/Utils-API-BFF.postman_environment.json) | Variables de entorno |

### Sistema de Mensajes (MessageCatalog)
| Archivo | Descripcion |
|---------|-------------|
| [catalogos.config.ts](../../../backend/mrch.backend.somx.utils-api/src/config/catalogos.config.ts) | Configuracion cliente catalogos |
| [catalogos.client.ts](../../../backend/mrch.backend.somx.utils-api/src/clients/catalogos.client.ts) | Cliente HTTP catalogos-api |
| [UtilsMessageCode.ts](../../../backend/mrch.backend.somx.utils-api/src/enums/UtilsMessageCode.ts) | Enum codigos de mensaje |
| [UtilsException.ts](../../../backend/mrch.backend.somx.utils-api/src/exceptions/UtilsException.ts) | Excepcion personalizada |
| [messageCatalog.service.ts](../../../backend/mrch.backend.somx.utils-api/src/services/messageCatalog.service.ts) | Servicio de mensajes |
| [MESSAGE_HANDLING.md](./MESSAGE_HANDLING.md) | Documentacion del sistema |

---

## Como ejecutar

```bash
cd c:\workspace-fbc\backend\mrch.backend.somx.utils-api

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env
# Editar .env con credenciales de BD

# Ejecutar migraciones
npm run migration:run

# Iniciar en desarrollo
npm run dev

# Swagger: http://localhost:3712/docs
```

---

## Notas Tecnicas

- **Sin triggers**: La auditoria (created_at, updated_at) se maneja con TypeORM usando `@CreateDateColumn` y `@UpdateDateColumn`
- **PKs catalogos**: Usar SERIAL (INTEGER autoincremental), no UUID
- **Consistente con**: mrch.backend.somx.finanzas-api
- **Puerto Backend**: 3712
- **Puerto BFF**: 3800

---

## Pendiente para Despliegue

- [ ] Ejecutar migraciones en ambiente DEV
- [ ] Ejecutar prueba de humo
- [ ] Configurar variables de entorno en servidor
- [ ] Desplegar aplicacion
- [ ] Ejecutar script `07_utils_api_mensajes.sql` en catalogos-api
- [ ] **Decidir**: Deprecar cat_message? (ver [MESSAGE_HANDLING.md](./MESSAGE_HANDLING.md))
