# Analisis y Propuesta - Monitor de Descarga OC Proveedores Detecno

**Fecha:** 2026-03-30
**Proyecto:** `soporte/finanzas_descarga_oc_prov_detecno`

---

## Situacion actual

El batch tiene `batch=false`, lo que hace que consulte las fechas del API de parametros de Sodimac. Actualmente el rango esta configurado del **2026/01/01 al 2027/01/01** (un ano completo). Cada vez que se ejecuta, descarga **todas las OC de todo el ano** desde Detecno, las inserta en la tabla temporal `OrdenCompraProveedorTemp` y ejecuta el SP `uspRegistroOrdenCompraProveedor`. No hay validacion de si ya existen registros.

### Flujo actual

```
run.bat
  -> MainComponent.mainMethod()
    -> DetecnoClient.getOrdenesCompra()
      -> SodimacParametrosClient.getFechasParametros()  [batch=false]
        -> Consulta API parametros: FechaInicio=2026/01/01, FechaFin=2027/01/01
      -> Descarga TODAS las OC del rango (1 ano)
    -> OrdenCompraServiceImpl.saveOrdenesBatch()  [inserta en temp]
    -> OrdenCompraServiceImpl.ejecutaSP()  [procesa a tablas finales]
```

### Archivos clave

| Archivo | Linea | Que hace |
|---|---|---|
| `SodimacParametrosClient.java` | 46 | Flag `batch`: true=fechas automaticas, false=fechas del API |
| `DetecnoClient.java` | 86 | Obtiene parametros de fechas |
| `DetecnoClient.java` | 88-94 | Usa fechas del API de parametros |
| `DetecnoClient.java` | 96-113 | Logica automatica: ayer o semana si es domingo |
| `application.properties` | 35 | `batch=false` (actualmente consulta rango del API) |

---

## Por que falla

- El volumen de un ano completo es excesivo para una sola llamada al API de Detecno
- Cualquier timeout, corte de red o error hace que la descarga falle completa
- No hay reintento parcial ni verificacion de que dias ya se descargaron
- No hay forma de saber que dias faltan sin revisar manualmente la BD
- Cuando falla, hay que cambiar configuracion y re-ejecutar manualmente

---

## Propuesta de solucion: Monitor de 10 dias

Agregar una verificacion automatica de los ultimos 10 dias en cada ejecucion.

### Flujo propuesto

```
INICIO (ejecucion diaria)
  |
  [1] Obtener ultimos 10 dias
  |
  [2] Por cada dia:
      +-- Consultar BD: existen registros para esa fecha?
      |   +-- SI -> log "fecha YYYY-MM-DD OK" -> siguiente dia
      |   +-- NO -> Llamar API Detecno solo para esa fecha
      |       +-- Hay datos -> insertar en temp + ejecutar SP
      |       +-- No hay datos -> log "sin OC en Detecno para esa fecha"
      |
  [3] Resumen: dias OK, dias descargados, dias sin datos
  |
FIN
```

### Ventajas

- Descarga solo lo que falta, no todo el ano
- Auto-recupera fallos de los ultimos 10 dias sin intervencion manual
- No duplica datos (verifica antes de descargar)
- Reduce drasticamente el volumen por llamada (1 dia vs 1 ano)
- El proceso manual sigue disponible como fallback para casos >10 dias

---

## Plan de implementacion

| # | Tarea | Detalle | Tiempo |
|---|---|---|---|
| 1 | Query de verificacion | Nuevo metodo en `OrdenCompraProveedorRepository` para contar registros por fecha | 2 hrs |
| 2 | Refactor DetecnoClient | Que `getOrdenesCompra()` acepte fechaInicio y fechaFin como parametros | 2 hrs |
| 3 | Servicio monitor | Nuevo metodo que itere 10 dias atras, verifique BD y descargue faltantes | 4 hrs |
| 4 | Cambiar a batch=true | Ajustar `application.properties` y que el flujo default sea el monitor | 1 hr |
| 5 | Logging/resumen | Log tipo monitor: dias OK, dias descargados, dias sin datos en Detecno | 2 hrs |
| 6 | Pruebas en DEV | Probar con datos reales, validar que no duplique ni pierda registros | 4 hrs |
| 7 | Documentacion | Actualizar README con instrucciones del monitor y proceso manual (>10 dias) | 1 hr |
| 8 | Pase a produccion | Deploy del JAR y validacion en PROD | 2 hrs |

**Total estimado: 3 dias** (desarrollo + pruebas + pase a produccion)

---

## Cambios tecnicos esperados

### Archivos a modificar
- `OrdenCompraProveedorRepository.java` - agregar query de conteo por fecha
- `DetecnoClient.java` - parametrizar fechas en `getOrdenesCompra()`
- `OrdenCompraServiceImpl.java` - nuevo metodo monitor de 10 dias
- `MainComponent.java` - llamar al monitor en lugar del flujo actual
- `application.properties` - cambiar `batch=true`

### Archivos nuevos
- Ninguno (se reutiliza la estructura existente)

### Riesgo: Bajo
- No se modifica la logica de insercion ni el SP
- Solo se agrega la capa de verificacion por encima
- El proceso manual sigue disponible como fallback
