# STM-1167: Monitoreo, trazabilidad y auditoría de ejecuciones batch de extracción y envío de información

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Estado** | To Do |
| **Asignado** | g_dop02 |
| **Prioridad** | Alta |
| **Labels** | Proyecto |

---

Contar con una solución que permita **monitorear la ejecución de los procesos**, visualizar los **pasos ejecutados** y consultar **logs detallados**

 

Identificar oportunamente incidencias en los procesos de extracción y envío de información entre sistemas origen y destino, facilitar el análisis por parte del equipo de Sistemas y asegurar trazabilidad y soporte para auditoría.

 

Reglas de negocio:

 

 - Operar sobre una nueva base de datos llamada **SODIMAC_BATCH_DEV**

 - Registrar de forma centralizada la ejecución de los procesos batch

 - Controlar cifras de registros procesados entre origen y destino

 - Registrar la secuencia y detalle de cada paso del proceso

 - Almacenar logs funcionales y técnicos

 - Permitir la consulta histórica para monitoreo, soporte y auditoría

 - Tablas

 - 
 

 - catCatalogo

 - adminCatalogo

 - ctrlProcesoCab

 - ctrlProcesoDet

 - ctrlProcesoElemento

 - ctrlLog

 

 

 - Relaciones
 

 - catCatalogo(1) → adminCatalogo (N)

 - ctrlProcesoCab(1) → ctrlProcesoDet(N)

 - ctrlProcesoCab(1) → ctrlLog(N)

 - ctrlProcesoCab(1) → ctrlProcesoElemento(N)

 

 

 - 
## Tabla: catCatalogo

**Descripción:** Almacena o registra todos los catálogos que utiliza el sistema

| Campo | Tipo | Descripción |
|---|---|---|
| idCatalogo | INT | Id de catálogo |
| nombre | VARCHAR(100) | Nombre del catálogo |
| descripcion | VARCHAR(255) | Descripción del catálogo |
| estatus | INT | Estatus del catálogo |
| usuarioCreacion | INT | Usuario de creación |
| fechaCreacion | DATETIME | Fecha de creación |
| usuarioActualizacion | INT | Usuario de actualización |
| fechaActualizacion | DATETIME | Fecha de actualización |

 - 
## Tabla: adminCatalogo

**Descripción:** Almacena todos los valores utilizados en los catálogos configurados.

| Campo | Tipo | Descripción |
|---|---|---|
| idCatalogo | INT | Id de catálogo |
| idElemento | INT | Id del elemento |
| descripcion | VARCHAR(50) | Descripción funcional del elemento |
| estatus | INT | Estatus del elemento |
| usuarioCreacion | INT | Usuario de creación |
| fechaCreacion | DATETIME | Fecha de creación |
| usuarioActualizacion | INT | Usuario actualización |
| fechaActualizacion | DATETIME | Fecha actualización |
| elementoConversion | VARCHAR(50) | Elemento de conversión |

---

## Tabla: ctrlProcesoCab

**Descripción:** Control de ejecución y cifras origen–destino.

| Campo | Tipo | Descripción |
|---|---|---|
| id_ejecucion | INT | Identificador único de la ejecución |
| id_proceso | INT | Proceso ejecutado (Relación con el catálogo adminCatalogo) |
| registros_origen | INT | Total de registros en origen |
| registros_destino | INT | Total de registros en destino |
| fecha_inicio | DATETIME | Inicio de ejecución |
| fecha_final | DATETIME | Fin de ejecución |
| estatus | VARCHAR(20) | Estado de la ejecución |

---

## Tabla: ctrlProcesoDet

**Descripción:** Secuencia de pasos del proceso.

| Campo | Tipo | Descripción |
|---|---|---|
| id_flujo | INT | Identificador del flujo |
| id_ejecucion | INT | Ejecución asociada |
| nombre_paso | VARCHAR(100) | Nombre del paso |
| secuencia | INT | Orden de ejecución |
| fecha_inicio_registro | DATETIME | Inicio del paso |
| parametros_registro | VARCHAR(255) | Parámetros usados |
| detalle | VARCHAR(255) | Resultado del paso |

---

## Tabla: ctrlLog

**Descripción:** Logs técnicos y funcionales.

| Campo | Tipo | Descripción |
|---|---|---|
| id_log | INT | Identificador del log |
| nombre | VARCHAR(100) | Tipo o nombre del evento |
| id_ejecucion | INT | Proceso relacionado en la ejecución |
| log | TEXT | Mensaje del log |
| fecha_registro | DATETIME | Fecha del evento |
| |

---

## Tabla: ctrlProcesoElemento

**Descripción:** Llevar el control de todos los documentos o valores procesados

| Campo | Tipo | Descripción |
|---|---|---|
| idElemento | INT | Id del elemento ejecutado |
| id_ejecución | INT | Proceso relacionado en la ejecución |
| valor | VARCHAR(50) | Valor único del proceso |
| valorAlterno | VARCHAR(250) | Valor alterno o de conversión |
| secuencia | INT | Valor de secuencia del valor procesado |
| fechaRegistro | DATETIME | Fecha de registro del elemento |