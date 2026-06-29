package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.dto.BulkDownloadRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceDto;
import com.sodimac.fiscal.api.model.dto.InvoiceRegistrationResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceSearchResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceUpdateRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceUpdateResponse;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusSearchRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusUpdateRequest;
import com.sodimac.fiscal.api.model.dto.InvoiceStatusUpdateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para gestión de facturas (I) y notas de crédito (E).
 *
 * Implementa las validaciones de negocio definidas en:
 * - STM-337: Registro de facturas y notas de crédito
 * - STM-339: Actualización de facturas y notas de crédito
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
public interface InvoiceService {

    /**
     * Obtiene todas las facturas y notas de crédito paginadas.
     *
     * @param pageable Configuración de paginación
     * @return Página de facturas/NC
     */
    Page<InvoiceDto> findAll(Pageable pageable);

    /**
     * Registra una factura o nota de crédito en el sistema (STM-337).
     *
     * Realiza las siguientes validaciones:
     * - Validación de RFC receptor autorizado
     * - Validación de versión CFDI vigente
     * - Validación de estructura y contenido de addenda
     * - Validación con SAT mediante PAC
     * - Registro en base de datos
     *
     * @param xmlFile Archivo XML del CFDI
     * @param idTransaccion ID de transacción para trazabilidad en bitácora (STM-704)
     * @return Respuesta del registro con código de negocio (BUS1xxx o BUS2xxx)
     */
    InvoiceRegistrationResponse registerInvoice(MultipartFile xmlFile, String idTransaccion,
            String receptionId, String supplierNumber, String purchaseOrderNumber, MultipartFile pdfFile,
            String tipoNotaCredito, boolean confirmarCancelacionNc);

    /**
     * Actualiza una factura o nota de crédito existente (STM-339).
     *
     * Permite actualizar:
     * - Estatus del documento (obligatorio)
     * - Usuario que realiza la actualización (obligatorio)
     * - Información de addenda (opcional)
     *
     * Validaciones:
     * - Valida que el documento exista
     * - Valida que pertenezca al proveedor especificado
     * - Valida transiciones de estatus permitidas
     * - Valida estructura de addenda si se proporciona
     *
     * @param request Datos de actualización
     * @return Respuesta de actualización con código de negocio (BUS3xxx)
     */
    InvoiceUpdateResponse updateInvoice(InvoiceUpdateRequest request);

    /**
     * Busca facturas y notas de crédito con filtros (STM-338).
     *
     * Filtros obligatorios:
     * - RFC Emisor
     * - Fecha Inicio/Fin de recepción
     * - Tipo de documento (I o E)
     *
     * Filtros opcionales:
     * - RFC Receptor
     * - ID Proveedor
     * - Serie
     * - Folio
     * - UUID
     *
     * Resultado incluye:
     * - Información del Comprobante
     * - Datos del Emisor
     * - Datos del Receptor
     * - Información de Addenda (si existe)
     *
     * @param searchRequest Filtros de búsqueda
     * @return Página de resultados con datos completos
     */
    Page<InvoiceSearchResponse> searchInvoices(InvoiceSearchRequest searchRequest);

    Page<InvoiceSearchResponse> searchInvoices(InvoiceSearchRequest searchRequest, java.util.List<String> allowedVendors);

    // ========== DESCARGA MASIVA (STM-396) ==========

    /**
     * Genera un archivo ZIP con los XML de los documentos solicitados (STM-396).
     *
     * @param request Lista de invoice_uuid a descargar
     * @return Bytes del archivo ZIP con los XMLs
     */
    byte[] downloadXmlZip(BulkDownloadRequest request);

    /**
     * Genera un archivo ZIP con los PDF de los documentos solicitados (STM-396).
     *
     * Nota: Por ahora genera PDFs basicos desde el XML.
     * En el futuro se podria integrar con un servicio de generacion de PDF.
     *
     * @param request Lista de invoice_uuid a descargar
     * @return Bytes del archivo ZIP con los PDFs
     */
    byte[] downloadPdfZip(BulkDownloadRequest request);

    /**
     * Genera un archivo CSV con los datos de los documentos segun filtros (STM-396).
     *
     * Exporta los campos principales de los documentos encontrados.
     *
     * @param searchRequest Filtros de busqueda (mismos que endpoint /search)
     * @return Bytes del archivo CSV
     */
    byte[] exportCsv(InvoiceSearchRequest searchRequest);

    /**
     * Genera un archivo XLSX con los datos de los documentos segun filtros.
     *
     * El archivo contiene 2 hojas:
     * - Hoja 1 "Facturas": Lista de facturas encontradas
     * - Hoja 2 "Notas de Credito": NC relacionadas a las facturas
     *
     * @param searchRequest Filtros de busqueda (mismos que endpoint /search)
     * @param lang Idioma para headers (es, en)
     * @return Bytes del archivo XLSX
     */
    byte[] exportToXlsx(InvoiceSearchRequest searchRequest, String lang);

    // ========== DESCARGA INDIVIDUAL XML ==========

    /**
     * Obtiene el contenido XML de un documento fiscal por su UUID.
     *
     * Busca primero en facturas/NC, luego en complementos de pago.
     *
     * @param fiscalUuid UUID fiscal del documento (TimbreFiscalDigital)
     * @return Contenido XML del documento
     * @throws IllegalArgumentException si el documento no existe
     */
    String getXmlByFiscalUuid(String fiscalUuid);

    byte[] getPdfByInvoiceUuid(String invoiceUuid);

    // ========== STM-410: BÚSQUEDA Y ACTUALIZACIÓN POR ESTATUS ==========

    /**
     * Busca facturas y notas de crédito con estatus obligatorio (STM-410).
     *
     * Similar a searchInvoices pero con estatus como campo obligatorio.
     * Usado para filtrar documentos en estados específicos del flujo.
     *
     * @param searchRequest Filtros de búsqueda con estatus obligatorio
     * @return Página de resultados
     */
    Page<InvoiceSearchResponse> searchInvoicesByStatus(InvoiceStatusSearchRequest searchRequest);

    /**
     * Actualiza el estatus de un documento con validación de transición (STM-410).
     *
     * Valida la transición contra el servicio de tren de estatus (catalogos-api).
     * Si el estatus destino es 7 (Pendiente de Pago), guarda fechaContabilizacion.
     *
     * Códigos de error:
     * - WRN7010: Estatus origen no catalogado
     * - WRN7011: Transición no permitida
     *
     * @param uuid UUID fiscal del documento
     * @param request Datos de actualización (estatusOrigen, estatusDestino, fechaContabilizacion)
     * @return Respuesta con resultado de la operación
     */
    InvoiceStatusUpdateResponse updateInvoiceStatus(String uuid, InvoiceStatusUpdateRequest request);
}