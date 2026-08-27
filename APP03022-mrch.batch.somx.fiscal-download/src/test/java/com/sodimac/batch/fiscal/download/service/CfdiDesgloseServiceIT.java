package com.sodimac.batch.fiscal.download.service;

import com.sodimac.batch.fiscal.download.model.entity.sap.ComprobanteEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integracion contra SODIMAC_SAP_DEV real (perfil dev). @Transactional sobre
 * sapTransactionManager: todo se revierte al terminar, no deja datos.
 * Los CommandLineRunner NO se ejecutan bajo @SpringBootTest, asi que el test
 * no dispara el batch contra la fiscal-api.
 */
@SpringBootTest(properties = "batch.autorun=false")
@Transactional(transactionManager = "sapTransactionManager")
class CfdiDesgloseServiceIT {

    private static final String UUID_PRUEBA = "AAAAAAAA-BBBB-CCCC-DDDD-EEEEFFFF0001";

    private static final String CFDI_EJEMPLO =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<cfdi:Comprobante xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\" " +
        "xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" " +
        "Version=\"4.0\" Serie=\"TST\" Folio=\"777\" Fecha=\"2026-07-06T12:00:00\" " +
        "SubTotal=\"200.00\" Descuento=\"10.00\" Total=\"220.40\" Moneda=\"MXN\" " +
        "TipoDeComprobante=\"I\" MetodoPago=\"PUE\" FormaPago=\"99\" " +
        "CondicionesDePago=\"Contado\" LugarExpedicion=\"44100\" Exportacion=\"01\">" +
        "<cfdi:Emisor Rfc=\"TST010101TS1\" Nombre=\"PROVEEDOR DE PRUEBA\" RegimenFiscal=\"601\"/>" +
        "<cfdi:Receptor Rfc=\"SOD990101AB2\" Nombre=\"SODIMAC MEXICO\" UsoCFDI=\"G03\" " +
        "RegimenFiscalReceptor=\"601\" DomicilioFiscalReceptor=\"06600\"/>" +
        "<cfdi:Conceptos>" +
        "<cfdi:Concepto ClaveProdServ=\"30181605\" Cantidad=\"2\" ClaveUnidad=\"H87\" Unidad=\"Pieza\" " +
        "Descripcion=\"Producto uno\" ValorUnitario=\"50.00\" Importe=\"100.00\">" +
        "<cfdi:Impuestos><cfdi:Traslados>" +
        "<cfdi:Traslado Base=\"100.00\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"16.00\"/>" +
        "</cfdi:Traslados></cfdi:Impuestos>" +
        "</cfdi:Concepto>" +
        "<cfdi:Concepto ClaveProdServ=\"30181800\" Cantidad=\"1\" ClaveUnidad=\"H87\" Unidad=\"Pieza\" " +
        "Descripcion=\"Producto dos\" ValorUnitario=\"100.00\" Importe=\"100.00\">" +
        "<cfdi:Impuestos>" +
        "<cfdi:Traslados>" +
        "<cfdi:Traslado Base=\"100.00\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"16.00\"/>" +
        "<cfdi:Traslado Base=\"100.00\" Impuesto=\"003\" TipoFactor=\"Tasa\" TasaOCuota=\"0.080000\" Importe=\"8.00\"/>" +
        "</cfdi:Traslados>" +
        "<cfdi:Retenciones>" +
        "<cfdi:Retencion Base=\"100.00\" Impuesto=\"001\" TipoFactor=\"Tasa\" TasaOCuota=\"0.100000\" Importe=\"10.00\"/>" +
        "</cfdi:Retenciones>" +
        "</cfdi:Impuestos>" +
        "</cfdi:Concepto>" +
        "</cfdi:Conceptos>" +
        "<cfdi:Complemento>" +
        "<tfd:TimbreFiscalDigital UUID=\"" + UUID_PRUEBA + "\" FechaTimbrado=\"2026-07-06T12:01:00\" " +
        "RfcProvCertif=\"SAT970701NN3\" NoCertificadoSAT=\"00001000000504465028\"/>" +
        "</cfdi:Complemento>" +
        "</cfdi:Comprobante>";

    @Autowired
    private CfdiDesgloseService service;

    @Autowired
    @Qualifier("sapJdbcTemplate")
    private JdbcTemplate sapJdbcTemplate;

    @Test
    void desglosaCfdiCompletoEnEsquemaLegado() throws Exception {
        ComprobanteEntity comp = service.desglosar(CFDI_EJEMPLO, "11111111-2222-3333-4444-555566660001");
        String uuid = comp != null ? comp.getFiscalUuid() : null;
        assertEquals(UUID_PRUEBA, uuid);

        Map<String, Object> compRow = sapJdbcTemplate.queryForMap(
                "SELECT Serie, Folio, Total, Moneda, TipoDeComprobante, Estatus, FechaCadena, fiscal_uuid, invoice_uuid " +
                "FROM Comprobante WHERE Uuid = ?", uuid);
        assertEquals("TST", compRow.get("Serie"));
        assertEquals("777", compRow.get("Folio"));
        assertEquals(0, ((Number) compRow.get("Estatus")).intValue(), "Debe insertarse pendiente de envio");
        assertEquals("6/7/2026 12:00:00", compRow.get("FechaCadena"));
        assertEquals(UUID_PRUEBA, compRow.get("fiscal_uuid"));
        assertNotNull(compRow.get("invoice_uuid"));
        assertEquals(0, new BigDecimal("220.40").compareTo((BigDecimal) compRow.get("Total")));

        Map<String, Object> emisor = sapJdbcTemplate.queryForMap(
                "SELECT Nombre, Rfc, Regimen FROM Emisor WHERE Uuid = ?", uuid);
        assertEquals("TST010101TS1", emisor.get("Rfc"));
        assertEquals("601", emisor.get("Regimen"));

        List<Map<String, Object>> conceptos = sapJdbcTemplate.queryForList(
                "SELECT IdPadre, ClaveProdServ, Cantidad, Importe FROM Concepto WHERE Uuid = ? ORDER BY IdPadre", uuid);
        assertEquals(2, conceptos.size());
        assertEquals("30181605", conceptos.get(0).get("ClaveProdServ"));
        assertEquals("2", conceptos.get(0).get("Cantidad"), "Cantidad se guarda como cadena en el esquema legado");

        List<Map<String, Object>> impuestos = sapJdbcTemplate.queryForList(
                "SELECT IdPadre, ClaveProdServ, TipoImpuesto, Impuesto, Importe, Base FROM DetalleImpuesto " +
                "WHERE Uuid = ? ORDER BY IdPadre, TipoImpuesto, Impuesto", uuid);
        // concepto 1: 1 traslado IVA; concepto 2: 2 traslados (IVA+IEPS) + 1 retencion ISR
        assertEquals(4, impuestos.size());
        long idPadre2 = ((Number) conceptos.get(1).get("IdPadre")).longValue();
        long trasladosC2 = impuestos.stream().filter(d ->
                ((Number) d.get("IdPadre")).longValue() == idPadre2 && "1".equals(d.get("TipoImpuesto"))).count();
        long retencionesC2 = impuestos.stream().filter(d ->
                ((Number) d.get("IdPadre")).longValue() == idPadre2 && "2".equals(d.get("TipoImpuesto"))).count();
        assertEquals(2, trasladosC2);
        assertEquals(1, retencionesC2);
    }

    @Test
    void omiteComprobanteDuplicadoSinError() throws Exception {
        service.desglosar(CFDI_EJEMPLO, null);
        // segunda pasada: mismo uuid, no debe tronar ni duplicar
        service.desglosar(CFDI_EJEMPLO, null);
        Integer filas = sapJdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM Comprobante WHERE Uuid = ?", Integer.class, UUID_PRUEBA);
        assertEquals(1, filas);
    }
}
