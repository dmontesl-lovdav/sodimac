package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.CfdiRelacionadosDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.dto.response.FiscalXmlResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regla Ivan 2026-07-17: al publicar/consultar una NC, solo se toma el UUID del bloque
 * CfdiRelacionados con TipoRelacion="01". Cualquier otro bloque (04, 03, etc.) se ignora.
 *
 * Cubre las dos rutas que elegían el bloque equivocado:
 * - Register: JAXB InvoiceXmlDto (era un solo bloque, ahora lista + filtro "01").
 * - Consulta: FiscalXmlTransformerServiceImpl (tomaba el primer bloque, ahora filtra "01").
 */
class CfdiRelacionadosTipo01Test {

    private static final String UUID_04 = "F286E374-5C08-45BA-82A6-037A06ACB938"; // bloque 04 (a ignorar)
    private static final String UUID_01 = "2FDC848B-2E55-4170-93C8-E96C7D822131"; // bloque 01 (correcto)

    private final FiscalXmlTransformerServiceImpl transformer =
            new FiscalXmlTransformerServiceImpl(new XmlDocumentTypeDetectorServiceImpl());

    /** NC con dos bloques: 04 primero (como en 'nc 3.xml'), 01 después. */
    private String ncConDosBloques(String primerTipo, String primerUuid,
                                   String segundoTipo, String segundoUuid) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<cfdi:Comprobante xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\" "
                + "Version=\"4.0\" Serie=\"NC\" Folio=\"9001\" TipoDeComprobante=\"E\" "
                + "SubTotal=\"100.00\" Total=\"116.00\" Moneda=\"MXN\">"
                + "<cfdi:CfdiRelacionados TipoRelacion=\"" + primerTipo + "\">"
                + "<cfdi:CfdiRelacionado UUID=\"" + primerUuid + "\"/></cfdi:CfdiRelacionados>"
                + "<cfdi:CfdiRelacionados TipoRelacion=\"" + segundoTipo + "\">"
                + "<cfdi:CfdiRelacionado UUID=\"" + segundoUuid + "\"/></cfdi:CfdiRelacionados>"
                + "<cfdi:Emisor Rfc=\"AAA010101AAA\" Nombre=\"EMISOR\" RegimenFiscal=\"601\"/>"
                + "<cfdi:Receptor Rfc=\"CSD161207R2A\" Nombre=\"SODIMAC\" UsoCFDI=\"G02\" "
                + "RegimenFiscalReceptor=\"601\" DomicilioFiscalReceptor=\"53150\"/>"
                + "<cfdi:Complemento><tfd:TimbreFiscalDigital "
                + "xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" Version=\"1.1\" "
                + "UUID=\"11112222-3333-4444-5555-666677778888\"/></cfdi:Complemento>"
                + "</cfdi:Comprobante>";
    }

    // ---- Ruta de consulta (FiscalXmlTransformer) ----

    @Test
    void consulta_tomaBloque01_cuandoElPrimeroEs04() {
        String xml = ncConDosBloques("04", UUID_04, "01", UUID_01);
        FiscalXmlResponse resp = transformer.transformToStructuredResponse(xml);
        assertEquals("01", resp.getComprobante().getTipoRelacion());
        assertEquals(UUID_01, resp.getComprobante().getUuidRelacionado());
    }

    @Test
    void consulta_tomaBloque01_sinImportarElOrden() {
        String xml = ncConDosBloques("01", UUID_01, "04", UUID_04); // 01 primero
        FiscalXmlResponse resp = transformer.transformToStructuredResponse(xml);
        assertEquals("01", resp.getComprobante().getTipoRelacion());
        assertEquals(UUID_01, resp.getComprobante().getUuidRelacionado());
    }

    @Test
    void consulta_ignoraRelacionado_cuandoNoHayBloque01() {
        String xml = ncConDosBloques("04", UUID_04, "03", "99998888-7777-6666-5555-444433332222");
        FiscalXmlResponse resp = transformer.transformToStructuredResponse(xml);
        assertNull(resp.getComprobante().getUuidRelacionado());
        assertNull(resp.getComprobante().getTipoRelacion());
    }

    // ---- Ruta de register (JAXB InvoiceXmlDto) ----

    @Test
    void register_jaxbCapturaTodosLosBloques_yFiltra01() throws Exception {
        String xml = ncConDosBloques("04", UUID_04, "01", UUID_01);
        JAXBContext ctx = JAXBContext.newInstance(InvoiceXmlDto.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        InvoiceXmlDto dto = (InvoiceXmlDto) unmarshaller.unmarshal(new StringReader(xml));

        List<CfdiRelacionadosDto> bloques = dto.getCfdiRelacionados();
        assertNotNull(bloques);
        assertEquals(2, bloques.size(), "Debe capturar AMBOS bloques CfdiRelacionados");

        CfdiRelacionadosDto bloque01 = bloques.stream()
                .filter(b -> "01".equals(b.getTipoRelacion()))
                .findFirst().orElseThrow();
        assertEquals(UUID_01, bloque01.getCfdiRelacionado().get(0).getUuid());
    }
}
