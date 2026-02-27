package com.sodimac.fiscal.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sodimac.fiscal.api.model.dto.invoicexml.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convertidor de archivos XML de facturas CFDI a formato JSON.
 *
 * Esta clase proporciona funcionalidades para convertir archivos XML de facturas
 * electrónicas mexicanas (CFDI) a formato JSON, así como extraer información
 * específica estructurada de las facturas.
 *
 * Funcionalidades principales:
 * - Conversión directa XML a JSON con formato pretty-print
 * - Extracción de datos específicos a DTOs estructurados
 * - Soporte para archivos desde resources y uploads dinámicos
 * - Procesamiento de complementos como CartaPorte y TimbreFiscalDigital
 *
 * @author g_dco018
 * @version 1.0
 * @since 2024
 */
@Component
public class XmlToJsonConverter {

    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;

    /**
     * Constructor que inicializa los mappers necesarios para la conversión.
     *
     * @author g_dco018
     */
    public XmlToJsonConverter() {
        this.xmlMapper = new XmlMapper();
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Convierte un archivo XML a JSON
     * @param xmlFilePath Ruta del archivo XML
     * @return String con el contenido JSON
     * @throws IOException Si hay error al leer el archivo
     */
    public String convertXmlFileToJson(String xmlFilePath) throws IOException {
        try (InputStream inputStream = new ClassPathResource(xmlFilePath).getInputStream()) {
            JsonNode jsonNode = xmlMapper.readTree(inputStream);
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        }
    }

    /**
     * Convierte un archivo XML cargado dinámicamente a formato JSON.
     *
     * Este método permite procesar archivos XML de facturas subidos por el usuario
     * mediante formularios multipart, validando el contenido y convirtiendo
     * directamente a JSON con formato legible.
     *
     * @param xmlFile Archivo XML cargado vía upload (MultipartFile)
     * @return String con el contenido JSON formateado con pretty-print
     * @throws IOException Si hay error al leer el archivo o si está vacío
     * @author g_dco018
     */
    public String convertUploadedXmlToJson(MultipartFile xmlFile) throws IOException {
        if (xmlFile.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        try (InputStream inputStream = xmlFile.getInputStream()) {
            JsonNode jsonNode = xmlMapper.readTree(inputStream);
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        }
    }

    /**
     * Lee todos los archivos XML de facturas desde resources/invoice y los convierte a JSON
     * @return Lista de objetos JSON como String
     */
    public List<String> convertAllInvoiceXmlsToJson() {
        List<String> jsonResults = new ArrayList<>();

        try {
            // Archivos XML de facturas conocidos
            String[] invoiceFiles = {
                "invoice/FacturaIngreso.xml",
                "invoice/FacturaIngreso2.xml",
                "invoice/FacturaIngreso3.xml"
            };

            for (String filePath : invoiceFiles) {
                try {
                    String json = convertXmlFileToJson(filePath);
                    jsonResults.add(json);
                } catch (IOException e) {
                    System.err.println("Error procesando archivo " + filePath + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error general al procesar archivos XML: " + e.getMessage());
        }

        return jsonResults;
    }

    /**
     * Extrae información específica de la factura desde el JSON
     * @param xmlFilePath Ruta del archivo XML
     * @return InvoiceXmlDto con datos específicos extraídos
     */
    public InvoiceXmlDto extractInvoiceInfo(String xmlFilePath) throws IOException {
        try (InputStream inputStream = new ClassPathResource(xmlFilePath).getInputStream()) {
            JsonNode jsonNode = xmlMapper.readTree(inputStream);

            InvoiceXmlDto info = new InvoiceXmlDto();

            // Extraer datos del comprobante principal
            if (jsonNode.has("Version")) {
                info.setVersion(jsonNode.get("Version").asText());
            }
            if (jsonNode.has("Serie")) {
                info.setSerie(jsonNode.get("Serie").asText());
            }
            if (jsonNode.has("Folio")) {
                info.setFolio(jsonNode.get("Folio").asText());
            }
            if (jsonNode.has("Fecha")) {
                info.setFecha(jsonNode.get("Fecha").asText());
            }
            if (jsonNode.has("SubTotal")) {
                info.setSubTotal(jsonNode.get("SubTotal").asText());
            }
            if (jsonNode.has("Total")) {
                info.setTotal(jsonNode.get("Total").asText());
            }
            if (jsonNode.has("TipoDeComprobante")) {
                info.setTipoDeComprobante(jsonNode.get("TipoDeComprobante").asText());
            }
            if (jsonNode.has("MetodoPago")) {
                info.setMetodoPago(jsonNode.get("MetodoPago").asText());
            }
            if (jsonNode.has("FormaPago")) {
                info.setFormaPago(jsonNode.get("FormaPago").asText());
            }
            if (jsonNode.has("CondicionesDePago")) {
                info.setCondicionesDePago(jsonNode.get("CondicionesDePago").asText());
            }
            if (jsonNode.has("Moneda")) {
                info.setMoneda(jsonNode.get("Moneda").asText());
            }
            if (jsonNode.has("TipoCambio")) {
                info.setTipoCambio(jsonNode.get("TipoCambio").asText());
            }
            if (jsonNode.has("NoCertificado")) {
                info.setNoCertificado(jsonNode.get("NoCertificado").asText());
            }
            if (jsonNode.has("Certificado")) {
                info.setCertificado(jsonNode.get("Certificado").asText());
            }
            if (jsonNode.has("Sello")) {
                info.setSello(jsonNode.get("Sello").asText());
            }
            if (jsonNode.has("Exportacion")) {
                info.setExportacion(jsonNode.get("Exportacion").asText());
            }
            if (jsonNode.has("Descuento")) {
                info.setDescuento(jsonNode.get("Descuento").asText());
            }
            if (jsonNode.has("LugarExpedicion")) {
                info.setLugarExpedicion(jsonNode.get("LugarExpedicion").asText());
            }

            // Extraer datos del emisor
            if (jsonNode.has("Emisor")) {
                JsonNode emisorNode = jsonNode.get("Emisor");
                EmisorDto emisor = new EmisorDto();
                if (emisorNode.has("Rfc")) {
                    emisor.setRfc(emisorNode.get("Rfc").asText());
                }
                if (emisorNode.has("Nombre")) {
                    emisor.setNombre(emisorNode.get("Nombre").asText());
                }
                if (emisorNode.has("RegimenFiscal")) {
                    emisor.setRegimenFiscal(emisorNode.get("RegimenFiscal").asText());
                }
                info.setEmisor(emisor);
            }

            // Extraer datos del receptor
            if (jsonNode.has("Receptor")) {
                JsonNode receptorNode = jsonNode.get("Receptor");
                ReceptorDto receptor = new ReceptorDto();
                if (receptorNode.has("Rfc")) {
                    receptor.setRfc(receptorNode.get("Rfc").asText());
                }
                if (receptorNode.has("Nombre")) {
                    receptor.setNombre(receptorNode.get("Nombre").asText());
                }
                if (receptorNode.has("DomicilioFiscalReceptor")) {
                    receptor.setDomicilioFiscalReceptor(receptorNode.get("DomicilioFiscalReceptor").asText());
                }
                if (receptorNode.has("RegimenFiscalReceptor")) {
                    receptor.setRegimenFiscalReceptor(receptorNode.get("RegimenFiscalReceptor").asText());
                }
                if (receptorNode.has("UsoCFDI")) {
                    receptor.setUsoCFDI(receptorNode.get("UsoCFDI").asText());
                }
                info.setReceptor(receptor);
            }

            // Extraer UUID del timbre fiscal - ya no se guarda en InvoiceXmlDto directamente
            // El UUID fiscal está disponible en invoiceDto.getTimbreFiscalDigital().getUuid()

            // Extraer conceptos
            info.setConceptos(extractConceptos(jsonNode));

            // Extraer adendas
            info.setAddendas(extractAddendas(jsonNode));

            // Extraer Complemento (CartaPorte y TimbreFiscalDigital)
            ComplementoDto complemento = new ComplementoDto();
            complemento.setCartaPorte(extractCartaPorte(jsonNode));
            complemento.setTimbreFiscalDigital(extractTimbreFiscalDigital(jsonNode));
            info.setComplemento(complemento);

            // Extraer impuestos consolidados
            info.setImpuestosFactura(extractImpuestosFactura(jsonNode));

            return info;
        }
    }

    /**
     * Extrae los conceptos de la factura
     * @param jsonNode Nodo JSON del comprobante
     * @return Lista de ConceptoDto
     */
    private List<ConceptoDto> extractConceptos(JsonNode jsonNode) {
        List<ConceptoDto> conceptos = new ArrayList<>();

        if (jsonNode.has("Conceptos") && jsonNode.get("Conceptos").has("Concepto")) {
            JsonNode conceptosNode = jsonNode.get("Conceptos").get("Concepto");

            // Si es un array de conceptos
            if (conceptosNode.isArray()) {
                for (JsonNode conceptoNode : conceptosNode) {
                    conceptos.add(extractConcepto(conceptoNode));
                }
            } else {
                // Si es un solo concepto
                conceptos.add(extractConcepto(conceptosNode));
            }
        }

        return conceptos;
    }

    /**
     * Extrae un concepto individual
     * @param conceptoNode Nodo JSON del concepto
     * @return ConceptoDto
     */
    private ConceptoDto extractConcepto(JsonNode conceptoNode) {
        ConceptoDto concepto = new ConceptoDto();

        if (conceptoNode.has("ClaveProdServ")) {
            concepto.setClaveProdServ(conceptoNode.get("ClaveProdServ").asText());
        }
        if (conceptoNode.has("NoIdentificacion")) {
            concepto.setNoIdentificacion(conceptoNode.get("NoIdentificacion").asText());
        }
        if (conceptoNode.has("Cantidad")) {
            concepto.setCantidad(conceptoNode.get("Cantidad").asText());
        }
        if (conceptoNode.has("ClaveUnidad")) {
            concepto.setClaveUnidad(conceptoNode.get("ClaveUnidad").asText());
        }
        if (conceptoNode.has("Unidad")) {
            concepto.setUnidad(conceptoNode.get("Unidad").asText());
        }
        if (conceptoNode.has("Descripcion")) {
            concepto.setDescripcion(conceptoNode.get("Descripcion").asText());
        }
        if (conceptoNode.has("ValorUnitario")) {
            concepto.setValorUnitario(conceptoNode.get("ValorUnitario").asText());
        }
        if (conceptoNode.has("Importe")) {
            concepto.setImporte(conceptoNode.get("Importe").asText());
        }
        if (conceptoNode.has("ObjetoImp")) {
            concepto.setObjetoImp(conceptoNode.get("ObjetoImp").asText());
        }

        // Extraer impuestos
        if (conceptoNode.has("Impuestos")) {
            JsonNode impuestosNode = conceptoNode.get("Impuestos");
            concepto.setTraslados(extractTraslados(impuestosNode));
            concepto.setRetenciones(extractRetenciones(impuestosNode));
        }

        return concepto;
    }

    /**
     * Extrae los traslados de impuestos
     * @param impuestosNode Nodo JSON de impuestos
     * @return Lista de TrasladoDto
     */
    private List<TrasladoDto> extractTraslados(JsonNode impuestosNode) {
        List<TrasladoDto> traslados = new ArrayList<>();

        if (impuestosNode.has("Traslados") && impuestosNode.get("Traslados").has("Traslado")) {
            JsonNode trasladosNode = impuestosNode.get("Traslados").get("Traslado");

            if (trasladosNode.isArray()) {
                for (JsonNode trasladoNode : trasladosNode) {
                    traslados.add(extractTraslado(trasladoNode));
                }
            } else {
                traslados.add(extractTraslado(trasladosNode));
            }
        }

        return traslados;
    }

    /**
     * Extrae un traslado individual
     * @param trasladoNode Nodo JSON del traslado
     * @return TrasladoDto
     */
    private TrasladoDto extractTraslado(JsonNode trasladoNode) {
        TrasladoDto traslado = new TrasladoDto();

        if (trasladoNode.has("Base")) {
            traslado.setBase(trasladoNode.get("Base").asText());
        }
        if (trasladoNode.has("Impuesto")) {
            traslado.setImpuesto(trasladoNode.get("Impuesto").asText());
        }
        if (trasladoNode.has("TipoFactor")) {
            traslado.setTipoFactor(trasladoNode.get("TipoFactor").asText());
        }
        if (trasladoNode.has("TasaOCuota")) {
            traslado.setTasaOCuota(trasladoNode.get("TasaOCuota").asText());
        }
        if (trasladoNode.has("Importe")) {
            traslado.setImporte(trasladoNode.get("Importe").asText());
        }

        return traslado;
    }

    /**
     * Extrae las retenciones de impuestos
     * @param impuestosNode Nodo JSON de impuestos
     * @return Lista de RetencionDto
     */
    private List<RetencionDto> extractRetenciones(JsonNode impuestosNode) {
        List<RetencionDto> retenciones = new ArrayList<>();

        if (impuestosNode.has("Retenciones") && impuestosNode.get("Retenciones").has("Retencion")) {
            JsonNode retencionesNode = impuestosNode.get("Retenciones").get("Retencion");

            if (retencionesNode.isArray()) {
                for (JsonNode retencionNode : retencionesNode) {
                    retenciones.add(extractRetencion(retencionNode));
                }
            } else {
                retenciones.add(extractRetencion(retencionesNode));
            }
        }

        return retenciones;
    }

    /**
     * Extrae una retención individual
     * @param retencionNode Nodo JSON de la retención
     * @return RetencionDto
     */
    private RetencionDto extractRetencion(JsonNode retencionNode) {
        RetencionDto retencion = new RetencionDto();

        if (retencionNode.has("Base")) {
            retencion.setBase(retencionNode.get("Base").asText());
        }
        if (retencionNode.has("Impuesto")) {
            retencion.setImpuesto(retencionNode.get("Impuesto").asText());
        }
        if (retencionNode.has("TipoFactor")) {
            retencion.setTipoFactor(retencionNode.get("TipoFactor").asText());
        }
        if (retencionNode.has("TasaOCuota")) {
            retencion.setTasaOCuota(retencionNode.get("TasaOCuota").asText());
        }
        if (retencionNode.has("Importe")) {
            retencion.setImporte(retencionNode.get("Importe").asText());
        }

        return retencion;
    }

    /**
     * Extrae las adendas de la factura
     * @param jsonNode Nodo JSON del comprobante
     * @return Lista de AddendaDto
     */
    private List<AddendaDto> extractAddendas(JsonNode jsonNode) {
        List<AddendaDto> addendas = new ArrayList<>();

        if (jsonNode.has("Addenda")) {
            JsonNode addendaNode = jsonNode.get("Addenda");

            // Extraer BOVEDAFISCAL si existe
            if (addendaNode.has("BOVEDAFISCAL")) {
                addendas.add(extractBovedaFiscal(addendaNode.get("BOVEDAFISCAL")));
            }

            // Extraer AddendaK si existe
            if (addendaNode.has("AddendaK")) {
                addendas.add(extractAddendaK(addendaNode.get("AddendaK")));
            }

            // Extraer Addenda_Sodimac_Detecno si existe
            if (addendaNode.has("Addenda_Sodimac_Detecno")) {
                addendas.add(extractAddendaSodimac(addendaNode.get("Addenda_Sodimac_Detecno")));
            }
        }

        return addendas;
    }

    /**
     * Extrae addenda BOVEDAFISCAL
     * @param bovedaNode Nodo JSON de BOVEDAFISCAL
     * @return AddendaDto
     */
    private AddendaDto extractBovedaFiscal(JsonNode bovedaNode) {
        AddendaDto addenda = new AddendaDto();
        addenda.setTipoAddenda("BOVEDAFISCAL");
        addenda.setNamespace("http://kontender.mx/namespace/boveda");

        Map<String, Object> contenido = new HashMap<>();

        if (bovedaNode.has("ImporteLetra") && bovedaNode.get("ImporteLetra").has("importe")) {
            addenda.setImporteLetra(bovedaNode.get("ImporteLetra").get("importe").asText());
            contenido.put("importeLetra", addenda.getImporteLetra());
        }

        if (bovedaNode.has("BovedaFiscal")) {
            JsonNode bovedaFiscalNode = bovedaNode.get("BovedaFiscal");
            if (bovedaFiscalNode.has("numeroCliente")) {
                addenda.setNumeroCliente(bovedaFiscalNode.get("numeroCliente").asText());
            }
            if (bovedaFiscalNode.has("razonSocialCliente")) {
                addenda.setRazonSocialCliente(bovedaFiscalNode.get("razonSocialCliente").asText());
            }
            contenido.put("bovedaFiscal", bovedaFiscalNode);
        }

        if (bovedaNode.has("Generales")) {
            JsonNode generalesNode = bovedaNode.get("Generales");
            if (generalesNode.has("noFactura")) {
                addenda.setNumeroFactura(generalesNode.get("noFactura").asText());
            }
            if (generalesNode.has("noPedido")) {
                addenda.setNumeroPedido(generalesNode.get("noPedido").asText());
            }
            if (generalesNode.has("contrato")) {
                addenda.setContrato(generalesNode.get("contrato").asText());
            }
            if (generalesNode.has("ordenCompra")) {
                addenda.setOrdenCompra(generalesNode.get("ordenCompra").asText());
            }
            contenido.put("generales", generalesNode);
        }

        addenda.setContenido(contenido);
        return addenda;
    }

    /**
     * Extrae addenda K
     * @param addendaKNode Nodo JSON de AddendaK
     * @return AddendaDto
     */
    private AddendaDto extractAddendaK(JsonNode addendaKNode) {
        AddendaDto addenda = new AddendaDto();
        addenda.setTipoAddenda("AddendaK");
        addenda.setNamespace("http://kontender.mx/namespace");

        Map<String, Object> contenido = new HashMap<>();

        if (addendaKNode.has("Kgral")) {
            JsonNode kgralNode = addendaKNode.get("Kgral");
            if (kgralNode.has("nfactura")) {
                addenda.setNumeroFactura(kgralNode.get("nfactura").asText());
            }
            if (kgralNode.has("npedido")) {
                addenda.setNumeroPedido(kgralNode.get("npedido").asText());
            }
            if (kgralNode.has("contrato")) {
                addenda.setContrato(kgralNode.get("contrato").asText());
            }
            if (kgralNode.has("nocompra")) {
                addenda.setOrdenCompra(kgralNode.get("nocompra").asText());
            }
            contenido.put("kgral", kgralNode);
        }

        if (addendaKNode.has("Kreceptor")) {
            JsonNode kreceptorNode = addendaKNode.get("Kreceptor");
            if (kreceptorNode.has("ncliente")) {
                addenda.setNumeroCliente(kreceptorNode.get("ncliente").asText());
            }
            contenido.put("kreceptor", kreceptorNode);
        }

        addenda.setContenido(contenido);
        return addenda;
    }

    /**
     * Extrae addenda Sodimac
     * @param sodimacNode Nodo JSON de Addenda_Sodimac_Detecno
     * @return AddendaDto
     */
    private AddendaDto extractAddendaSodimac(JsonNode sodimacNode) {
        AddendaDto addenda = new AddendaDto();
        addenda.setTipoAddenda("Addenda_Sodimac_Detecno");

        Map<String, Object> contenido = new HashMap<>();

        if (sodimacNode.has("NoOC")) {
            addenda.setOrdenCompra(sodimacNode.get("NoOC").asText());
        }
        if (sodimacNode.has("Folio")) {
            addenda.setNumeroFactura(sodimacNode.get("Folio").asText());
        }
        if (sodimacNode.has("Proveedor")) {
            addenda.setNumeroCliente(sodimacNode.get("Proveedor").asText());
        }

        contenido.put("contenidoCompleto", sodimacNode);
        addenda.setContenido(contenido);

        return addenda;
    }

    /**
     * Extrae CartaPorte del complemento
     * @param jsonNode Nodo JSON del comprobante
     * @return CartaPorteDto
     */
    private CartaPorteDto extractCartaPorte(JsonNode jsonNode) {
        if (jsonNode.has("Complemento") && jsonNode.get("Complemento").has("CartaPorte")) {
            JsonNode cartaPorteNode = jsonNode.get("Complemento").get("CartaPorte");
            return extractCartaPorteData(cartaPorteNode);
        }
        return null;
    }

    /**
     * Extrae los datos del CartaPorte
     * @param cartaPorteNode Nodo JSON del CartaPorte
     * @return CartaPorteDto
     */
    private CartaPorteDto extractCartaPorteData(JsonNode cartaPorteNode) {
        CartaPorteDto cartaPorte = new CartaPorteDto();

        if (cartaPorteNode.has("Version")) {
            cartaPorte.setVersion(cartaPorteNode.get("Version").asText());
        }
        if (cartaPorteNode.has("TranspInternac")) {
            cartaPorte.setTranspInternac(cartaPorteNode.get("TranspInternac").asText());
        }
        if (cartaPorteNode.has("TotalDistRec")) {
            cartaPorte.setTotalDistRec(cartaPorteNode.get("TotalDistRec").asText());
        }
        if (cartaPorteNode.has("IdCCP")) {
            cartaPorte.setIdCCP(cartaPorteNode.get("IdCCP").asText());
        }

        // Extraer ubicaciones
        cartaPorte.setUbicaciones(extractUbicaciones(cartaPorteNode));

        // Extraer mercancías
        cartaPorte.setMercancias(extractMercancias(cartaPorteNode));

        // Extraer figuras de transporte
        cartaPorte.setFigurasTransporte(extractFigurasTransporte(cartaPorteNode));

        return cartaPorte;
    }

    /**
     * Extrae las ubicaciones del CartaPorte
     * @param cartaPorteNode Nodo JSON del CartaPorte
     * @return Lista de UbicacionDto
     */
    private List<UbicacionDto> extractUbicaciones(JsonNode cartaPorteNode) {
        List<UbicacionDto> ubicaciones = new ArrayList<>();

        if (cartaPorteNode.has("Ubicaciones") && cartaPorteNode.get("Ubicaciones").has("Ubicacion")) {
            JsonNode ubicacionesNode = cartaPorteNode.get("Ubicaciones").get("Ubicacion");

            if (ubicacionesNode.isArray()) {
                for (JsonNode ubicacionNode : ubicacionesNode) {
                    ubicaciones.add(extractUbicacion(ubicacionNode));
                }
            } else {
                ubicaciones.add(extractUbicacion(ubicacionesNode));
            }
        }

        return ubicaciones;
    }

    /**
     * Extrae una ubicación individual
     * @param ubicacionNode Nodo JSON de la ubicación
     * @return UbicacionDto
     */
    private UbicacionDto extractUbicacion(JsonNode ubicacionNode) {
        UbicacionDto ubicacion = new UbicacionDto();

        if (ubicacionNode.has("TipoUbicacion")) {
            ubicacion.setTipoUbicacion(ubicacionNode.get("TipoUbicacion").asText());
        }
        if (ubicacionNode.has("RFCRemitenteDestinatario")) {
            ubicacion.setRfcRemitenteDestinatario(ubicacionNode.get("RFCRemitenteDestinatario").asText());
        }
        if (ubicacionNode.has("FechaHoraSalidaLlegada")) {
            ubicacion.setFechaHoraSalidaLlegada(ubicacionNode.get("FechaHoraSalidaLlegada").asText());
        }
        if (ubicacionNode.has("NombreRemitenteDestinatario")) {
            ubicacion.setNombreRemitenteDestinatario(ubicacionNode.get("NombreRemitenteDestinatario").asText());
        }
        if (ubicacionNode.has("DistanciaRecorrida")) {
            ubicacion.setDistanciaRecorrida(ubicacionNode.get("DistanciaRecorrida").asText());
        }

        // Extraer domicilio
        if (ubicacionNode.has("Domicilio")) {
            ubicacion.setDomicilio(extractDomicilio(ubicacionNode.get("Domicilio")));
        }

        return ubicacion;
    }

    /**
     * Extrae el domicilio
     * @param domicilioNode Nodo JSON del domicilio
     * @return DomicilioDto
     */
    private DomicilioDto extractDomicilio(JsonNode domicilioNode) {
        DomicilioDto domicilio = new DomicilioDto();

        if (domicilioNode.has("Calle")) {
            domicilio.setCalle(domicilioNode.get("Calle").asText());
        }
        if (domicilioNode.has("Estado")) {
            domicilio.setEstado(domicilioNode.get("Estado").asText());
        }
        if (domicilioNode.has("Pais")) {
            domicilio.setPais(domicilioNode.get("Pais").asText());
        }
        if (domicilioNode.has("CodigoPostal")) {
            domicilio.setCodigoPostal(domicilioNode.get("CodigoPostal").asText());
        }
        if (domicilioNode.has("Colonia")) {
            domicilio.setColonia(domicilioNode.get("Colonia").asText());
        }
        if (domicilioNode.has("Municipio")) {
            domicilio.setMunicipio(domicilioNode.get("Municipio").asText());
        }
        if (domicilioNode.has("NumeroExterior")) {
            domicilio.setNumeroExterior(domicilioNode.get("NumeroExterior").asText());
        }
        if (domicilioNode.has("NumeroInterior")) {
            domicilio.setNumeroInterior(domicilioNode.get("NumeroInterior").asText());
        }

        return domicilio;
    }

    /**
     * Extrae las mercancías del CartaPorte
     * @param cartaPorteNode Nodo JSON del CartaPorte
     * @return MercanciasDto
     */
    private MercanciasDto extractMercancias(JsonNode cartaPorteNode) {
        if (!cartaPorteNode.has("Mercancias")) {
            return null;
        }

        JsonNode mercanciasNode = cartaPorteNode.get("Mercancias");
        MercanciasDto mercancias = new MercanciasDto();

        if (mercanciasNode.has("NumTotalMercancias")) {
            mercancias.setNumTotalMercancias(mercanciasNode.get("NumTotalMercancias").asText());
        }
        if (mercanciasNode.has("UnidadPeso")) {
            mercancias.setUnidadPeso(mercanciasNode.get("UnidadPeso").asText());
        }
        if (mercanciasNode.has("PesoBrutoTotal")) {
            mercancias.setPesoBrutoTotal(mercanciasNode.get("PesoBrutoTotal").asText());
        }

        // Extraer mercancías individuales
        List<MercanciaDto> listaMercancias = new ArrayList<>();
        if (mercanciasNode.has("Mercancia")) {
            JsonNode mercanciaNode = mercanciasNode.get("Mercancia");
            if (mercanciaNode.isArray()) {
                for (JsonNode mercancia : mercanciaNode) {
                    listaMercancias.add(extractMercancia(mercancia));
                }
            } else {
                listaMercancias.add(extractMercancia(mercanciaNode));
            }
        }
        mercancias.setMercancias(listaMercancias);

        // Extraer autotransporte
        if (mercanciasNode.has("Autotransporte")) {
            mercancias.setAutotransporte(extractAutotransporte(mercanciasNode.get("Autotransporte")));
        }

        return mercancias;
    }

    /**
     * Extrae una mercancía individual
     * @param mercanciaNode Nodo JSON de la mercancía
     * @return MercanciaDto
     */
    private MercanciaDto extractMercancia(JsonNode mercanciaNode) {
        MercanciaDto mercancia = new MercanciaDto();

        if (mercanciaNode.has("PesoEnKg")) {
            mercancia.setPesoEnKg(mercanciaNode.get("PesoEnKg").asText());
        }
        if (mercanciaNode.has("BienesTransp")) {
            mercancia.setBienesTransp(mercanciaNode.get("BienesTransp").asText());
        }
        if (mercanciaNode.has("Descripcion")) {
            mercancia.setDescripcion(mercanciaNode.get("Descripcion").asText());
        }
        if (mercanciaNode.has("Cantidad")) {
            mercancia.setCantidad(mercanciaNode.get("Cantidad").asText());
        }
        if (mercanciaNode.has("ClaveUnidad")) {
            mercancia.setClaveUnidad(mercanciaNode.get("ClaveUnidad").asText());
        }
        if (mercanciaNode.has("ValorMercancia")) {
            mercancia.setValorMercancia(mercanciaNode.get("ValorMercancia").asText());
        }
        if (mercanciaNode.has("Moneda")) {
            mercancia.setMoneda(mercanciaNode.get("Moneda").asText());
        }

        return mercancia;
    }

    /**
     * Extrae el autotransporte
     * @param autotransporteNode Nodo JSON del autotransporte
     * @return AutotransporteDto
     */
    private AutotransporteDto extractAutotransporte(JsonNode autotransporteNode) {
        AutotransporteDto autotransporte = new AutotransporteDto();

        if (autotransporteNode.has("PermSCT")) {
            autotransporte.setPermSCT(autotransporteNode.get("PermSCT").asText());
        }
        if (autotransporteNode.has("NumPermisoSCT")) {
            autotransporte.setNumPermisoSCT(autotransporteNode.get("NumPermisoSCT").asText());
        }

        // Extraer identificación vehicular
        if (autotransporteNode.has("IdentificacionVehicular")) {
            autotransporte.setIdentificacionVehicular(
                extractIdentificacionVehicular(autotransporteNode.get("IdentificacionVehicular"))
            );
        }

        // Extraer seguros
        if (autotransporteNode.has("Seguros")) {
            autotransporte.setSeguros(extractSeguros(autotransporteNode.get("Seguros")));
        }

        // Extraer remolques
        if (autotransporteNode.has("Remolques") && autotransporteNode.get("Remolques").has("Remolque")) {
            List<RemolqueDto> remolques = new ArrayList<>();
            JsonNode remolquesNode = autotransporteNode.get("Remolques").get("Remolque");

            if (remolquesNode.isArray()) {
                for (JsonNode remolqueNode : remolquesNode) {
                    remolques.add(extractRemolque(remolqueNode));
                }
            } else {
                remolques.add(extractRemolque(remolquesNode));
            }
            autotransporte.setRemolques(remolques);
        }

        return autotransporte;
    }

    /**
     * Extrae la identificación vehicular
     * @param idVehicularNode Nodo JSON de identificación vehicular
     * @return IdentificacionVehicularDto
     */
    private IdentificacionVehicularDto extractIdentificacionVehicular(JsonNode idVehicularNode) {
        IdentificacionVehicularDto idVehicular = new IdentificacionVehicularDto();

        if (idVehicularNode.has("ConfigVehicular")) {
            idVehicular.setConfigVehicular(idVehicularNode.get("ConfigVehicular").asText());
        }
        if (idVehicularNode.has("PlacaVM")) {
            idVehicular.setPlacaVM(idVehicularNode.get("PlacaVM").asText());
        }
        if (idVehicularNode.has("AnioModeloVM")) {
            idVehicular.setAnioModeloVM(idVehicularNode.get("AnioModeloVM").asText());
        }
        if (idVehicularNode.has("PesoBrutoVehicular")) {
            idVehicular.setPesoBrutoVehicular(idVehicularNode.get("PesoBrutoVehicular").asText());
        }

        return idVehicular;
    }

    /**
     * Extrae los seguros
     * @param segurosNode Nodo JSON de seguros
     * @return SegurosDto
     */
    private SegurosDto extractSeguros(JsonNode segurosNode) {
        SegurosDto seguros = new SegurosDto();

        if (segurosNode.has("AseguraRespCivil")) {
            seguros.setAseguraRespCivil(segurosNode.get("AseguraRespCivil").asText());
        }
        if (segurosNode.has("PolizaRespCivil")) {
            seguros.setPolizaRespCivil(segurosNode.get("PolizaRespCivil").asText());
        }
        if (segurosNode.has("AseguraMedAmbiente")) {
            seguros.setAseguraMedAmbiente(segurosNode.get("AseguraMedAmbiente").asText());
        }
        if (segurosNode.has("PolizaMedAmbiente")) {
            seguros.setPolizaMedAmbiente(segurosNode.get("PolizaMedAmbiente").asText());
        }
        if (segurosNode.has("AseguraCarga")) {
            seguros.setAseguraCarga(segurosNode.get("AseguraCarga").asText());
        }
        if (segurosNode.has("PolizaCarga")) {
            seguros.setPolizaCarga(segurosNode.get("PolizaCarga").asText());
        }
        if (segurosNode.has("PrimaSeguro")) {
            seguros.setPrimaSeguro(segurosNode.get("PrimaSeguro").asText());
        }

        return seguros;
    }

    /**
     * Extrae un remolque
     * @param remolqueNode Nodo JSON del remolque
     * @return RemolqueDto
     */
    private RemolqueDto extractRemolque(JsonNode remolqueNode) {
        RemolqueDto remolque = new RemolqueDto();

        if (remolqueNode.has("SubTipoRem")) {
            remolque.setSubTipoRem(remolqueNode.get("SubTipoRem").asText());
        }
        if (remolqueNode.has("Placa")) {
            remolque.setPlaca(remolqueNode.get("Placa").asText());
        }

        return remolque;
    }

    /**
     * Extrae las figuras de transporte
     * @param cartaPorteNode Nodo JSON del CartaPorte
     * @return Lista de FiguraTransporteDto
     */
    private List<FiguraTransporteDto> extractFigurasTransporte(JsonNode cartaPorteNode) {
        List<FiguraTransporteDto> figuras = new ArrayList<>();

        if (cartaPorteNode.has("FiguraTransporte") && cartaPorteNode.get("FiguraTransporte").has("TiposFigura")) {
            JsonNode figurasNode = cartaPorteNode.get("FiguraTransporte").get("TiposFigura");

            if (figurasNode.isArray()) {
                for (JsonNode figuraNode : figurasNode) {
                    figuras.add(extractFiguraTransporte(figuraNode));
                }
            } else {
                figuras.add(extractFiguraTransporte(figurasNode));
            }
        }

        return figuras;
    }

    /**
     * Extrae una figura de transporte
     * @param figuraNode Nodo JSON de la figura
     * @return FiguraTransporteDto
     */
    private FiguraTransporteDto extractFiguraTransporte(JsonNode figuraNode) {
        FiguraTransporteDto figura = new FiguraTransporteDto();

        if (figuraNode.has("TipoFigura")) {
            figura.setTipoFigura(figuraNode.get("TipoFigura").asText());
        }
        if (figuraNode.has("RFCFigura")) {
            figura.setRfcFigura(figuraNode.get("RFCFigura").asText());
        }
        if (figuraNode.has("NumLicencia")) {
            figura.setNumLicencia(figuraNode.get("NumLicencia").asText());
        }
        if (figuraNode.has("NombreFigura")) {
            figura.setNombreFigura(figuraNode.get("NombreFigura").asText());
        }

        return figura;
    }

    /**
     * Extrae información del TimbreFiscalDigital
     * @param jsonNode Nodo JSON raíz del comprobante
     * @return TimbreFiscalDigitalDto o null si no existe
     */
    private TimbreFiscalDigitalDto extractTimbreFiscalDigital(JsonNode jsonNode) {
        TimbreFiscalDigitalDto timbre = new TimbreFiscalDigitalDto();

        try {
            JsonNode complementoNode = jsonNode.path("Complemento");
            if (complementoNode.isMissingNode()) {
                return null;
            }

            JsonNode tfdNode = complementoNode.path("TimbreFiscalDigital");
            if (tfdNode.isMissingNode()) {
                return null;
            }

            if (tfdNode.has("Version")) {
                timbre.setVersion(tfdNode.get("Version").asText());
            }
            if (tfdNode.has("UUID")) {
                timbre.setUuid(tfdNode.get("UUID").asText());
            }
            if (tfdNode.has("FechaTimbrado")) {
                timbre.setFechaTimbrado(tfdNode.get("FechaTimbrado").asText());
            }
            if (tfdNode.has("RfcProvCertif")) {
                timbre.setRfcProvCertif(tfdNode.get("RfcProvCertif").asText());
            }
            if (tfdNode.has("SelloCFD")) {
                timbre.setSelloCFD(tfdNode.get("SelloCFD").asText());
            }
            if (tfdNode.has("NoCertificadoSAT")) {
                timbre.setNoCertificadoSAT(tfdNode.get("NoCertificadoSAT").asText());
            }
            if (tfdNode.has("SelloSAT")) {
                timbre.setSelloSAT(tfdNode.get("SelloSAT").asText());
            }

            return timbre;
        } catch (Exception e) {
            System.err.println("Error al extraer TimbreFiscalDigital: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrae impuestos consolidados a nivel factura
     * @param jsonNode Nodo JSON raíz del comprobante
     * @return ImpuestosFacturaDto o null si no existen
     */
    private ImpuestosFacturaDto extractImpuestosFactura(JsonNode jsonNode) {
        ImpuestosFacturaDto impuestos = new ImpuestosFacturaDto();

        try {
            JsonNode impuestosNode = jsonNode.path("Impuestos");
            if (impuestosNode.isMissingNode()) {
                return null;
            }

            if (impuestosNode.has("TotalImpuestosRetenidos")) {
                impuestos.setTotalImpuestosRetenidos(impuestosNode.get("TotalImpuestosRetenidos").asText());
            }
            if (impuestosNode.has("TotalImpuestosTrasladados")) {
                impuestos.setTotalImpuestosTrasladados(impuestosNode.get("TotalImpuestosTrasladados").asText());
            }

            // Extraer retenciones consolidadas
            if (impuestosNode.has("Retenciones")) {
                List<RetencionDto> retenciones = new ArrayList<>();
                JsonNode retencionesNode = impuestosNode.get("Retenciones");

                if (retencionesNode.has("Retencion")) {
                    JsonNode retencionNode = retencionesNode.get("Retencion");
                    if (retencionNode.isArray()) {
                        for (JsonNode ret : retencionNode) {
                            retenciones.add(extractRetencion(ret));
                        }
                    } else {
                        retenciones.add(extractRetencion(retencionNode));
                    }
                }
                impuestos.setRetenciones(retenciones);
            }

            // Extraer traslados consolidados
            if (impuestosNode.has("Traslados")) {
                List<TrasladoDto> traslados = new ArrayList<>();
                JsonNode trasladosNode = impuestosNode.get("Traslados");

                if (trasladosNode.has("Traslado")) {
                    JsonNode trasladoNode = trasladosNode.get("Traslado");
                    if (trasladoNode.isArray()) {
                        for (JsonNode tras : trasladoNode) {
                            traslados.add(extractTraslado(tras));
                        }
                    } else {
                        traslados.add(extractTraslado(trasladoNode));
                    }
                }
                impuestos.setTraslados(traslados);
            }

            return impuestos;
        } catch (Exception e) {
            System.err.println("Error al extraer impuestos de factura: " + e.getMessage());
            return null;
        }
    }
}