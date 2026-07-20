# NC — tipos de relación permitidos desde catálogo `CatTipoRelacionFacturaNC`

**Fecha:** 2026-07-20 · **Solicita:** Ivan · **Módulo:** fiscal-api · **Commit mirror:** `ef4229c`

## Regla de negocio

Al **publicar** o **consultar** una Nota de Crédito, solo se liga a su factura el/los bloque(s)
`cfdi:CfdiRelacionados` cuyo `TipoRelacion` esté **permitido**. Los tipos permitidos ya **no están
fijos en código** (antes solo `01`): se leen del catálogo **`CatTipoRelacionFacturaNC`**, que negocio
administra desde el portal.

Un CFDI puede traer **varios** bloques `CfdiRelacionados`, uno por `TipoRelacion`. Cualquier bloque
cuyo tipo no esté en el catálogo se **ignora**.

### Catálogo (creado por Ivan en UAT, portal → catálogos id 104)

| Clave | Valor | Descripción |
|---|---|---|
| TFN0001 | `01` | Nota de crédito de los documentos relacionados |
| TFN0002 | `03` | Devolución de mercancía sobre facturas o traslados previos |

Para agregar/quitar tipos permitidos: solo se edita el catálogo, **sin cambio de código**.

## Implementación

- **Lectura del catálogo — DIRECTA a `shared_catalogs` (NO util-api), solo estatus activo** (decisión
  del equipo). `AddendumRepository.findActiveCatalogValues(catalogCode)`:
  ```sql
  SELECT cd.value
  FROM shared_catalogs.catalog_header ch
  JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
  WHERE ch.code = :catalogCode AND ch.status = 1 AND cd.status = 1
  ```
- **Register** — `InvoiceServiceImpl.saveRelatedCfdis`: filtra e itera todos los bloques cuyo
  `TipoRelacion` esté en el catálogo; guarda una relación por cada CFDI relacionado. `BUS042` si
  ningún bloque aplica. (Depende de que `InvoiceXmlDto.cfdiRelacionados` sea `List` para capturar
  todos los bloques — ver commit `76380dc`.)
- **Consulta** — `FiscalXmlTransformerServiceImpl`: inyecta `AddendumRepository`, toma el primer
  bloque cuyo tipo esté permitido (`getCfdiRelacionadosPermitido`).
- Constante del código de catálogo: `CAT_TIPO_RELACION_NC = "CatTipoRelacionFacturaNC"`.

## Validación

- **Unit** (`CfdiRelacionadosTipo01Test`, 5 casos): 01 permitido con 04 primero; orden inverso; 03
  permitido; ninguno permitido (04+05) → sin relación; JAXB captura todos los bloques.
- **E2E local** (data real UAT): registrada la factura `2FDC848B`, luego:
  - NC con bloques `04`+`01` → `related_cfdi.relation_type = 01`, liga a `2FDC848B`.
  - NC con bloques `04`+`03` → `related_cfdi.relation_type = 03`, liga a `2FDC848B`.
  - El bloque `04` ignorado en ambos casos.

## Despliegue

- **UAT no requiere seed**: Ivan ya creó el catálogo `CatTipoRelacionFacturaNC` (01, 03) por el portal.
- **Local (pruebas)**: sí hay que seedear el catálogo (header + detail 01/03, `status=1`) porque la BD
  local no lo trae.

## Antecedente

- `76380dc` — primera versión: filtro fijo `TipoRelacion="01"` (superado por este cambio) + fix de
  `InvoiceXmlDto.cfdiRelacionados` de objeto único a `List` (antes JAXB guardaba un solo bloque,
  dependiente del orden del XML).
