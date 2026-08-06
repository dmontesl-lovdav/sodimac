# Migracion Tomcat Yulike - Servidor .25

**IMPORTANTE (correccion 2026-07-14): `.25` (`somxvlcarteleriaq`) NO es el servidor productivo.** Es un servidor de **laboratorio** creado para probar la actualizacion de Tomcat antes de aplicarla en real. El servidor productivo real es **`10.138.150.87`** (coincide con el `url: https://10.138.150.87` del profile `prod` en `totemgcis/src/main/resources/application.yml`). Lo que se actualizo de verdad fue el Tomcat, no la BD — la BD en real nunca cambio, sigue en `150.29`.

**Verificado 2026-07-14**: se bajaron los `.war` reales de produccion (`.87`) a `sesiones/git/entregables .87/entregables/`. Hash SHA256 identico byte a byte contra los 3 wars de `.25` ya analizados (`totem.war`, `wsct.war`, `gcis.war`) — el laboratorio era copia fiel de lo real. Toda la verificacion repo-vs-war de este documento (drift Hikari, applicationdev.properties, clases 1:1) sigue siendo valida contra produccion real, no hace falta repetirla.

Tarea original: repuntar BDs de los wars corriendo en Tomcat `10.138.153.25` hacia el nuevo servidor de BD `10.138.153.29` (laboratorio), y verificar que el codigo fuente en `sesiones/git/` coincide con lo desplegado.

Origen: nota `sesiones/git/war/20260707-yulike.txt` + tabla `sesiones/git/war/image01.png`.

---

## Acceso servidor

| Campo | Valor |
|---|---|
| Servidor | `10.138.153.25` |
| Hostname | `somxvlcarteleriaq` |
| OS | Linux (RHEL 9) |
| Usuario | `g_dco018` |
| Password temporal | `WajmcPYm3h3Fjt` |
| Password nueva | `WajmcPYm3h3FjtDML!` (usar esta para `sudo su -`, no la temporal) |
| Tomcat | `/opt/tomcat/apache-tomcat-9.0.118/webapps/<contexto>/WEB-INF/classes/` (wars expandidos: `wsct/`, `totem/`, `gcis/`) |

## Mapa WAR -> DB (segun imagen recibida)

La imagen trae 2 grupos por color:
- **Verde**: `carteleria-rest_localhost.war` → DB `carteleria`. **Fuera de scope** — no es parte de esta tarea, no tocar.
- **Amarillo** (grupo real de esta migracion): `gcis.war`, `totem.war`, `wsct.war` (fila sin datos en la imagen, pero sí tiene datasource sobre la misma DB).

| WAR | DB (segun cree Yulike, imagen) | Servidor nuevo |
|---|---|---|
| `gcis.war` | `totem` | `10.138.153.29` |
| `totem.war` | `totemconsultas` | `10.138.153.29` |
| `wsct.war` | (sin fila) | `10.138.153.29` |

**La columna DB de la imagen es lo que Yulike cree que esta apuntando cada war — no es el ground truth.** Lo correcto es lo que dicen los `.properties` reales dentro del war (ver seccion "Realidad encontrada"): `totem.war` en realidad usa DB `totem`, y quien usa `totemconsultas` es el datasource `ws` de `wsct.war`. Se documenta y se actua sobre la realidad, no sobre la etiqueta de la fila.

---

## Realidad encontrada (properties + wars decompilados)

Wars analizados en `sesiones/git/war/*.war` (extraidos en `sesiones/git/war/extract/`). No hubo que decompilar bytecode de la app — los `.properties` viajan en texto plano dentro del war. Si hubo que decompilar la libreria interna `cifrado-0.0.1-SNAPSHOT.jar` (dentro de `wsct.war`, clase `CryptoServiceImpl`) para entender el algoritmo de cifrado de credenciales.

### Algoritmo de cifrado (`com.sodimac.lib.cifrado`)
- AES-256/CBC/PKCS7, libreria BouncyCastle.
- Key: `crypto.key` de `cifrado.properties` (32 chars ASCII = 32 bytes).
- IV real usado en el cipher: `crypto.iv` de `cifrado.properties` (`0000000000000000`, 16 bytes ASCII cero).
- Detalle: el valor cifrado en el `.properties` es `base64( IV_literal(16 bytes) + ciphertext )`. Los primeros 16 bytes decodificados se descartan al desencriptar (no son un IV real, es un prefijo fijo).
- Clave/IV en `wsct.war` actual: `crypto.key=SODIMAC2020MARZOSODIMAC2020MARZO`, `crypto.iv=0000000000000000` (coincide con el repo `sodimacfinanzaswsct`).

### wsct.war (proyecto `sodimacfinanzaswsct`) — 4 datasources

| Datasource | Motor | Host actual | DB | Usuario | Password |
|---|---|---|---|---|---|
| `bct` | Oracle | `ramsay.falabella.cl:1531` | `arsmxpr` (SID) | `USW_BCT` | `ubct234dv8` |
| `dad` | SQL Server | `10.138.150.124:5319` | `SODIMAC_WEB_PROD` | `SODIMACADM` | `Pa55word` |
| `msk` | SQL Server | `10.138.150.124:5319` | `SODIMAC_PEDIDOS` | `SODIMACADM` | `Pa55word` (mismas creds que `dad`) |
| `ws` | MySQL | `10.138.150.29:3306` | `totemconsultas` | `wsconsultaUser` | `wsconsultaUser` |

Solo `ws` (MySQL, `totemconsultas`) esta en el rango `150.29 -> 153.29` de esta migracion. `bct`, `dad` y `msk` son otra infraestructura (Oracle Falabella / SQL Server Fiscal-Pedidos) y **no** deben tocarse en esta tarea.

### totem.war (proyecto `totemback`)
- `spring.datasource.url = jdbc:mysql://10.138.150.29:3306/totem?...` (MySQL, DB `totem`, plano, sin cifrar).
- Usuario `wstotemUser` / password `wstotemUser`.
- Historial de hosts probados, comentados en el mismo archivo: `153.10`, `150.89`, `150.29` (actual).

### gcis.war (proyecto `totemgcis`)
- No tiene datasource propio. Es un frontend Thymeleaf que llama por HTTP al backend `totemback` (`endpoint=http://localhost:8080/totemback`, mismo Tomcat/servidor).
- No requiere cambio para esta migracion de BD.

### carteleria-rest_localhost.war
**Fuera de scope — no tocar.** Grupo verde en la imagen, no es parte de esta migracion. No esta en `sesiones/git/war/` ni hay repo clonado; no se analizo.

---

## Cambios a aplicar en el servidor (DB 150.29 -> 153.29)

### 1. `totem.war` -> `WEB-INF/classes/application.properties` (linea ~15)
```
# actual
spring.datasource.url= jdbc:mysql://10.138.150.29:3306/totem?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&reWriteBatchedInserts=true

# nuevo
spring.datasource.url= jdbc:mysql://10.138.153.29:3306/totem?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&reWriteBatchedInserts=true
```
Texto plano, edicion directa. Confirmar antes que la DB `totem` ya exista replicada en `153.29`.

### 2. `wsct.war` -> `WEB-INF/classes/databaseWs.properties` (`jdbc.ws.url`)
Valor cifrado, calculado con la misma key/IV del `cifrado.properties` del propio war (verificado con round-trip encrypt->decrypt):
```
# actual (apunta a 150.29)
jdbc.ws.url=MDAwMDAwMDAwMDAwMDAwMG9P6VRjJbgY4HNO+fz5AOuRA5GqB77ZTcBjchs3swQRrxStswnV7JRksRUyfa0t2ydq/EFNDf8vyj3OqBVTYpP32XexECFD2RMLvD01i48Jaz0Q0RGQNKfOq3jHPeZd4Y6QOpWjBVLPCLYKRzTUp81o+Rpoz00ySYzKSyuMtvXg/zHwgCjvC7+zEloubKkDgDtpcG+ss1no4sok8DLvN5I=

# nuevo (apunta a 153.29, mismo user/pass, mismo query string)
jdbc.ws.url=MDAwMDAwMDAwMDAwMDAwMG9P6VRjJbgY4HNO+fz5AOtovPhvPaMspaAcbhliye7CfdQ/aSFLrevFt+rq32CLkvIElLr/tVUVhVc6X0gpchD7nsp11LDvquww2B15JEebN3J+yGksGay+I/IT/dzHf+7ypZo+HQbMjwFwmXSvgy9K0XsofaeYfYQggrGljsCNz5BP0CPZO/A86lhSgu+5iW2wEL/AZeHMMfKyUllJpIo=
```
`jdbc.ws.user` y `jdbc.ws.pass` no cambian (no dependen del host).

### 3. `carteleria-rest_localhost.war`
**No tocar.** Fuera de scope.

### 4. `gcis.war`
Sin cambios necesarios.

Tras editar, reempaquetar o solo reemplazar el `.properties` dentro de `WEB-INF/classes/` del war desplegado (Tomcat expande el war; si se edita el expandido directamente basta con reiniciar el contexto) y reiniciar el servicio.

**Gotcha confirmado en este server (2026-07-07):** soltar un `.war` nuevo encima de `webapps/<contexto>.war` **no** re-explota el directorio si `webapps/<contexto>/` ya existe expandido — Tomcat 9 en este server no lo detecta como cambio. El `WEB-INF/classes/*.properties` expandido se queda con el valor viejo aunque el `.war` en disco ya tenga el nuevo. Fix que funciono: editar directo el archivo expandido (`webapps/<contexto>/WEB-INF/classes/...properties`) y **reiniciar el servicio Tomcat completo** (`systemctl restart tomcat` o el script que use este server) — el datasource/pool (c3p0) se arma al arrancar el contexto, tocar el archivo sin reiniciar no alcanza.

---

## Codigo fuente (`sesiones/git/`) vs wars desplegados

| Repo | War | Resultado |
|---|---|---|
| `sodimacfinanzaswsct` | `wsct.war` | **Identico**. `application.properties`, `cifrado.properties` y los 4 `database*.properties` byte a byte iguales entre `src/main/resources` y el war extraido. |
| `totemback` | `totem.war` | **Resuelto 2026-07-14**. El war tenia un bloque Hikari (`spring.datasource.hikari.*`, 6 lineas) que no existia en ningun commit/rama del repo. Recuperado y commiteado en `C:\workspace-sodimac-legacy\totemback` (rama `Develop`), host de BD queda en `10.138.150.29` (el de la migracion a `153.29` fue solo prueba de laboratorio en esta sesion, NO aplicado en real — ver seccion Pendiente). |
| `totemgcis` | `gcis.war` | **NO coincide**. El war tiene un archivo `applicationdev.properties` que no existe en el repo local (el repo solo trae `application.properties` + `application.yml`). Tampoco aparece en ninguna rama remota. |
| `carteleria` (?) | `carteleria-rest_localhost.war` | Fuera de scope, no revisado. |

Conclusion: hay **drift** repo-vs-servidor en `totemback` y `totemgcis` — cambios hechos en caliente en el Tomcat que nunca se commitearon. Antes de redeployar desde el repo (pipeline/Jenkins/manual) hay que decidir si esos cambios (Hikari pool, `applicationdev.properties`) se pierden o se portan al repo primero.

---

## Pendiente
- [x] Bloque Hikari de `totemback` recuperado y commiteado en `C:\workspace-sodimac-legacy\totemback`, rama `Develop` (2026-07-14). Falta merge `Develop` -> `main` y re-deploy en `.25`.
- [ ] Decidir si el `applicationdev.properties` de `totemgcis` se commitea al repo antes de tocar nada (para no perderlo en el proximo deploy desde Git).
- [x] Conectividad de red `.25` -> `.29` puerto `3306` (MySQL) **validada 2026-07-07**: `timeout 5 bash -c '</dev/tcp/10.138.153.29/3306'` -> `OK`. Confirma solo TCP/firewall, no que las DBs/usuarios ya existan del otro lado.
- [ ] Confirmar con Yulike que las DBs `totem` y `totemconsultas` ya existen con datos/permisos para `wstotemUser`/`wsconsultaUser` en `10.138.153.29` antes de repuntar (mensaje enviado, ver seccion de mensaje pendiente de respuesta).
- [ ] **Correccion 2026-07-14: NINGUNO de los 3 wars se repunto de verdad.** Los 3 escenarios (`totem.war`, `wsct.war`, `gcis.war`) fueron solo prueba de laboratorio en esta sesion — ni el reinicio de Tomcat de `wsct` fue en el server real de Sodimac. Los 3 siguen en `150.29` en produccion real. El plan de repunte completo (properties nuevos, gotcha de Tomcat, valor cifrado nuevo) sigue vigente como guia, pero nada de eso esta aplicado todavia. Usuario va a volver a bajar los `.war` productivos reales para evitar confusion entre lo de laboratorio y lo real.
- [ ] Password de `g_dco018` reciporterse — la temporal exige cambio en primer login (`WajmcPYm3h3FjtDML!` ya generada).
