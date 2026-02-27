# STM-393 GAP 05: Validacion de Rango de Fechas en Backend

## Descripcion del GAP

El backend (fiscal-api) debe validar que el rango de fechas de busqueda no exceda el maximo permitido (6 meses por defecto). Actualmente esta validacion no existe.

## Impacto

- **Severidad**: Media
- **Servicios afectados**: fiscal-api
- **Dependencias**: GAP 04 (parametro MAX_SEARCH_MONTHS)

---

## Validaciones Requeridas

| Validacion | Condicion | Codigo Error | Mensaje |
|------------|-----------|--------------|---------|
| Fecha inicio <= fecha final | startDate > endDate | VAL001 | WRN7000 |
| Rango <= N meses | diffMonths > MAX_SEARCH_MONTHS | VAL002 | WRN7005 |

---

## Implementacion Requerida

### 1. Codigo de Error

**Archivo:** `src/main/java/com/sodimac/fiscal/api/exception/FiscalErrorCode.java`

```java
// Agregar codigos de validacion de fechas
DATE_RANGE_INVALID("VAL001", "Rango de fechas invalido"),
DATE_RANGE_EXCEEDED("VAL002", "Rango de fechas excede el maximo permitido");
```

### 2. Validador de Fechas

**Archivo:** `src/main/java/com/sodimac/fiscal/api/validation/DateRangeValidator.java`

```java
package com.sodimac.fiscal.api.validation;

import com.sodimac.fiscal.api.exception.FiscalErrorCode;
import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.service.UtilsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
@Slf4j
public class DateRangeValidator {

    private final UtilsApiClient utilsApiClient;

    // Valor por defecto si no se puede obtener del servicio
    private static final int DEFAULT_MAX_MONTHS = 6;

    /**
     * Valida el rango de fechas de busqueda
     * @param startDate Fecha inicio
     * @param endDate Fecha final
     * @throws FiscalException si la validacion falla
     */
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        // Validacion 1: Fecha inicio no puede ser mayor a fecha final
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            log.warn("Validacion fallida: fecha inicio {} > fecha final {}",
                startDate, endDate);
            throw new FiscalException(
                FiscalErrorCode.DATE_RANGE_INVALID,
                "WRN7000" // Codigo de mensaje para frontend
            );
        }

        // Validacion 2: Rango no puede exceder maximo permitido
        if (startDate != null && endDate != null) {
            int maxMonths = getMaxSearchMonths();
            int monthsDiff = calculateMonthsDifference(startDate, endDate);

            if (monthsDiff > maxMonths) {
                log.warn("Validacion fallida: rango {} meses > maximo {} meses",
                    monthsDiff, maxMonths);
                throw new FiscalException(
                    FiscalErrorCode.DATE_RANGE_EXCEEDED,
                    "WRN7005", // Codigo de mensaje para frontend
                    maxMonths  // Parametro para mensaje
                );
            }
        }

        log.debug("Validacion de fechas exitosa: {} - {}", startDate, endDate);
    }

    /**
     * Obtiene el maximo de meses permitido desde utils-api
     */
    private int getMaxSearchMonths() {
        try {
            return utilsApiClient.getMaxSearchMonths();
        } catch (Exception e) {
            log.warn("No se pudo obtener MAX_SEARCH_MONTHS, usando default: {}",
                DEFAULT_MAX_MONTHS);
            return DEFAULT_MAX_MONTHS;
        }
    }

    /**
     * Calcula la diferencia en meses entre dos fechas
     */
    private int calculateMonthsDifference(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        return period.getYears() * 12 + period.getMonths();
    }
}
```

### 3. Actualizar Servicio de Busqueda

**Archivo:** `src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final DateRangeValidator dateRangeValidator; // <-- Inyectar validador
    // ... otros campos

    @Override
    public Page<InvoiceSearchResponse> searchInvoices(InvoiceSearchRequest request) {
        log.info("Buscando facturas con filtros: {}", request);

        // Validar rango de fechas ANTES de ejecutar la busqueda
        dateRangeValidator.validateDateRange(
            request.getFechaInicioRecepcion(),
            request.getFechaFinalRecepcion()
        );

        // Continuar con la busqueda existente...
        Specification<InvoiceEntity> spec = InvoiceSpecification.buildSpecification(request);

        Pageable pageable = PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(Sort.Direction.fromString(request.getSortDirection()),
                    request.getSortBy())
        );

        Page<InvoiceEntity> invoices = invoiceRepository.findAll(spec, pageable);

        // ... resto de la implementacion
    }
}
```

### 4. Manejo de Excepcion

**Archivo:** `src/main/java/com/sodimac/fiscal/api/exception/FiscalException.java`

```java
@Getter
public class FiscalException extends RuntimeException {

    private final FiscalErrorCode errorCode;
    private final String messageKey;  // Clave del mensaje en catalogos-api
    private final Object[] messageParams; // Parametros para el mensaje

    public FiscalException(FiscalErrorCode errorCode, String messageKey) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageParams = null;
    }

    public FiscalException(FiscalErrorCode errorCode, String messageKey, Object... params) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageParams = params;
    }
}
```

### 5. Exception Handler

**Archivo:** `src/main/java/com/sodimac/fiscal/api/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FiscalException.class)
    public ResponseEntity<ErrorResponse> handleFiscalException(FiscalException ex) {
        log.error("FiscalException: {} - messageKey: {}",
            ex.getErrorCode(), ex.getMessageKey());

        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .messageKey(ex.getMessageKey()) // Para que frontend obtenga mensaje traducido
            .messageParams(ex.getMessageParams())
            .timestamp(LocalDateTime.now())
            .build();

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(error, status);
    }

    private HttpStatus determineHttpStatus(FiscalErrorCode errorCode) {
        return switch (errorCode) {
            case DATE_RANGE_INVALID, DATE_RANGE_EXCEEDED -> HttpStatus.BAD_REQUEST;
            case INVOICE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

@Data
@Builder
class ErrorResponse {
    private String code;
    private String message;
    private String messageKey;
    private Object[] messageParams;
    private LocalDateTime timestamp;
}
```

---

## Respuesta de Error

### Ejemplo: Rango excedido

**Request:**
```json
POST /invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "fechaInicioRecepcion": "2024-01-01",
  "fechaFinalRecepcion": "2025-01-01",
  "tipoDocumento": "I"
}
```

**Response (400 Bad Request):**
```json
{
  "code": "VAL002",
  "message": "Rango de fechas excede el maximo permitido",
  "messageKey": "WRN7005",
  "messageParams": [6],
  "timestamp": "2025-01-06T10:30:00"
}
```

### Ejemplo: Fecha inicio > fecha final

**Request:**
```json
POST /invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "fechaInicioRecepcion": "2025-06-01",
  "fechaFinalRecepcion": "2025-01-01",
  "tipoDocumento": "I"
}
```

**Response (400 Bad Request):**
```json
{
  "code": "VAL001",
  "message": "Rango de fechas invalido",
  "messageKey": "WRN7000",
  "timestamp": "2025-01-06T10:30:00"
}
```

---

## Uso en Frontend

```typescript
// Manejo de error en Angular
async searchInvoices(request: InvoiceSearchRequest): Promise<void> {
    try {
        const response = await this.invoiceService.search(request).toPromise();
        this.invoices = response.content;
    } catch (error) {
        if (error.status === 400 && error.error?.messageKey) {
            // Obtener mensaje traducido de catalogos-api
            const message = await this.catalogService.getMessage(error.error.messageKey);

            // Si hay parametros, reemplazarlos en el mensaje
            let formattedMessage = message.description;
            if (error.error.messageParams) {
                formattedMessage = this.formatMessage(
                    message.description,
                    error.error.messageParams
                );
            }

            this.showWarning(formattedMessage);
        } else {
            this.showError('Error al buscar facturas');
        }
    }
}
```

---

## Pruebas Unitarias

**Archivo:** `src/test/java/com/sodimac/fiscal/api/validation/DateRangeValidatorTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DateRangeValidatorTest {

    @Mock
    private UtilsApiClient utilsApiClient;

    @InjectMocks
    private DateRangeValidator validator;

    @Test
    void shouldPassWhenDateRangeIsValid() {
        when(utilsApiClient.getMaxSearchMonths()).thenReturn(6);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 1);

        assertDoesNotThrow(() -> validator.validateDateRange(startDate, endDate));
    }

    @Test
    void shouldFailWhenStartDateAfterEndDate() {
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 1);

        FiscalException exception = assertThrows(FiscalException.class,
            () -> validator.validateDateRange(startDate, endDate));

        assertEquals(FiscalErrorCode.DATE_RANGE_INVALID, exception.getErrorCode());
        assertEquals("WRN7000", exception.getMessageKey());
    }

    @Test
    void shouldFailWhenRangeExceedsMaxMonths() {
        when(utilsApiClient.getMaxSearchMonths()).thenReturn(6);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 1); // 12 meses

        FiscalException exception = assertThrows(FiscalException.class,
            () -> validator.validateDateRange(startDate, endDate));

        assertEquals(FiscalErrorCode.DATE_RANGE_EXCEEDED, exception.getErrorCode());
        assertEquals("WRN7005", exception.getMessageKey());
    }

    @Test
    void shouldUseDefaultWhenUtilsApiUnavailable() {
        when(utilsApiClient.getMaxSearchMonths()).thenThrow(new RuntimeException("Service unavailable"));

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 1); // 4 meses < 6 default

        assertDoesNotThrow(() -> validator.validateDateRange(startDate, endDate));
    }
}
```

---

## Checklist de Implementacion

- [ ] FiscalErrorCode actualizado con VAL001, VAL002
- [ ] DateRangeValidator creado
- [ ] UtilsApiClient creado (del GAP 04)
- [ ] InvoiceServiceImpl actualizado
- [ ] GlobalExceptionHandler actualizado
- [ ] Pruebas unitarias creadas
- [ ] Pruebas de integracion completadas

---

## Archivos a Crear/Modificar

| Archivo | Tipo | Accion |
|---------|------|--------|
| `FiscalErrorCode.java` | Java | Modificar |
| `DateRangeValidator.java` | Java | Crear |
| `FiscalException.java` | Java | Modificar |
| `GlobalExceptionHandler.java` | Java | Modificar |
| `InvoiceServiceImpl.java` | Java | Modificar |
| `DateRangeValidatorTest.java` | Java | Crear |

---

**Esfuerzo estimado:** 6 horas
**Dependencias:** GAP 04 (parametro MAX_SEARCH_MONTHS)
