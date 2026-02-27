# STM-1212: Manejo de Mensajes en utils-api

> **Fecha**: 2025-12-08
> **Autor**: Equipo Desarrollo
> **Estado**: Implementado (Steps 1-3), Pendiente Decision (Step 4)

## Resumen

Este documento describe la implementacion del sistema de mensajes centralizado para `utils-api`, siguiendo el patron establecido en `fiscal-api` para consumir mensajes desde `catalogos-api`.

---

## Arquitectura de Mensajes

```
┌─────────────────┐     HTTP GET      ┌──────────────────┐
│   utils-api     │ ───────────────►  │  catalogos-api   │
│                 │   /message/{key}  │                  │
│ MessageCatalog  │ ◄───────────────  │ dictionary_lang  │
│    Service      │     {description} │ catalog_detail   │
└─────────────────┘                   └──────────────────┘
        │
        │ (fallback si catalogos-api no disponible)
        ▼
┌─────────────────┐
│ UtilsMessage    │
│   Fallback      │
│   (enum)        │
└─────────────────┘
```

---

## Componentes Implementados

### Step 1: Scripts SQL para catalogos-api

**Archivo**: `backend/mrch.backend.somx.catalogos-api/src/main/resources/db/07_utils_api_mensajes.sql`

| Tipo | Rango dict_id | Rango key | Cantidad |
|------|---------------|-----------|----------|
| Errores Tecnicos (ERR) | 6000-6019 | ERR100-ERR107 | 8 |
| Errores Negocio (BUS) | 6020-6049 | BUS100-BUS111 | 12 |
| Exito (RES) | 6050-6069 | RES100-RES104 | 5 |
| Informativos (INF) | 6070-6089 | INF100-INF102 | 3 |
| Advertencias (WRN) | 6090-6099 | WRN100-WRN102 | 3 |

**Idiomas soportados**: Espanol (1), Ingles (2), Portugues (3)

### Step 2: Cliente HTTP para catalogos-api

**Archivos creados**:
- `src/config/catalogos.config.ts` - Configuracion
- `src/clients/catalogos.client.ts` - Cliente HTTP con cache

**Variables de entorno**:
```env
CATALOGOS_API_URL=http://catalogos-api:8081
CATALOGOS_API_ENABLED=true
CATALOGOS_API_LANG=1
CATALOGOS_API_TIMEOUT=5000
CATALOGOS_API_CACHE_TTL=300000
```

### Step 3: MessageCatalogService

**Archivos creados**:
- `src/enums/UtilsMessageCode.ts` - Enum de codigos y fallback
- `src/exceptions/UtilsException.ts` - Excepcion personalizada
- `src/services/messageCatalog.service.ts` - Servicio de mensajes

**Archivo actualizado**:
- `src/middlewares/errorHandler.ts` - Soporte para UtilsException

---

## Uso del MessageCatalogService

### Ejemplo basico
```typescript
import { messageCatalog } from './services/messageCatalog.service.js';
import { UtilsMessageCode } from './enums/UtilsMessageCode.js';

// Lanzar excepcion simple
messageCatalog.throwException(UtilsMessageCode.BUS102);

// Lanzar excepcion con parametros
messageCatalog.throwExceptionWithParams(
    UtilsMessageCode.BUS101,
    [moduleId]
);

// Crear excepcion sin lanzar (para usar con throw)
throw messageCatalog.createException(UtilsMessageCode.ERR104, 'Detalle adicional');
```

### Ejemplo en un servicio
```typescript
// src/services/parameter.service.ts
import { messageCatalog } from './messageCatalog.service.js';
import { UtilsMessageCode } from '../enums/UtilsMessageCode.js';

async findById(id: number): Promise<CatParameter> {
    const parameter = await parameterRepo.findById(id);
    if (!parameter) {
        // Lanza: "El parametro con ID {id} no existe" (BUS102)
        messageCatalog.throwExceptionWithParams(UtilsMessageCode.BUS102, [id]);
    }
    return parameter;
}
```

---

## Step 4: Deprecacion de cat_message (PENDIENTE DECISION)

### Situacion Actual

La tabla `cat_message` en utils-api almacena mensajes locales:

```sql
CREATE TABLE cat_message (
    id_message SERIAL PRIMARY KEY,
    message_code VARCHAR(20) UNIQUE NOT NULL,
    id_message_type INTEGER NOT NULL,
    description VARCHAR(500) NOT NULL,
    ...
);
```

### Opciones para Decision

#### Opcion A: Mantener cat_message (Uso Local)
- **Pros**:
  - Independencia de catalogos-api
  - Mensajes especificos de utils
- **Contras**:
  - Duplicacion de datos
  - Sin multi-idioma

#### Opcion B: Deprecar cat_message (Centralizar Todo)
- **Pros**:
  - Un solo punto de verdad
  - Multi-idioma automatico
- **Contras**:
  - Dependencia de catalogos-api
  - Requiere migracion de datos

#### Opcion C: Hibrido (Recomendado)
- Usar `catalogos-api` para mensajes de error/exito del sistema
- Mantener `cat_message` para configuraciones especificas del negocio
- Migrar gradualmente segun necesidad

### Acciones si se decide deprecar

1. **Migrar datos existentes**:
   ```sql
   -- Exportar mensajes de cat_message a catalogos-api
   INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
   SELECT ... FROM utils.cat_message;
   ```

2. **Actualizar referencias en codigo**:
   - Cambiar de `messageRepo.findByCode()` a `messageCatalog.getMessage()`

3. **Marcar como deprecated**:
   - Agregar comentarios en entidad
   - Agregar warnings en endpoints

4. **Eliminar en version futura**:
   - Eliminar tabla despues de periodo de transicion

### Pregunta para el equipo

> **¿Se debe deprecar `cat_message` completamente o mantenerla para casos especificos?**
>
> Considerar:
> - ¿Hay mensajes que NO deben estar en catalogos-api?
> - ¿Se requiere que utils-api funcione sin catalogos-api?
> - ¿Hay planes de agregar mas idiomas?

---

## Archivos Relacionados

| Archivo | Descripcion |
|---------|-------------|
| [07_utils_api_mensajes.sql](../../../backend/mrch.backend.somx.catalogos-api/src/main/resources/db/07_utils_api_mensajes.sql) | Script SQL para catalogos-api |
| [catalogos.config.ts](../../../backend/mrch.backend.somx.utils-api/src/config/catalogos.config.ts) | Configuracion del cliente |
| [catalogos.client.ts](../../../backend/mrch.backend.somx.utils-api/src/clients/catalogos.client.ts) | Cliente HTTP |
| [UtilsMessageCode.ts](../../../backend/mrch.backend.somx.utils-api/src/enums/UtilsMessageCode.ts) | Enum de codigos |
| [UtilsException.ts](../../../backend/mrch.backend.somx.utils-api/src/exceptions/UtilsException.ts) | Excepcion personalizada |
| [messageCatalog.service.ts](../../../backend/mrch.backend.somx.utils-api/src/services/messageCatalog.service.ts) | Servicio de mensajes |

---

## Siguiente Paso

1. Ejecutar script `07_utils_api_mensajes.sql` en BD de catalogos-api
2. Configurar variables de entorno en utils-api
3. Probar integracion con catalogos-api
4. **Decidir** sobre deprecacion de cat_message
