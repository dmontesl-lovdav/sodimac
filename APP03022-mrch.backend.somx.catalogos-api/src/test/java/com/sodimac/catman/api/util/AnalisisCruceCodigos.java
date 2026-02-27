package com.sodimac.catman.api.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Script de análisis para cruzar códigos de fiscal-api (enums)
 * con los catálogos definidos en catalogos-api.
 *
 * Objetivo: Identificar qué mensajes de fiscal-api pueden completar
 * los catálogos marcados como "pendientes" (status=-1/amarillo).
 */
public class AnalisisCruceCodigos {

    // ========================================================================
    // DATOS DE FISCAL-API (FiscalErrorCode, FiscalSuccessCode, FiscalWarningCode)
    // ========================================================================

    static Map<String, String> fiscalErrorCodes = new LinkedHashMap<>();
    static Map<String, String> fiscalSuccessCodes = new LinkedHashMap<>();
    static Map<String, String> fiscalWarningCodes = new LinkedHashMap<>();

    // ========================================================================
    // DATOS DE CATALOGOS-API (03_data_insert.sql)
    // ========================================================================

    static Map<String, String> catalogoMsgExitoso = new LinkedHashMap<>();      // RES - header_id=5
    static Map<String, String> catalogoMsgNegocio = new LinkedHashMap<>();      // BUS - header_id=7
    static Map<String, String> catalogoMsgAdvertencia = new LinkedHashMap<>();  // WRN - header_id=11
    static Map<String, String> catalogoMsgInformativo = new LinkedHashMap<>();  // INF - header_id=10

    // Catálogos pendientes (status=-1, amarillos) - SIN DATOS AÚN
    // CatMsgErrorSistema (ERR) - header_id=6
    // CatMsgErrorInfra (INE) - header_id=8
    // CatMsgExcepcion (EXC) - header_id=9
    // CatPacRespuestasDetecno (PDT) - header_id=12
    // CatMsgConfirmacion (CFM) - header_id=14
    // CatEstatusComplemento (ECM) - header_id=19

    @Disabled("Utilidad de análisis - ejecutar manualmente cuando sea necesario")
    @Test
    public void analizarCruceCodigos() {
        cargarDatosFiscalApi();
        cargarDatosCatalogosApi();

        StringBuilder report = new StringBuilder();
        report.append("=".repeat(100)).append("\n");
        report.append(" ANÁLISIS DE CRUCE: fiscal-api vs catalogos-api\n");
        report.append(" Fecha: ").append(new Date()).append("\n");
        report.append("=".repeat(100)).append("\n\n");

        // 1. Análisis de coincidencias exactas
        report.append(analizarCoincidencias());

        // 2. Propuesta para llenar catálogos pendientes
        report.append(proponerDatosParaCatalogosPendientes());

        // 3. Resumen de códigos de fiscal-api por categoría
        report.append(resumenCodigosFiscalApi());

        // 4. SQL propuesto para completar catálogos
        report.append(generarSqlPropuesto());

        System.out.println(report);

        // Guardar a archivo
        try (PrintWriter pw = new PrintWriter(new FileWriter("analisis_cruce_codigos.txt"))) {
            pw.print(report);
            System.out.println("\n>>> Reporte guardado en: analisis_cruce_codigos.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void cargarDatosFiscalApi() {
        // ===== FiscalErrorCode =====
        // Errores XML (100-199)
        fiscalErrorCodes.put("FISCAL-ERR-100", "El archivo está vacío");
        fiscalErrorCodes.put("FISCAL-ERR-101", "El archivo debe tener extensión .xml");
        fiscalErrorCodes.put("FISCAL-ERR-102", "Error procesando archivo XML fiscal");
        fiscalErrorCodes.put("FISCAL-ERR-103", "El XML no tiene una estructura válida");
        fiscalErrorCodes.put("FISCAL-ERR-104", "Error leyendo archivo XML");

        // Errores XSD (200-299)
        fiscalErrorCodes.put("FISCAL-ERR-200", "El XML no cumple con la estructura XSD");
        fiscalErrorCodes.put("FISCAL-ERR-201", "El XML no cumple con la estructura XSD Pagos 2.0");

        // Errores JAXB (300-399)
        fiscalErrorCodes.put("FISCAL-ERR-300", "Error configurando procesador CartaPorte");
        fiscalErrorCodes.put("FISCAL-ERR-301", "Error configurando procesador CFDI");
        fiscalErrorCodes.put("FISCAL-ERR-302", "Error configurando procesador Pagos");
        fiscalErrorCodes.put("FISCAL-ERR-303", "Error procesando CartaPorte");
        fiscalErrorCodes.put("FISCAL-ERR-304", "Error procesando CFDI");
        fiscalErrorCodes.put("FISCAL-ERR-305", "Error procesando Pagos");

        // Errores Tipo Documento (400-499)
        fiscalErrorCodes.put("FISCAL-ERR-400", "Error detectando tipo de documento XML");
        fiscalErrorCodes.put("FISCAL-ERR-401", "Tipo de documento fiscal no válido");
        fiscalErrorCodes.put("FISCAL-ERR-402", "Tipo de CFDI no soportado en este procesador");
        fiscalErrorCodes.put("FISCAL-ERR-403", "Se esperaba Factura (I) pero se encontró tipo diferente");
        fiscalErrorCodes.put("FISCAL-ERR-404", "Se esperaba Nota de Crédito (E) pero se encontró tipo diferente");
        fiscalErrorCodes.put("FISCAL-ERR-405", "Tipo de comprobante no válido");
        fiscalErrorCodes.put("FISCAL-ERR-406", "El tipo de comprobante debe ser 'P' (Pago)");

        // Errores Complemento Pago (500-599)
        fiscalErrorCodes.put("FISCAL-ERR-500", "Datos de Pagos no pueden ser nulos");
        fiscalErrorCodes.put("FISCAL-ERR-501", "Complemento de Pago debe incluir nodo Totales");
        fiscalErrorCodes.put("FISCAL-ERR-502", "Nodo Totales debe especificar el monto total de pagos");
        fiscalErrorCodes.put("FISCAL-ERR-503", "Complemento de Pago debe contener al menos un elemento Pago");
        fiscalErrorCodes.put("FISCAL-ERR-504", "Complemento de Pago debe especificar la versión");
        fiscalErrorCodes.put("FISCAL-ERR-505", "El complemento de pago ya se encuentra registrado en el sistema");
        fiscalErrorCodes.put("FISCAL-ERR-506", "Error convirtiendo XML a Document");

        // Errores Validación Negocio (600-699)
        fiscalErrorCodes.put("FISCAL-ERR-600", "El tipo de addenda debe ser 5 para complementos de pago");
        fiscalErrorCodes.put("FISCAL-ERR-601", "El RFC receptor no está autorizado en el sistema");
        fiscalErrorCodes.put("FISCAL-ERR-602", "La versión del complemento de pago no es vigente en el sistema");
        fiscalErrorCodes.put("FISCAL-ERR-603", "El documento relacionado no se encuentra registrado en el sistema");

        // Errores SAT (700-799)
        fiscalErrorCodes.put("FISCAL-ERR-700", "Complemento rechazado por SAT");
        fiscalErrorCodes.put("FISCAL-ERR-701", "El UUID del complemento de pago no se encuentra registrado en el SAT");
        fiscalErrorCodes.put("FISCAL-ERR-702", "El complemento de pago se encuentra cancelado en el SAT");
        fiscalErrorCodes.put("FISCAL-ERR-703", "El RFC no es válido según el SAT");
        fiscalErrorCodes.put("FISCAL-ERR-704", "El sello digital del complemento de pago no es válido");
        fiscalErrorCodes.put("FISCAL-ERR-705", "Error en la validación SAT");

        // Errores CartaPorte (800-899)
        fiscalErrorCodes.put("FISCAL-ERR-800", "El comprobante debe contener el complemento CartaPorte");
        fiscalErrorCodes.put("FISCAL-ERR-801", "Tipo de comprobante no válido para CartaPorte");
        fiscalErrorCodes.put("FISCAL-ERR-802", "Datos de CartaPorte no pueden ser nulos");
        fiscalErrorCodes.put("FISCAL-ERR-803", "CartaPorte debe tener versión especificada");
        fiscalErrorCodes.put("FISCAL-ERR-804", "CartaPorte debe tener al menos una ubicación");
        fiscalErrorCodes.put("FISCAL-ERR-805", "CartaPorte debe tener al menos ubicación de origen y destino");
        fiscalErrorCodes.put("FISCAL-ERR-806", "CartaPorte debe especificar las mercancías");
        fiscalErrorCodes.put("FISCAL-ERR-807", "Debe especificar el número total de mercancías");
        fiscalErrorCodes.put("FISCAL-ERR-808", "Debe especificar el peso bruto total");
        fiscalErrorCodes.put("FISCAL-ERR-809", "Debe especificar al menos una mercancía");

        // Errores Factura/CFDI (900-999)
        fiscalErrorCodes.put("FISCAL-ERR-900", "Factura debe tener al menos un concepto");
        fiscalErrorCodes.put("FISCAL-ERR-901", "Factura debe tener un total válido");
        fiscalErrorCodes.put("FISCAL-ERR-902", "Factura debe tener RFC del receptor");
        fiscalErrorCodes.put("FISCAL-ERR-903", "Nota de Crédito debe tener al menos un concepto");
        fiscalErrorCodes.put("FISCAL-ERR-904", "Nota de Crédito debe tener un total válido");

        // Errores Transformación, BD, PAC, Seguridad
        fiscalErrorCodes.put("FISCAL-ERR-1000", "Error en la transformación del XML");
        fiscalErrorCodes.put("FISCAL-ERR-1100", "Error guardando complemento de pago en base de datos");
        fiscalErrorCodes.put("FISCAL-ERR-1101", "Error consultando base de datos");
        fiscalErrorCodes.put("FISCAL-ERR-1200", "No se encontró el PAC configurado");
        fiscalErrorCodes.put("FISCAL-ERR-1201", "Error conectando con el servicio PAC");
        fiscalErrorCodes.put("FISCAL-ERR-1300", "La clave pública JWT no es válida");

        // Errores NC (1400-1499)
        fiscalErrorCodes.put("FISCAL-ERR-1400", "El archivo XML de NC es obligatorio");
        fiscalErrorCodes.put("FISCAL-ERR-1401", "No se pudo parsear el XML de la Nota de Crédito");
        fiscalErrorCodes.put("FISCAL-ERR-1402", "No se encontró el TimbreFiscalDigital en NC");
        fiscalErrorCodes.put("FISCAL-ERR-1403", "El XML no es una Nota de Crédito");
        fiscalErrorCodes.put("FISCAL-ERR-1404", "El XML de NC no contiene sección CfdiRelacionados");
        fiscalErrorCodes.put("FISCAL-ERR-1405", "El UUID de la factura no está relacionado en NC");
        fiscalErrorCodes.put("FISCAL-ERR-1499", "Error interno al validar XML de NC");

        // Errores genéricos (9000-9999)
        fiscalErrorCodes.put("FISCAL-ERR-9000", "Error inesperado en el sistema");
        fiscalErrorCodes.put("FISCAL-ERR-9001", "Error registrando complemento de pago");
        fiscalErrorCodes.put("FISCAL-ERR-9002", "Error parseando XML");
        fiscalErrorCodes.put("FISCAL-ERR-9003", "Error convirtiendo XML String a Document");
        fiscalErrorCodes.put("FISCAL-ERR-9004", "No se encontró el nodo Complemento en el XML");
        fiscalErrorCodes.put("FISCAL-ERR-9005", "No se encontró el nodo Pagos en el Complemento");
        fiscalErrorCodes.put("FISCAL-ERR-9007", "Error en el procesamiento del XML");
        fiscalErrorCodes.put("FISCAL-ERR-9009", "Tipo de documento XML no soportado");
        fiscalErrorCodes.put("FISCAL-ERR-9010", "Tipo de comprobante no soportado");

        // ===== Errores de Negocio BUSxxxx =====
        // RFC Receptor (2000-2099)
        fiscalErrorCodes.put("BUS2002", "El RFC del receptor %s no acepta documentos fiscales");
        fiscalErrorCodes.put("BUS2003", "El RFC del receptor no está activo en el período de vigencia especificado");
        fiscalErrorCodes.put("BUS2004", "El RFC del receptor no se encuentra registrado en el sistema");

        // Addenda (2100-2199)
        fiscalErrorCodes.put("BUS2001", "La addenda de la factura no cumple con la estructura requerida");
        fiscalErrorCodes.put("BUS2101", "La addenda debe contener el nodo Addenda_Sodimac");
        fiscalErrorCodes.put("BUS2102", "La addenda debe contener el nodo Addenda_Sodimac_CartaPorte");
        fiscalErrorCodes.put("BUS2103", "El campo RFC en la addenda es obligatorio");
        fiscalErrorCodes.put("BUS2104", "El campo UUID en la addenda es obligatorio");
        fiscalErrorCodes.put("BUS2105", "El campo Folio en la addenda es obligatorio");
        fiscalErrorCodes.put("BUS2106", "El campo NoOC en la addenda es obligatorio");
        fiscalErrorCodes.put("BUS2107", "El campo Proveedor en la addenda es obligatorio");
        fiscalErrorCodes.put("BUS2108", "El formato del RFC en la addenda no es válido");
        fiscalErrorCodes.put("BUS2109", "El UUID en la addenda no coincide con el UUID del TimbreFiscalDigital");

        // Versión CFDI (2200-2299)
        fiscalErrorCodes.put("BUS2201", "La versión %s del documento no se encuentra configurada");
        fiscalErrorCodes.put("BUS2202", "La versión %s del documento no está vigente");
        fiscalErrorCodes.put("BUS2203", "El documento debe ser versión CFDI 4.0");

        // Tipo de Documento (2300-2399)
        fiscalErrorCodes.put("BUS2301", "El tipo de documento debe ser 'I' (Factura) o 'E' (Nota de Crédito)");
        fiscalErrorCodes.put("BUS2302", "El tipo de comprobante no está permitido para este proceso");
        fiscalErrorCodes.put("BUS2303", "Las Notas de Crédito deben incluir CFDIs relacionados con tipo '01'");

        // Validación SAT (2400-2499)
        fiscalErrorCodes.put("BUS2401", "El documento fiscal no se encuentra vigente en el SAT");
        fiscalErrorCodes.put("BUS2402", "El documento fiscal se encuentra cancelado en el SAT");
        fiscalErrorCodes.put("BUS2403", "El UUID no se encuentra registrado en el SAT");
        fiscalErrorCodes.put("BUS2404", "El sello digital del documento no es válido según el SAT");
        fiscalErrorCodes.put("BUS2405", "No fue posible validar el estatus del documento en el SAT");

        // Emisor (2500-2599)
        fiscalErrorCodes.put("BUS2501", "El RFC del emisor no se encuentra registrado como proveedor autorizado");
        fiscalErrorCodes.put("BUS2502", "El emisor se encuentra inactivo en el sistema");
        fiscalErrorCodes.put("BUS2503", "El RFC del emisor no es válido");

        // Duplicidad (2600-2699)
        fiscalErrorCodes.put("BUS2601", "El documento con UUID ya se encuentra registrado en el sistema");
        fiscalErrorCodes.put("BUS2602", "El documento con Serie y Folio ya se encuentra registrado");
        fiscalErrorCodes.put("BUS2603", "El archivo ya fue procesado anteriormente");

        // Validación Contenido (2700-2799)
        fiscalErrorCodes.put("BUS2701", "El documento debe contener al menos un concepto");
        fiscalErrorCodes.put("BUS2702", "El total del documento no puede ser cero");
        fiscalErrorCodes.put("BUS2703", "Los montos de los conceptos no coinciden con el total");
        fiscalErrorCodes.put("BUS2704", "Los impuestos calculados no coinciden con los declarados");
        fiscalErrorCodes.put("BUS2705", "El documento contiene impuestos no permitidos por Sodimac");

        // CFDIs Relacionados NC (2800-2899)
        fiscalErrorCodes.put("BUS2801", "La Nota de Crédito debe incluir al menos un CFDI relacionado");
        fiscalErrorCodes.put("BUS2802", "La Factura relacionada no se encuentra registrada");
        fiscalErrorCodes.put("BUS2803", "El CFDI relacionado no es una Factura (tipo I)");
        fiscalErrorCodes.put("BUS2804", "El tipo de relación no es válido para Notas de Crédito");

        // Actualización (3100-3399)
        fiscalErrorCodes.put("BUS3101", "El documento con UUID no se encuentra registrado");
        fiscalErrorCodes.put("BUS3102", "El documento no pertenece al proveedor especificado");
        fiscalErrorCodes.put("BUS3103", "La addenda del documento no se encuentra registrada");
        fiscalErrorCodes.put("BUS3201", "El estatus no es válido para el tipo de documento");
        fiscalErrorCodes.put("BUS3202", "El estatus no existe en el catálogo");
        fiscalErrorCodes.put("BUS3203", "La transición de estatus no está permitida");
        fiscalErrorCodes.put("BUS3204", "No se puede actualizar documento en estatus final");
        fiscalErrorCodes.put("BUS3205", "El estatus del documento ya es el solicitado");
        fiscalErrorCodes.put("BUS3301", "El usuario no tiene permisos para actualizar documentos fiscales");
        fiscalErrorCodes.put("BUS3302", "Solo el proveedor propietario puede actualizar este documento");
        fiscalErrorCodes.put("BUS3303", "El documento está en proceso automático");

        // ===== FiscalSuccessCode =====
        fiscalSuccessCodes.put("BUS1001", "Factura registrada exitosamente");
        fiscalSuccessCodes.put("BUS1002", "Factura registrada exitosamente - Pendiente de Addenda");
        fiscalSuccessCodes.put("BUS1003", "Nota de Crédito registrada exitosamente");
        fiscalSuccessCodes.put("BUS1004", "Nota de Crédito registrada exitosamente - Pendiente de Addenda");
        fiscalSuccessCodes.put("BUS1005", "Complemento de Pago registrado exitosamente");
        fiscalSuccessCodes.put("BUS3001", "Factura actualizada exitosamente");
        fiscalSuccessCodes.put("BUS3002", "Factura actualizada exitosamente - Addenda actualizada");
        fiscalSuccessCodes.put("BUS3003", "Nota de Crédito actualizada exitosamente");
        fiscalSuccessCodes.put("BUS3004", "Nota de Crédito actualizada exitosamente - Addenda actualizada");

        // ===== FiscalWarningCode =====
        fiscalWarningCodes.put("FISCAL-WARN-2000", "El total calculado no coincide con el total declarado");
        fiscalWarningCodes.put("FISCAL-WARN-2001", "Tipo de cambio no especificado, usando valor por defecto");
        fiscalWarningCodes.put("FISCAL-WARN-2002", "Moneda no especificada, usando MXN por defecto");
        fiscalWarningCodes.put("FISCAL-WARN-2100", "El pago es parcial, saldo pendiente");
        fiscalWarningCodes.put("FISCAL-WARN-2101", "Número de operación no especificado");
        fiscalWarningCodes.put("FISCAL-WARN-2102", "Información bancaria incompleta");
        fiscalWarningCodes.put("FISCAL-WARN-2200", "Documento relacionado con saldo cero");
        fiscalWarningCodes.put("FISCAL-WARN-2201", "El monto pagado excede el saldo del documento");
        fiscalWarningCodes.put("FISCAL-WARN-2300", "Datos opcionales de CartaPorte no especificados");
        fiscalWarningCodes.put("FISCAL-WARN-2301", "Figuras de transporte no especificadas");
        fiscalWarningCodes.put("FISCAL-WARN-2400", "Addenda sin contenido");
        fiscalWarningCodes.put("FISCAL-WARN-2401", "Campos opcionales de addenda vacíos");
        fiscalWarningCodes.put("FISCAL-WARN-2500", "Emisor no existía, se creó nuevo registro");
        fiscalWarningCodes.put("FISCAL-WARN-2501", "Receptor no existía, se creó nuevo registro");
        fiscalWarningCodes.put("FISCAL-WARN-2502", "Archivo con nombre similar ya fue procesado");
        fiscalWarningCodes.put("FISCAL-WARN-2600", "Validación SAT tardó más de lo esperado");
        fiscalWarningCodes.put("FISCAL-WARN-2601", "Respuesta SAT incompleta pero válida");
        fiscalWarningCodes.put("FISCAL-WARN-2900", "Versión del documento será obsoleta próximamente");
        fiscalWarningCodes.put("FISCAL-WARN-2901", "Datos opcionales no especificados");
        // Warning codes de recepción (estos son BUS3xxx pero son warnings)
        fiscalWarningCodes.put("WRN-BUS3001", "Documento registrado sin número de recepción");
        fiscalWarningCodes.put("WRN-BUS3002", "La recepción no se encuentra disponible");
        fiscalWarningCodes.put("WRN-BUS3003", "La recepción ya se encuentra vinculada a otro documento");
    }

    static void cargarDatosCatalogosApi() {
        // CatMsgExitoso (RES) - header_id=5
        catalogoMsgExitoso.put("RES001", "El registro del proveedor {0} se realizó exitosamente.");
        catalogoMsgExitoso.put("RES002", "El reporte se ha descargado exitosamente.");
        catalogoMsgExitoso.put("RES003", "El registro de la guía de embarque se realizó exitosamente.");

        // CatMsgNegocio (BUS) - header_id=7
        catalogoMsgNegocio.put("BUS001", "La addenda de la factura no cumple con la estructura requerida.");
        catalogoMsgNegocio.put("BUS002", "El RFC del receptor {0} no acepta documentos fiscales.");
        catalogoMsgNegocio.put("BUS003", "La guía carta porte no contiene un archivo xml y csv asociado.");
        catalogoMsgNegocio.put("BUS004", "No fue posible registrar la guía carta porte.");
        catalogoMsgNegocio.put("BUS005", "La guía de embarque se encuentra previamente registrada.");
        catalogoMsgNegocio.put("BUS006", "Las facturas no corresponden al complemento que desea publicar.");
        catalogoMsgNegocio.put("BUS007", "Las notas de crédito no corresponden al complemento que desea publicar.");

        // CatMsgAdvertencia (WRN) - header_id=11
        catalogoMsgAdvertencia.put("WRN001", "La fecha inicio no puede ser superior a la fecha final.");
        catalogoMsgAdvertencia.put("WRN002", "Se requiere capturar por lo menos un filtro de búsqueda.");
        catalogoMsgAdvertencia.put("WRN003", "No existe información con los filtros de búsqueda capturados.");
        catalogoMsgAdvertencia.put("WRN004", "No es posible publicar el complemento de pago.");
        catalogoMsgAdvertencia.put("WRN005", "Se requiere publicar el complemento de pago en formato XML.");
        catalogoMsgAdvertencia.put("WRN006", "El periodo de búsqueda no puede ser superior a 6 meses.");
        catalogoMsgAdvertencia.put("WRN007", "La fecha final no puede ser menor a la fecha inicio.");
        catalogoMsgAdvertencia.put("WRN008", "Se requiere publicar factura en formato XML.");

        // CatMsgInformativo (INF) - header_id=10
        catalogoMsgInformativo.put("INF001", "No existe información con los criterios establecidos.");
    }

    static String analizarCoincidencias() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(100)).append("\n");
        sb.append(" 1. ANÁLISIS DE COINCIDENCIAS ENTRE fiscal-api Y catalogos-api\n");
        sb.append("=".repeat(100)).append("\n\n");

        // Buscar coincidencias BUS001 (catalogos) vs BUS2001 (fiscal-api)
        sb.append(">>> Coincidencias encontradas (mensaje similar/idéntico):\n\n");

        // BUS001 vs BUS2001
        sb.append("   CATÁLOGO BUS001 : ").append(catalogoMsgNegocio.get("BUS001")).append("\n");
        sb.append("   FISCAL   BUS2001: ").append(fiscalErrorCodes.get("BUS2001")).append("\n");
        sb.append("   RESULTADO: ✅ COINCIDE (mismo concepto)\n\n");

        // BUS002 vs BUS2002
        sb.append("   CATÁLOGO BUS002 : ").append(catalogoMsgNegocio.get("BUS002")).append("\n");
        sb.append("   FISCAL   BUS2002: ").append(fiscalErrorCodes.get("BUS2002")).append("\n");
        sb.append("   RESULTADO: ✅ COINCIDE (mismo concepto)\n\n");

        sb.append(">>> Observación importante:\n");
        sb.append("   Los códigos en catalogos-api usan formato BUS001, BUS002...\n");
        sb.append("   Los códigos en fiscal-api usan formato BUS2001, BUS2002...\n");
        sb.append("   RECOMENDACIÓN: Unificar nomenclatura para evitar confusión.\n\n");

        return sb.toString();
    }

    static String proponerDatosParaCatalogosPendientes() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(100)).append("\n");
        sb.append(" 2. PROPUESTA PARA LLENAR CATÁLOGOS PENDIENTES (status=-1/amarillo)\n");
        sb.append("=".repeat(100)).append("\n\n");

        // CatMsgErrorSistema (ERR)
        sb.append(">>> CatMsgErrorSistema (header_id=6, prefijo=ERR)\n");
        sb.append("    ESTADO ACTUAL: Sin datos (status=-1)\n");
        sb.append("    PROPUESTA: Agregar códigos FISCAL-ERR-xxx de fiscal-api\n\n");
        sb.append("    Ejemplos de códigos a importar:\n");
        int count = 0;
        for (Map.Entry<String, String> e : fiscalErrorCodes.entrySet()) {
            if (e.getKey().startsWith("FISCAL-ERR") && count < 10) {
                sb.append(String.format("    - %-20s: %s\n", e.getKey(), e.getValue()));
                count++;
            }
        }
        sb.append("    ... y ").append(fiscalErrorCodes.entrySet().stream()
                .filter(e -> e.getKey().startsWith("FISCAL-ERR")).count() - 10)
                .append(" más\n\n");

        // CatMsgNegocio (BUS) - Ya tiene datos pero faltan muchos
        sb.append(">>> CatMsgNegocio (header_id=7, prefijo=BUS)\n");
        sb.append("    ESTADO ACTUAL: 7 registros (BUS001-BUS007)\n");
        sb.append("    PROPUESTA: Agregar códigos BUSxxxx de fiscal-api\n\n");
        sb.append("    Códigos de fiscal-api que NO están en catalogos-api:\n");
        count = 0;
        for (Map.Entry<String, String> e : fiscalErrorCodes.entrySet()) {
            if (e.getKey().startsWith("BUS") && count < 15) {
                sb.append(String.format("    - %-10s: %s\n", e.getKey(), truncate(e.getValue(), 70)));
                count++;
            }
        }
        sb.append("    ... y más\n\n");

        // Mensajes de éxito
        sb.append(">>> CatMsgExitoso (header_id=5, prefijo=RES)\n");
        sb.append("    ESTADO ACTUAL: 3 registros (RES001-RES003)\n");
        sb.append("    PROPUESTA: Agregar códigos BUS1xxx (éxito) de fiscal-api\n\n");
        sb.append("    Códigos de éxito en fiscal-api:\n");
        for (Map.Entry<String, String> e : fiscalSuccessCodes.entrySet()) {
            sb.append(String.format("    - %-10s: %s\n", e.getKey(), e.getValue()));
        }
        sb.append("\n");

        // Advertencias
        sb.append(">>> CatMsgAdvertencia (header_id=11, prefijo=WRN)\n");
        sb.append("    ESTADO ACTUAL: 8 registros (WRN001-WRN008)\n");
        sb.append("    PROPUESTA: Agregar códigos FISCAL-WARN-xxxx de fiscal-api\n\n");
        sb.append("    Códigos de advertencia en fiscal-api:\n");
        count = 0;
        for (Map.Entry<String, String> e : fiscalWarningCodes.entrySet()) {
            if (e.getKey().startsWith("FISCAL-WARN") && count < 10) {
                sb.append(String.format("    - %-20s: %s\n", e.getKey(), truncate(e.getValue(), 60)));
                count++;
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    static String resumenCodigosFiscalApi() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(100)).append("\n");
        sb.append(" 3. RESUMEN DE CÓDIGOS EN FISCAL-API\n");
        sb.append("=".repeat(100)).append("\n\n");

        sb.append(">>> FiscalErrorCode:\n");
        sb.append("    - Errores técnicos (FISCAL-ERR-xxx): ")
          .append(fiscalErrorCodes.entrySet().stream().filter(e -> e.getKey().startsWith("FISCAL-ERR")).count())
          .append(" códigos\n");
        sb.append("    - Errores de negocio (BUSxxxx): ")
          .append(fiscalErrorCodes.entrySet().stream().filter(e -> e.getKey().startsWith("BUS")).count())
          .append(" códigos\n\n");

        sb.append(">>> FiscalSuccessCode:\n");
        sb.append("    - Mensajes de éxito (BUS1xxx, BUS3xxx): ")
          .append(fiscalSuccessCodes.size())
          .append(" códigos\n\n");

        sb.append(">>> FiscalWarningCode:\n");
        sb.append("    - Advertencias (FISCAL-WARN-xxxx): ")
          .append(fiscalWarningCodes.entrySet().stream().filter(e -> e.getKey().startsWith("FISCAL-WARN")).count())
          .append(" códigos\n");
        sb.append("    - Advertencias negocio (WRN-BUSxxxx): ")
          .append(fiscalWarningCodes.entrySet().stream().filter(e -> e.getKey().startsWith("WRN-BUS")).count())
          .append(" códigos\n\n");

        sb.append(">>> TOTAL de códigos en fiscal-api: ")
          .append(fiscalErrorCodes.size() + fiscalSuccessCodes.size() + fiscalWarningCodes.size())
          .append("\n\n");

        return sb.toString();
    }

    static String generarSqlPropuesto() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(100)).append("\n");
        sb.append(" 4. SQL PROPUESTO PARA COMPLETAR CATÁLOGOS\n");
        sb.append("=".repeat(100)).append("\n\n");

        sb.append("-- ============================================================================\n");
        sb.append("-- NOTA: Este SQL debe ser revisado y ajustado antes de ejecutar\n");
        sb.append("-- Los dict_id deben calcularse para no duplicar\n");
        sb.append("-- ============================================================================\n\n");

        // Ejemplo para CatMsgErrorSistema
        sb.append("-- CatMsgErrorSistema (header_id=6, prefijo=ERR)\n");
        sb.append("-- Primero insertar en dictionary_lang, luego en catalog_detail\n\n");

        int dictId = 2000; // Empezamos desde un rango alto para no colisionar
        int sortOrder = 1;

        sb.append("-- Diccionario para errores técnicos\n");
        for (Map.Entry<String, String> e : fiscalErrorCodes.entrySet()) {
            if (e.getKey().startsWith("FISCAL-ERR") && sortOrder <= 10) {
                String key = "ERR" + String.format("%03d", sortOrder);
                sb.append(String.format("INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (%d, 1, '%s');\n",
                        dictId, escapeSql(e.getValue())));
                sb.append(String.format("-- catalog_detail: key='%s', dict_id=%d, código_original='%s'\n\n",
                        key, dictId, e.getKey()));
                dictId++;
                sortOrder++;
            }
        }

        sb.append("\n-- (Continuar con el resto de códigos...)\n\n");

        return sb.toString();
    }

    static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max-3) + "..." : s;
    }

    static String escapeSql(String s) {
        return s.replace("'", "''");
    }
}
