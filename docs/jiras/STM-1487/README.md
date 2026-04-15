# STM-1487: Migrar el módulo de auditoría a utilerias BFF

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-1487

## Metadatos

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Prioridad** | Media |
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
| **Creado** | Tue, 24 Mar 2026 17:45:44 -0400 |
| **Actualizado** | Tue, 14 Apr 2026 12:04:20 -0400 |

---

## Descripción

Como usuario de negocio se requiere fusionar el módulo de auditoría a utilerías debido al desfase en el tiempo de implementación que se realizada con los Devops corporativos.

**Reglas de negocio:**

- Fusionar el código BFF de catálogos a utilerías

---

## Criterios de Aceptación

### CA-01. Endpoint de Health Check

El endpoint `GET /api/auth/health` debe retornar el estado de salud del módulo y el resultado individual de cada check de dependencia.

**Estructura de respuesta (HTTP 200 — todo UP):**

```json
{
  "status": "UP",
  "module": "authorization",
  "timestamp": "2026-03-19T14:30:00Z",
  "checks": {
    "database": {
      "status": "UP",
      "responseTime": "12ms"
    },
    "fbc": {
      "status": "UP",
      "responseTime": "45ms"
    }
  }
}
```

**Estructura de respuesta (HTTP 503 — algún check falla):**

```json
{
  "status": "DOWN",
  "module": "authorization",
  "timestamp": "2026-03-19T14:30:00Z",
  "checks": {
    "database": {
      "status": "DOWN",
      "error": "Connection timeout after 200ms"
    },
    "fbc": {
      "status": "UP",
      "responseTime": "38ms"
    }
  }
}
```

**Reglas de los checks:**

- **database:** Ejecutar una consulta mínima de conectividad (ej: `SELECT 1`). Si responde en ≤ 200ms → UP. Si excede 200ms o hay error de conexión → DOWN.
- **fbc:** Verificar conectividad al endpoint de FBC (ping o endpoint de health de FBC). Si responde en ≤ 300ms → UP. Si excede o hay error → DOWN.
- **Regla general:** Si cualquier check retorna DOWN → el `status` raíz es DOWN y el HTTP code es 503.
- El endpoint no requiere autenticación (debe ser accesible desde load balancers y herramientas externas).

📌 **Validación BA:**

1. BA solicita al developer ejecutar `GET /api/auth/health` con BD disponible
2. BA verifica que la respuesta es HTTP 200 con `status: "UP"` y ambos checks en UP con sus tiempos de respuesta
3. BA solicita al developer simular BD no disponible (ej: cortar conexión)
4. BA ejecuta nuevamente `GET /api/auth/health` → verifica HTTP 503 con `status: "DOWN"` y el check de `database` en DOWN con mensaje de error
5. BA verifica que el check de `fbc` sigue mostrando su estado real independientemente de BD
6. BA verifica que el endpoint responde en ≤ 500ms incluso cuando un check falla
7. BA verifica que el endpoint es accesible sin token de autenticación
8. BA aprueba si el endpoint retorna la estructura correcta y el código HTTP refleja el estado real del módulo

**Criterio:** `GET /api/auth/health` retorna HTTP 200 con estado UP cuando todo funciona; HTTP 503 con el check fallido detallado cuando alguna dependencia no responde; accesible sin autenticación; responde en ≤ 500ms.

---

### CA-02. Endpoint de Métricas Operativas

El endpoint `GET /api/admin/auth/metrics` debe retornar las métricas operativas acumuladas del módulo desde el inicio del proceso. Accesible solo para ROLE_SYSADMIN.

**Estructura de respuesta (HTTP 200):**

```json
{
  "module": "authorization",
  "timestamp": "2026-03-19T14:30:00Z",
  "uptime": "4h 23m",
  "permissions": {
    "totalVerifications": 15842,
    "verificationsPerMinute": 264,
    "allowed": 15201,
    "denied": 641,
    "deniedRate": 0.04,
    "avgLatency": "8.3ms",
    "errors": 12
  },
  "cache": {
    "hitRate": 0.91,
    "missRate": 0.09
  }
}
```

**Descripción de métricas:**

| Métrica | Descripción |
|---------|-------------|
| `totalVerifications` | Total de llamadas a `checkPermission` desde inicio del proceso |
| `verificationsPerMinute` | Promedio del último minuto |
| `allowed` | Verificaciones que retornaron `true` |
| `denied` | Verificaciones que retornaron `false` (403) |
| `deniedRate` | `denied / totalVerifications` |
| `avgLatency` | Latencia promedio de una verificación completa |
| `errors` | Verificaciones que fallaron por error técnico (BD caída, etc.) |
| `cache.hitRate` | Cache hit rate del módulo (complementa AUTH-020 CA-06) |

**Nota:** Las métricas se almacenan en memoria. Se reinician al reiniciar el proceso del servidor.

📌 **Validación BA:**

1. BA solicita al developer ejecutar ~100 requests de verificación de permisos en el sistema
2. BA accede a `GET /api/admin/auth/metrics` con token de SYSADMIN
3. BA verifica que la respuesta incluye todos los campos requeridos: totalVerifications, verificationsPerMinute, allowed, denied, deniedRate, avgLatency, errors, cache.hitRate
4. BA verifica que `totalVerifications` = `allowed` + `denied` + `errors` (suma coherente)
5. BA verifica que `deniedRate` es coherente con el ratio de denegaciones del sistema
6. BA intenta acceder con un usuario que no es SYSADMIN → verifica HTTP 403 Forbidden
7. BA aprueba si las métricas son coherentes, la suma es correcta y el acceso está restringido

**Criterio:** Endpoint retorna métricas coherentes con `totalVerifications` = `allowed` + `denied` + `errors`; `deniedRate` calculado correctamente; acceso restringido a ROLE_SYSADMIN con 403 para otros roles.

---

### CA-03. Logs Estructurados del Módulo

Cada verificación de permiso debe generar una entrada de log en formato JSON estructurado, con el nivel de severidad correspondiente al resultado.

**Formato de log por verificación:**

```json
{
  "timestamp": "2026-03-19T14:30:00.123Z",
  "level": "INFO",
  "module": "authorization",
  "action": "permission_check",
  "userId": "ANALISTA_MX_001",
  "permissionCode": "FIN-001",
  "result": "ALLOWED",
  "latencyMs": 7,
  "roleCode": "ROLE_ANALISTA_COMPRAS",
  "cacheHit": true
}
```

**Niveles de log según resultado:**

| Resultado | Nivel | Cuándo |
|-----------|-------|--------|
| ALLOWED | INFO | Verificación exitosa — permiso concedido |
| DENIED | WARN | Verificación exitosa — permiso denegado (sin error técnico) |
| ERROR | ERROR | Fallo técnico durante la verificación (BD caída, etc.) |

**Datos excluidos de los logs (seguridad):**

- Token JWT completo
- Valores de permisos en texto plano más allá del código
- Datos de segmentación del usuario

📌 **Validación BA:**

1. BA solicita al developer ejecutar una verificación de permiso exitosa (ALLOWED) y mostrar el log generado
2. BA verifica que el log tiene formato JSON con todos los campos: timestamp, level, module, action, userId, permissionCode, result, latencyMs, roleCode, cacheHit
3. BA solicita al developer ejecutar una verificación de un permiso que el usuario no tiene (DENIED) y mostrar el log
4. BA verifica que el nivel es WARN y el campo `result` es "DENIED"
5. BA solicita al developer simular un error técnico durante la verificación y mostrar el log
6. BA verifica que el nivel es ERROR con información del error
7. BA verifica que ningún log contiene el token JWT ni datos sensibles
8. BA aprueba si los tres niveles de log se generan correctamente con la estructura esperada

**Criterio:** Cada verificación genera log JSON estructurado con nivel INFO/WARN/ERROR según resultado; logs no contienen datos sensibles (tokens, etc.).

---

## Otros campos

- **Environment:** (vacío)
- **Due date:** (vacío)
- **Attachments:** (ninguno)
- **Subtasks:** (ninguna)
- **Votes:** 0
- **Watches:** 0
