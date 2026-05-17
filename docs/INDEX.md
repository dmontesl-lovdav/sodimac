# Documentacion FBC - Indice Maestro

> Ultima actualizacion: 2026-05-15

## 📚 WIKI DE NEGOCIO — ENTRADA RECOMENDADA

Para preguntas tipo "¿dónde guardo X?", "¿para qué sirve esta tabla?", "¿cómo fluye este proceso?":

→ **[Wiki Sodimac](wiki/README.md)** — punto único de entrada navegable

- [Conceptos clave (pago vs descuento, rebate, etc.)](wiki/conceptos-clave.md)
- [Glosario de tablas y términos](wiki/glosario.md)
- [Dónde vive qué (Q&A)](wiki/donde-vive-que.md)
- [Procesos de negocio](wiki/README.md#procesos)
- [Módulos: Fiscal](wiki/modulos/fiscal.md) · [Finanzas](wiki/modulos/finanzas.md)

## Estructura de Carpetas

```
docs/
├── analisis/           # Analisis tecnicos y reportes
├── arquitectura/       # Modelos de BD, diagramas, decisiones
│   ├── modelos-bd/     # Modelos entidad-relacion
│   └── diagramas/      # Diagramas de arquitectura
├── jiras/              # Documentacion por ticket JIRA
│   └── STM-XXX/        # Carpeta por cada ticket
└── planes/             # Planes de migracion, pruebas, releases
```

---

## Analisis Tecnicos

| Archivo | Descripcion |
|---------|-------------|
| [ANALISIS_CONSTRUCCION_FISCAL_API.md](analisis/ANALISIS_CONSTRUCCION_FISCAL_API.md) | Analisis de construccion de Fiscal API |
| [ANALISIS_ER_COMPLETO_PARTE2.md](analisis/ANALISIS_ER_COMPLETO_PARTE2.md) | Analisis del modelo ER - Parte 2 |
| [ANALISIS_ER_VS_IMPLEMENTACION.md](analisis/ANALISIS_ER_VS_IMPLEMENTACION.md) | Comparativa ER vs Implementacion |
| [ANALISIS_TABLAS_CFDI_DUPLICADAS.md](analisis/ANALISIS_TABLAS_CFDI_DUPLICADAS.md) | Analisis de tablas CFDI duplicadas |
| [CORRECCIONES_FISCAL_UUID.md](analisis/CORRECCIONES_FISCAL_UUID.md) | Correcciones de UUID en Fiscal |
| [EXPLICACION_FUNCIONAL_TABLAS.md](analisis/EXPLICACION_FUNCIONAL_TABLAS.md) | Explicacion funcional de tablas |
| [PROBLEMAS_CRITICOS_DATABASE.md](analisis/PROBLEMAS_CRITICOS_DATABASE.md) | Problemas criticos de base de datos |
| [RESUMEN_TABLAS_ER_VS_IMPLEMENTACION.md](analisis/RESUMEN_TABLAS_ER_VS_IMPLEMENTACION.md) | Resumen ER vs Implementacion |
| [SCHEMA_CORRECTIONS_CHANGELOG.md](analisis/SCHEMA_CORRECTIONS_CHANGELOG.md) | Changelog de correcciones de schema |
| [TRABAJO_COMPLETADO_RESUMEN.md](analisis/TRABAJO_COMPLETADO_RESUMEN.md) | Resumen de trabajo completado |
| [db/fks-20260515/](db/fks-20260515/) | Scripts agregar FKs faltantes a tenant_fiscal + tenant_finance (audit + cleanup + ADD CONSTRAINT, idempotente). Validado local, pendiente UAT. |

### Archivos de Datos (CSV/XLSX)

| Archivo | Descripcion |
|---------|-------------|
| [catalogos_fbc_revision.xlsx](analisis/catalogos_fbc_revision.xlsx) | Revision de catalogos FBC |
| [database_tables_analysis.csv](analisis/database_tables_analysis.csv) | Analisis de tablas de BD |
| [ESTADO_IMPLEMENTACION_TABLAS.csv](analisis/ESTADO_IMPLEMENTACION_TABLAS.csv) | Estado de implementacion de tablas |
| [FLUJO_NEGOCIO_DEPENDENCIAS.csv](analisis/FLUJO_NEGOCIO_DEPENDENCIAS.csv) | Flujo de negocio y dependencias |
| [RESUMEN_TABLAS_ER_IMPLEMENTACION.csv](analisis/RESUMEN_TABLAS_ER_IMPLEMENTACION.csv) | Resumen tablas ER implementacion |

---

## Arquitectura

### Documentacion Critica (ALTA PRIORIDAD)

| Documento | Descripcion |
|-----------|-------------|
| [EXPOSICION_ENDPOINTS_BFF.md](arquitectura/EXPOSICION_ENDPOINTS_BFF.md) | **CRITICO**: Como exponer endpoints a traves del BFF. Incluye diagrama de arquitectura, checklist y solucion a error 405. |
| [VERIFICACION_ENDPOINTS.md](arquitectura/VERIFICACION_ENDPOINTS.md) | Comparativa completa fiscal-api vs BFF. Lista endpoints faltantes y problemas detectados. |

### Modelos de Base de Datos

| Modelo | Descripcion |
|--------|-------------|
| [md-catalogos.png](arquitectura/modelos-bd/imagenes/md-catalogos.png) | Modelo de base de datos de Catalogos |

### Diagramas

_Ver diagrama de arquitectura BFF en [EXPOSICION_ENDPOINTS_BFF.md](arquitectura/EXPOSICION_ENDPOINTS_BFF.md)_

---

## Planes

| Archivo | Descripcion |
|---------|-------------|
| [FINALIZACION_MIGRACION_FISCAL_API.md](planes/FINALIZACION_MIGRACION_FISCAL_API.md) | Finalizacion de migracion Fiscal API |
| [PLAN_DE_PRUEBAS.md](planes/PLAN_DE_PRUEBAS.md) | Plan de pruebas |
| [PLAN_MIGRACION_RECEIPT_TO_RECEPTION.md](planes/PLAN_MIGRACION_RECEIPT_TO_RECEPTION.md) | Plan migracion Receipt to Reception |

---

## JIRAs

> Para buscar documentacion de un JIRA especifico, ve a `docs/jiras/STM-XXX/`

| JIRA | Estado | Descripcion |
|------|--------|-------------|
| [STM-338](jiras/STM-338/README.md) | Completado | Busqueda de facturas con filtros OC/Recepcion |
| [STM-1168](jiras/STM-1168/README.md) | Completado | NC relacionadas en busqueda |
| [STM-1169](jiras/STM-1169/README.md) | Completado | Datos OC/Recepcion en respuesta |
| [STM-1188](jiras/STM-1188/README.md) | Completado | Documentacion registro de facturas |
| [STM-1212](jiras/STM-1212/README.md) | Completado | Manejo de mensajes de respuesta |

### Como agregar un nuevo JIRA

1. Crear carpeta: `docs/jiras/STM-XXX/`
2. Copiar template: `docs/jiras/_TEMPLATE/README.md`
3. Completar el checklist y documentacion

---

## Otros Recursos

### Scripts

- **Database**: `scripts/database/` - Scripts SQL y migraciones
- **PowerShell**: `scripts/powershell/` - Scripts de automatizacion

### Postman

- **Colecciones**: `postman/` - Colecciones de API para testing

---

## Busqueda Rapida

- **Por JIRA**: `docs/jiras/STM-XXX/`
- **Modelos BD**: `docs/arquitectura/modelos-bd/`
- **Scripts SQL**: `scripts/database/`
- **Colecciones Postman**: `postman/`
