# Exposición de Endpoints — BFF util-api (estructura split openapi)

> **PRIORIDAD: ALTA**
> Complemento a [EXPOSICION_ENDPOINTS_BFF.md](EXPOSICION_ENDPOINTS_BFF.md).
> Aquel asume `api.yml` único (caso fiscal-api). bff-util usa estructura split — este doc cubre ese caso.

## Diferencia clave vs EXPOSICION_ENDPOINTS_BFF.md

| Aspecto | bff-fiscal / bff-finanzas | **bff-util** |
|---|---|---|
| Archivo openapi | `api.yml` único | `cloud-endpoint/src/root.yaml` + splits |
| Definiciones | Todo inline | `$ref` a `paths/<service>.yaml` |
| Razón | Archivo chico | Manejable, definiciones por dominio |

## Estructura de archivos

```
APP03022-mrch-bff-somx-ppsomx-util/
  cloud-endpoint/
    openapi.yaml                     # archivo compilado (auto-generado en build)
    src/
      root.yaml                      # registra paths con $ref
      definitions/
        common.yaml                  # ExceptionWrapper, etc.
        security.yaml                # schemas de domain security
        supplier.yaml
        catalog.yaml
        ...
      paths/
        security.yaml                # operations (get/post/...) del domain
        supplier.yaml
        ...
```

## Cómo agregar un endpoint nuevo

Ejemplo: agregar `GET /security/user-attributes-by-key/{userKey}`.

### Paso 1: Registrar el path en `root.yaml`

```yaml
paths:
  # ... otros paths ...
  /security/user-details/{userKey}:
    $ref: "./paths/security.yaml#/security-user-details-by-key"
  /security/user-attributes-by-key/{userKey}:
    $ref: "./paths/security.yaml#/security-user-attributes-by-key"   # ← agregar
  /security/user-catalog:
    $ref: "./paths/security.yaml#/security-user-catalog"
```

### Paso 2: Definir la operation en `paths/security.yaml`

```yaml
security-user-attributes-by-key:
  get:
    tags:
      - security
    summary: Get user attributes by sub key
    operationId: getUserAttributesByKey
    produces:
      - application/json
    parameters:
      - name: userKey
        in: path
        required: true
        type: string
    responses:
      "200":
        description: User attributes
        schema:
          type: object
          properties:
            success:
              type: boolean
            data:
              type: object
              properties:
                userDataId:
                  type: integer
                sub:
                  type: string
                attributes:
                  type: array
                  items:
                    type: object
                    properties:
                      typeKey:
                        type: string
                      valueKey:
                        type: string
      "404":
        description: User not found
        schema:
          $ref: "../definitions/common.yaml#/ExceptionWrapper"
    security:
      - bearerAuth: []
```

### Paso 3: PR feature → develop → uat

Igual que cualquier otro cambio. El pipeline compila `root.yaml` + `paths/*.yaml` → `openapi.yaml` final que GCP Cloud Endpoints registra.

## Error típico

Si el path NO está en `root.yaml`, el gateway responde:

```json
{ "code": 404, "message": "The current request is not defined by this API." }
```

El código backend puede existir y el pod responder OK por dentro, pero el gateway lo bloquea antes.

## Verificación post-deploy

```powershell
# Endpoint ya registrado en gateway UAT
Invoke-RestMethod "https://uat.fbusinesscenter.com/ppsomx/backend-util/security/user-attributes-by-key/sb000001"
```

Si responde con `success: true` → openapi compilado y desplegado OK.
Si responde con 404 "not defined by this API" → falta deploy del cloud-endpoint (ver [pipeline-manual-approval-uat.md](../tecnico/pipeline-manual-approval-uat.md)).

## Patrón de URL gateway

`/ppsomx/backend-<service>/<endpoint>`

NOT `/ppsomx/<service>/...` — ese cae al frontend single-spa.

Ejemplos:
- `/ppsomx/backend-util/security/user-attributes-by-key/sb000001` ✓
- `/ppsomx/backend-fiscal/invoices/search` ✓
- `/ppsomx/backend-finanzas/fiscal-payments` ✓

## Referencias

- [STM-1525 PR #26 bff-util](https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util/pull/26) — caso real de agregar endpoint a openapi
- [EXPOSICION_ENDPOINTS_BFF.md](EXPOSICION_ENDPOINTS_BFF.md) — patrón api.yml único (otros BFFs)
