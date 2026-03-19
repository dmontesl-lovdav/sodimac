# STM-704: Ajustar servicio del registro de una factura y nota de credito para guardar la bitacora de actividades

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-704

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | To Do |
| **Tipo** | Story |
| **Prioridad** | Media |
| **Asignado** | g_dco018 |
| **Reporter** | Ivan Saul Cortes Zamora |
| **Sprint** | FBC - Sprint 6 -2027 |
| **Story Points** | 3 |
| **Componente** | Portal de Proveedores FBC |
| **Area de Negocio** | Transversal |
| **Epic Link** | STM-815 |
| **Validadora** | Karen Alexandra Ayala Olaya |
| **Creado** | 2025-07-07 |
| **Actualizado** | 2026-03-12 |

---

## Descripcion

Ajustar el servicio de registro de Factura y Nota de Credito para almacenar bitacora completa de validaciones, el servicio de registro de facturas y notas de credito almacene en una bitacora todas las validaciones ejecutadas durante el proceso para contar con trazabilidad completa, facilitar el analisis de errores, y auditar el flujo de registro paso a paso.

---

## Descripcion funcional

Se requiere modificar los servicios actuales de registro de factura y registro de nota de credito para que, ademas de ejecutar las validaciones habituales, puedan:

1. Aceptar el folio o Id de transaccion enviado desde el front-end.
2. Registrar paso a paso todas las validaciones ejecutadas durante el proceso.
3. Guardar en bitacora cada validacion, incluyendo:
   - Mensaje de negocio
   - Mensaje tecnico completo
   - Estado de la validacion (exito/error)
4. Utilizar el servicio de utilerias para:
   - Registrar pasos de validacion
   - Registrar errores en log centralizado
   - Guardar copia del request y response de la operacion
5. Ajustar el flujo de registro para auditar los siguientes pasos:
   - Validacion de estructura del XML
   - Validacion XSD
   - Validacion de version de CFDI/XML
   - Validacion del tipo de documento (Factura / Nota de Credito)
   - Validacion de relacion factura <-> NC
   - Validacion de tolerancia (si no hay NC)
   - Validacion de datos fiscales de emisor
   - Validacion de datos fiscales de receptor
   - Validacion del certificado (vigencia/activo)

El objetivo final es garantizar trazabilidad completa y facilitar el diagnostico funcional y tecnico.

---

## Reglas de negocio

### 1. Entrada del servicio

El servicio debera recibir desde el front-end:
- Folio o Id de transaccion cliente
- Folio o Id de transaccion sistema
- XML o archivo base64
- Datos complementarios de la publicacion

### 2. Uso del servicio de utilerias

El servicio de utilerias debe permitir:
- Registrar cada validacion realizada
- Registrar mensajes funcionales y tecnicos
- Registrar cualquier error durante el proceso
- Almacenar request completo
- Almacenar response (si aplica)

### 3. Validaciones obligatorias

El servicio debe auditar y registrar las siguientes validaciones:
1. Validar estructura general del XML
2. Validar XSD
3. Validar version CFDI/XML
4. Validar tipo de documento publicado
5. Validar si la factura tiene Notas de Credito relacionadas
6. Si no tiene NC -> validar tolerancia recepcion vs factura
7. Validar datos fiscales del emisor
8. Validar datos fiscales del receptor
9. Validar certificado vigente

### 4. Bitacora

Cada paso debe guardar:
- Nombre de la validacion
- Resultado (OK / Error)
- Mensaje de error funcional
- Error tecnico completo (stack o detalle)
- Fecha y hora
- Id de transaccion
- Identificadores del documento

### 5. Request y Response

El servicio debera:
- Guardar el request completo (entrada de la publicacion)
- Guardar el response (en caso de aplicar)

---

## Criterios de aceptacion

- Aceptar folio o Id de transaccion
- Almacenar la bitacora de actividades

---

## Comentarios

| Fecha | Autor | Comentario |
|-------|-------|------------|
| 2025-07-29 | JIRAUSER249067 | Se agrega prueba de funcionalidad |
