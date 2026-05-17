# Índice de jiras Sodimac

Índice consultable. Cada entrada apunta a la carpeta del jira con su descripción XML + retro + postman + curl + SQL.

Documentación base:
- [`_INFRAESTRUCTURA.md`](_INFRAESTRUCTURA.md) — fuente de la verdad, mapa de entornos (PC Personal/Sodimac), flujo robocopy, reglas de ramas
- [`_GUIA-PROCESO.md`](_GUIA-PROCESO.md) — workflow estándar al recibir un jira nuevo

## Estado actual (2026-05-14)

### Epic STM-1403 — Filtro seguridad por atributo de usuario

Arquitectura: BFF decodifica JWT + consulta `util-api /api/security/user-attributes-by-key/{sub}` + inyecta headers anti-spoof `x-user-vendors/types/groups`. Backend filtra queries leyendo esos headers.

| Jira | Proyecto | Tema | Estado | Carpeta |
|------|----------|------|--------|---------|
| [STM-1403](STM-1403/) | fiscal-api + finanzas-api + 3 BFFs | epic seguridad base | ✓ develop | [STM-1403/](STM-1403/) |
| [STM-1525](STM-1525/) | util-api + bff-util | endpoint user-attributes-by-key | ✓ develop | [STM-1525/](STM-1525/) |
| [STM-323](STM-323/) | fiscal-api | filtro facturas (vendor) | ✓ develop PR #31 | [STM-323/](STM-323/) |
| [STM-1474](STM-1474/) | fiscal-api | filtro complementos pago | ✓ develop PR #31 | [STM-1474/](STM-1474/) |
| [STM-321](STM-321/) | finanzas-api | filtro Estado de Cuenta | ✓ develop `128fa2a` | [STM-321/](STM-321/) |
| [STM-1461](STM-1461/) | finanzas-api | filtro Carta Porte | ✓ develop `128fa2a` | [STM-1461/](STM-1461/) |
| [STM-1524](STM-1524/) | finanzas-api | filtro Three Way Match | ✓ develop `128fa2a` | [STM-1524/](STM-1524/) |

**bff.finanzas STM-1403 inject security** → push directo a develop `ad66b55` (2026-05-14, sin PR).

**Deploy UAT pendiente** en todos. Variables ambiente requeridas en kustomization — ver [Mensaje Bonelli](#mensaje-vars-bonelli).

### Epic STM-1403 — Sub-jiras (análisis 2026-05-14)

7 sub-jiras asignados a `g_dco018`. Análisis interno en `STM-XXXX_analisis.md` de cada carpeta.

| Jira | Módulo | Proyecto | Trabajo real | Estado análisis |
|------|--------|----------|--------------|------------------|
| [STM-322](STM-322/) | Complemento de Pago | fiscal-api | ✅ **Confirmado cubierto** — `PaymentRegistrationController` + `PaymentQueryServiceImpl` + `PaymentsRepositoryCustomImpl` ya leen `x-user-vendors`, manejan WRN7029, filtran con `IN (allowedVendors)`. Log literal: "Buscando complementos de pago con filtro de vendors". | ✓ [análisis](STM-322/STM-322_analisis.md) · [retro](STM-322/STM-322_respuesta-jira.md) |
| [STM-314](STM-314/) | Notas de Crédito | fiscal-api | ✅ **Confirmado cubierto** — `InvoiceController.search` documenta literal `tipoDocumento: I=Factura, E=Nota de Credito`. Mismo header + WRN7029 + `InvoiceSpecification` con `allowedVendors`. | ✓ [análisis](STM-314/STM-314_analisis.md) · [retro](STM-314/STM-314_respuesta-jira.md) |
| [STM-1421](STM-1421/) | Descuentos Comerciales (rebates) | finanzas-api | **Trabajo nuevo confirmado.** `rebate.controller/service/repo` NO usan `req.security`. Patrón STM-321 aplicable. ~3 SP. | ✓ [análisis](STM-1421/STM-1421_analisis.md) |
| [STM-1460](STM-1460/) | Pagos (finanzas-payment) | finanzas-api | **Trabajo nuevo confirmado.** `finanzasPayment.*` NO filtra. Aplicar patrón en repos Header + Payment. ~3 SP. | ✓ [análisis](STM-1460/STM-1460_analisis.md) |
| [STM-1527](STM-1527/) | Parámetros | util-api | ⚠ **XML placeholder sin reglas.** `cat_parameter` NO tiene vendor. Requiere clarificación de Ivan. | ✓ [análisis](STM-1527/STM-1527_analisis.md) |
| [STM-1528](STM-1528/) | Bitácora actividades | util-api (auditoria-api deprecado) | ⚠ **XML placeholder sin reglas.** `activity_logs` tiene `user_id` pero no `vendor_number`. Requiere clarificación. | ✓ [análisis](STM-1528/STM-1528_analisis.md) |
| [STM-1526](STM-1526/) | Catálogo de Catálogos | util-api (catalogos-api deprecado) | ⚠ **XML placeholder sin reglas.** `catalog_header` no relaciona con vendor. Requiere clarificación. | ✓ [análisis](STM-1526/STM-1526_analisis.md) |

### Otros jiras procesados

| Jira | Tema | Estado | Carpeta |
|------|------|--------|---------|
| [STM-1166](STM-1166/) | (ver carpeta) | — | [STM-1166/](STM-1166/) |
| [STM-1167](STM-1167/) | batch trazabilidad SQL Server | — | [STM-1167/](STM-1167/) |
| [STM-272](STM-272/) | bitácora registro complemento pago | — | [STM-272/](STM-272/) |
| [STM-704](STM-704/) | bitácora facturas | — | (varios commits, sin carpeta dedicada) |
| ... | ver listado completo en `ls docs/jiras/` | — | — |

Listado completo de carpetas existentes: `STM-1166, STM-1167, STM-1168, STM-1169, STM-1188, STM-1212, STM-1213, STM-1224, STM-1225, STM-1229, STM-1236, STM-1252, STM-1258, STM-1376, STM-1378, STM-1403, STM-1403-seguridad-atributos, STM-1461, STM-1463, STM-1474, STM-1487, STM-1524, STM-1525, STM-272, STM-304, STM-305, STM-321, STM-323, STM-333, STM-335, ...`

## Cómo consultar este índice

Cuando preguntes cosas tipo:
- "¿qué hicimos en STM-1525?" → busco la fila + leo la retro
- "¿qué jiras tocaron finanzas-api?" → grep la columna Proyecto
- "¿qué falta deployar?" → busco "Deploy UAT pendiente"
- "¿qué endpoints expone util-api?" → leo STM-1525_respuesta-jira.md

## Convenciones

- **Estados**: `📝 pendiente / 🔧 en progreso / ✓ develop / 🚀 UAT / ✅ prod`
- **Mención commit**: cualquier `STM-XXXX` en mensaje commit ata el cambio al jira
- **Carpeta del jira es self-contained**: XML + retro + postman + curl + SQL para que cualquiera pueda reproducir sin contexto adicional

---

## Mensaje pendiente Ivan — 7 sub-jiras epic STM-1403 (2026-05-14)

Hola Ivan,

Termine el analisis inicial de los 7 sub-jiras que asignaste del epic STM-1403. Resumen rapido:

**Posibles duplicados / ya cubiertos** (necesito que confirmes):

- **STM-322 (Complemento de Pago)** — el commit `a311b59` etiquetado STM-1474 toca `PaymentsRepositoryCustomImpl` (tabla `payments`, que ES complemento de pago CFDI con `fiscal_uuid` del TimbreFiscalDigital). Parece que ya está implementado. ¿STM-322 era esto o algo distinto?
- **STM-314 (Notas de Credito)** — las NC viven en la tabla `invoice` con `document_type='E'`. `InvoiceSpecification` (STM-323) ya filtra por `documentType` AND `allowedVendors`. ¿Hay endpoint o caso distinto que necesite trabajo adicional?

**Trabajo confirmado** (puedo arrancar ya):

- **STM-1421 (Descuentos Comerciales)** — finanzas-api, `rebate.*` no usan `req.security`. ~3 SP. Patron identico a STM-321.
- **STM-1460 (Pagos / finanzas-payment)** — finanzas-api, `finanzasPayment.*` no filtra. ~3 SP.

**Bloqueados — falta detalle en el ticket**:

- **STM-1526 (Catalogo de Catalogos)**, **STM-1527 (Parametros)**, **STM-1528 (Bitacora actividades)** — los XML estan en placeholder (sin reglas de negocio, sin acceptance criteria). Adicional, sus modelos de datos (`catalog_header`, `cat_parameter`, `activity_logs`) NO tienen columna `vendor_number` ni similar. Preguntas:
   - ¿Cual atributo aplica al filtrado (Proveedor / TipoProveedor / GrupoProveedor)?
   - ¿Como se relacionan estos modulos con un proveedor? (catalogo de tipo, modulo accesible, user_id, etc.)
   - ¿Las reglas son las mismas del epic (5 reglas + WRN7029)? Si si, pega el detalle al ticket por favor.

Detalle tecnico por jira en `docs/jiras/STM-XXXX/STM-XXXX_analisis.md`.

Avisame cuando confirmes y arranco implementacion de los que esten claros.

---

## Mensaje vars Bonelli

Pendiente agregar a kustomization (todos los ambientes UAT/PROD):

**`APP03022-mrch.bff.somx.ppsomx.finanzas`** y **`APP03022-mrch.bff.somx.ppsomx.fiscal`**:
- `UTIL_API_URL` (local: `http://localhost:3712`)

**`APP03022-mrch.backend.somx.fiscal-api`**:
- `SECURITY_ENABLED=true`
- `UTILS_API_ENABLED=true`
- `UTILS_API_URL` (local: `http://localhost:3712`)
- `UTILS_API_TIMEOUT=5000`

---

## URLs pase a UAT (PR develop→uat)

| Repo | Compare URL |
|------|-------------|
| bff.finanzas | https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.finanzas/compare/uat...develop |
| bff.fiscal | https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.fiscal/compare/uat...develop |
| fiscal-api | https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.fiscal-api/compare/uat...develop |
| finanzas-api | https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.finanzas-api/compare/uat...develop |
| util-api | https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.util-api/compare/uat...develop |
| bff-util | https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util/compare/uat...develop |
