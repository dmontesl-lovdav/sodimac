# Verificacion de Endpoints: fiscal-api vs BFF api.yml

> **Fecha:** 2025-12-10
> **Proposito:** Asegurar que todos los endpoints de fiscal-api esten expuestos en el BFF

## Leyenda

| Simbolo | Significado |
|---------|-------------|
| OK | Endpoint existe en ambos |
| FALTA BFF | Endpoint en fiscal-api pero NO en BFF (no accesible publicamente) |
| SOLO BFF | Endpoint en BFF pero NO en fiscal-api (error 404) |

---

## Comparativa de Endpoints

### Endpoints de Facturas (InvoiceController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/invoices` | `/invoices` | OK | GET |
| `/invoices/search` | `/invoices/search` | OK | POST |
| `/invoices/register` | - | FALTA BFF | POST |
| `/invoices` (update) | - | FALTA BFF | PUT |
| `/invoices/validate-nc-relation` | - | FALTA BFF | POST |

### Endpoints de Pagos (PaymentController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/payments` | `/payments` | OK | GET |
| `/payments/{uuid}` | - | FALTA BFF | GET |

### Endpoints de Documentos de Pago (PaymentsDocumentsController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/payment-documents` | - | FALTA BFF | GET |
| `/payment-documents/{uuid}` | - | FALTA BFF | GET |

### Endpoints de Registro de Pagos (PaymentRegistrationController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/fiscal/complementos-pago/registrar` | - | FALTA BFF | POST |
| `/fiscal/complementos-pago/buscar` | - | FALTA BFF | GET |

### Endpoints de Addendums (AddendumController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/addendums` | `/addendums` | OK | GET |

### Endpoints de Catalogos

| fiscal-api | BFF api.yml | Estado | Metodo | Controller |
|------------|-------------|--------|--------|------------|
| `/authorized-receivers` | `/authorized-receivers` | OK | GET | AuthorizedReceiverCatalogController |
| `/issuers` | `/issuers` | OK | GET | IssuerController |
| `/receivers` | `/receivers` | OK | GET | ReceiverController |
| `/pac-catalog` | `/pac-catalog` | OK | GET | PacCatalogController |
| `/version-catalog` | - | FALTA BFF | GET | VersionCatalogController |
| `/equivalence-dr` | - | FALTA BFF | GET | EquivalenceDrController |

### Endpoints de Procesamiento XML (FiscalXmlProcessorController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/fiscal/xml/process` | `/fiscal/xml/process` | OK | POST |
| `/fiscal/xml/process/file` | `/fiscal/xml/process/file` | OK | POST |
| `/fiscal/xml/detect` | `/fiscal/xml/detect` | OK | POST |
| `/fiscal/xml/validate` | `/fiscal/xml/validate` | OK | POST |
| `/fiscal/xml/health` | `/fiscal/xml/health` | OK | GET |

### Endpoints de Validacion (FiscalValidationController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/validacion/upload` | `/validacion/upload` | OK | POST |
| `/get` | - | FALTA BFF | GET |

### Endpoints de PDF (PdfController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/api/fiscal/pdf/from-file` | `/api/fiscal/pdf/from-file` | OK | POST |
| `/api/fiscal/pdf/from-fiscal-uuid/{invoiceUuid}` | `/api/fiscal/pdf/from-fiscal-uuid/{invoiceUuid}` | OK | GET |

### Endpoints de PDF de Pagos (PaymentPdfController)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| `/api/payment/pdf/from-file` | `/api/payment/pdf/from-file` | OK | POST |
| `/api/payment/pdf/from-uuid/{invoiceUuid}` | `/api/payment/pdf/from-uuid/{invoiceUuid}` | OK | GET |

### Endpoints de Receiver RFC (NO HAY CONTROLLER EN fiscal-api)

| fiscal-api | BFF api.yml | Estado | Metodo |
|------------|-------------|--------|--------|
| - | `/receiver-rfc` | SOLO BFF | POST/GET |
| - | `/receiver-rfc/search` | SOLO BFF | GET |
| - | `/receiver-rfc/{id}` | SOLO BFF | PUT |

### Otros Endpoints (fiscal-api)

| fiscal-api | BFF api.yml | Estado | Metodo | Controller |
|------------|-------------|--------|--------|------------|
| `/logs` | - | FALTA BFF | GET | LogController |
| `/totals` | - | FALTA BFF | GET | TotalsController |
| `/related-cfdi` | - | FALTA BFF | GET | RelatedCfdiController |
| `/related-documents` | - | FALTA BFF | GET | RelatedDocumentsController |
| `/related-documents/by-payment/{paymentsUuid}` | - | FALTA BFF | GET | RelatedDocumentsController |
| `/xml-invoices/convert-all` | - | FALTA BFF | GET | XmlInvoiceController |
| `/xml-invoices/upload-convert` | - | FALTA BFF | POST | XmlInvoiceController |
| `/xml-invoices/detect-document-type` | - | FALTA BFF | POST | XmlInvoiceController |
| `/health` | - | INTERNO | GET | HealthController |

---

## Resumen

### Endpoints OK (Expuestos correctamente): 18

1. `/invoices` GET
2. `/invoices/search` POST (STM-338)
3. `/payments` GET
4. `/addendums` GET
5. `/authorized-receivers` GET
6. `/issuers` GET
7. `/receivers` GET
8. `/pac-catalog` GET
9. `/fiscal/xml/process` POST
10. `/fiscal/xml/process/file` POST
11. `/fiscal/xml/detect` POST
12. `/fiscal/xml/validate` POST
13. `/fiscal/xml/health` GET
14. `/validacion/upload` POST
15. `/api/fiscal/pdf/from-file` POST
16. `/api/fiscal/pdf/from-fiscal-uuid/{invoiceUuid}` GET
17. `/api/payment/pdf/from-file` POST
18. `/api/payment/pdf/from-uuid/{invoiceUuid}` GET

### Endpoints FALTA BFF (No accesibles publicamente): 17

1. `/invoices/register` POST
2. `/invoices` PUT
3. `/invoices/validate-nc-relation` POST
4. `/payments/{uuid}` GET
5. `/payment-documents` GET
6. `/payment-documents/{uuid}` GET
7. `/fiscal/complementos-pago/registrar` POST
8. `/fiscal/complementos-pago/buscar` GET
9. `/version-catalog` GET
10. `/equivalence-dr` GET
11. `/get` GET
12. `/logs` GET
13. `/totals` GET
14. `/related-cfdi` GET
15. `/related-documents` GET
16. `/related-documents/by-payment/{paymentsUuid}` GET
17. `/xml-invoices/*` (3 endpoints)

### Endpoints SOLO BFF (CAUSAN ERROR 404): 3

**CONFIRMADO:** NO existe ReceiverRfcController en fiscal-api

1. `/receiver-rfc` POST/GET - **ERROR 404**
2. `/receiver-rfc/search` GET - **ERROR 404**
3. `/receiver-rfc/{id}` PUT - **ERROR 404**

---

## Notas

1. Los endpoints "FALTA BFF" funcionan localmente pero NO desde la URL publica
2. Los endpoints "SOLO BFF" pueden causar error 404 si no existe el controller en fiscal-api
3. El endpoint `/health` es interno y no necesita exponerse en BFF

---

## Acciones Recomendadas

### Para nuevos tickets que usen estos endpoints:

Si necesitas usar alguno de los endpoints "FALTA BFF", debes:

1. Agregar el endpoint al `api.yml` del BFF
2. Agregar las definiciones de Request/Response si aplica
3. Desplegar el BFF

### Endpoints de receiver-rfc (PROBLEMA DETECTADO):

**CONFIRMADO:** NO existe `ReceiverRfcController` en fiscal-api.
Los endpoints `/receiver-rfc/*` en el BFF causan error 404.

**Opciones:**
1. **Eliminar del api.yml del BFF** (si no se usan)
2. **Crear el controller en fiscal-api** (si se necesitan para algun ticket)
