# Manual de cobertura — Sistemas legacy y de soporte

**Para:** Josue · **Cobertura:** 1 semana · **Fecha:** agosto 2026

Este manual describe **qué hace cada sistema, dónde vive, a qué base de datos pega y con qué scripts se revisa**. Un sistema por sección.

> ⚠️ Contiene contraseñas de producción. No sacar de la PC corporativa ni compartir por canales abiertos.

---

## Índice de sistemas

| # | Sistema | En una línea |
|---|---|---|
| 1 | **wsft** | Batch nocturno que timbra los CFDI ante el PAC |
| 2 | **AdminFacturación** | Servicio de parámetros y catálogos SAT que consume el batch |
| 3 | **Middleware de Ticket** | Servicio que entrega el detalle de una venta |
| 4 | **Autofacturador** | Portal donde el cliente se factura solo |
| 5 | **WSCT** | Servicio de consulta de detalle de ticket |
| 6 | **Rebates** | Portal de acuerdos comerciales con proveedores |
| 7 | **Tótem** (back + admin) | Conteo cíclico de inventarios en tienda |
| 8 | **BCT Facturación** | Batch de sincronización + Facturación Global |
| 9 | **Portal CFDI** | Portal web de consulta de CFDI |
| 10 | **Descarga OC Detecno** | Descarga órdenes de compra de proveedor |
| 11 | **Consolas** | CrediVoucher, Puntos CES, Pagos Santander |

---

# 1. wsft — Timbrado CFDI

## Objetivo

Es el **batch nocturno que timbra las facturas ante el PAC (Detecno)**. Toma las ventas pendientes, arma el XML del CFDI (versiones 3.3 y 4.0), lo manda al PAC y guarda el UUID que regresa. Es el proceso más crítico de la operación: si no corre, no se timbra nada.

Para armar cada factura pide los parámetros y catálogos SAT al sistema **AdminFacturación** (#2) y el detalle de la venta al **Middleware de Ticket** (#3).

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Batch / servicio Java (Spring Boot), corre en Tomcat |
| Servidor | `10.138.150.77` |
| Cuándo corre | Todas las noches |
| Base de datos | MySQL `10.138.150.77:3306` — BD `wsfacturacion` |
| Usuario BD | `wsfacturacionUser` / `wsfacturacionUser` |
| PAC | Detecno — portal de certificados: `https://genera.emisiondetecno.mx/Sodimac/Detickets/cfdiWebEmision_Servicio40_SodimacDetickets/asp/Certificados.aspx` (usuario `Administrador` / `4343fdfd657jhfg0`) |

## Usuario que usa el sistema

Para hablar con AdminFacturación se autentica como:

| Usuario | Password |
|---|---|
| `userWsft` | `E0R9KWF482` |

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\fiscal-timbradoPacDetecno.sql` | Ver qué se timbró: `TIMBRADO_PAC_DETECNO`, `CONTROL_PAC_TICKET`, `CONTROL_PAC_SERIE`, `BITACORA_ACTIVIDADES` |
| `Scripts\nuevos\sodimacfiscal-consultasGenerales.sql` | Buscar una factura por UUID o RFC |
| `Scripts\nuevos\wsfacturacion-usuarioToken.sql` | Ver los usuarios del servicio (`usuariosws`) y el SP de token |

## Qué suele fallar

- **Facturas con UsoCFDI equivocado (`G02`)**: hay un problema conocido y abierto — cuando un cliente hace una devolución, su registro queda marcado con el uso de Nota de Crédito y **todas sus ventas posteriores salen timbradas mal**. Si reportan esto, no es un caso aislado: son ~3,800 clientes. No intentar arreglarlo de raíz esta semana; documentar el caso y avisarme.
- **El batch no timbra nada**: revisar primero que AdminFacturación (#2) esté arriba, porque sin login no arranca.

---

# 2. AdminFacturación — Parámetros y catálogos SAT

## Objetivo

Servicio que **guarda y entrega toda la parametrización fiscal**: régimen fiscal, uso de CFDI (3.3 y 4.0), tipo de tienda, tipo de persona, forma y método de pago, configuración del emisor por tienda y folios. Nadie lo usa a mano — su cliente principal es el batch **wsft** (#1), que le pega masivamente cada noche.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Servicio REST Java (Spring Boot) sobre Tomcat |
| Servidor | `10.138.150.88` (nombre `somxvladmfiscwebp`) |
| URL | `http://10.138.150.88:8080/finanzasadminfacturacion` · HTTPS en `:8443` |
| Documentación de la API | `https://10.138.150.88:8443/finanzasadminfacturacion/swagger-ui.html` |
| Base de datos | MySQL `10.138.150.71:3306` — BD `configuracion` |
| Usuario BD | `configUser` / `ki&de$w29oEK` |
| Log de la aplicación | `/opt/tomcat/latest/logs/wsft/wsft-logger.log` (se llama `wsft` por herencia, pero es de esta app) |
| Log del servidor | `/opt/tomcat/latest/logs/catalina.out` |

> En este mismo Tomcat conviven **otras 4 aplicaciones** (Middleware de Ticket, wsobtenerticket, wsprmfac, serviciopuntoventapos). Reiniciar el Tomcat **las tumba a todas**.

## Usuarios que usa el sistema

| Usuario | Password | Quién lo usa |
|---|---|---|
| `userWsft` | `E0R9KWF482` | el batch de timbrado (#1) |
| `userWsPrmFac` | `E0R9KWF482` | el servicio wsprmfac |

## Cómo probar que está vivo

```
POST http://10.138.150.88:8080/finanzasadminfacturacion/api/login
{ "username": "userWsft", "password": "E0R9KWF482" }
```
Debe responder **200** con un token. Con ese token:
```
POST http://10.138.150.88:8080/finanzasadminfacturacion/api/emisor
{ "rfc":"CSD161207R2A", "sucursal":1100, "tipoDeComprobante":"DC",
  "tipoDeOperacion":"T", "version":"4.0", "idAplicacion":3, "formaPago":"05" }
```

Esos dos, más `/api/versiontimbrado`, son exactamente los que golpea el batch de noche.

## Qué suele fallar

- **Se acaba de actualizar (6 de agosto)**. Vigilar las corridas nocturnas del batch: es la primera semana con la versión nueva.
- Si aparecen errores de **RFC o razón social ilegibles**, es el archivo de cifrado `crypto.properties` que vive suelto en el servidor. Avisarme antes de tocarlo.

---

# 3. Middleware de Ticket — Detalle de una venta

## Objetivo

Servicio que, dado un número de ticket, **devuelve el detalle completo de la venta** (conceptos, importes, descuentos, datos de la tienda). Lo consumen el Autofacturador (#4) y el batch de timbrado (#1) para poder armar la factura.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Servicio SOAP Java (Spring Boot + CXF) sobre Tomcat |
| Servidor | `10.138.150.88`, puerto de aplicación `8088` |
| Endpoint | `https://10.138.150.88:8443/wsmdlwticket/Ticket/Obtener/v1.0?wsdl` |
| Origen de los datos | Oracle BCT — `ramsay.falabella.cl:1531`, SID `arsmxpr` |
| Usuario BD | `USW_BCT` / `ubct234dv8` |

## Cómo se arma un número de ticket

```
fecha(AAAAMMDD) + tienda(4) + caja(3) + transacción
20260721 + 2038 + 004 + 3911  →  2026072120380043911
```

Plantilla de la petición SOAP lista para copiar: archivo `notas-consultaTicket40.txt` en esta misma carpeta.

Prueba rápida de que responde:
```
curl -v "http://10.138.150.88:8088/wsmdlwticket/Ticket/Obtener/v1.0?wsdl"
```

## Qué suele fallar

- Si en el log ves un error que menciona `PKG_FACTURA_UNITARIA.GENERAR_COMPROBANTE`, **ignorá ese nombre**: es un mensaje viejo que quedó sin actualizar y manda a la pista equivocada. El procedimiento que realmente se usa es otro.

---

# 4. Autofacturador — Portal de autofacturación al cliente

## Objetivo

Portal público donde **el cliente se factura solo**: captura su número de ticket y el monto, el sistema valida contra la venta real y genera/timbra el CFDI. Tiene tres pantallas: generación de timbrado, consulta de un timbre y consulta de varios timbres.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Aplicación web Java (Spring MVC + JSP) sobre Tomcat |
| Servidor | `10.138.150.77` |
| Base de datos | MySQL/MariaDB `10.138.150.77:3306` — BD `facturacion` |
| Usuario BD | `facturaUser` / `facturaUser` |
| Consulta a la venta | Oracle BCT + Middleware de Ticket (#3) |

## Importante: se configura por base de datos, no por archivo

Mucho del comportamiento **no está en el código**, está en la tabla `catConfiguracion` de la BD `facturacion`. Si piden cambiar un límite o un texto, casi siempre es un `UPDATE` ahí, sin tocar la aplicación:

| Parámetro | Qué controla |
|---|---|
| `Aplicacion.ToleranciaTicket` | Diferencia máxima permitida entre el monto que captura el cliente y el real |
| `Aplicacion.DiasPermitidosFacturar` | Antigüedad máxima del ticket para poder facturarse |
| `ExpresionRegular.Monto` | Formato válido del monto |
| `DeshabilitarTimbradoLibre` | Bloquea el timbrado libre en devoluciones |
| `WebService.ObtenerTicket.Url` | A qué servicio le pide el detalle del ticket |

Los **textos de error que ve el cliente** salen de la tabla `catMensajes` (por número de mensaje), tampoco están en el código.

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\facturacion-consultasGenerales.sql` | Desglose completo de un ticket: `call cli_2fac_3cab_4det_5lfa_6ler('2026071610500066579')`. También `call uspEliminarTicket('<ticket>')` para liberar un ticket atorado |
| `Scripts\nuevos\facturacion-pendienteEnvio71.sql` | Facturas que quedaron pendientes de envío (`estatusenviado = 0`) |
| `Scripts\nuevos\facturacion-procedures.sql` | Definición de los procedimientos almacenados |

## Qué suele fallar

- **"¡Ticket o monto total inválido!"** — es el reclamo más común. Casi siempre el cliente capturó el neto de una nota de crédito en vez del total con IVA. Revisar el ticket con el script de desglose antes de escalar.
- Si un ticket quedó "en espera" y el cliente no puede volver a facturar, se libera con `uspEliminarTicket`.

---

# 5. WSCT — Consulta de detalle de ticket

## Objetivo

Servicio que **obtiene el detalle de un ticket de venta** a partir del número de ticket, de una orden de compra o de una guía de despacho manual. Consulta cuatro orígenes distintos según de dónde venga el dato.

## Datos básicos

| Origen | Motor | Servidor | Base de datos | Usuario | Password |
|---|---|---|---|---|---|
| Transacciones de tienda / Puntos CES | Oracle | `ramsay.falabella.cl:1531` | `arsmxpr` | `USW_BCT` | `ubct234dv8` |
| Portal web Sodimac | SQL Server | `10.138.150.124:5319` | `SODIMAC_WEB_PROD` | `SODIMACADM` | `Pa55word` |
| Pedidos | SQL Server | `10.138.150.124:5319` | `SODIMAC_PEDIDOS` | `SODIMACADM` | `Pa55word` |
| Consultas del Tótem | MySQL | `10.138.150.29:3306` | `totemconsultas` | `wsconsultaUser` | `wsconsultaUser` |

---

# 6. Rebates — Acuerdos comerciales con proveedores

## Objetivo

Portal web donde se administra la operación de **rebates con proveedores**: acuerdos, exclusiones, fill rate, pólizas, cálculo de MSI y reportes a Excel. Los usuarios de negocio cargan archivos y descargan reportes.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Aplicación web Java (Spring Boot + Thymeleaf) |
| Base de datos | SQL Server `10.138.150.124:5319` — BD `SODIMAC_REBATES_PROD` |
| Usuario BD | `SodimacUsrReb` |
| Ambiente de pruebas | BD `SODIMAC_REBATES_DEV` en `10.138.153.10` |

## Cómo funcionan los reportes

Cada reporte a Excel **lee una vista de la base de datos y vuelca columna por columna, sin transformar nada**. Si un reporte sale con un dato mal, el problema está en la vista, no en la aplicación. La principal es `vw_reporte_usuario`.

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\rebates-prodConsultasGenerales.sql` | Acuerdos, periodos y `controlDocumento` en producción |
| `Scripts\nuevos\rebates-devControlDocumento.sql` | Lo mismo en el ambiente de pruebas |
| `docs\soporte\rebates.sql` y `rebates-query-directa.sql` | Consultas armadas para casos de soporte |

## Qué suele fallar

- **"Cargué el archivo en Rebates y no se ve en SAP"** — reclamo recurrente (tickets OCR-762012, OCR-763681). Revisar `controlDocumento` con el script de producción: el archivo puede haber quedado con estatus pendiente.

## Contactos de negocio

`acastellanosc@sodimac.com.mx` · `smendozah@sodimac.com.mx` · `jlgomezg@sodimac.com.mx`

---

# 7. Tótem — Conteo cíclico de inventarios

## Objetivo

Dos aplicaciones que trabajan juntas para el **conteo cíclico de inventarios en tienda**:

- **Tótem Back**: el motor. Gestiona dispositivos, usuarios, asignación de conteos, catálogos de ubicación/zona/tipo de inventario y los reportes de artículos inventariados.
- **Tótem Admin**: la pantalla de administración de esos catálogos (sucursales, zonas, ubicaciones, dispositivos, usuarios web). **No tiene base de datos propia** — todo se lo pide al back.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Aplicaciones web Java (Spring Boot; el admin usa Thymeleaf) |
| Servidor | `10.138.150.87` |
| Base de datos | MySQL `10.138.150.29:3306` — BD `totem` |
| Usuario BD | `wstotemUser` / `wstotemUser` |
| Acceso al servidor | usuario `g_dco018` / `aWTezPuIIksZMQ` |

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\totem-consultasGenerales.sql` | Catálogos del tótem (tipos de seguridad, etc.) |
| `Scripts\nuevos\totem-redimensionamientoImagen.sql` | Ajustar la URL y el ancho de las imágenes de los comprobantes (parámetro en `catConfiguracion`) |

---

# 8. BCT Facturación — Sincronización y Facturación Global

## Objetivo

Batch con **cinco procesos de sincronización** entre BCT, el Portal In House, Fiscal y Puntos CES, más el proceso de **Facturación Global** (la factura mensual que agrupa todas las ventas al público general).

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Batch Java |
| Origen | Oracle BCT `ramsay.falabella.cl:1531` / `arsmxpr` — usuario `BATSW_FAC` / `M5R89NJYVS` |
| Destino fiscal | SQL Server `10.138.150.124:5319` — `SODIMAC_FISCAL_PROD` — `UserBatchFinanzas` / `kiTuNs39#m2$qPy2n1` |

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\oracle-facturacionGlobal.sql` | Extracción del detalle que usa Facturación Global |
| `Scripts\nuevos\etl-SincronizarFacturas77-71.sql` | Revisar la tabla temporal de la sincronización entre servidores |

## Qué suele fallar

- **El proceso de Facturación Global llena el espacio temporal de la base Oracle** (llegó a 32 GB, incidente 905861). Si el DBA reporta el TEMP lleno de madrugada, es este proceso. Es un problema conocido y abierto: reportarlo, no intentar corregir el proceso esta semana.

---

# 9. Portal CFDI

## Objetivo

Portal web de consulta de CFDI.

## Datos básicos

| Dato | Valor |
|---|---|
| URL | `https://10.138.150.76:8443/cfdi` |
| Servidor | `10.138.150.76` — acceso `user` / `@ifr2020#ProF$` |
| Base de datos fiscal | MySQL `10.138.150.71:3306` — BD `sodimacfiscal` — `UserBatchFinanzas` / `$gd20#45FcQ@` |

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\sodimacfiscal-consultasGenerales.sql` | Buscar facturas por UUID o RFC |
| `Scripts\nuevos\sodimacfiscal-borrarComplemento.sql` | Borrar el complemento y los folios de una factura (usar con cuidado, es destructivo) |

## Contacto

`juespiritu@sodimac.com.mx`

---

# 10. Descarga de OC de proveedor (Detecno)

## Objetivo

Proceso que **descarga las órdenes de compra de proveedor desde Detecno** y las deja disponibles para el resto de los sistemas.

## Datos básicos

| Dato | Valor |
|---|---|
| Tipo | Proceso Java, se lanza con `run.bat` |
| Dónde se valida | Servidores `10.138.150.83` y `10.138.153.13` |
| Base de datos | SQL Server `SODIMAC_SAP_PROD` (tablas `OrdenCompraProveedor`, `ControlVentaCes`) |

## Scripts de ayuda

| Script | Para qué |
|---|---|
| `Scripts\nuevos\fiscal-ordenCompraDetecno.sql` | Verificar cuántas OC se cargaron y de qué fechas |

---

# 11. Consolas (procesos en Windows)

Tres procesos que **no son aplicaciones web**: corren como ejecutables o `.bat` en servidores Windows.

| Proceso | Servidor | Ruta | Qué hace |
|---|---|---|---|
| **CrediVoucher** | `10.138.150.77` | `D:\CrediVoucher\credivoucher.bat` | Proceso de credivouchers (ticket STM-473) |
| **Puntos CES** | `10.138.150.88` | `D:\Consolas\BctFacturacionCes\bctfacturacionPuntosCes.bat` | Facturación de Puntos CES (ticket STM-182). También existe en el `.77`, pero ahí está **deshabilitado** — no activarlo |
| **Pagos Santander** | `10.138.150.76` | `Tickets_PagosVVEE_EnvioFacturacion.exe` | Envío de facturación de pagos. Su configuración está en el `appConfig` junto al ejecutable |

---

# Accesos a servidores

Todos por escritorio remoto o SSH; donde dice `:33689` ése es el puerto.

| Servidor | Dirección | Usuario | Password |
|---|---|---|---|
| Producción `.88` (AdminFacturación, Middleware, Puntos CES) | `10.138.150.88` | `g_dco018` | `UraTuA5WO5S=lM2` |
| Producción `.77` (wsft, Autofacturador, CrediVoucher) | `10.138.150.77:33689` | `user` | `$gdd2019#FacQ@` |
| Base MySQL producción `.71` | `10.138.150.71` | `rmartineztap` | `SodimacMexico2025*` |
| `.76` (Portal CFDI, Pagos Santander) | `10.138.150.76:33689` | `user` | `@ifr2020#ProF$` |
| Tótem `.87` | `10.138.150.87` | `g_dco018` | `aWTezPuIIksZMQ` |
| `.83` (Puntos CES) | `10.138.150.83:33689` | `ODAMX-DEV\admin-dev` | `sup3Rm@ri0Br055` |
| Procesos ETL | `10.138.150.38:33689` | `.\user` | `sup3Rm@ri0Br055` |
| Ambiente de pruebas | `10.138.153.10:33689` | `user` | `$0d1M4cPr0j3Ct` |
| Ambiente de pruebas (2) | `10.138.153.20:33689` | `user` | `$0d1M4cPr0j3CtS` |

**Comprobar si un servicio responde:**
```powershell
Test-NetConnection 10.138.150.88 -Port 8080
```

---

# Dónde están los scripts

Todos los scripts SQL de esta guía viven en:

```
C:\workspace-sodimac\docs\compartido\vacaciones\Scripts\nuevos\
```

| Script | Sistema |
|---|---|
| `sodimacfiscal-consultasGenerales.sql` | Portal CFDI / wsft — buscar facturas |
| `sodimacfiscal-borrarComplemento.sql` | Portal CFDI — borrar complemento (destructivo) |
| `facturacion-consultasGenerales.sql` | Autofacturador — desglose de un ticket |
| `facturacion-pendienteEnvio71.sql` | Autofacturador — pendientes de envío |
| `facturacion-procedures.sql` | Autofacturador — procedimientos |
| `fiscal-timbradoPacDetecno.sql` | wsft — qué se timbró |
| `wsfacturacion-usuarioToken.sql` | wsft — usuarios del servicio |
| `etl-SincronizarFacturas77-71.sql` | BCT Facturación — sincronización |
| `oracle-facturacionGlobal.sql` | BCT Facturación — Facturación Global |
| `fiscal-ordenCompraDetecno.sql` | Descarga OC Detecno |
| `rebates-prodConsultasGenerales.sql` | Rebates — producción |
| `rebates-devControlDocumento.sql` | Rebates — pruebas |
| `totem-consultasGenerales.sql` | Tótem — catálogos |
| `totem-redimensionamientoImagen.sql` | Tótem — imágenes de comprobantes |

En la carpeta `Scripts\` (un nivel arriba) hay consultas históricas sin clasificar, por si hace falta buscar algo puntual.

---

# Reglas de la semana

1. **Nada de cambios en producción sin avisarme.** Consultar, diagnosticar y documentar: sí. Desplegar, borrar o actualizar: no, salvo urgencia real.
2. **Reiniciar el Tomcat del `.88` tumba 5 aplicaciones a la vez.** Si hay que hacerlo, coordinar ventana y avisar.
3. **Nunca desplegar ni reiniciar en horario del batch nocturno de timbrado.**
4. Los problemas marcados como *conocidos y abiertos* (UsoCFDI G02, espacio temporal de Facturación Global) **no se arreglan esta semana** — se documenta el caso y se escala.
5. Todo lo que reporte alguien, anotarlo con fecha, qué pasó y qué se hizo. A la vuelta lo revisamos juntos.

---

# A quién buscar

| Tema | Persona |
|---|---|
| Cualquier duda de estos sistemas | David (a la vuelta) |
| Aprobar cambios en el repositorio | Marco |
| Rebates | Iván, Ana Castellanos |
| Fiscal / CFDI | Fernando, Bonelli |
| Tickets de SAP / OCR | María Guadalupe Martínez |
| Órdenes de compra | Gabo |

Tickets: `https://jira.falabella.tech/browse/STM-XXX`
