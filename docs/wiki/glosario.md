# Glosario — Tablas y conceptos de negocio

> Definición de negocio por tabla. Para detalle técnico de columnas ver [módulos](modulos/).

## Por módulo

### Módulo Fiscal (`tenant_fiscal`)

| Tabla | Definición de negocio |
|---|---|
| **invoice** | CFDI completo (Factura, Nota de Crédito o Complemento de Pago). Contiene el XML crudo y datos extraídos para queries. `document_type`: `I`=factura, `E`=NC, `P`=pago. |
| **issuer** | Emisor del CFDI (típicamente el proveedor que factura a Sodimac). |
| **receiver** | Receptor del CFDI (típicamente Sodimac o alguna de sus razones sociales). |
| **authorized_receiver_catalog** | Catálogo de RFCs autorizados para recibir CFDI (las razones sociales válidas de Sodimac). |
| **related_cfdi** | Vínculo entre 2 CFDIs (NC sustituye factura, NC referencia factura, etc.). |
| **payments** | El CFDI Complemento de Pago (REP) completo — un XML timbrado para SAT. ⚠ No confundir con `tenant_finance.fiscal_payments`. |
| **payment** | Cada pago individual dentro de un REP. Un REP puede tener N pagos. |
| **totals** | Sumatorias y desglose de IVA/ISR de un REP. |
| **related_documents** | Las facturas que un pago liquida (DoctoRelacionados del CFDI REP). |
| **equivalence_dr** | Datos de equivalencia DR cuando hay cambio de divisa en el pago. |
| **tax** | Encabezado de impuestos de una factura. |
| **tax_detail** | Detalle de impuesto por concepto/producto. |
| **tax_transfer** | IVA/IEPS trasladados (que cobra el proveedor a Sodimac). |
| **tax_withholding** | Retenciones IVA/ISR que aplica el receptor. |
| **addendum** | Addenda Sodimac dentro del CFDI (datos comerciales: OC, recepción, guía). |
| **invoice_status_history** | Trazabilidad de cambios de status de una factura. |
| **pac_catalog** | Proveedores Autorizados de Certificación (PACs) que timbran. |
| **version_catalog** | Versiones de XML aceptadas por cada PAC y tipo de documento. |
| **payment_file_registry** | Archivos REP procesados en carga masiva. |
| **payment_response_catalog** | Catálogo de códigos de respuesta para mensajes del flujo de pago. |

### Módulo Finanzas (`tenant_finance`)

| Tabla | Definición de negocio |
|---|---|
| **fiscal_payments** | ⚠ Naming engañoso. Es el **pago operativo al proveedor** (legacy single-row). NO es CFDI. Lo toca `POST /fiscal-payments`. |
| **payment_header** | Encabezado del pago refactorizado (versión nueva, 1:N). |
| **payment_detail** | Detalle del pago refactorizado. Múltiples documentos pueden caber en un mismo header. |
| **rebate** | Descuento comercial operativo (pre-timbrado). Lo toca `POST /rebates`. |
| **stamped_rebate** | Descuento ya timbrado fiscalmente. Vincula al CFDI de NC en `tenant_fiscal.invoice`. |
| **accounts_payable** | Registro contable SAP de una cuenta por pagar (entra por ETL desde el ERP). |
| **sap_document** | Documento enviado a SAP con su respuesta/status. |
| **account_statement** | Cabecera del estado de cuenta mensual del proveedor. Versionado. |
| **account_statement_invoice** | Facturas incluidas en un estado de cuenta. |
| **account_statement_credit_note** | NCs incluidas en un estado de cuenta. |
| **account_statement_discount** | Descuentos incluidos en un estado de cuenta. |
| **account_statement_payment** | Pagos incluidos en un estado de cuenta. |
| **account_statement_purchase_order** | OCs incluidas en un estado de cuenta. |
| **account_statement_reception** | Recepciones incluidas en un estado de cuenta. |
| **purchase_order** | Orden de compra al proveedor. |
| **reception** | Recepción de mercancía contra una OC. |
| **reception_sku** | SKUs individuales recibidos en una recepción. |
| **shipping_guide** | Guía de embarque del proveedor (camión, conductor, fecha entrega). Carta Porte. |
| **shipping_guide_document** | Documentos asociados a la guía (XML, CSV, fotos). |
| **shipping_guide_purchase_order** | Tabla puente N:M entre guía de embarque y OCs. |
| **three_way_match** | Resultado consolidado de la conciliación OC ↔ Recepción ↔ Factura. |
| **twm_ejecucion** | Cada corrida del job de Three Way Match. |
| **twm_logs** | Logs por ejecución del TWM. |
| **twm_cifras_control** | Totales por paso del proceso TWM (para auditoría). |
| **vendor_block** | Bloqueo de un proveedor (impide pago). |
| **addendum_manual** | Addenda capturada manualmente cuando el proveedor no la envía con su CFDI. |
| **pac_catalog** | ⚠ Duplicado del de fiscal. Deuda de diseño. |
| **version_catalog** | ⚠ Duplicado del de fiscal. Deuda de diseño. |
| **activity_logs** | Bitácora de actividades operativas (módulo activity_logs, antes auditoria-api). |
| **migrations** | Control de migraciones TypeORM. |

## Términos de negocio

| Término | Significado | Tabla(s) |
|---|---|---|
| **CFDI** | Comprobante Fiscal Digital por Internet. Estándar SAT México. | `invoice`, `payments` |
| **REP** | Recibo Electrónico de Pago (CFDI tipo P / Complemento de Pago). | `payments`, `payment` |
| **NC** | Nota de Crédito (CFDI tipo E). | `invoice` con `document_type='E'` |
| **TFD / UUID Fiscal** | Timbre Fiscal Digital. ID único asignado por SAT al timbrar. | `invoice.fiscal_uuid` |
| **Addenda** | Información comercial NO fiscal agregada al XML del CFDI (OC, recepción, etc.). | `addendum`, `addendum_manual` |
| **3-Way Match / TWM** | Conciliación OC ↔ Recepción ↔ Factura para autorizar pago. | `three_way_match` |
| **PAC** | Proveedor Autorizado de Certificación (timbra CFDIs ante SAT). | `pac_catalog` |
| **Rebate** | Descuento comercial al proveedor (acuerdo de volumen, exhibición, etc.). | `rebate`, `stamped_rebate` |
| **Cuenta por pagar (CxP)** | Registro contable SAP del adeudo al proveedor. | `accounts_payable` |
| **MIGO** | Documento SAP de recepción de mercancía. | (vía `sap_document` + endpoint `/api/migo`) |
| **DoctoRelacionado (DR)** | Factura referenciada por un complemento de pago. | `related_documents` |
| **Vendor / Supplier / Proveedor** | Misma cosa: contraparte que vende a Sodimac. Campo siempre `vendor_number`. | múltiples |
| **Estado de cuenta** | Snapshot mensual versionado de todos los movimientos del proveedor. | `account_statement` + 6 sub-tablas |
| **Bloqueo proveedor** | Marca que impide procesar pago al proveedor (por incidencia, validación, etc.). | `vendor_block` |

## Atributos de seguridad del usuario (epic STM-1403)

> Headers `x-user-*` inyectados por BFF tras decodificar JWT + consultar util-api.

| Atributo | Header HTTP | Significado | Mapeo BD típico |
|---|---|---|---|
| **Proveedor** (ATR001) | `x-user-vendors` | Lista de vendor_numbers que el usuario puede ver | `vendor_number` en cada tabla |
| **TipoProveedor** (ATR002) | `x-user-types` | Tipo de proveedor (nacional, extranjero, etc.) | `shared_catalogs` (catálogo proveedor) |
| **GrupoProveedor** (ATR004) | `x-user-groups` | Grupo de proveedores | `shared_catalogs` |

Valores especiales:
- `null` (header ausente) → admin, sin restricción
- `[]` (header vacío) → bloqueado, retorna WRN7029
- `"-1"` → wildcard, sin restricción

Detalle en [memoria proyecto](../../../.claude/projects/c--workspace-sodimac/memory/project_security_headers_semantics.md).
