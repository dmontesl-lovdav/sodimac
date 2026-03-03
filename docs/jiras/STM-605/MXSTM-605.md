Sodimac TI MXSTM-605
Ajuste en el MER para colocar una cabecera y detalle en los pagos
Epic Link:Gestión de pagos y descuentos
Sprint:FBC - Sprint 10 -2025, FBC - Sprint 11 -2025, FBC - Sprint 4 -2026
Acceptance Criteria:
Permite registrar el id de un proveedor para considerarlo como bloqueo de pago a un proveedor que no cumpla con las reglas de negocio.
API REST: Implementar una API RESTful que permita la comunicación con otros servicios del ecosistema del portal de proveedores.
Seguridad: Autenticación y Autorización
Manejar documentación: Proporcionar una documentación completa de la API utilizando Swagger/OpenAPI o dependiendo la definición que entregue el equipo corporativo.
Pruebas y Calidad:
Pruebas Unitarias: Incluir pruebas unitarias para todas las funciones críticas.
Pruebas de Integración: Asegurar que el microservicio interactúe correctamente con otros sistemas.
Escenarios de Prueba:
Permitir registrar un nuevo proveedor
Validar que no exista el proveedor previamente 
El Api debe manejar código de errores tipificados en la API de catálogo de catálogos 
Area de Negocio:Transversal
Description
Como usuario se requiere tener un servicio para dar de alta el bloqueo de proveedores para evitar realizar carga de facturas, notas de crédito o complementos de pago con la finalidad de generar un candado para notificar al proveedor que hacen falta documentos por completar su proceso de pago.

Reglas de negocio:

El servicio requiere proporcionar seguridad por medio de un token para ser consumido.
En el siguiente link de encuentra el diccionario de datos:
https://confluence.falabella.tech/x/3gauKw
La tabla se encuentra creada previamente en la historia STM-333 revisar el nombre y estructura de la tabla con Roberto 
No sera posible dar de alta un bloqueo de un proveedor si no se encuentra registrado en el catálogo de proveedores (Revisar con Marco el nombre de la tabla de catálogo de proveedores)
Respetar la longitud y tipo de dato de la tabla en caso de no coincidir mandar un mensaje personalizado por columna indicando el motivo por el cual fue rechazado el registro, los tipos de dato se pueden obtener de la historia SMT-333
Ejemplo:
El número de proveedor supera la longitud permitida
En caso de mandar una columna de tipo obligatorio rechazar el registro indicando por un código y mensaje de error.
El API no deberá tener ningún mensaje en hardcode, todo debe estar catálogo utilizando el API de mensajes, revisar el funcionamiento con Gabriel Galvan
La fecha inicio no puede ser mayor a la fecha final, utilizar el mensaje "WRN7000 - La fecha inicio no puede ser superior a la fecha final"
Campos para dar de alta el bloqueo de un proveedor
numeroProveedor
Tipo de Dato: Numérico
Campo Obligatorio
fechaInicio
Tipo de Dato: Fecha
Campo Obligatorio
fechaFinal
Tipo de Dato: Fecha
Campo Obligatorio
estatus
Tipo de Dato: Numérico
Campo Obligatorio
Valor: Valor inicial por default 1 (Proveedor inactivado)
idUsuario
Tipo de Dato: Numérico
Campo Obligatorio
 fechaRegistro
Tipo de Dato: Numérico
Campo Obligatorio
Valor: Fecha actual de registro
 fechaActualizacion
Tipo de Dato: Fecha
Campo Opcional
Valor: Null dejarlo por default
Mandar un código y mensaje notificando el registro exitoso.
Utilizar el código del msg: "RES1001 - El registro del proveedor {XXXX} se realizó exitosamente." 
Mandar un código y mensaje notificando un error al intentar actualizar la información.