package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.invoicexml.CfdiRelacionadosDto;
import com.sodimac.fiscal.api.model.dto.invoicexml.InvoiceXmlDto;
import com.sodimac.fiscal.api.model.dto.response.FiscalXmlResponse;
import com.sodimac.fiscal.api.repository.AddendumRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Regla Ivan 2026-07-20: al publicar/consultar una NC, solo se toma el UUID del bloque
 * CfdiRelacionados cuyo TipoRelacion esté en el catálogo CatTipoRelacionFacturaNC
 * (hoy 01 y 03). Cualquier otro tipo (04, 05, etc.) se ignora. Los tipos se leen DIRECTO
 * de shared_catalogs, solo estatus activo.
 *
 * Cubre la ruta de consulta (FiscalXmlTransformerServiceImpl). El catálogo se simula con un
 * mock del repositorio que devuelve {01, 03}.
 */
class CfdiRelacionadosTipo01Test {

    private static final String UUID_04 = "F286E374-5C08-45BA-82A6-037A06ACB938"; // 04 (no permitido)
    private static final String UUID_01 = "2FDC848B-2E55-4170-93C8-E96C7D822131"; // 01 (permitido)
    private static final String UUID_03 = "AAAA0303-0000-0000-0000-000000000303"; // 03 (permitido)

    private final AddendumRepository addendumRepository = mock(AddendumRepository.class);

    private FiscalXmlTransformerServiceImpl transformer() {
        when(addendumRepository.findActiveCatalogValues("CatTipoRelacionFacturaNC"))
                .thenReturn(Arrays.asList("01", "03"));
        return new FiscalXmlTransformerServiceImpl(new XmlDocumentTypeDetectorServiceImpl(), addendumRepository);
    }

    /** NC con dos bloques CfdiRelacionados, uno por cada (tipo, uuid). */
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

    // ---- Ruta de consulta (FiscalXmlTransformer) contra catálogo {01, 03} ----

    @Test
    void consulta_tomaBloquePermitido_cuandoElPrimeroNoLoEs() {
        String xml = ncConDosBloques("04", UUID_04, "01", UUID_01); // 04 no permitido, 01 sí
        FiscalXmlResponse resp = transformer().transformToStructuredResponse(xml);
        assertEquals("01", resp.getComprobante().getTipoRelacion());
        assertEquals(UUID_01, resp.getComprobante().getUuidRelacionado());
    }

    @Test
    void consulta_tomaBloquePermitido_sinImportarElOrden() {
        String xml = ncConDosBloques("01", UUID_01, "04", UUID_04); // 01 primero
        FiscalXmlResponse resp = transformer().transformToStructuredResponse(xml);
        assertEquals("01", resp.getComprobante().getTipoRelacion());
        assertEquals(UUID_01, resp.getComprobante().getUuidRelacionado());
    }

    @Test
    void consulta_tomaTipo03_porqueTambienEstaEnCatalogo() {
        String xml = ncConDosBloques("04", UUID_04, "03", UUID_03); // 03 permitido por catálogo
        FiscalXmlResponse resp = transformer().transformToStructuredResponse(xml);
        assertEquals("03", resp.getComprobante().getTipoRelacion());
        assertEquals(UUID_03, resp.getComprobante().getUuidRelacionado());
    }

    @Test
    void consulta_ignoraRelacionado_cuandoNingunBloqueEsPermitido() {
        String xml = ncConDosBloques("04", UUID_04, "05", "99998888-7777-6666-5555-444433332222");
        FiscalXmlResponse resp = transformer().transformToStructuredResponse(xml);
        assertNull(resp.getComprobante().getUuidRelacionado());
        assertNull(resp.getComprobante().getTipoRelacion());
    }

    // ---- Ruta de register (JAXB InvoiceXmlDto captura todos los bloques) ----

    @Test
    void register_jaxbCapturaTodosLosBloques() throws Exception {
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
