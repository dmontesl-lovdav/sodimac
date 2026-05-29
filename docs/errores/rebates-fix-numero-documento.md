# Fix: Truncamiento NUMERO_DOCUMENTO en carga CSV rebates

**Fecha**: 2026-05
**Commit**: `5545687` (rama `dmontes`)
**Proyecto**: `soporte/sodimacfinanzasrebatesweb`
**Archivo**: `src/main/java/com/sodimac/rebates/service/ControlDocumentoService.java:299`

---

## Sintoma

Al cargar el CSV `PP_NC_251135_PERSIANAS BEST_ABRIL.csv` (notas de credito) el endpoint de upload de rebates falla con:

```
SQL Error: 8152, SQLState: 22001
String or binary data would be truncated.
ERROR al guardar registro 1: could not execute statement
```

Stack apunta a `enviosApRepository.save(enviosAp)` (linea 327 antes del fix), invocado desde `readCsvEnviosAp` (linea 251) y `ControlDocumentoController.saveControlDocumento` (linea 334).

## Root cause

El CSV trae campos con **saltos de linea dentro de comillas dobles** — formato valido para OpenCSV. Ejemplo:

```csv
2005,15/04/2026,NC-B18883," 
13-Des Prom 24-013270002-22D
 
 ",MXN,1,D,...
```

`NUMERO_DOCUMENTO` queda como `" \n13-Des Prom 24-013270002-22D\n \n "` (con CR/LF y espacios). El string final supera el `varchar` de la columna `Envios_Ap_Temp.NUMERO_DOCUMENTO` en SQL Server.

`EnviosAp.NUMERO_DOCUMENTO` no tiene `@Column(length=...)` — usa la definicion de columna en SQL Server tal cual.

## Fix aplicado

Sanitizar antes de `save`:

```java
// ANTES
enviosAp.setNUMERO_DOCUMENTO(item.getNumeroDocumento());

// DESPUES
enviosAp.setNUMERO_DOCUMENTO(item.getNumeroDocumento().replaceAll("[\\r\\n]+", " ").trim());
```

Elimina CR/LF y trim de bordes. No requiere cambio de schema ni del CSV de entrada — protege contra futuros CSV con multi-linea.

## Por que NO ampliar columna ni editar CSV

- **Ampliar columna**: corrige solo el caso actual, no protege contra strings genuinamente largos
- **Editar CSV**: requiere intervencion manual cada vez, no escala
- **Saneamiento en codigo**: proteccion permanente, transparente, sin cambios externos

## Archivos relacionados

- [ControlDocumentoService.java](../../soporte/sodimacfinanzasrebatesweb/src/main/java/com/sodimac/rebates/service/ControlDocumentoService.java)
- [EnviosAp.java](../../soporte/sodimacfinanzasrebatesweb/src/main/java/com/sodimac/rebates/model/EnviosAp.java)
- [rebates-error.txt](rebates-error.txt) — log original
- [PP_NC_251135_PERSIANAS BEST_ABRIL.csv](PP_NC_251135_PERSIANAS%20BEST_ABRIL.csv) — CSV que dispara el bug
