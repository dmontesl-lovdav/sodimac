# Guía de proceso de jiras Sodimac

Workflow estándar para cada nuevo jira asignado.

## Entrada (lo que tú compartes)

Carpeta con nombre exacto del jira:

```
docs/jiras/STM-XXXX/
└── STM-XXXX.xml          ← descripción oficial del jira exportada de Jira (NO TOCAR)
```

Ese XML es la fuente de verdad. Nunca lo modifico ni lo regenero.

## Procesamiento (lo que yo hago)

### Paso 1 — Lectura literal del XML

Genero `MXSTM-XXXX.md` que es **copia fiel del contenido del XML** en markdown (transcripción 1:1 sin interpretar). Sirve como vista legible del ticket.

> Regla guardada en memoria: el README/transcripción del jira debe ser copia identica del XML, sin análisis. El análisis va en archivo aparte. Ver [[feedback_jira_readme]].

### Paso 2 — Análisis técnico

`STM-XXXX_analisis.md` (opcional, si jira complejo):
- Qué se va a hacer (alcance traducido a tareas técnicas)
- Qué proyectos/repos se tocan
- Qué endpoints/tablas/middlewares se modifican
- Riesgos detectados

### Paso 3 — Retro para pegar en Jira

`STM-XXXX_respuesta-jira.md`:
- Resumen ejecutivo
- Cambios realizados (archivos + descripción corta)
- Cómo probar (curl rápido + endpoint + respuesta esperada)
- Queries SQL de validación (resumen)
- Estado: merged/PR/deploy

Este es el texto que tú copias y pegas en el comentario del jira.

### Paso 4 — Colección Postman para QA

`STM-XXXX_postman.json`:
- Sin variables `{{baseUrl}}` ni `{{jwt}}`
- URLs hardcodeadas: `http://localhost:8080/api/...` (o el puerto del servicio)
- Bodies con datos concretos listos para correr
- Tests asserts (status code mínimo)

### Paso 5 — Curl rápido para CMD

`STM-XXXX_curl.ps1` (PowerShell) y/o `STM-XXXX_curl.sh` (bash):
- Múltiples curls cubriendo casos del jira
- Sin variables — todo hardcodeado
- Comentarios cortos arriba de cada curl explicando qué prueba

### Paso 6 — Queries SQL para validación directa en DB

`STM-XXXX_queries_bd.sql`:
- SELECT para verificar estado post-prueba
- INSERT/UPDATE de seed si el jira requiere data prep
- DELETE/cleanup si aplica
- Comentarios indicando qué valida cada query

### Paso 7 — Actualizar índice

Agrego entrada en `docs/jiras/INDEX.md` con:
- Estado actual
- Proyectos tocados
- Sub-tareas/dependencias
- Link a la carpeta y archivos clave

## Salida final esperada

```
docs/jiras/STM-XXXX/
├── STM-XXXX.xml                  ← original (no se toca)
├── MXSTM-XXXX.md                 ← transcripción 1:1 del XML
├── STM-XXXX_analisis.md          ← opcional
├── STM-XXXX_respuesta-jira.md    ← retro para pegar en Jira
├── STM-XXXX_postman.json         ← collection sin variables
├── STM-XXXX_curl.ps1             ← curls PowerShell
├── STM-XXXX_curl.sh              ← curls bash
└── STM-XXXX_queries_bd.sql       ← validación DB
```

## Reglas

- **No invento contenido del jira** — si el XML no dice algo, no lo asumo. Pregunto.
- **Variables hardcodeadas en postman/curl** — porque QA y soporte corren sin setear nada.
- **Trazabilidad** — todo commit/PR menciona `STM-XXXX` en el mensaje.
- **Memoria persistente** — el INDEX.md es la fuente para responder preguntas tipo "¿qué hicimos en STM-1525?".
