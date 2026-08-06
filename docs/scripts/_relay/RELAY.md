# RELAY — canal personal ↔ Sodimac (vía mirror)

Este archivo es el "buzón". Se escribe de un lado, se sube con `relay-push.ps1`, se baja del
otro con `relay-pull.ps1`. Deja tu bloque más reciente ARRIBA con fecha y quién lo escribe.

## Cómo funciona
- **Mandar (cualquier lado):** edita este archivo (o los que sea) → `powershell -File docs\scripts\relay-push.ps1 -Message "..."`
- **Recibir (cualquier lado):** `powershell -File docs\scripts\relay-pull.ps1`
- **Desplegar a UAT (SOLO en la PC de Sodimac):** `powershell -File docs\scripts\deploy-to-uat.ps1 -Message "..." -Projects "APP03022-mrch.backend.somx.fiscal-api"`

---

## 2026-08-06 — PERSONAL → SODIMAC: Pase identidad de usuario a UUID

**Contexto:** unificación de identidad de usuario a UUID en fiscal. Va coordinado: back (fiscal-api),
front (Fer, 2 endpoints), batches (Robert). El código ya está en el mirror (commit dmontes).

### Pasos en la PC de Sodimac

1. **Confirmar que Fer y Robert están listos** para soltar su lado en esta ventana
   (front: `PUT /invoices/{uuid}/status` y `PUT /invoices` mandan `idUsuarioActualizacion` como UUID;
    batches `fiscal-download` + `invoice-status-sync` mandan UUID de sistema `00000000-0000-0000-0000-000000000000`).

2. **Desplegar fiscal-api** (mirror → develop → uat):
   ```powershell
   powershell -File docs\scripts\deploy-to-uat.ps1 -Message "feat(fiscal): identidad de usuario UUID" -Projects "APP03022-mrch.backend.somx.fiscal-api"
   ```

3. **En cuanto el deploy esté reiniciando el pod, correr el ALTER en UAT** (por el túnel IAP):
   ```
   psql -h 127.0.0.1 -p <puerto-tunel> -U <user> -d b2b_portal -f "C:\workspace-fbc-github\APP03022-mrch.backend.somx.fiscal-api\migration\QA-2026-08-06-identidad-usuario-uuid.sql"
   ```
   Debe imprimir los `NOTICE ALTER ... -> uuid`, la verificación con todas en `uuid`, y `COMMIT`.
   (Idempotente y USING NULL; borra el histórico de created_by/updated_by — acordado, no importa.)

4. **Validar** (curl a UAT): PUT status con `idUsuarioActualizacion` = un UUID → 200; y en BD
   `invoice.updated_by` / `invoice_status_history.changed_by` quedan con ese UUID.

### Devolver resultado
Escribe aquí abajo el resultado (OK / errores / salida del ALTER) y sube con `relay-push.ps1`.

#### SODIMAC → PERSONAL (resultado):
_(pendiente)_
