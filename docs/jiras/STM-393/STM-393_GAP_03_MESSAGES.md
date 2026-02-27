# STM-393 GAP 03: Mensajes INF6000, WRN7000, WRN7005, WRN7006

## Descripcion del GAP

Los mensajes requeridos por STM-393 para validaciones y notificaciones no existen en el catalogo de mensajes de catalogos-api.

## Impacto

- **Severidad**: Media
- **Servicios afectados**: catalogos-api
- **Tablas afectadas**: shared_catalogs.dictionary_lang, shared_catalogs.catalog_detail

---

## Mensajes Requeridos

| idMsg | Tipo | Mensaje (ES) | Mensaje (EN) | Mensaje (PT) |
|-------|------|--------------|--------------|--------------|
| INF6000 | Informativo | No existe informacion con los criterios establecidos | No information found with the established criteria | Nao existem informacoes com os criterios estabelecidos |
| WRN7000 | Advertencia | La fecha inicio no puede ser superior a la fecha final | Start date cannot be greater than end date | A data de inicio nao pode ser posterior a data final |
| WRN7005 | Advertencia | El periodo de busqueda no puede ser superior a 6 meses, favor de validar | Search period cannot exceed 6 months, please validate | O periodo de busca nao pode exceder 6 meses, por favor valide |
| WRN7006 | Advertencia | La fecha final no puede ser menor a la fecha inicio | End date cannot be less than start date | A data final nao pode ser anterior a data de inicio |

---

## Implementacion Requerida

### Script SQL

**Archivo:** `src/main/resources/db/11_fiscal_pantalla_facturas_mensajes.sql`

```sql
-- ============================================================================
-- STM-393: Mensajes para Pantalla Consulta de Facturas
-- Fecha: 2025-01-06
-- Descripcion: Mensajes informativos y advertencias para validaciones de busqueda
-- ============================================================================

-- ============================================================================
-- PASO 1: Obtener el siguiente dict_id disponible
-- ============================================================================
-- Ejecutar primero para verificar:
-- SELECT MAX(dict_id) FROM shared_catalogs.dictionary_lang;
-- Asumimos que el siguiente disponible es 8000

-- ============================================================================
-- PASO 2: DICTIONARY_LANG - Traducciones
-- ============================================================================

-- INF6000: Sin resultados (dict_id: 8000)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8000, 1, 'No existe informacion con los criterios establecidos'),
(8000, 2, 'No information found with the established criteria'),
(8000, 3, 'Nao existem informacoes com os criterios estabelecidos');

-- WRN7000: Fecha inicio > fecha final (dict_id: 8001)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8001, 1, 'La fecha inicio no puede ser superior a la fecha final'),
(8001, 2, 'Start date cannot be greater than end date'),
(8001, 3, 'A data de inicio nao pode ser posterior a data final');

-- WRN7005: Rango > 6 meses (dict_id: 8002)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8002, 1, 'El periodo de busqueda no puede ser superior a 6 meses, favor de validar'),
(8002, 2, 'Search period cannot exceed 6 months, please validate'),
(8002, 3, 'O periodo de busca nao pode exceder 6 meses, por favor valide');

-- WRN7006: Fecha final < fecha inicio (dict_id: 8003)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8003, 1, 'La fecha final no puede ser menor a la fecha inicio'),
(8003, 2, 'End date cannot be less than start date'),
(8003, 3, 'A data final nao pode ser anterior a data de inicio');

-- ============================================================================
-- PASO 3: CATALOG_DETAIL - Registros en catalogos de mensajes
-- ============================================================================

-- Obtener header_id de CatMsgInformativo (deberia ser 10)
-- SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgInformativo';

-- Obtener header_id de CatMsgAdvertencia (deberia ser 11)
-- SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia';

-- Mensaje Informativo
INSERT INTO shared_catalogs.catalog_detail
(header_id, key, dict_id, color, sort_order, status, internal_status)
VALUES
(10, 'INF6000', 8000, '#17a2b8', 6000, 1, 6000);

-- Mensajes de Advertencia
INSERT INTO shared_catalogs.catalog_detail
(header_id, key, dict_id, color, sort_order, status, internal_status)
VALUES
(11, 'WRN7000', 8001, '#ffc107', 7000, 1, 7000),
(11, 'WRN7005', 8002, '#ffc107', 7005, 1, 7005),
(11, 'WRN7006', 8003, '#ffc107', 7006, 1, 7006);

-- ============================================================================
-- PASO 4: VERIFICACION
-- ============================================================================

-- Verificar traducciones insertadas
SELECT
    dl.dict_id,
    dl.lang_id,
    CASE dl.lang_id WHEN 1 THEN 'ES' WHEN 2 THEN 'EN' WHEN 3 THEN 'PT' END as idioma,
    dl.description
FROM shared_catalogs.dictionary_lang dl
WHERE dl.dict_id BETWEEN 8000 AND 8003
ORDER BY dl.dict_id, dl.lang_id;

-- Verificar detalles de catalogo insertados
SELECT
    ch.code as catalogo,
    cd.key as mensaje_key,
    dl.description as mensaje_es,
    cd.color,
    cd.status
FROM shared_catalogs.catalog_detail cd
INNER JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id
INNER JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.key IN ('INF6000', 'WRN7000', 'WRN7005', 'WRN7006')
ORDER BY cd.key;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
```

---

## Como Consumir los Mensajes

### Endpoint

```
GET /message/{key}?lang=1
```

### Ejemplos

**Sin resultados:**
```bash
curl "http://localhost:8083/message/INF6000?lang=1"
```

**Respuesta:**
```json
{
  "key": "INF6000",
  "description": "No existe informacion con los criterios establecidos",
  "type": "INFORMATIVO",
  "color": "#17a2b8"
}
```

**Fecha invalida:**
```bash
curl "http://localhost:8083/message/WRN7000?lang=1"
```

**Respuesta:**
```json
{
  "key": "WRN7000",
  "description": "La fecha inicio no puede ser superior a la fecha final",
  "type": "ADVERTENCIA",
  "color": "#ffc107"
}
```

---

## Uso en Frontend

### Validacion de fechas

```typescript
// Angular/TypeScript ejemplo
async validateDates(startDate: Date, endDate: Date): Promise<string | null> {
    if (startDate > endDate) {
        const message = await this.catalogService.getMessage('WRN7000');
        return message.description;
    }

    // Validar rango de 6 meses
    const diffMonths = this.getMonthsDifference(startDate, endDate);
    if (diffMonths > 6) {
        const message = await this.catalogService.getMessage('WRN7005');
        return message.description;
    }

    return null;
}

// Mostrar mensaje sin resultados
async handleNoResults(): Promise<void> {
    const message = await this.catalogService.getMessage('INF6000');
    this.showInfoMessage(message.description);
}
```

---

## Script de Rollback

```sql
-- Rollback: Eliminar mensajes STM-393
DELETE FROM shared_catalogs.catalog_detail
WHERE key IN ('INF6000', 'WRN7000', 'WRN7005', 'WRN7006');

DELETE FROM shared_catalogs.dictionary_lang
WHERE dict_id BETWEEN 8000 AND 8003;
```

---

## Checklist de Implementacion

- [ ] Verificar dict_id disponibles (ejecutar query)
- [ ] Script SQL creado
- [ ] Script ejecutado en ambiente DEV
- [ ] Verificar mensajes con endpoint GET /message/{key}
- [ ] Script ejecutado en ambiente UAT
- [ ] Documentacion actualizada

---

## Archivos a Crear

| Archivo | Ubicacion | Descripcion |
|---------|-----------|-------------|
| `11_fiscal_pantalla_facturas_mensajes.sql` | catalogos-api/src/main/resources/db/ | Script de insercion |
| `STM-393_01_messages.sql` | docs/jiras/STM-393/scripts/ | Copia para documentacion |

---

**Esfuerzo estimado:** 2 horas
**Dependencias:** Ninguna
