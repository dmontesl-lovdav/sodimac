# Pase a Sodimac — fix 403 permisos (cancelar / reproceso / complemento)

Fecha: 2026-09-28
Commit mirror: `96feb615` (rama `dmontes`)

## Qué arregla
El backend exigía eventos inexistentes (EVT0052/EVT0051 cancelar/reproceso, EVT0124
complemento) y validaba contra el modelo por rol (`event_permission/role_permission`),
que no tiene seed → **403 para todos**, aunque el botón del front sí aparecía.

- `fiscal-api`: valida contra `user-details` (modelo por perfil, ya poblado, misma fuente
  que el front) vía nuevo `UtilApiSecurityClient.hasEvent`.
- Alinea códigos al set común del front: cancelar **EVT011**, reproceso **EVT012**,
  complemento **EVT016** (Publicar) — ya cableados en APL009/APL010/APL011 (seed
  `20_STM-rbac_wire_app_events.sql`, perfil admin PER009).
- `fiscal.spa`: complemento PUBLISH usa `COMMON.PUBLISH` (EVT016) en vez de EVT0124.

**Sin cambios en BD.** Corrige los issues QA 403 al cancelar NC/factura y al registrar complemento.

## Archivos (5)
- fiscal-api:
  - `src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java`
  - `src/main/java/com/sodimac/fiscal/api/controller/PaymentRegistrationController.java`
  - `src/main/java/com/sodimac/fiscal/api/security/PermissionInterceptor.java`
  - `src/main/java/com/sodimac/fiscal/api/security/UtilApiSecurityClient.java`
- fiscal.spa:
  - `src/shared/security/eventCodes.ts`

> Regla dura del pase: **copiar solo estos 5 archivos, NUNCA `robocopy /MIR` ni `git add -A`**
> (vuela archivos de DevOps/CI del working tree del repo real).

---

## 1. Actualizar el mirror local (C:\local)
```powershell
cd C:\local
git pull
```

## 2. Copiar los 5 archivos mirror → repos reales
```powershell
$mir = "C:\local"
$api = "C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api"
$spa = "C:\workspace-fbc-github\APP03022-mrch.frontend.somx.fiscal.spa"

# fiscal-api (4)
Copy-Item "$mir\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api\controller\InvoiceController.java"           "$api\src\main\java\com\sodimac\fiscal\api\controller\InvoiceController.java" -Force
Copy-Item "$mir\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api\controller\PaymentRegistrationController.java" "$api\src\main\java\com\sodimac\fiscal\api\controller\PaymentRegistrationController.java" -Force
Copy-Item "$mir\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api\security\PermissionInterceptor.java"          "$api\src\main\java\com\sodimac\fiscal\api\security\PermissionInterceptor.java" -Force
Copy-Item "$mir\APP03022-mrch.backend.somx.fiscal-api\src\main\java\com\sodimac\fiscal\api\security\UtilApiSecurityClient.java"          "$api\src\main\java\com\sodimac\fiscal\api\security\UtilApiSecurityClient.java" -Force

# fiscal.spa (1)
Copy-Item "$mir\APP03022-mrch.frontend.somx.fiscal.spa\src\shared\security\eventCodes.ts" "$spa\src\shared\security\eventCodes.ts" -Force
```

## 3. fiscal-api → develop → uat
```powershell
cd C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api
git checkout develop; git pull
# compila lo que el CI cacha ANTES de commitear
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; & "C:\apache-maven-3.9.6-indra\bin\mvn.cmd" -P '!MEJINGEN_profile' test-compile -DskipTests
git add src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java src/main/java/com/sodimac/fiscal/api/controller/PaymentRegistrationController.java src/main/java/com/sodimac/fiscal/api/security/PermissionInterceptor.java src/main/java/com/sodimac/fiscal/api/security/UtilApiSecurityClient.java
git commit -m "fix: alinea gate de permisos cancelar/reproceso/complemento al modelo real (user-details)"
git push origin develop
git checkout uat; git pull; git merge develop --no-ff -m "merge: fix gate permisos a uat"; git push origin uat
```

## 4. fiscal.spa → develop → uat
```powershell
cd C:\workspace-fbc-github\APP03022-mrch.frontend.somx.fiscal.spa
git checkout develop; git pull
git add src/shared/security/eventCodes.ts
git commit -m "fix: complemento usa evento EVT016 (Publicar) del set comun"
git push origin develop
git checkout uat; git pull; git merge develop --no-ff -m "merge: fix complemento evento a uat"; git push origin uat
```

## 5. Validar en UAT (tras deploy)
Con user de perfil admin (PER009):
- Cancelar NC/factura → ya no 403 → corre la cascada (Recibido Parcial + cancelar NCs relacionadas).
- Registrar complemento de pago → ya no 403.

## Notas
- Confirmar que `C:\workspace-fbc-github\...` sea la ruta real de repos (ajustar `$api`/`$spa` si difiere).
- Si el push del sync da `rejected`: `git pull origin <rama> --no-rebase` y reintentar (sin force).
- Nada de BD en este pase.
- De fondo (fuera de este pase): el modelo por rol (`role_user/role_permission/event_permission`)
  sigue sin seed; quedó sin dueño tras la salida de ggalvan (STM-1403). Este fix esquiva ese
  modelo usando el de perfil, que sí está poblado.
