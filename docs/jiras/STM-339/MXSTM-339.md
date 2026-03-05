# STM-339: Agregar metodo de actualización al microservicio de facturas y notas de crédito

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Estado** | Done |
| **Asignado** | g_dco018 |
| **Prioridad** | Media |
| **Labels** | Proyecto |

---

Como usuario se requiere agregar el método para actualizar los datos de una factura o nota de crédito publicado por un proveedor o usuario administrador.

Reglas de negocio:

 - Antes de iniciar la historia es importante validar que se haya concluido el siguiente ticket:
 

 - STM-337 Creación de microservicio para el registro de una factura o NC - Jira Falabella

 

 

 - Descargar el código de GIT de la historia 337 para complementar el servicio de factura y NC

 - Para actualizar una factura o Nc se requieren los siguientes datos de manera obligatoria
 

 - Uuid

 - Número de Proveedor

 

 

 - Los valores que se pueden actualizar son los siguientes
 

 - Estatus Factura - Obligatorio

 - Id Usuario Actualización - Obligatorio

 - Actualizar la addenda, dependiendo el tipo de documento (Los valores de Addenda se pueden actualizar siempre y cuando sean indicados - Valores opcionales)
 

 - Addenda factura 
 

 - Uuid

 - IdProveedor

 - Serie

 - Folio

 - RFC

 - IdGuiaEntrega

 - IdViaje

 - NoOC

 - NoRecepcion

 - TipoAddenda

 - TipoProveedor

 

 

 - Addenda NC
 

 - Uuid

 - UuidNC

 - IdProveedor

 - Serie

 - Folio

 - RFC

 - TipoProveedor

 - TipoNotaCredito

 

 

 

 

 

 

 - Los estatus permitidos para actualizar una factura son los siguientes:
 

 - Factura: [https://confluence.falabella.tech/x/pTyzLQ](https://confluence.falabella.tech/x/pTyzLQ)

 - Los estatus de la NC catalogarlos en el API de Catálogo de catálogos

 - En caso de no existir el estatus mandar un error con un código y mensaje

 - Guardar el error en la bitácora 

 

 

 - Estatus permitidos para actualizar una NC
 

 - NC: [https://confluence.falabella.tech/x/qjyzLQ](https://confluence.falabella.tech/x/qjyzLQ)

 - Los estatus de la NC catalogarlos en el API de Catálogo de catálogos

 - En caso de no existir el estatus mandar un error con un código y mensaje

 - Guardar el error en la bitácora 

 

 

 - Guardar en la bitácora de actividades la información de los parámetros enviados y resultado de la actualización.