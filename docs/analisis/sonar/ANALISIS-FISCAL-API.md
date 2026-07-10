# Análisis SonarQube — fiscal-api

> Fuente: SonarQube corp Falabella (`sonarqube-corp.falabella.tech`), projectKey `gh-1122007633`, branch `uat`.
> Export: 2026-07-10 (`docs/scripts/sonar-export.ps1`). Datos crudos: `*_issues.csv`, `*_issues.json`, `*_rules.json` en esta carpeta.
> Total: **666 issues** — 11 Security, 6 Reliability(bugs), 649 Maintainability.

---

## Resumen ejecutivo

| Categoría | # | Severidad top | Nota |
|---|---|---|---|
| SECURITY | 11 | HIGH | Todo XXE (`S2755`), 1 solo patrón de fix |
| RELIABILITY (bug) | 6 | HIGH/MED/LOW | 6 archivos, fixes puntuales |
| MAINTAINABILITY | 649 | HIGH 84 | **~261 son falsos-positivos S1068 en DTOs** |

**El número "649 mantenibilidad" engaña.** El grueso (265 = `S1068` campos privados sin uso) cae **261 en DTOs/JAXB** cuyos campos SÍ se usan por serialización XML/JSON (reflexión que Sonar no ve). Borrarlos rompe (de)serialización. Se atacan por **exclusión/supresión**, no borrando.

Trabajo *real* de fondo mucho menor que 666. Orden recomendado abajo.

---

## Tier 1 — Seguridad + bugs (17 issues) · PRIORIDAD

### 1a. XXE — `java:S2755` (11, SECURITY=HIGH)

Todos los parsers son `javax.xml.DocumentBuilderFactory` + `javax.xml.transform.TransformerFactory`. Fix uniforme.

| Archivo | Línea | Factory |
|---|---|---|
| `service/impl/FiscalXmlTransformerServiceImpl.java` | 265 | DocumentBuilderFactory |
| `service/impl/PacServiceDetecnoImpl.java` | 259 | DocumentBuilderFactory |
| `service/impl/PacServiceDetecnoImpl.java` | 269, 290 | TransformerFactory |
| `service/impl/ToolsServiceImpl.java` | 85 | DocumentBuilderFactory |
| `service/impl/ToolsServiceImpl.java` | 95 | TransformerFactory |
| `service/impl/ValidaXmlServiceImpl.java` | 197 | DocumentBuilderFactory |
| `service/impl/ValidaXmlServiceImpl.java` | 206 | TransformerFactory |
| `service/impl/XmlDocumentTypeDetectorServiceImpl.java` | 127 | DocumentBuilderFactory |
| `util/UtilsFile.java` | 49 | DocumentBuilderFactory |
| `util/UtilsFile.java` | 65 | TransformerFactory |

**Fix DocumentBuilderFactory:**
```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
```
**Fix TransformerFactory:**
```java
TransformerFactory tf = TransformerFactory.newInstance();
tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
```
Import: `javax.xml.XMLConstants`.

**Recomendado:** helper único `XmlSecurity.harden(factory)` / `hardenTransformer(tf)` en `util/` → evita duplicar (y de paso no dispara `S1192`). 8 archivos lo llaman. Riesgo: bajo — estos parsers procesan CFDI de terceros (PAC/proveedor), justo el vector XXE. `disallow-doctype-decl=true` es seguro salvo que algún XML legítimo traiga DOCTYPE (los CFDI SAT no).

### 1b. Bugs (6)

| Regla | Sev | Archivo:línea | Problema | Fix |
|---|---|---|---|---|
| `S2095` | HIGH | `util/UtilsFile.java:31` | `FileInputStream fis` sin cerrar en `writeToZipFile` | envolver en try-with-resources |
| `S3077` | LOW | `pdf/impl/PaymentPdfServiceImpl.java:41` | `volatile Path tempXslDir` (no-primitivo, volatile no garantiza thread-safety) | usar `AtomicReference<Path>` o sincronizar la init |
| `S3077` | LOW | `pdf/impl/PdfRenderServiceImpl.java:51` | idem | idem |
| `S2142` | MED | `security/UtilApiSecurityClient.java:90` | `catch (Exception e)` traga `InterruptedException` sin re-interrumpir | catch específico `InterruptedException` → `Thread.currentThread().interrupt()` |
| `S5850` | MED | `service/impl/GcsStorageServiceImpl.java:42` | regex `"^/+\|/+$"` con anchors sin agrupar alternativas | agrupar: `"(?:^/+)\|(?:/+$)"` (comportamiento ya correcto, es claridad/robustez) |
| `S899` | LOW | `util/UtilsFile.java:74` | `archivo.delete()` ignora retorno boolean | evaluar retorno; mejor `Files.delete(path)` (cubre además `S4042` misma línea) |

**Nota S5850:** `replaceAll("^/+\|/+$","")` — con `\|` (alternación) y anchors `^`/`$`, Java aplica el anchor solo a cada rama, que aquí es lo deseado, pero Sonar pide agrupación explícita para que la intención sea inequívoca. Cambio cosmético, sin cambio de comportamiento.

**Esfuerzo Tier 1:** ~2-3h. Quita los 11 HIGH de seguridad + 6 bugs. Máximo golpe de riesgo.

---

## Tier 2 — Mecánicos seguros (auto-fix IDE, ~120 reales)

Bajo riesgo, la mayoría auto-corregibles con IDE (IntelliJ "Code Cleanup" / SonarLint quick-fix).

| Regla | # | Qué | Riesgo | Cómo |
|---|---|---|---|---|
| `S1124` | 42 | orden de modificadores (`static final` etc.) | nulo | auto |
| `S2293` | 39 | operador diamante `new ArrayList<>()` | nulo | auto |
| `S125` | 19 | código comentado | nulo | borrar |
| `S1135` | 12 | tags `TODO` | nulo | revisar/limpiar o convertir a ticket |
| `S1130` | 10 | `throws` superfluo | bajo | quitar excepción no lanzada |
| `S1488` | 8 | var declarada y retornada de una | nulo | inline |
| `S100` | 7 | nombres de método (naming) | bajo | renombrar (ojo API pública) |
| `S1133` | 9 | código deprecated a remover | medio | verificar sin callers |
| `S116`/`S117` | 6/13 | naming campos/locals | bajo | renombrar |
| `S1481`/`S1854` | 3/3 | vars/asignaciones sin uso | nulo | borrar |
| `S4087`/`S7158`/`S1155`/`S1197`/`S1128` | 1-3 c/u | limpiezas menores | nulo | auto |

**Esfuerzo:** ~1 día. Baja el conteo notable sin tocar lógica.

---

## Tier 3 — Requieren criterio (~90 reales)

| Regla | # | Qué | Nota |
|---|---|---|---|
| `S1192` | 68 (HIGH) | strings duplicados → constante | extraer a `static final`. Muchos vienen del helper XXE (Tier 1 los reduce). Revisar duplicados legítimos vs mensajes |
| `S3776` | 14 (HIGH) | complejidad cognitiva alta | refactor de métodos (`InvoiceServiceImpl`, procesadores XML). Requiere pruebas antes/después |
| `S1874` | 62 (LOW) | uso de API `@Deprecated` | verificar reemplazo por cada API. **No mecánico** — puede requerir cambio funcional |
| `S107` | 10 | métodos con demasiados parámetros | agrupar en objeto param. Refactor con impacto en callers |
| `S112` | 10 | lanza `Exception` genérica | crear excepciones específicas |
| `S1141` | 6 | try-catch anidados | aplanar |
| `S3740` | 6 | raw types | parametrizar genéricos |
| `S106` | 5 | `System.out` en vez de logger | cambiar a `log.*` |
| `S1144`/`S1172` | 8/4 | métodos/params privados sin uso | borrar (verificar reflexión) |
| `S1118` | 3 | utility class con constructor público | constructor privado |
| resto | ~10 | varios | — |

---

## Tier 4 — S1068 (265) · NO borrar en masa ⚠️

**261 de 265 caen en DTOs/JAXB/response** (`ObjectFactory.java` 82, `ComprobanteResponse`, `ParsedPaymentXmlDto`, `PaymentSearchResponse`, etc.). Esos campos SÍ se usan por (de)serialización XML/JSON vía reflexión — Sonar no lo ve y los marca "unused". **Borrarlos rompe el parseo de CFDI/pagos.**

**Estrategia correcta:**
1. **Excluir generados del análisis** — `ObjectFactory` y clases JAXB generadas → `sonar.exclusions` en `sonar-project.properties` (o `pom.xml` plugin config). Quita ~82+ de un golpe legítimamente.
2. **DTOs propios** — si son de binding, marcar la clase o mantener; no borrar campos.
3. **Solo los 4 de lógica real** son borrables tras verificar:
   - `InvoiceServiceImpl.java` (2)
   - `PacServiceDetecnoImpl.java` (1)
   - `PaymentRegistrationServiceImpl.java` (1)

Sin la exclusión, estos 261 quedan como deuda "aceptada" — no se arreglan borrando.

---

## Plan de ataque sugerido

1. **Tier 1** (seguridad + bugs, ~2-3h) — arrancar aquí. PR aparte, fácil de revisar.
2. **Exclusión JAXB en sonar config** (Tier 4 punto 1) — baja ~82 sin código.
3. **Tier 2** (mecánicos, ~1d) — commit por regla o por paquete.
4. **Tier 3** (con criterio + pruebas) — por lotes, priorizando HIGH (`S1192`, `S3776`).
5. **Tier 4 DTOs** — decidir con Ivan si se excluyen o se aceptan como deuda.

**Fixes se editan en PC personal (código) → viajan por mirror a Sodimac → re-análisis Sonar confirma reducción.**

---

## Inventario completo de reglas

| Regla | # | Impacto | Nombre |
|---|---|---|---|
| java:S1068 | 265 | MAINTAINABILITY=MEDIUM | Unused "private" fields should be removed |
| java:S1192 | 68 | MAINTAINABILITY=HIGH | String literals should not be duplicated |
| java:S1874 | 62 | MAINTAINABILITY=LOW | "@Deprecated" code should not be used |
| java:S1124 | 42 | MAINTAINABILITY=LOW | Modifiers should be declared in the correct order |
| java:S2293 | 39 | MAINTAINABILITY=LOW | The diamond operator should be used |
| java:S125 | 19 | MAINTAINABILITY=MEDIUM | Sections of code should not be commented out |
| java:S3776 | 14 | MAINTAINABILITY=HIGH | Cognitive Complexity should not be too high |
| java:S117 | 13 | MAINTAINABILITY=LOW | Local variable naming convention |
| java:S1135 | 12 | MAINTAINABILITY=LOW | Track uses of "TODO" tags |
| java:S2755 | 11 | SECURITY=HIGH | XML parsers vulnerable to XXE |
| java:S107 | 10 | MAINTAINABILITY=MEDIUM | Too many parameters |
| java:S112 | 10 | MAINTAINABILITY=MEDIUM | Generic exceptions should never be thrown |
| java:S1130 | 10 | MAINTAINABILITY=LOW | Superfluous "throws" |
| java:S1133 | 9 | MAINTAINABILITY=LOW | Deprecated code should be removed |
| java:S1144 | 8 | MAINTAINABILITY=MEDIUM | Unused "private" methods |
| java:S1488 | 8 | MAINTAINABILITY=LOW | Immediately returned variable |
| java:S100 | 7 | MAINTAINABILITY=LOW | Method naming convention |
| java:S116 | 6 | MAINTAINABILITY=LOW | Field naming convention |
| java:S1141 | 6 | MAINTAINABILITY=MEDIUM | Nested try-catch |
| java:S3740 | 6 | MAINTAINABILITY=MEDIUM | Raw types should not be used |
| java:S106 | 5 | MAINTAINABILITY=MEDIUM | Standard outputs for logging |
| java:S1172 | 4 | MAINTAINABILITY=MEDIUM | Unused method parameters |
| java:S1118 | 3 | MAINTAINABILITY=MEDIUM | Utility class public constructor |
| java:S6541 | 3 | MAINTAINABILITY=INFO | Brain method |
| java:S1854 | 3 | MAINTAINABILITY=MEDIUM | Unused assignments |
| java:S1481 | 3 | MAINTAINABILITY=LOW | Unused local variables |
| java:S4087 | 3 | MAINTAINABILITY=LOW | Redundant "close()" |
| java:S7158 | 2 | MAINTAINABILITY=LOW | Use "String.isEmpty()" |
| java:S3077 | 2 | RELIABILITY=LOW | Non-primitive volatile fields |
| java:S1197 | 1 | MAINTAINABILITY=LOW | Array designators on the type |
| java:S1186 | 1 | MAINTAINABILITY=HIGH | Empty method |
| java:S4144 | 1 | MAINTAINABILITY=MEDIUM | Identical method implementations |
| java:S1128 | 1 | MAINTAINABILITY=LOW | Unnecessary imports |
| java:S2142 | 1 | RELIABILITY=MEDIUM | InterruptedException ignored |
| java:S5850 | 1 | RELIABILITY=MEDIUM | Regex alternatives grouping |
| java:S135 | 1 | MAINTAINABILITY=LOW | Multiple break/continue in loop |
| java:S1155 | 1 | MAINTAINABILITY=LOW | Use "Collection.isEmpty()" |
| java:S2095 | 1 | RELIABILITY=HIGH | Resources should be closed |
| java:S899 | 1 | RELIABILITY=LOW | Ignored operation status return |
| java:S4042 | 1 | MAINTAINABILITY=MEDIUM | Prefer "Files#delete" |
| java:S2093 | 1 | MAINTAINABILITY=HIGH | Use try-with-resources |
| java:S2589 | 1 | MAINTAINABILITY=MEDIUM | Gratuitous boolean expression |

---

## Top archivos por # issues

| # | Archivo | Nota |
|---|---|---|
| 82 | ObjectFactory.java | **JAXB generado — excluir de Sonar** |
| 45 | InvoiceServiceImpl.java | lógica core, tiene S3776 |
| 35 | XmlToJsonConverter.java | conversión XML |
| 31 | PacServiceDetecnoImpl.java | tiene 3 XXE |
| 25 | ComprobanteResponse.java | DTO (S1068 falsos-pos) |
| 24 | ParsedPaymentXmlDto.java | DTO |
| 21 | PaymentSearchResponse.java | DTO |
| 19 | ValidaXmlServiceImpl.java | tiene 2 XXE |
| 16 | InvoiceRegistrationResponse.java | DTO |
| 15 | PaymentRegistrationServiceImpl.java | lógica |
