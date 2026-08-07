# Manual de cobertura — vacaciones David (legacy + soporte)

> **Para:** Josue — cobertura de 1 semana.
> **Alcance:** proyectos **legacy** (`C:\workspace-sodimac-legacy`) y **soporte** (`C:\workspace-sodimac\soporte`).
> **Fuera de alcance:** todo lo de **FBC / APP03022-mrch.\*** (fiscal-api, finanzas-api, BFFs, SPAs). Eso NO lo cubre este manual.
> Última actualización: 2026-08-07. Versión 1 — se va depurando.

> ⚠️ **Este documento contiene credenciales de producción en claro.** No sacarlo de la PC corporativa, no subirlo a repos públicos ni a Confluence abierto. Compartir sólo por canal interno.

---

## 0. Arranque rápido (primer día)

```powershell
# 1. Clonar/actualizar los repos legacy, cada uno en SU rama (no todos usan Develop)
cd C:\
C:\workspace-sodimac-legacy\docs\clonar-proyectos.ps1

# 2. Ver en qué rama quedó cada repo
Get-ChildItem C:\workspace-sodimac-legacy -Directory | ForEach-Object {
  if (Test-Path "$($_.FullName)\.git") {
    Push-Location $_.FullName
    "{0,-30} {1}" -f $_.Name, (git branch --show-current)
    Pop-Location
  }
}
```

**Lectura obligada antes de tocar nada:**

| Documento | Para qué |
|---|---|
| `C:\workspace-sodimac-legacy\docs\kb\README.md` | Índice del grafo de conocimiento (proyectos, servicios, hallazgos, runbooks) |
| `C:\workspace-sodimac-legacy\docs\ESTADO-PROYECTOS.md` | Estado real de cada repo: rama, verificación vs war de prod, MRs |
| `C:\workspace-sodimac-legacy\docs\BASES-DE-DATOS.md` | Qué BD usa cada proyecto |
| `C:\workspace-sodimac-legacy\docs\CHECKLIST-PENDIENTES.md` | Trabajo abierto |
| `C:\workspace-sodimac\docs\compartido\vacaciones\notas.txt` | Volcado crudo de credenciales/servidores (fuente de la sección 2) |

---

## 1. Mapa de proyectos

### 1.1 Legacy — clones de GitLab (`C:\workspace-sodimac-legacy\`)

| Proyecto | Qué es | Tecnología | Rama de trabajo | Servidor prod |
|---|---|---|---|---|
| `autofacturador` | Portal de autofacturación al cliente (CFDI) | Spring MVC + JSP, war | `master` | `10.138.150.77` |
| `sodimacfinanzaswsft` (`wsft`) | Batch nocturno de timbrado CFDI ante el PAC (Detecno) | Spring Boot 2.3.3, Java 8, war | `developer` | `10.138.150.77` |
| `finanzasadminfacturacion` | REST de parámetros y catálogos SAT + login JWT | Spring Boot 2.3.3, Java 8, war | `Develop` | `10.138.150.88` (`somxvladmfiscwebp`) |
| `finanzas_ws_mdw_ticket` (`wsmdlwticket`) | Middleware SOAP: detalle de ticket | Spring Boot 2.7.2, CXF, war | `redisenio-bd` | `10.138.150.88` puerto 8088 |
| `sodimacfinanzaswsct` (`wsct`) | WS de detalle de ticket (4 datasources) | Spring, war | `Develop` (destino **`master`**) | — |
| `sodimacfinanzasrebatesweb` | Portal web de Rebates con proveedores | Spring Boot + Thymeleaf | `master` | — |
| `totemback` | Backend de conteo cíclico de inventarios (tótems) | Spring Boot, war | `Develop` | `10.138.150.87` |
| `totemgcis` | Frontend Thymeleaf de administración del Tótem | Spring Boot + Thymeleaf | `Develop` | `10.138.150.87` |

### 1.2 Soporte — copias de trabajo (`C:\workspace-sodimac\soporte\`)

Estas carpetas **no son clones git independientes**: son subárboles del monorepo espejo `github.com/dmontesl-lovdav/sodimac` (rama `dmontes`). Para trabajar con git de verdad, usar el clon de `workspace-sodimac-legacy`.

| Carpeta | Repo GitLab | Qué es |
|---|---|---|
| `autofacturador` | `.../autofacturador.git` | igual que legacy |
| `finanzasadminfacturacion` | `mavasquezvi/finanzasadminfacturacion.git` | igual que legacy |
| `finanzas_ws_mdw_ticket` | `.../finanzas_ws_mdw_ticket.git` | igual que legacy |
| `sodimacfinanzaswsft` | `.../sodimacfinanzaswsft.git` | igual que legacy |
| `sodimacfinanzasrebatesweb` | `.../sodimacfinanzasrebatesweb.git` | igual que legacy |
| `bctfacturacion` | `.../finanzas_bctfacturacion.git` | Batch: 5 procesos de sincronización (BCT ↔ Portal In House ↔ Fiscal ↔ CES) + Facturación Global |
| `cfdi` | `.../finanzaswebcfdi.git` (`FinanzasWebCfdi`) | Portal web CFDI. Ver `CONFIGURACION-PERFILES.md` y `CONFIGURACION-WAR-PERFILES.md` en la carpeta |
| `finanzas_descarga_oc_prov_detecno` | `.../finanzas_descarga_oc_prov_detecno.git` | Descarga de órdenes de compra de proveedor desde Detecno. Trae `run.bat` y colección postman |
| `sap` | — | No es repo: carpeta de tickets OCR de SAP (`soporte SAP.txt`) |

**Base de todos los repos GitLab:** `https://gitlab.falabella.tech/sodimac-corp/desarrollo-sodimac-mexico/<repo>.git`
**Excepción:** `finanzasadminfacturacion` vive en el namespace **personal de Marco**: `https://gitlab.falabella.tech/mavasquezvi/finanzasadminfacturacion.git`

### 1.3 Consolas / batch .NET (no están en git)

| Proceso | Dónde corre | Ruta | Jira |
|---|---|---|---|
| CrediVoucher | `.77` | `D:\CrediVoucher\credivoucher.bat` | STM-473 |
| Puntos CES | `.88` (en `.77` está deshabilitado) | `D:\Consolas\BctFacturacionCes\bctfacturacionPuntosCes.bat` | STM-182 |
| Pagos Santander / Envío Facturación | `.76` | `Tickets_PagosVVEE_EnvioFacturacion.exe` (+ `appConfig`) | — |

---

## 2. Accesos y credenciales

### 2.1 Git

| Qué | Valor |
|---|---|
| GitLab | `https://gitlab.falabella.tech` — usuario corporativo, PAT propio |
| **Ojo** | El PAT da **403 en `/api/v4`**. La API REST no sirve. Los MR se crean con **push options** (sección 5.3) |
| Descubrir repos | Sin API: `git ls-remote <url>` |
| Autor a configurar en cada clon | `git config user.name "David Montes"` / `user.email "g_dco018@sodimac.com.mx"` — cambiar por los tuyos |
| Mirror personal (puente PC Sodimac ↔ PC con Claude) | `github.com/dmontesl-lovdav/sodimac`, única rama `dmontes` |
| Token GIT viejo en notas | `Vj3SXKxajT5wFujdMGS1` |

### 2.2 Servidores (SSH / RDP, puerto 33689 donde aplica)

| Servidor | IP:puerto | Usuario | Password | Qué corre |
|---|---|---|---|---|
| `somxvladmfiscwebp` (.88) | `10.138.150.88` | `g_dco018` | `UraTuA5WO5S=lM2` (anterior `UraTuA5WO5S=lM1`) | Tomcat: `finanzasadminfacturacion`, `wsmdlwticket`, `wsobtenerticket`, `wsprmfac`, `serviciopuntoventapos` + consola Puntos CES |
| .88 (otro acceso) | `10.138.150.88` | `rmartineztap` | `Oracle2x17` | — |
| Producción .77 | `10.138.150.77:33689` | `user` | `$gdd2019#FacQ@` | `wsft`, `autofacturador`, CrediVoucher |
| MySQL PROD (.71) | `10.138.150.71` | `rmartineztap` | `SodimacMexico2025*` | BD MySQL productiva |
| `somxvladmfiscdbp` (.12) | `10.138.150.12` | `g_dco018` | `++sm0t9QBao#ak1` | BD AdminFiscal (destino de migración desde .71) |
| .76 | `10.138.150.76:33689` | `user` | `@ifr2020#ProF$` | Consola Pagos Santander; `https://10.138.150.76:8443/cfdi` |
| .83 (BCFacturacionPuntoCes) | `10.138.150.83:33689` | `ODAMX-DEV\admin-dev` | `sup3Rm@ri0Br055` | — |
| ETLs nuevos (.38) | `10.138.150.38:33689` | `.\user` | `sup3Rm@ri0Br055` | ETLs |
| ETLs dev (.13) | `10.138.153.13:33689` | `.\admin-dev` | `sup3Rm@ri0Br055` | ETLs DEV |
| Pruebas (.10) | `10.138.153.10:33689` | `user` | `$0d1M4cPr0j3Ct` | Ambiente de pruebas |
| Pruebas (.20) | `10.138.153.20:33689` | `user` | `$0d1M4cPr0j3CtS` | Ambiente de pruebas |
| Totem PROD (.87) | `10.138.150.87` | `g_dco018` | `aWTezPuIIksZMQ` / `totemOracle2x252!` / `P#vRf_ImPHO9Iz` | `totemback`, `totemgcis` |
| Laboratorio (.25) | `10.138.153.25` | `g_dco018` | `WajmcPYm3h3FjtDML!` (temporal `WajmcPYm3h3Fjt`) | Laboratorio Oracle Linux (app `10.138.153.87`, BD `10.138.153.29`) |

> **Migración pendiente/anunciada:** AdminFiscal Web `150.88 → 150.7`, AdminFiscal DB `150.71 → 150.12`. En la migración hay que ajustar también el CFDI fiscal, que apunta al usuario `dba_mysql` del `.71`.

### 2.3 Bases de datos

**MySQL**

| Host:puerto | BD | Usuario | Password | Usado por |
|---|---|---|---|---|
| `10.138.150.71:3306` | `configuracion` | `configUser` | `ki&de$w29oEK` | `finanzasadminfacturacion` |
| `10.138.150.71:3306` | `sodimacfiscal` | `UserBatchFinanzas` | `$gd20#45FcQ@` | ETL reportes CFDI (destino) |
| `10.138.150.71:3306` | `sodimacfiscal` | `UserEtl` | `$gd20#45FcQ@` | ETL 77→71 |
| `10.138.150.71:3306` | `sodimacfiscal` | `userPagos` | `djIe7I4k7dn9ws$` | Consola Pagos VVEE |
| `10.138.150.71` (SSH) | admin | `dba_mysql` | `Sodimac123*` | administración |
| `10.138.150.12` (SSH) | admin | `dba_mysql` | `Ewqasdcxz123.MYSQL` | administración (nuevo host) |
| `10.138.150.77:3306` | `facturacion` | `facturaUser` | `facturaUser` | `autofacturador` (PROD) |
| `10.138.150.77:3306` | `wsfacturacion` | `wsfacturacionUser` | `wsfacturacionUser` | `wsft` |
| `10.138.150.74:3306` | `wsfacturacion` | `wsfacturacionUser` | `wsfacturacionUser` | — |
| `10.138.150.29:3306` | `totem` | `wstotemUser` | `wstotemUser` | `totemback` (texto plano) |
| `10.138.150.29:3306` | `totemconsultas` | `wsconsultaUser` | `wsconsultaUser` | `wsct` (datasource `ws`) |
| `10.138.153.10:3306` | `configuracion` | `configUser` | `configUser` | DEV |
| `10.138.153.10:3306` | `sodimacfiscal` | `sodimacfiscal` | `s$0d1m4cFi2c4L` | DEV |
| `10.138.153.10:4306` | `wsfacturacion` | `wsfacturacionUser` | `wsfacturacionUser` | DEV |

**SQL Server**

| Host:puerto | BD | Usuario | Password | Usado por |
|---|---|---|---|---|
| `10.138.150.124:5319` | `SODIMAC_FISCAL_PROD` | `UserBatchFinanzas` | `kiTuNs39#m2$qPy2n1` | reportes CFDI batch (origen) |
| `10.138.150.124:5319` | `SODIMAC_SAP_PROD` | `SodimacEtlUsr` | `$0dimac2020` | Puntos CES |
| `10.138.150.124:5319` | `SODIMAC_WEB_PROD` | `SODIMACADM` | `Pa55word` | `wsct` (datasource `dad`) |
| `10.138.150.124:5319` | `SODIMAC_PEDIDOS` | `SODIMACADM` | `Pa55word` | `wsct` (datasource `msk`) |
| `10.138.150.124:5319` | `SODIMAC_REBATES_PROD` | `SodimacUsrReb` | (ver `application.properties` del repo) | `sodimacfinanzasrebatesweb` |
| `10.138.150.83:1433` | `SODIMAC_FISCAL_PROD` | `UserBatchFinanzas` | `kiTuNs39#m2$qPy2n1` | — |
| `10.138.11.54` | `SODIMAC_TICKETS_OP` | `SODIMACADM` | `Pa55word` | Consola Pagos VVEE |
| `10.138.153.10:5319` | `SODIMAC_SAP_DEV` | `SodimacDevUsr` | `Pa55wordDev` | DEV |

**Oracle**

| Host:puerto:SID | Usuario | Password | Uso |
|---|---|---|---|
| `ramsay.falabella.cl:1531:arsmxpr` | `USW_BCT` | `ubct234dv8` | BCT PROD (`wsct`) |
| `ramsay.falabella.cl:1531:arsmxpr` | `BATSW_FAC` | `M5R89NJYVS` | BCT PROD (batch) |
| `10.222.109.24:1541:arsmxts` | `BATSW_FAC` | `lAMSL01LAM2` | BCT DEV/TEST (`autofacturador`) |
| `f8cloud1129:1541:arsmxts` | `oromero` | `DFVGRTHJy1Gj` | BCP DEV |
| `embalse:1531:odsrmxpr` | `usapmx` | `74OmUqP3` | "Sapito" PROD |

**PostgreSQL**

| Host:puerto | Password |
|---|---|
| `10.138.153.10:5432` | `Sodim@cP0str3s` (admin `adminpg4`) |
| `10.138.153.20:5432` | `Sodim@cP0sgr3s` |

### 2.4 Usuarios que usa el sistema (service accounts de aplicación)

| Usuario | Dónde se usa | Password |
|---|---|---|
| `userWsft` | Login JWT de `wsft` contra `finanzasadminfacturacion` | `E0R9KWF482` |
| `userWsPrmFac` | Login JWT de `wsprmfac` contra `finanzasadminfacturacion` | `E0R9KWF482` |
| `configUser`, `wstotemUser`, `wsconsultaUser`, `facturaUser`, `wsfacturacionUser`, `UserBatchFinanzas`, `UserEtl`, `userPagos`, `SodimacUsrReb`, `SodimacEtlUsr`, `SODIMACADM` | ver sección 2.3 | ídem |

Tabla de usuarios del WS: `wsfacturacion.usuariosws` (+ SP `uspExistToken`). Ver `Scripts/nuevos/wsfacturacion-usuarioToken.sql`.

### 2.5 Portales y cuentas de terceros

| Sistema | URL / cuenta | Password |
|---|---|---|
| PAC Detecno (certificados) | `https://genera.emisiondetecno.mx/Sodimac/Detickets/cfdiWebEmision_Servicio40_SodimacDetickets/asp/Certificados.aspx` — `Administrador` | `4343fdfd657jhfg0` |
| Rebates | `acastellanosc@sodimac.com.mx` | `KqN48j&S#Bmr` |
| Rebates | `smendozah@sodimac.com.mx` / `jlgomezg@sodimac.com.mx` | `20r3B4t3sS41d` |
| Fiscal (Iván) | `iscortesz@sodimac.com.mx` | `E0r9KwF$482s` |
| CFDI | `juespiritu@sodimac.com.mx` | `J0r4KaF$937x` |
| Otros | `molguinm@sod.com.mx` `veAR2XDJFZ` · `atorresga@sodimac.com.mx` `BQu4JomESrt` | |
| Confluence | `https://confluence.falabella.tech/display/SMI/Nuevo+portal+de+proveedores` | SSO |

### 2.6 Cifrado de credenciales en los properties

Casi todos los proyectos guardan url/usuario/password **cifrados** (AES-256/CBC/PKCS7, BouncyCastle, librería interna `com.sodimac.lib.cifrado`, clase `CryptoServiceImpl`). Cada `cifrado.properties` trae su `crypto.key` / `crypto.iv`.

- Clave vista en notas: `crypto.key=SODIMAC2020MARZOSODIMAC2020MARZO`, `crypto.iv=0000000000000000`
- Convención de las notas: `MDAW → crypto.key && crypto.iv`, `454SD → crypto.pass && crypto.salt`
- Imagen del proceso de encriptado: `docs/compartido/vacaciones/encriptar.png`
- **`finanzasadminfacturacion` usa DOS mecanismos distintos**: `cifrado.properties` (viaja **dentro** del war, descifra la BD) y `crypto.properties` (vive **suelto en el expandido**, lo exige `catconfiguracion.ENCRYPT_PATH`, cifra RFC/razón social). Ver riesgo HZ-009 en la sección 6.

---

## 3. Ficha por proyecto

### 3.1 `finanzasadminfacturacion` — REST de parámetros/catálogos SAT ⭐ el más caliente

| Dato | Valor |
|---|---|
| Repo | `https://gitlab.falabella.tech/mavasquezvi/finanzasadminfacturacion.git` (namespace personal de Marco) |
| Rama de trabajo | `Develop` (== `main`, ambas en `9e4d8d2`) |
| Tecnología | Spring Boot 2.3.3, Java 8, `packaging=war`, paquete `com.sodimac.wsconfiguracion` |
| Servidor | `10.138.150.88` (`somxvladmfiscwebp`), http 8080 / HTTPS 8443, Tomcat externo compartido |
| `CATALINA` real | **`/opt/tomcat/latest`** (NO `apache-tomcat-9.0.105` — la doc vieja miente; hacer `readlink -f` primero) |
| BD | MySQL `10.138.150.71:3306`, `configuracion`, `configUser` / `ki&de$w29oEK` |
| Log de la app | `$CATALINA/logs/wsft/wsft-logger.log` (sí, se llama `wsft`; el `logback.xml` heredó la ruta de otro proyecto) |
| Consumidor | el batch nocturno `wsft` desde `10.138.150.77` |

**Estado:** desplegado en PROD el **2026-08-06** con el fix del RCA `.88`; se timbró end-to-end. Runbook completo con bitácora: `docs/kb/runbooks/desplegar-finanzasadminfacturacion-prod.md`.

**Endpoints que golpea el batch:**
```
POST /finanzasadminfacturacion/api/login            {"username":"userWsft","password":"E0R9KWF482"}
POST /finanzasadminfacturacion/api/emisor           (con token)
POST /finanzasadminfacturacion/api/versiontimbrado  (con token)
```

**Qué vigilar esta semana:** las corridas nocturnas del batch de `wsft`. Es la primera vez que el código nuevo aguanta carga real. Si el pool se degrada → mirar `catalina.out` y el log de arriba.

**Pendientes:** MR `Develop→main`; desplegar el mismo fix en DEV; agregar `c3p0.config.checkoutTimeout`; fix de raíz de `crypto.properties`.

---

### 3.2 `sodimacfinanzaswsft` (`wsft`) — batch nocturno de timbrado CFDI

| Dato | Valor |
|---|---|
| Repo | `.../sodimacfinanzaswsft.git` |
| Rama viva | **`developer`** (no existe `develop` ni `Develop`) |
| Tecnología | Spring Boot 2.3.3, Java 8, war, `<name>wsft</name>` |
| Servidor | `10.138.150.77` |
| BD | MySQL `10.138.150.77:3306/wsfacturacion`, `wsfacturacionUser`/`wsfacturacionUser` |

Es quien **timbra ante el PAC (Detecno)**. Cliente SOAP en `clientews/wcfemision40/.../Detecno.java` (CFDI 4.0) y `wcfemision/` (3.3). El armado del CFDI está en `FacturasServiceImpl`: `obtenerDetalleTicket()` (:3271), `transformarXmlTicketXmlPacDetecno()` (:3386), `timbrarTipo40()` (:3168).

**No recalcula el descuento del concepto**, sólo reformatea decimales. Es un reenviador fiel de lo que manda el middleware.

🚩 **Trampa de ramas:** los 3 últimos commits de `developer` (2026-02-17) son **Reverts** de STM-1067 (timeout de consulta de ticket a BCT) + scripts de memoria/timeouts de Tomcat. `master` (último release, 2025-11-25) **sí** incluye STM-1067. No se sabe cuál corre en prod. **En pausa por decisión del usuario — no tocar.** Detalle: `docs/kb/hallazgos/HZ-003-stm-1067-revertido.md`.

🚩 **Bug abierto crítico (HZ-010):** CFDI de Ingreso timbrado con `UsoCFDI="G02"` (uso de Nota de Crédito). Causa raíz probada (correlación 95.9%): una devolución contamina `clientes.idUsoCfdi` a G02 vía SP `uspCrearTemporal`, y **todas las ventas posteriores de ese cliente salen mal**. 3,808 clientes, 19,672 facturas afectadas. Si reportan facturas con UsoCFDI equivocado esta semana, es esto.

---

### 3.3 `finanzas_ws_mdw_ticket` (`wsmdlwticket`) — middleware SOAP de detalle de ticket

| Dato | Valor |
|---|---|
| Repo | `.../finanzas_ws_mdw_ticket.git` |
| **Rama de PROD** | **`redisenio-bd`** (verificado contra el war real 2026-08-05). `main` quedó obsoleta |
| Tecnología | Spring Boot 2.7.2 + CXF, war, puerto 8088, `cxf.path=/` |
| Endpoint PROD | `https://10.138.150.88:8443/wsmdlwticket/Ticket/Obtener/v1.0?wsdl` |

🚩 **`origin/HEAD` apunta a `main`.** Quien clone y compile sin cambiar de rama **construye la app vieja** y la despliega contra un SP que ya no es el vivo. `main` y `redisenio-bd` no son la misma app con un fix de diferencia: usan **stored procedures distintos**.

| | `main` (obsoleta) | `redisenio-bd` (PROD) |
|---|---|---|
| SP | `PKG_FACTURA_UNITARIA.GENERAR_COMPROBANTE` | `SW_FAC.PKG_CFDI_CLI.GET_DATOS_TICKET` |
| Parámetros | 8 | 38 |
| Devuelve | CLOB con XML, parseado con DOM | out params tipados + 2 cursores |

**Pista falsa conocida:** `QueryBctRepository.java:228` loguea el error como `PKG_FACTURA_UNITARIA.GENERAR_COMPROBANTE` (nombre viejo, copy-paste sin actualizar). Durante un incidente ese log manda a la pista equivocada.

**Armar un request de ticket:** runbook `docs/kb/runbooks/armar-request-obtener-ticket.md`. Plantilla SOAP en `docs/compartido/vacaciones/notas-consultaTicket40.txt`. Formato de ticket = `fecha(AAAAMMDD) + tienda(4) + caja(3) + transaccion` → p.ej. `20260721 2038 004 3911` = `2026072120380043911`.

---

### 3.4 `autofacturador` — portal de autofacturación al cliente

| Dato | Valor |
|---|---|
| Repo | `.../autofacturador.git` |
| Rama viva | **`master`**. Existe `develop` **minúscula** muerta desde 2020 — **no usarla** (y en Windows crear `Develop` colisionaría, FS case-insensitive) |
| Tecnología | Spring MVC clásico (no Boot), war, JSP + JSTL, jQuery/Bootstrap/SweetAlert2, paquete `com.sodimac.facturacion` |
| BD | MariaDB `facturacion` — PROD `10.138.150.77:3306`, DEV `10.138.153.10:4306`; Oracle BCT `10.222.109.24:1541:arsmxts` |

**Pantallas:** generación de timbrado (`factura-form.jsp`), consulta de timbre (`factura-consulta.jsp`), consulta múltiple (`factura-multiple.jsp`).

**Muchísimo comportamiento vive en la tabla `catConfiguracion`, no en el código:** `Aplicacion.ToleranciaTicket`, `Aplicacion.DiasPermitidosFacturar`, `ExpresionRegular.Monto`, `WebService.ObtenerTicket.Url`, `WebService.Sodimac.Pais/.Comercio/.Canal`, `DeshabilitarTimbradoLibre`. Los textos de error salen de `catMensajes`.

**Flujo de validación** (`TicketsServiceImpl.validarTicketWS`, :109) — cada paso hace return temprano, el orden importa:
1. ya facturado → msg 51 · 2. en espera → msg 52 · 3. **no existe en BCT → `return "OK"` sin validar monto** · 4. TIPO_TRX 9/10 = devolución · 5. muy viejo → msg 124 · 6. en proceso en BCT → msg 51 · 7. **compara montos contra el WS** → msg 55 · 8. timbrado libre deshabilitado → msg 115 · 9. devolución sin UUID → msg 114.

🚩 **Riesgos abiertos:** (a) en `master` la URL de BD **activa es la de DEV** y la de prod está comentada — quien buildee de `master` despliega apuntando a DEV (HZ-002); (b) el WSDL del cliente SOAP está clavado a DEV en el war de prod (HZ-007, confirmado en el binario); (c) un ticket de tipo no soportado **se salta TODAS las validaciones** (HZ-005, bug real).

**Si reportan "¡Ticket o monto total inválido!"** → runbook `docs/kb/runbooks/diagnosticar-monto-invalido.md`.

---

### 3.5 `sodimacfinanzaswsct` (`wsct`) — WS de detalle de ticket

| Dato | Valor |
|---|---|
| Repo | `.../sodimacfinanzaswsct.git` |
| Rama principal | **`master`** (no `main`). Rama de trabajo: `Develop` |
| Datasources | 4: `bct` (Oracle), `dad` / `msk` (SQL Server), `ws` (MySQL) — ver 2.3 |
| Verificado | 118/118 clases idénticas al `wsct.war` de prod |

Host de BD sigue en `10.138.150.29`; el cambio a `153.29` fue **sólo laboratorio**, no aplicado en real.

---

### 3.6 `totemback` + `totemgcis` — conteo cíclico de inventarios

| | `totemback` | `totemgcis` |
|---|---|---|
| Qué es | Backend de conteo cíclico (dispositivos, usuarios, asignación, catálogos, reportes) | Frontend Thymeleaf de administración de catálogos |
| Rama | `Develop` | `Develop` |
| BD | MySQL `10.138.150.29:3306/totem`, `wstotemUser`/`wstotemUser` (texto plano) | **ninguna** — todo por HTTP a `totemback` |
| Servidor | `10.138.150.87` | `10.138.150.87` |

🚩 En `totemgcis` la rama real de prod era **`conteo-ciclico`**, no `main` (a `main` le faltaban 66 commits). `SONARQUBE` tampoco sirve de referencia.
🚩 Drift abierto: `applicationdev.properties` existe en el war pero **nunca estuvo en ningún commit**. Probablemente archivo muerto. Decidido no tocar.

---

### 3.7 `sodimacfinanzasrebatesweb` — portal de Rebates

| Dato | Valor |
|---|---|
| Repo | `.../sodimacfinanzasrebatesweb.git` |
| Rama | `master` (última 2024-12-18) |
| Tecnología | Spring Boot + Thymeleaf, `com.sodimac.rebates`, ~312 clases |
| BD | SQL Server `SODIMAC_REBATES_PROD` en `10.138.150.124:5319`, usuario `SodimacUsrReb` |

🚩 La rama **`master-FixBtnPeriodo`** (2025-07, STM-710: cambia cuenta de correo, quita carga a SFTP, reimplementa FTP) es **más reciente que `master` y NO está mergeada**. Confirmar cuál corre en prod antes de tocar. La `Develop` de este repo está muerta (2024-01).

**Reportes a Excel:** Apache POI `SXSSFWorkbook`, en `util/Export*Excel.java`. Cada reporte lee una vista de BD y vuelca columna por columna sin transformar. Mapeo del "Reporte Usuario": `docs/kb/datos/vw-reporte-usuario.md`.

---

### 3.8 `bctfacturacion` — batch de sincronización + Facturación Global

Repo `.../finanzas_bctfacturacion.git`. 5 procesos de sincronización (BCT ↔ Portal In House ↔ Fiscal ↔ CES) + Facturación Global.

🚩 **HZ-008 (bug real):** el job de Facturación Global **llena el TEMP de `arsmxpr` (32 GB, incidente 905861)** por un join cabecera × detalle. Si el DBA reporta TEMP lleno de noche, es esto.

---

### 3.9 `cfdi` (`FinanzasWebCfdi`) y `finanzas_descarga_oc_prov_detecno`

- `cfdi`: portal web CFDI. Config por perfiles documentada en la propia carpeta (`CONFIGURACION-PERFILES.md`, `CONFIGURACION-WAR-PERFILES.md`). Accesible en `https://10.138.150.76:8443/cfdi`.
- `finanzas_descarga_oc_prov_detecno`: descarga de OC de proveedor desde Detecno. Trae `run.bat` y colección Postman en `postman/`. Validar en `.83` / `.13` que el proceso corra bien.

---

## 4. Compilar (esto SIEMPRE tropieza)

### 4.1 Maven: el perfil de Indra rompe la resolución

El `mvn` del PATH es `C:\apache-maven-3.9.6-indra`, apunta al nexus de Indra (`slmaven.indra.es`) y **exige VPN**. Además trae un perfil de otro cliente activo por defecto.

```bash
# Opción A — desactivar el perfil que estorba
mvn -P '!MEJINGEN_profile' clean package -DskipTests

# Opción B — usar el Maven default contra Maven Central (sin VPN)
/c/apache-maven-3.9.6/bin/mvn -s /c/apache-maven-3.9.6/conf/settings.xml clean package -DskipTests
```

### 4.2 Jars propietarios que no están en Central

Vienen en `lib/` de cada repo (ver `lib/How to Install Libs.txt`). Instalar una vez en el `.m2`:

```bash
cd lib/crypto  && mvn install:install-file -Dfile=crypto-0.0.1-SNAPSHOT.jar  -DpomFile=pom.xml
cd lib/cifrado && mvn install:install-file -Dfile=cifrado-0.0.1-SNAPSHOT.jar -DpomFile=pom.xml
mvn install:install-file -Dfile=lib/ojdbc6/ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=10.2.0.4.0 -Dpackaging=jar

# jars de esquemas SAT (autofacturador / wsft)
mvn install:install-file -Dfile=libs\cfdv40.jar -DgroupId=mx.gob.sat -DartifactId=cfdv40 -Dversion=1.0 -Dpackaging=jar
```

> Si en `~/.m2/repository/com/oracle/ojdbc6/` sólo hay archivos `.lastUpdated`, eso **no** es el jar — instalarlo igual.

### 4.3 JDK

```bat
SET JAVA_HOME=C:\software\java\jdk1.8.0_441   :: legacy (todo lo de este manual)
SET JAVA_HOME=C:\Program Files\Java\jdk-17    :: FBC (fuera de alcance)
SET PATH=%JAVA_HOME%\bin;%PATH%
```

---

## 5. Runbooks

### 5.1 Desplegar a producción (Tomcat externo)

Runbooks completos, con rollback y smoke test:
- `docs/kb/runbooks/desplegar-finanzasadminfacturacion-prod.md` (incluye bitácora del deploy real del 2026-08-06)
- `docs/kb/runbooks/desplegar-wsmdlwticket-prod.md`

Esqueleto común (todo como usuario `tomcat`):

```bash
readlink -f /opt/tomcat/latest        # 0. resolver CATALINA REAL, no confiar en la doc
mvn clean package -DskipTests         # 1. desde la rama correcta, git status limpio
scp target/<app>-0.0.1-SNAPSHOT.war  server:/tmp/<app>.war
sha256sum /tmp/<app>.war              #    verificar integridad

ls -la $CATALINA/webapps
$CATALINA/bin/shutdown.sh             # 2. TUMBA TODAS las apps del Tomcat

cd $CATALINA/webapps
mv <app>.war <app>.war.$(date +%Y%m%d).rollback     # 3. convención de respaldo del server
cp -a <app> ~/expandido-<app>-$(date +%Y%m%d).bak   #    *** OBLIGATORIO: archivos sueltos ***
ls -la <app>/*.properties
rm -rf <app>

cp /tmp/<app>.war $CATALINA/webapps/<app>.war       # 4.
$CATALINA/bin/startup.sh                            # 5.
tail -f $CATALINA/logs/catalina.out

# 5b. reponer los sueltos (p.ej. crypto.properties), chown tomcat:tomcat, chmod 640
# 6. smoke test — 7. rollback: shutdown, rm -rf, mv .rollback, startup
```

⚠️ **El shutdown del `.88` tumba también** `wsmdlwticket`, `wsobtenerticket`, `wsprmfac` y `serviciopuntoventapos`. Coordinar ventana y **desplegar fuera del horario del batch nocturno de timbrado**.

### 5.2 JVM del Tomcat (`setenv.sh` del `.88`)

```bash
export JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m"
export JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
export JAVA_OPTS="$JAVA_OPTS -Duser.timezone=America/Mexico_City -Dfile.encoding=UTF-8"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```
Copia completa en `docs/soporte/88.txt`.

### 5.3 Crear un Merge Request (el PAT da 403 en la API)

`main` y `master` están **protegidas** — todo entra por MR y lo aprueba/mergea alguien con Maintainer (Marco = `mavasquezvi`).

Método recomendado: empujar a una rama nueva desechable (siempre dispara las opciones):

```bash
git push -v \
  -o merge_request.create \
  -o merge_request.target=master \
  -o merge_request.assign=mavasquezvi \
  -o merge_request.title="Titulo del MR" \
  -o merge_request.remove_source_branch \
  origin refs/heads/Develop:refs/heads/mr/develop-a-master
```

- Salida esperada: `remote: View merge request for ...`. Si dice `To create a merge request, visit...` → **NO se creó**.
- **La trampa:** si el push no actualiza ninguna referencia, git dice `Everything up-to-date` y GitLab nunca ve las opciones. Por eso la rama desechable.
- Usar **refspec completo** (`refs/heads/X:refs/heads/Y`) y **`-v`**.
- **`merge_request.target` cambia por repo**: unos usan `main`, otros `master`. No asumir.

Detalle: `docs/kb/runbooks/crear-mr-gitlab-push-options.md`.

### 5.4 Comparar un war contra el repo (¿qué corre realmente en prod?)

```bash
unzip -o -q <war> "WEB-INF/classes/**"   # OJO: el patrón "WEB-INF/classes/*" NO cruza "/" → falsos negativos
javap -c -p -classpath WEB-INF/classes <FQCN>
```

### 5.5 Otros comandos útiles

```powershell
Test-NetConnection 10.138.150.88 -Port 8080
Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*java*" } | Select-Object ProcessId,Name,CommandLine
subst d: c:\      # montar unidad C en D (lo piden algunas consolas)
```
```bash
curl -v "http://10.138.150.88:8088/wsmdlwticket/Ticket/Obtener/v1.0?wsdl" \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H "SOAPAction: http://mdwcorp.falabella.com/SOD/CORP/OSB/wsdl/Cliente/Ticket/Obtener-v1.0/Op" \
  -d @request.xml
```

---

## 6. Riesgos vivos — qué puede explotar esta semana

| ID | Qué | Proyecto | Estado |
|---|---|---|---|
| **HZ-009** | `crypto.properties` vive **fuera del war**, en el expandido. **Cada deploy lo borra y tumba el cifrado** de RFC/razón social | `finanzasadminfacturacion` | Golpeó en el deploy del 2026-08-05/06. Repuesto a mano; falta fix de raíz |
| **HZ-010** | CFDI de Ingreso timbrado con `UsoCFDI="G02"`. Una devolución contamina `clientes.idUsoCfdi` vía SP `uspCrearTemporal` y todas las ventas posteriores salen mal | `wsft` | Abierto — causa raíz probada. 3,808 clientes / 19,672 facturas |
| **HZ-008** | Facturación Global llena el TEMP de `arsmxpr` (32 GB, incidente 905861) | `bctfacturacion` | Abierto, bug real |
| **HZ-005** | Ticket de tipo no soportado **se salta todas las validaciones** | `autofacturador` | Abierto, bug real |
| **HZ-007** | WSDL del cliente SOAP clavado a **DEV** en el war de prod | `autofacturador` | Confirmado en el binario |
| **HZ-002** | `master` tiene la BD de **DEV** activa y prod comentada | `autofacturador` | Abierto |
| **HZ-003** | `developer` revirtió STM-1067, `master` lo tiene. No se sabe qué corre en prod | `wsft` | **En pausa — no tocar** |
| **HZ-004** | `condicionesPago` se pide al SP y nunca se setea | `wsmdlwticket` | Abierto |
| **RCA .88** | `c3p0.config.checkoutTimeout` sigue **sin configurar**. El refactor JPA aísla la contención, la causa raíz de configuración sigue viva | `finanzasadminfacturacion` | Parcial |

Fichas completas en `C:\workspace-sodimac-legacy\docs\kb\hallazgos\`.

### Cosas que NO hay que hacer

- ❌ No compilar `finanzas_ws_mdw_ticket` desde `main` (es la app vieja, `origin/HEAD` apunta ahí).
- ❌ No usar la rama `develop` minúscula de `autofacturador` (muerta desde 2020).
- ❌ No tocar el tema STM-1067 de `wsft` (en pausa por decisión del usuario).
- ❌ No hacer `rm -rf` del expandido sin respaldar antes los `.properties` sueltos.
- ❌ No hacer push directo a `main`/`master` (protegidas, se rechaza).
- ❌ No desplegar en el `.88` durante la ventana del batch nocturno.
- ❌ No asumir que la rama de trabajo es `Develop`: **cambia por repo** (ver tabla 1.1).

---

## 7. Scripts SQL de consulta y soporte

**Ruta:** `C:\workspace-sodimac\docs\compartido\vacaciones\Scripts\`

### 7.1 Los organizados por proyecto (`Scripts\nuevos\`) — usar estos primero

| Archivo | Motor / BD | Para qué |
|---|---|---|
| `sodimacfiscal-consultasGenerales.sql` | MySQL `sodimacfiscal` | Consultas de facturas por UUID/RFC. El más usado |
| `sodimacfiscal-borrarComplemento.sql` | MySQL `sodimacfiscal` | Borrar complemento/folios de una factura (`facturas`, `foliofactura`, `foliofacturadet`) |
| `facturacion-consultasGenerales.sql` | MySQL `facturacion` | `call cli_2fac_3cab_4det_5lfa_6ler('<ticket>')` — desglose completo de un ticket. `call uspEliminarTicket('<ticket>')` |
| `facturacion-procedures.sql` | MySQL `facturacion` | DDL de SPs (`uspObtenerFacturaTicket`, etc.) |
| `facturacion-pendienteEnvio71.sql` | MySQL | Facturas con `estatusenviado=0` pendientes de envío |
| `etl-SincronizarFacturas77-71.sql` | SQL Server `SODIMAC_FISCAL_PROD` | Revisar `Facturas_Temp` de la ETL 77→71 |
| `fiscal-timbradoPacDetecno.sql` | SQL Server | `TIMBRADO_PAC_DETECNO`, `CONTROL_PAC_TICKET`, `CONTROL_PAC_SERIE`, `BITACORA_ACTIVIDADES` |
| `fiscal-ordenCompraDetecno.sql` | SQL Server `SODIMAC_SAP_PROD` | `OrdenCompraProveedor`, `ControlVentaCes` |
| `oracle-facturacionGlobal.sql` | Oracle BCT | Extracción de detalle para Facturación Global |
| `rebates-prodConsultasGenerales.sql` | SQL Server `SODIMAC_REBATES_PROD` | Acuerdos, periodos, `controlDocumento` |
| `rebates-devControlDocumento.sql` | SQL Server `SODIMAC_REBATES_DEV` | Ídem en DEV |
| `totem-consultasGenerales.sql` | MySQL `totem` | Catálogos del tótem (`cattiposeguridad`, etc.) |
| `totem-redimensionamientoImagen.sql` | MySQL | `catConfiguracion` → `Response.Comprobante.Concepto.Url` y ancho de imagen |
| `wsfacturacion-usuarioToken.sql` | MySQL `wsfacturacion` | `usuariosws` + `uspExistToken` |
| `nuevos\fbc\fbc-mensajesValidacionFactura.sql` | — | (FBC, fuera de alcance) |

### 7.2 `Script-1.sql` … `Script-53.sql`

Volcado histórico sin clasificar (algunos vacíos: 33, 44, 50). Sirven de archivo; buscar con:
```powershell
Select-String -Path 'C:\workspace-sodimac\docs\compartido\vacaciones\Scripts\*.sql' -Pattern 'loQueBusco'
```
**Depuración pendiente:** clasificar estos 53 en `nuevos\` por proyecto.

### 7.3 Otros scripts útiles

| Script | Qué hace |
|---|---|
| `C:\workspace-sodimac-legacy\docs\clonar-proyectos.ps1` | Clona/actualiza los 8 repos legacy, cada uno en su rama |
| `C:\workspace-sodimac\docs\scripts\sync-sodimac-to-mirror.ps1` | Sincroniza repos reales → mirror (sólo FBC) |
| `C:\workspace-sodimac\docs\soporte\rebates.sql`, `rebates-query-directa.sql` | Consultas de soporte de Rebates |
| `C:\workspace-sodimac\soporte\sodimacfinanzasrebatesweb\script_REBATES_PROD.sql` | Script de PROD de Rebates |
| `C:\workspace-sodimac\soporte\sodimacfinanzasrebatesweb\configuracion_ssl_tomcat9.txt` | Configurar SSL en Tomcat 9 |

---

## 8. Registro de soporte del equipo

Cuando alguien reporte algo, dejarlo registrado en `C:\workspace-sodimac\docs\soporte\<persona>.md` (entrada nueva **al inicio**, con fecha, contexto, problema, solución y Jira).

Archivos existentes: `bonelli.md`, `fer.md`, `ivan.md`, `josue.md`, `jose-luis.md`, `robert.md`, `eli.md`. Plantilla: `_TEMPLATE.md`.

Buscar en todo el historial:
```powershell
Select-String -Path 'C:\workspace-sodimac\docs\soporte\*.md' -Pattern 'texto a buscar'
```

**Jira:** `https://jira.falabella.tech/browse/STM-XXX`

---

## 9. Estado de los clones al momento de escribir esto (2026-08-07)

Ojo: algunos repos quedaron en ramas de trabajo distintas a la "oficial". Si vas a compilar, **verificá primero**.

| Repo | Rama checkeada ahora | Rama oficial de trabajo | Último commit |
|---|---|---|---|
| `autofacturador` | ⚠️ `fix/wsdl-endpoint-prod` | `master` | 2026-07-31 `26aaae7` Apuntar WSDL a producción |
| `finanzas_ws_mdw_ticket` | ⚠️ `merge-prep/redisenio-to-main` | `redisenio-bd` | 2026-08-06 `0eb0c52` |
| `finanzasadminfacturacion` | `Develop` ✔ | `Develop` | 2025-10-07 `9e4d8d2` |
| `sodimacfinanzaswsct` | `Develop` ✔ | `Develop` | 2025-08-19 `af631f4` |
| `sodimacfinanzaswsft` | `developer` ✔ | `developer` | 2026-02-17 `2448904` (Revert) |
| `sodimacfinanzasrebatesweb` | `master` ✔ | `master` | 2024-12-18 `7606afb` |
| `totemback` | `Develop` ✔ | `Develop` | 2026-07-14 `664811c` fix pool Hikari |
| `totemgcis` | `Develop` ✔ | `Develop` | 2025-07-03 `0e9565c` |

---

## 10. Pendientes abiertos (por si hay tiempo, no urgentes)

- MRs sin crear: `totemback`→`main`, `totemgcis`→`main`, `sodimacfinanzaswsct`→**`master`**, `finanzasadminfacturacion` `Develop`→`main`.
- `finanzasadminfacturacion`: desplegar el fix en DEV; `c3p0.config.checkoutTimeout`; fix de raíz de `crypto.properties`; confirmar que `GET /crypto.properties` con token válido da 404.
- Revisar si las otras apps del `.88` (`wsprmfac`, `serviciopuntoventapos`, `wsobtenerticket`) tienen archivos sueltos en el expandido que un redeploy borraría.
- `wsft`: resolver STM-1067 (en pausa) y cerrar HZ-010.
- `autofacturador`: verificar `master` contra el war real de prod; decidir manejo de config por ambiente.
- `sodimacfinanzasrebatesweb`: confirmar si prod corre `master` o `master-FixBtnPeriodo`.
- `finanzas_ws_mdw_ticket`: decidir qué se hace con `main` (obsoleta y sigue siendo la rama por defecto del remoto).
- Depurar/clasificar los 53 scripts sueltos de `Scripts\`.

---

## 11. A quién escribirle

| Tema | Persona |
|---|---|
| MRs / aprobar y mergear en GitLab (Maintainer) | Marco — usuario GitLab `mavasquezvi` |
| Rebates | Iván (`iscortesz@sodimac.com.mx`), Ana C. (`acastellanosc@sodimac.com.mx`) |
| Fiscal / CFDI | Fernando, Bonelli |
| SAP / OCR | María Guadalupe Martínez de la Cruz |
| Órdenes de compra | Gabo |

---

_Documento vivo. Cada cosa que descubras o corrijas esta semana: anotala acá o en `C:\workspace-sodimac-legacy\docs\kb\`._
