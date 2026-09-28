# Pase a Sodimac — permiso de eventos directo de BD (fix 403, parte 2)

Fecha: 2026-09-28
Commit mirror: `8c20b91d` (rama `dmontes`)
Depende del pase anterior: `20260928-pase-fix-403-permisos.md` (alineación EVT011/012/016 + front).

## Por qué esta segunda parte
El primer pase alineó los códigos de evento, pero el 403 siguió porque la validación
seguía consultando **util-api**, que es **inalcanzable desde los backends en UAT** (mismo
motivo por el que los catálogos ya se leen directo de BD). Este pase quita esa dependencia:
el permiso se resuelve **directo en la BD** de fiscal-api.

- Nuevo `PermissionResolver` + `AddendumRepository.existsEventForUser`: resuelve el evento por
  el modelo de **perfil** (`user_data → profile_user → profile_module_process → module_process
  → CatEvento`, todo `status=1`) contra la conexión propia de fiscal-api.
- `InvoiceController` y `PermissionInterceptor` usan el resolver en vez de
  `UtilApiSecurityClient.hasEvent`.
- Fail-closed ante error. **Solo backend (fiscal-api). No toca front.**

## Archivos (5) — solo fiscal-api
- `src/main/java/com/sodimac/fiscal/api/repository/AddendumRepository.java` (mod)
- `src/main/java/com/sodimac/fiscal/api/security/PermissionResolver.java` (**nuevo**)
- `src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java` (mod)
- `src/main/java/com/sodimac/fiscal/api/security/PermissionInterceptor.java` (mod)
- `src/main/java/com/sodimac/fiscal/api/security/UtilApiSecurityClient.java` (mod)

> Regla dura: **copiar solo estos 5 archivos, NUNCA `robocopy /MIR` ni `git add -A`**.

---

## 1. Actualizar el mirror local (C:\local)
```powershell
cd C:\local
git pull
```

## 2. Copiar los 5 archivos mirror → repo real
```powershell
$mir = "C:\local\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api"
$api = "C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api"

Copy-Item "$mir\repository\AddendumRepository.java"      "$api\repository\AddendumRepository.java" -Force
Copy-Item "$mir\security\PermissionResolver.java"        "$api\security\PermissionResolver.java" -Force
Copy-Item "$mir\controller\InvoiceController.java"       "$api\controller\InvoiceController.java" -Force
Copy-Item "$mir\security\PermissionInterceptor.java"     "$api\security\PermissionInterceptor.java" -Force
Copy-Item "$mir\security\UtilApiSecurityClient.java"     "$api\security\UtilApiSecurityClient.java" -Force
```

## 3. fiscal-api → develop → uat
```powershell
cd C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api
git checkout develop; git pull
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; & "C:\apache-maven-3.9.6-indra\bin\mvn.cmd" -P '!MEJINGEN_profile' test-compile -DskipTests
git add src/main/java/com/sodimac/fiscal/api/repository/AddendumRepository.java src/main/java/com/sodimac/fiscal/api/security/PermissionResolver.java src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java src/main/java/com/sodimac/fiscal/api/security/PermissionInterceptor.java src/main/java/com/sodimac/fiscal/api/security/UtilApiSecurityClient.java
git commit -m "fix: resuelve permiso de eventos directo de BD, sin util-api"
git push origin develop
git checkout uat; git pull; git merge develop --no-ff -m "merge: permiso directo de BD a uat"; git push origin uat
```

---

## 4. Verificar el DATO en la BD UAT (clave)
El código ya no depende de util-api, pero exige que el cableado del perfil esté **activo
(status=1)**. Corre en la **BD UAT**:

```sql
SELECT ev.key evento, app.key app, per.key perfil, mp.status mp_st, pmp.status pmp_st
FROM shared_catalogs.catalog_detail ev
JOIN shared_catalogs.catalog_header hev ON hev.id=ev.header_id AND hev.code='CatEvento'
JOIN core_security.module_process mp ON mp.catalog_detail_process_id=ev.id
JOIN shared_catalogs.catalog_detail app ON app.id=mp.catalog_detail_module_id
JOIN core_security.profile_module_process pmp ON pmp.module_process_id=mp.module_process_id
JOIN shared_catalogs.catalog_detail per ON per.id=pmp.catalog_detail_profile_id
WHERE ev.key IN ('EVT011','EVT012','EVT016') AND app.key IN ('APL009','APL010','APL011')
ORDER BY app.key, ev.key;
```

- Si `mp_st`/`pmp_st` = **1** para el perfil del usuario → listo, el gate pasa.
- Si = **0** (inactivo) → correr el seed que los activa: **`20_STM-rbac_wire_app_events.sql`**
  (util-api/src/database) en la BD UAT. Activa module_process + profile_module +
  profile_module_process para el perfil admin **PER009** de forma idempotente.

Y confirmar que el usuario tenga el perfil asignado y activo:
```sql
SELECT ud.preferred_username, ud.email, per.key perfil, pu.status
FROM core_security.user_data ud
JOIN core_security.profile_user pu ON pu.user_data_id=ud.user_data_id
JOIN shared_catalogs.catalog_detail per ON per.id=pu.catalog_detail_profile_id
WHERE ud.preferred_username = '<usuario_de_ivan>' OR ud.email = '<correo_de_ivan>';
```
Si el usuario no tiene un perfil con esos eventos activos → asignarle PER009 (o el perfil
que corresponda) desde la pantalla de seguridad del portal.

## 5. Validar en UAT (tras deploy + dato activo)
Con el usuario ya con perfil activo: cancelar NC/factura y registrar complemento → ya no 403.

## Notas
- Ajustar `$api` si la ruta real de repos difiere de `C:\workspace-fbc-github\...`.
- Nada de BD **salvo** el paso 4 si el cableado está inactivo.
- De fondo: el modelo por rol (`role_user/role_permission`) sigue sin seed y sin dueño (salida
  de ggalvan); este fix usa el modelo por perfil, que es el poblado y el que alimenta los botones.
