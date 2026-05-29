# Bug: DB_SSL invertido en finanzas-api typeorm-datasource.ts

## Síntoma

Al levantar finanzas-api local con `DB_SSL=false` en `.env`:

```
[DB CONNECT] {
  host: 'localhost',
  port: 5434,
  user: 'wwwb2bportal',
  db: 'b2b_portal',
  schema: 'tenant_finance',
  ssl: true                       # ← incorrecto, debería ser false
}
CAN'T INITIALIZE SERVER Error: The server does not support SSL connections
    at Socket.<anonymous> (node_modules/pg/lib/connection.js:76:37)
```

Docker postgres local no soporta SSL → la app falla al iniciar.

## Causa raíz

Archivo: `src/config/typeorm-datasource.ts` línea 101 (antes del fix)

```typescript
const dbSSL = process.env.DB_SSL === 'false';  // ← INVERTIDO
```

Cuando `DB_SSL=false` (string), `dbSSL` resulta `true` → SSL se activa.
Cuando `DB_SSL=true` o no existe, `dbSSL` resulta `false` → SSL se desactiva.

Comportamiento opuesto al esperado.

## Fix

```typescript
const dbSSL = process.env.DB_SSL !== 'false';   // ← correcto
```

Ahora:
- `DB_SSL=false` → `dbSSL=false` → SSL deshabilitado (local Docker OK)
- `DB_SSL=true` → `dbSSL=true` → SSL habilitado (UAT/PROD con Cloud SQL)
- vacío/ausente → default `true` (seguro)

## Commit

- Branch: `dmontes` en mirror
- Commit: `340389c fix: finanzas-api DB_SSL logic invertido en typeorm-datasource`
- Fecha: 2026-05-08

## Cómo se detectó

Al levantar finanzas-api local para probar STM-1403:

```
npm run dev
[DATASOURCE] DataSource created with 35 entities
[FiscalApiClient] URL configured: http://localhost:8082
...
[DB CONNECT] { ..., ssl: true }
CAN'T INITIALIZE SERVER Error: The server does not support SSL connections
```

El log mostraba `ssl: true` aún con `DB_SSL=false` en .env. Inspección del código reveló la comparación invertida.

## Impacto

- **Local**: bloquea levantar la app contra Docker postgres local.
- **UAT/PROD**: depende del valor en configmap/secret. Si históricamente alguien usó `DB_SSL=true` esperando habilitar SSL, en realidad lo deshabilitaba (riesgo de seguridad — conexión sin TLS a Cloud SQL).

Verificar configmaps de UAT/PROD después del deploy del fix:
```bash
kubectl get configmap finanzas-api-configmap -o yaml | grep DB_SSL
```

Si decía `true` → conexión iba sin SSL (problema). Tras el fix, mismo valor habilita SSL correctamente.

## Lecciones

- Variables `true`/`false` en env vars: comparación de strings es tricky. Mejor:
  - `=== 'true'` para opt-in (default false)
  - `!== 'false'` para opt-out (default true)
- En PostgreSQL `pg` driver: `ssl: false` (boolean) deshabilita, `ssl: { rejectUnauthorized: false }` (object) habilita con cert relaxado, `ssl: true` requiere cert válido.
