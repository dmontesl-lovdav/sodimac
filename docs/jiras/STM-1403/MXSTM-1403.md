# STM-1403: PP_FBC: Módulo de Autorización

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-1403

| Campo | Valor |
|-------|-------|
| **Tipo** | Epic |
| **Estado** | To Do |
| **Epic Status** | To Do |
| **Asignado** | g_dti07 |
| **Reporter** | g_dti07 |
| **Prioridad** | Crítica |
| **Componente** | Portal de Proveedores FBC |
| **Área de Negocio** | Transversal |
| **Labels** | Proyecto |
| **Automatizado** | No |
| **% Avance Plans** | 8.33% |
| **Creado** | Thu, 19 Feb 2026 |
| **Actualizado** | Thu, 16 Apr 2026 |

---

## 📖 DESCRIPCIÓN

Como **administrador del sistema y usuario del portal**, quiero que el sistema controle de manera **granular y auditable** qué acciones puede ejecutar cada usuario y qué información puede visualizar en los módulos Fiscal y Financiero, para garantizar que cada usuario acceda únicamente a las funciones y datos que le corresponden según su rol corporativo y tipo de proveedor, cumpliendo con políticas de seguridad y aislamiento de datos entre proveedores.

---

## 🎯 OBJETIVOS

### Objetivo General

Implementar un módulo de autorización que controle permisos, perfiles, y segmentación de datos para los módulos Fiscal y Financiero del Portal de Proveedores, integrado con Falabella Business Center (FBC).

### Objetivos Específicos

1. **Gestión de Permisos:** Administrar un catálogo de 81 permisos granulares (acción + vista + módulo)
2. **Gestión de Perfiles:** Definir y administrar perfiles funcionales que agrupen permisos
3. **Mapeo Rol-Perfil:** Relacionar roles corporativos de FBC con perfiles del portal
4. **Segmentación de Datos:** Aplicar filtros automáticos por providerId, country, businessUnit, region, store
5. **Integración FBC:** Validar tokens JWT de FBC y extraer claims de identidad y contexto
6. **Auditoría:** Generar eventos auditables por cada acción autorizada
7. **Aislamiento de Datos:** Garantizar que un proveedor NUNCA vea datos de otro proveedor

---

## 🎁 BENEFICIOS ESPERADOS

### Seguridad

- ✅ **Control granular de acceso:** Cada usuario solo ejecuta acciones permitidas por su rol
- ✅ **Aislamiento total de datos:** Proveedores no pueden ver información de otros proveedores
- ✅ **Auditoría completa:** Trazabilidad de quién hizo qué y cuándo
- ✅ **Integración con FBC:** Autenticación centralizada, sin gestión local de usuarios

### Operacional

- ✅ **Flexibilidad:** Perfiles funcionales se pueden ajustar sin cambiar código
- ✅ **Escalabilidad:** Diseño multi-país desde el inicio (México + futuras unidades Sodimac)
- ✅ **Performance:** Caché inteligente de permisos y segmentación
- ✅ **Mantenibilidad:** Modelo de datos claro y bien documentado

### Negocio

- ✅ **Cumplimiento normativo:** Segmentación de datos cumple con políticas de privacidad
- ✅ **Reducción de riesgos:** Accesos no autorizados prevenidos por diseño
- ✅ **Facilita adopción:** Diseño multi-país facilita expansión a Chile, Perú, Colombia, etc.

---

## ✅ CRITERIOS DE ACEPTACIÓN (EPIC)

### AC-01: Gestión de Permisos

- ✅ Catálogo de 81 permisos cargado en base de datos
- ✅ CRUD completo de permisos (Crear, Leer, Actualizar, Desactivar)
- ✅ Clasificación por nivel de sensibilidad: Público, Restringido, Crítico
- ✅ Interfaz de administración para gestionar permisos

### AC-02: Gestión de Perfiles Funcionales

- ✅ Definición de perfiles por módulo (Fiscal y Financiero)
- ✅ Asignación de permisos a perfiles (relación N:N)
- ✅ CRUD completo de perfiles
- ✅ Activación/Desactivación de perfiles sin eliminarlos

### AC-03: Integración con FBC

- ✅ Validación de token JWT de FBC en cada request
- ✅ Extracción de claims: `userId`, `role`, `providerId`, `country`, `providerType`
- ✅ Manejo de errores: token expirado, firma inválida, claims faltantes
- ✅ Configuración de llaves públicas de FBC vía variables de entorno

### AC-04: Segmentación de Datos

- ✅ Middleware que enriquece requests con dimensiones de segmentación
- ✅ Filtros automáticos por: `providerId`, `country`, `businessUnit`
- ✅ Filtros opcionales por: `region`, `store` (si aplican roles regionales)
- ✅ Roles administrativos sin filtro por providerId (ven todos los proveedores)
- ✅ Caché de datos de segmentación por proveedor (TTL: 1 hora)

### AC-05: Verificación de Permisos en Tiempo Real

- ✅ Endpoint `/api/v1/auth/check-permission` implementado
- ✅ Endpoint batch `/api/v1/auth/check-permissions-batch` para consultas múltiples
- ✅ Tiempo de respuesta < 100ms en percentil 95
- ✅ Caché de permisos por rol (TTL: 10 minutos)

### AC-06: Auditoría

- ✅ Generación de eventos por cada acción autorizada
- ✅ Eventos incluyen: `userId`, `role`, `permissionCode`, `providerId`, `timestamp`, `result`
- ✅ Integración con Módulo de Auditoría (si existe) o logs estructurados

### AC-07: Aislamiento de Datos Entre Proveedores

- ✅ Regla inviolable: `WHERE provider_id = :tokenProviderId` en queries de datos
- ✅ Pruebas de seguridad: intentar acceder a datos de otro proveedor debe fallar con 403
- ✅ Roles no-proveedor (admin, sysadmin) pueden ver todos los proveedores

### AC-08: Interfaz de Administración (Backoffice)

- ✅ Pantalla de gestión de permisos (listado, búsqueda, edición)
- ✅ Pantalla de gestión de perfiles (listado, creación, asignación de permisos)
- ✅ Pantalla de mapeo Rol FBC → Perfil(es) del portal
- ✅ Exportación de configuración (permisos, perfiles, mapeos) en CSV/XLSX
