# STM-393 GAP 04: Parametro MAX_SEARCH_MONTHS

## Descripcion del GAP

Se requiere un parametro de configuracion para definir el rango maximo de meses permitido en la busqueda de facturas. Segun STM-393, el valor debe ser 6 meses.

## Impacto

- **Severidad**: Media
- **Servicios afectados**: utils-api
- **Tablas afectadas**: core_utils.cat_parameter

---

## Parametro Requerido

| Campo | Valor |
|-------|-------|
| **name** | MAX_SEARCH_MONTHS |
| **description** | Maximo de meses permitidos en rango de busqueda de facturas |
| **value** | 6 |
| **id_module** | 1 (FISCAL) |
| **id_type** | 1 (CONFIGURACION) |
| **version** | 1.0 |
| **status** | 1 (Activo) |

---

## Implementacion Requerida

### Script SQL

**Archivo:** `src/database/STM-393_02_parameter.sql`

```sql
-- ============================================================================
-- STM-393: Parametro MAX_SEARCH_MONTHS
-- Fecha: 2025-01-06
-- Descripcion: Maximo de meses permitidos en rango de busqueda de facturas
-- ============================================================================

-- Verificar si el parametro ya existe
SELECT * FROM core_utils.cat_parameter WHERE name = 'MAX_SEARCH_MONTHS';

-- Insertar parametro (si no existe)
INSERT INTO core_utils.cat_parameter
(id_module, id_type, name, description, value, version, start_date, status, created_by, created_at)
VALUES
(1, 1, 'MAX_SEARCH_MONTHS', 'Maximo de meses permitidos en rango de busqueda de facturas', '6', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP)
ON CONFLICT (name, version) DO NOTHING;

-- Verificar insercion
SELECT
    id_parameter,
    name,
    description,
    value,
    version,
    status
FROM core_utils.cat_parameter
WHERE name = 'MAX_SEARCH_MONTHS';

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
```

---

## Como Consumir el Parametro

### Endpoint

```
GET /api/parameters?name=MAX_SEARCH_MONTHS&status=1
```

### Respuesta

```json
{
  "success": true,
  "data": [
    {
      "idParameter": 22,
      "idModule": 1,
      "idType": 1,
      "name": "MAX_SEARCH_MONTHS",
      "description": "Maximo de meses permitidos en rango de busqueda de facturas",
      "value": "6",
      "version": "1.00",
      "startDate": "2025-01-06T10:00:00.000Z",
      "endDate": null,
      "status": 1
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 1
  }
}
```

### Obtener por ID

```
GET /api/parameters/{id}
```

---

## Uso en fiscal-api

### Servicio para obtener parametro

**Archivo:** `src/main/java/com/sodimac/fiscal/api/service/UtilsApiClient.java`

```java
@Service
@Slf4j
public class UtilsApiClient {

    @Value("${sodimac.utils.api.url:http://localhost:3712}")
    private String utilsApiUrl;

    private final RestTemplate restTemplate;

    public UtilsApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Obtiene el valor de un parametro por nombre
     */
    public Optional<String> getParameterValue(String parameterName) {
        try {
            String url = String.format("%s/api/parameters?name=%s&status=1",
                utilsApiUrl, parameterName);

            ResponseEntity<ParameterListResponse> response = restTemplate.getForEntity(
                url, ParameterListResponse.class);

            if (response.getBody() != null &&
                response.getBody().getData() != null &&
                !response.getBody().getData().isEmpty()) {

                return Optional.of(response.getBody().getData().get(0).getValue());
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Error obteniendo parametro {}: {}", parameterName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene el maximo de meses para busqueda
     */
    public int getMaxSearchMonths() {
        return getParameterValue("MAX_SEARCH_MONTHS")
            .map(Integer::parseInt)
            .orElse(6); // Default 6 meses si no se puede obtener
    }
}

@Data
class ParameterListResponse {
    private boolean success;
    private List<ParameterDto> data;
}

@Data
class ParameterDto {
    private Integer idParameter;
    private String name;
    private String value;
    private String version;
    private Integer status;
}
```

### Configuracion application.yml

```yaml
sodimac:
  utils:
    api:
      url: ${UTILS_API_URL:http://localhost:3712}
      enabled: true
```

---

## Uso en Frontend

### Angular Service

```typescript
@Injectable({
  providedIn: 'root'
})
export class ParameterService {

  private baseUrl = environment.utilsApiUrl;

  constructor(private http: HttpClient) {}

  async getMaxSearchMonths(): Promise<number> {
    try {
      const response = await this.http.get<any>(
        `${this.baseUrl}/api/parameters?name=MAX_SEARCH_MONTHS&status=1`
      ).toPromise();

      if (response?.data?.length > 0) {
        return parseInt(response.data[0].value, 10);
      }
      return 6; // Default
    } catch (error) {
      console.error('Error obteniendo MAX_SEARCH_MONTHS:', error);
      return 6; // Default
    }
  }
}
```

### Validacion en componente

```typescript
async validateDateRange(startDate: Date, endDate: Date): Promise<boolean> {
    const maxMonths = await this.parameterService.getMaxSearchMonths();
    const diffMonths = this.getMonthsDifference(startDate, endDate);

    if (diffMonths > maxMonths) {
        const message = await this.catalogService.getMessage('WRN7005');
        this.showWarning(message.description);
        return false;
    }

    return true;
}

private getMonthsDifference(start: Date, end: Date): number {
    const months = (end.getFullYear() - start.getFullYear()) * 12;
    return months + end.getMonth() - start.getMonth();
}
```

---

## Script de Versionado (Cambio de valor)

Si en el futuro se necesita cambiar el valor de 6 a otro numero:

```bash
# Usando el endpoint de versionado de STM-1213
curl -X POST "http://localhost:3712/api/parameters/{id}/versions" \
  -H "Content-Type: application/json" \
  -d '{
    "value": "12",
    "changeReason": "Ampliacion de rango por requerimiento de negocio"
  }'
```

---

## Checklist de Implementacion

- [ ] Script SQL creado
- [ ] Script ejecutado en ambiente DEV
- [ ] Verificar parametro con GET /api/parameters?name=MAX_SEARCH_MONTHS
- [ ] UtilsApiClient creado en fiscal-api (opcional si validacion es frontend)
- [ ] Script ejecutado en ambiente UAT
- [ ] Documentacion actualizada

---

## Archivos a Crear

| Archivo | Ubicacion | Descripcion |
|---------|-----------|-------------|
| `STM-393_02_parameter.sql` | docs/jiras/STM-393/scripts/ | Script de insercion |
| `UtilsApiClient.java` | fiscal-api (opcional) | Cliente para utils-api |

---

**Esfuerzo estimado:** 2 horas
**Dependencias:** utils-api debe estar disponible
