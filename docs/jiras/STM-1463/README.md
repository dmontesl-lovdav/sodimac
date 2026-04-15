# STM-1463: Migrar el módulo de auditoría a utilerias BackEnd

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-1463

## Metadatos

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Prioridad** | Alta |
| **Estado** | In Progress |
| **Resolución** | Unresolved |
| **Proyecto** | Sodimac TI MX (STM) |
| **Componente** | Portal de Proveedores FBC |
| **Epic Link** | STM-1403 |
| **Sprint** | FBC - Sprint 8 -2027 |
| **Story Points** | 3.0 |
| **Asignado** | g_dco018@Sodimac.com.mx |
| **Reporter** | g_dti07@sodimac.com.mx |
| **Labels** | Proyecto |
| **Área de Negocio** | Transversal |
| **Automatizado** | No |
| **Pasa a revisión de plan de acción** | NO |
| **Creado** | Thu, 12 Mar 2026 11:50:20 -0400 |
| **Actualizado** | Tue, 14 Apr 2026 12:03:59 -0400 |

---

## Descripción

Como usuario de negocio se requiere fusionar el módulo de auditoría a utilerías debido al desfase en el tiempo de implementación que se realizada con los Devops corporativos.

**Reglas de negocio:**

- Fusionar el código BackEnd de catálogos a utilerías

---

## Criterios de Aceptación

### CA-01. Caché de Mapeo Rol → Perfiles

El sistema debe cachear los perfiles asociados a un rol para evitar consultas repetidas a BD en cada verificación de permisos.

**Comportamiento esperado:**

```
Primera consulta del rol (cache miss):
  → Consultar BD: perfiles asociados al rol
  → Almacenar en caché con TTL 30 minutos
  → Registrar en log: "Cache MISS: role_profiles:{roleCode}"
  → Latencia: ~50-100ms

Consultas subsecuentes dentro del TTL (cache hit):
  → Leer perfiles del caché
  → Registrar en log: "Cache HIT: role_profiles:{roleCode}"
  → Latencia: ~1-5ms

Al expirar el TTL:
  → Próxima consulta → cache miss → consulta BD → recachear

Invalidación manual (AUTH-008 modifica el mapeo):
  → Eliminar clave del caché inmediatamente después del commit
  → Próxima consulta → cache miss → datos actualizados desde BD
```

📌 **Validación BA:**

1. BA solicita al developer autenticarse como usuario con ROLE_ADMIN_FINANCIERO por primera vez
2. BA verifica en logs del sistema: mensaje de "Cache MISS" y latencia de ~50-100ms
3. BA solicita al developer hacer una segunda verificación inmediata del mismo rol
4. BA verifica en logs: mensaje de "Cache HIT" y latencia de ~1-5ms
5. BA verifica que la mejora de performance es de 10x o más entre cache miss y cache hit
6. BA solicita al developer mostrar el valor almacenado en caché para ese rol — debe ser una lista de IDs de perfiles
7. BA aprueba si el cache miss/hit funciona con mejora de performance verificable en logs

**Criterio:** Cache miss en primera consulta (~50-100ms); cache hit en subsecuentes (~1-5ms); valor en caché es lista de profile IDs.

---

### CA-02. Caché de Mapeo Perfil → Permisos

El sistema debe cachear los permisos asociados a un perfil para evitar consultas repetidas a BD.

**Comportamiento:**

- Clave conceptual: por perfil, TTL 30 minutos
- Cache miss: consulta BD, almacena resultado, latencia ~50-100ms
- Cache hit: lectura en memoria, latencia ~1-5ms
- Invalidación: al modificar asignaciones de permisos en AUTH-015

📌 **Validación BA:**

1. BA solicita al developer una demostración similar a CA-01, pero para mapeo perfil→permisos
2. BA verifica en logs: cache miss en primera consulta con latencia ~50-100ms
3. BA verifica: cache hit en segunda consulta con latencia ~1-5ms
4. BA verifica que el valor almacenado en caché es una lista de IDs de permisos
5. BA verifica que el TTL del valor almacenado es de 30 minutos (1800 segundos)
6. BA aprueba si el comportamiento de caché es consistente con CA-01

**Criterio:** Cache miss en primera consulta; cache hit en subsecuentes; TTL de 30 minutos; valor es lista de permission IDs.

---

### CA-03. Caché de Segmentación de Usuario

El sistema debe cachear el contexto de segmentación del usuario para evitar consultas repetidas a las tablas de segmentación.

**Comportamiento:**

- Clave conceptual: por userId, TTL 10 minutos (más corto — datos más dinámicos)
- Cache miss: consulta `user_segmentation` y `user_provider_types`, almacena resultado
- Valor cacheado: objeto con `country`, `businessUnit`, `providerTypes`, `store`
- Cache hit: lectura en memoria
- Invalidación: al modificar segmentación en AUTH-017

📌 **Validación BA:**

1. BA solicita al developer autenticar usuario PROV001 por primera vez
2. BA verifica en logs: cache miss con consulta a las tablas de segmentación
3. BA verifica que el valor almacenado en caché contiene `country`, `businessUnit`, `providerTypes` y `store` con los valores correctos del usuario
4. BA verifica que el TTL del valor almacenado es de 10 minutos (600 segundos) — diferente al de roles/perfiles
5. BA solicita al developer autenticar al mismo usuario inmediatamente → verifica cache hit sin consultas a BD
6. BA aprueba si la segmentación se cachea correctamente con TTL diferenciado de 10 minutos

**Criterio:** Segmentación del usuario cacheada con TTL de 10 minutos (diferente a los 30 min de roles/perfiles); cache hit en requests subsecuentes del mismo usuario; valor contiene objeto completo de segmentación.

---

### CA-04. Invalidación Automática al Modificar Datos

Al modificar datos maestros en las pantallas de backoffice, el sistema debe invalidar automáticamente el caché afectado — después del commit a BD, no antes.

**Tabla de eventos de invalidación:**

| HU que escribe | Operación | Clave invalidada |
|----------------|-----------|------------------|
| AUTH-008 | Asignar / remover perfil de un rol | `role_profiles:{roleCode}` del rol modificado |
| AUTH-015 | Asignar / remover permisos de un perfil | `profile_permissions:{profileId}` del perfil modificado |
| AUTH-017 | Modificar segmentación de un usuario | `user_segmentation:{userId}` del usuario modificado |

**Reglas de la invalidación:**

- La invalidación ocurre DESPUÉS del commit a BD — nunca antes
- Si el almacén de caché no está disponible al invalidar: registrar warning en logs y continuar (la operación de escritura en BD ya fue exitosa)
- La invalidación es selectiva (solo la clave afectada, no flush completo)

📌 **Validación BA:**

1. BA solicita al developer verificar que existe una clave activa en el caché para ROLE_ADMIN_FINANCIERO
2. BA accede a AUTH-008, agrega un perfil a ROLE_ADMIN_FINANCIERO y guarda los cambios
3. BA verifica en logs: mensaje de "Cache invalidated: role_profiles:ROLE_ADMIN_FINANCIERO" o equivalente
4. BA solicita al developer mostrar el estado de esa clave en el almacén de caché → debe estar vacía (eliminada)
5. BA solicita al developer hacer una nueva consulta del rol → verifica cache miss y que el nuevo perfil está incluido en el resultado
6. BA repite el flujo para AUTH-015 (modificar permisos de un perfil) y verifica invalidación de `profile_permissions:{profileId}`
7. BA aprueba si la invalidación ocurre automáticamente tras cada operación de escritura y el dato recacheado es correcto

**Criterio:** Clave del caché eliminada automáticamente después de cada commit a BD; próxima consulta produce cache miss y recachea con datos actualizados.

---

### CA-05. Degradación Graceful cuando el Caché No Está Disponible

Si el almacén de caché no responde, el sistema debe continuar funcionando consultando directamente la BD, sin retornar errores al usuario.

**Comportamiento esperado:**

```
Sistema intenta conectar al almacén de caché:
  SI no responde dentro del timeout configurado (≤50ms):
    → Registrar warning: "Cache unavailable, using DB fallback"
    → Activar modo fallback: consultar BD directamente
    → Continuar procesando el request normalmente
    → NO retornar error HTTP al usuario
    → Performance degradado (~80ms) pero sistema operativo

  Cuando el caché se restaura:
    → Sistema automáticamente retoma el uso del caché
    → Próximas consultas vuelven a patrón normal de cache miss/hit
```

📌 **Validación BA:**

1. BA solicita al developer detener el servicio del almacén de caché en ambiente local
2. BA accede al portal como usuario normal y navega varias pantallas que requieren verificación de permisos
3. BA verifica que el sistema funciona sin errores visibles — no aparecen pantallas de error ni respuestas HTTP 5xx
4. BA verifica en logs del sistema: mensajes de warning indicando caché no disponible y uso de fallback a BD
5. BA solicita al developer reiniciar el servicio del almacén de caché
6. BA navega nuevamente → verifica en logs que el sistema retomó el caché (cache miss inicial, luego cache hits)
7. BA aprueba si el sistema funcionó correctamente sin caché y recuperó el caché automáticamente al restaurarse

**Criterio:** Sistema funciona sin errores al usuario cuando el caché no está disponible; fallback a BD automático con warning en logs; recuperación sin intervención al restaurar el servicio.

---

### CA-06. Endpoint de Métricas de Caché

El sistema debe exponer un endpoint para monitorear el uso y efectividad del caché.

**Métricas requeridas en la respuesta:**

- `cacheHitRate`: porcentaje de requests servidos desde caché
- `cacheMissRate`: porcentaje de requests que fueron a BD
- `totalRequests`: total de verificaciones procesadas
- `cacheHits` y `cacheMisses`: contadores absolutos
- `avgLatencyHit` y `avgLatencyMiss`: latencias promedio en ms
- `totalKeys`: cantidad de claves activas en el almacén de caché

**Acceso:** Solo usuarios con ROLE_SYSADMIN pueden consultar este endpoint. Otros roles reciben 403 Forbidden.

📌 **Validación BA:**

1. BA solicita al developer ejecutar una carga de ~100 requests con el mismo usuario (para generar datos de métricas)
2. BA accede al endpoint de métricas con token de SYSADMIN
3. BA verifica que la respuesta incluye todos los campos requeridos: hit rate, miss rate, total requests, latencias promedio y cantidad de claves
4. BA verifica que el hit rate es ≥80% después de los 100 requests (el primer request es miss, los demás son hits)
5. BA verifica que la diferencia de latencia entre cache hit (~4ms) y cache miss (~80ms) es significativa y coherente
6. BA intenta acceder al endpoint con usuario que no es SYSADMIN → verifica 403 Forbidden
7. BA aprueba si las métricas son coherentes, el hit rate cumple el objetivo y el acceso está correctamente restringido

**Criterio:** Endpoint de métricas retorna datos completos y coherentes; hit rate ≥80% en prueba de 100 requests; acceso restringido a SYSADMIN.

---

## Otros campos

- **Mensaje Pruebas Luis:** *1) Minuta de Periodic Value Review (PVR), donde debe indicarse presupuesto de Portfolio Cell* / *2) Excel de composición de equipo (nombre/correo/rol)*
- **Environment:** (vacío)
- **Due date:** (vacío)
- **Attachments:** (ninguno)
- **Subtasks:** (ninguna)
- **Votes:** 0
- **Watches:** 0
