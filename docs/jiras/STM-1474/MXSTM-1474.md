# STM-1474: Aplicar filtro de seguridad de información en la opción de recepción por atributo de usuario

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-1474

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Estado** | To Do |
| **Asignado** | g_dco018 |
| **Prioridad** | Alta |
| **Sprint** | FBC - Sprint 8 -2027 |
| **Story Points** | 3 |
| **Epic** | STM-1403 |
| **Componente** | Portal de Proveedores FBC |
| **Área de Negocio** | Transversal |
| **Labels** | Proyecto |

---

**Como** Usuario la información de las recepciones sea filtrada en el backend con base en los atributos asignados a cada usuario **Para** garantizar la correcta segmentación y seguridad de la información publicada a proveedores, considerando que los usuarios administradores, analistas, proveedores y comerciales acceden a la misma funcionalidad, pero visualizan resultados distintos según sus atributos.

---

## Descripción

El sistema debe implementar filtrado de información de recepciones a nivel backend utilizando atributos de usuario configurados en catálogos.
El filtrado debe aplicarse considerando seguridad por token y reglas específicas de negocio para asegurar que cada usuario acceda únicamente a la información permitida.

---

## Reglas de Negocio

### Regla 1. Filtrado de información por atributos de usuario

- El backend debe filtrar la información de las recepciones de acuerdo con los atributos asignados al usuario autenticado.
- El filtrado se realiza exclusivamente a nivel de usuario.

**Mensaje en caso de falla**

| IdUrl | IdMsg | Mensaje |
|-------|-------|---------|
| http://10.10.10.1/demo | WRN7029 | El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador |

### Regla 2. Seguridad mediante token

- Todas las peticiones deben validar el token conforme a las especificaciones dictadas por corporativo.
- El usuario obtenido del token será la base para aplicar el filtrado de atributos.

### Regla 3. Atributos utilizados para el filtrado

Los atributos que deben utilizarse para el filtrado de recepciones son:
- Proveedor
- TipoProveedor
- GrupoProveedor

### Regla 4. Uso del catálogo de catálogos

- El sistema debe obtener los atributos configurados desde la tabla de catálogo de catálogos.
- El filtrado solo debe considerar los atributos definidos en la regla 3.

### Regla 5. Manejo del valor especial "-1"

- Si un atributo tiene como valor **-1**, el usuario podrá consultar toda la información correspondiente a dicho atributo.
- Ejemplo: TipoProveedor = -1 → acceso a todos los tipos de proveedor.

### Regla 6. Usuario con múltiples atributos

- Cuando un usuario tenga más de un atributo asignado, el filtrado debe considerar que se cumpla **al menos una condición** (OR lógico).

---

## Acceptance Criteria

- Dado un usuario autenticado con atributos configurados
  Cuando consulta las recepciones
  Entonces el sistema retorna únicamente la información permitida por sus atributos.

- Dado un usuario con uno o más atributos configurados con valor -1
  Cuando consulta las recepciones
  Entonces el sistema permite acceso total a la información asociada a esos atributos.

- Dado un usuario con múltiples atributos configurados
  Cuando consulta las recepciones
  Entonces el sistema retorna información que cumpla al menos una condición válida.
