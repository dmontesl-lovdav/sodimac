# STM-322 — Análisis técnico (interno)

> Documento interno para implementación. No es respuesta de Jira.

## Resumen del jira

- **Módulo**: Complemento de Pago (CFDI tipo P)
- **Pide**: filtrar la información de complementos de pago por atributos del usuario autenticado (Proveedor, TipoProveedor, GrupoProveedor)
- **Prioridad**: Media
- **Epic**: STM-1403
- **Tipo**: Story

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.fiscal-api` (Java Spring Boot)

**Entity relevante**: `PaymentsEntity` ([src/main/java/com/sodimac/fiscal/api/model/entity/PaymentsEntity.java](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/entity/PaymentsEntity.java))
- Tabla: `payments`
- Campo clave: `fiscal_uuid` (TimbreFiscalDigital del SAT) → confirma que mapea complemento de pago CFDI

**Repos/services con lógica de filtro**:
- `PaymentsRepositoryCustom` + `PaymentsRepositoryCustomImpl.java` → método `searchPayments(searchRequest, allowedVendors)` **YA implementa el filtro**
  - `addVendorPredicate` aplica `vendor_number IN (...)`
  - `countPaymentsWithVendors` calcula total con el mismo filtro
- `PaymentQueryService` / `PaymentQueryServiceImpl` → consume el repo

**Endpoints**:
- `PaymentRegistrationController` (registro)
- `PaymentQueryService` (consulta) — invocado desde controller que recibe `req.security.vendors`

**Headers de seguridad**:
- `SecurityContextFilter.java` + `JwtTokenInterceptor.java` decodifican JWT → consulta `UtilApiSecurityClient` → sobrescribe `x-user-vendors/types/groups` anti-spoof.
- Servicios leen los headers para construir `allowedVendors`.

## Hallazgos

### ¿Funcionalidad ya existe?
**Sí.** El commit `a311b59 fix: STM-1474 convertir allowedVendors a BigDecimal para comparacion con supplier_number numeric` (etiquetado STM-1474) ya implementó el filtro en `PaymentsRepositoryCustomImpl`.

### ¿Por qué el commit STM-1474 toca complementos de pago si STM-1474 dice "recepciones"?
- Hipótesis A: el commit fue mal etiquetado por confusión de Ivan o del developer original (titular era recepciones pero el código fue a payments).
- Hipótesis B: STM-1474 originalmente abarcaba ambos temas y el título quedó atrasado.
- Hipótesis C: la entidad `PaymentsEntity` representa tanto complementos de pago como recepciones bajo el mismo modelo (poco probable dado el `fiscal_uuid`).

### Verificación pendiente
Recorrer `PaymentRegistrationController` / `PaymentQueryService` para confirmar que:
1. El filtro `allowedVendors` se pasa desde el header en TODOS los endpoints de complementos.
2. Endpoints que listan complementos respetan el WRN7029 si `vendors == []`.
3. No hay otro repo o entity que maneje "complemento" sin el filtro.

## Propuesta

### Si verificación es OK → cerrar como duplicado/ya implementado
1. Confirmar con Ivan que `PaymentsEntity/payments` es la tabla del complemento de pago.
2. Documentar en respuesta-jira que el código del commit `a311b59` (originalmente STM-1474) cubre STM-322.
3. QA puede correr pruebas usando la colección Postman de STM-1474 (si existe) o generar una nueva.
4. Cerrar STM-322 como `Done` con justificación.

### Si verificación detecta un endpoint sin filtro → trabajo nuevo mínimo
- Aplicar el mismo patrón: leer `req.security.vendors` → pasar a `PaymentsRepositoryCustomImpl.searchPayments`.
- Tests: regresión + caso vendor inválido (WRN7029).

## Archivos a tocar (si hay trabajo)

- `PaymentQueryServiceImpl.java` — verificar que extrae vendors del request y los pasa al repo.
- `PaymentRegistrationController.java` — verificar inyección de `req.security`.
- Posible documentación en Swagger/OpenAPI.

## Dudas para Ivan

- **¿Qué endpoint REST corresponde a complemento de pago?** Confirmar para QA. Candidatos: `/payments`, `/payments/search`, `/api/payments/...`.
- **¿STM-1474 cubrió complementos sin querer?** ¿O en su scope original estaba "recepciones y pagos"?
- **¿Existe otro entity de "complemento" en algún repo?** No detecté otro, pero confirmar.

## Estimación
- **Si ya cubierto**: 0 SP (solo QA + cierre).
- **Si falta un endpoint**: 1 SP máx (patrón ya conocido).

## Referencias
- Jira hermano STM-1474 (recepciones, mismo epic): [STM-1474/](../STM-1474/)
- Jira hermano STM-323 (facturas, mismo patrón): [STM-323/](../STM-323/)
- Epic STM-1403: [STM-1403/](../STM-1403/)
