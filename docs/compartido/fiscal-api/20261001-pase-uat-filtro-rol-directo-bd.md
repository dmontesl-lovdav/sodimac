# Pase a UAT — filtro por rol: atributos directo de BD (sin util-api)

Fecha: 2026-10-01
Commit mirror: `62238cfa` (rama `dmontes`)

## Qué hace
`SecurityContextFilter` (filtro de resultados por rol, STM-1403) obtenía los atributos del
usuario (vendors/tipos/grupos) y el lookup RFC→proveedor **vía util-api**, inalcanzable en UAT.
Este cambio los resuelve **directo de la BD** (core_security + shared_catalogs), mismo patrón que
el permiso de eventos y los catálogos.

- Nuevo `UserAttributeResolver` + `AddendumRepository.findUserSecurityAttributes` (merge
  `user_attribute` + `role_attribute` activos) y `findSupplierNumbersByRfcs` (RFC→supplier_number).
- `SecurityContextFilter` usa el resolver en vez de `UtilApiSecurityClient`.
- **No cambia el gate de activación** (`security.enabled`). Solo cambia la fuente de los atributos.

## ⚠️ Condición de seguridad ANTES de pasar
El filtro solo corre si `security.enabled=true`.
- Si en UAT `security.enabled=false` → este cambio es **inerte** (no corre) → pase seguro, solo
  deja el código listo.
- Si `security.enabled=true` → al pasar, los usuarios reales **sin vendors (ATR001) poblados**
  caerían en **WRN7029** (vendors vacío = bloqueado) en las búsquedas → rompería UAT.

**Confirmar el flag antes.** Estado de datos hoy en UAT: solo usuarios de prueba tienen vendors;
los reales (iscortesz, etc.) tienen tipos pero NO vendors. **No encender el flag** hasta poblar
vendors reales + definir el comportamiento sin-vendors.

## Archivos (3) — solo fiscal-api
- `src/main/java/com/sodimac/fiscal/api/repository/AddendumRepository.java` (mod)
- `src/main/java/com/sodimac/fiscal/api/security/UserAttributeResolver.java` (**nuevo**)
- `src/main/java/com/sodimac/fiscal/api/security/SecurityContextFilter.java` (mod)

> Regla dura: copiar solo estos 3 archivos, NUNCA `robocopy /MIR` ni `git add -A`.
> Prerrequisito: el fix del 403 (`96feb615` + `8c20b91d`) ya debe estar en UAT.

## 1. Actualizar mirror local
```powershell
cd C:\local
git pull
```

## 2. Copiar los 3 archivos mirror → repo real
```powershell
$mir = "C:\local\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api"
$api = "C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api"

Copy-Item "$mir\repository\AddendumRepository.java"   "$api\repository\AddendumRepository.java" -Force
Copy-Item "$mir\security\UserAttributeResolver.java"  "$api\security\UserAttributeResolver.java" -Force
Copy-Item "$mir\security\SecurityContextFilter.java"  "$api\security\SecurityContextFilter.java" -Force
```

## 3. fiscal-api → develop → uat
```powershell
cd C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api
git checkout develop; git pull
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; & "C:\apache-maven-3.9.6-indra\bin\mvn.cmd" -P '!MEJINGEN_profile' test-compile -DskipTests
git add src/main/java/com/sodimac/fiscal/api/repository/AddendumRepository.java src/main/java/com/sodimac/fiscal/api/security/UserAttributeResolver.java src/main/java/com/sodimac/fiscal/api/security/SecurityContextFilter.java
git commit -m "feat: filtro por rol resuelve atributos de usuario directo de BD, sin util-api"
git push origin develop
git checkout uat; git pull; git merge develop --no-ff -m "merge: filtro por rol directo de BD a uat"; git push origin uat
```

## 4. Después del deploy
- El código queda desplegado pero **inerte** mientras `security.enabled=false`.
- Para activar el filtro por rol (paso futuro, NO ahora): (1) poblar vendors (ATR001) de los
  usuarios reales, (2) definir si se bloquea sin vendors, (3) encender `security.enabled`, (4)
  validar que iscortesz/usuarios reales vean sus facturas (no WRN7029).

## Notas
- ATR005 "Tipo Rebate" apareció en la migración (atributo nuevo); el filtro de fiscal NO lo usa
  (es de rebates/finanzas). Confirmar con seguridad/finanzas si algún filtro debe considerarlo.
- Ajustar `$api` si la ruta real de repos difiere.
