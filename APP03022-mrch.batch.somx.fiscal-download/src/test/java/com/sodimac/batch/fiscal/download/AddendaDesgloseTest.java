package com.sodimac.batch.fiscal.download;

import com.sodimac.batch.fiscal.download.model.entity.sap.AddendaEntity;
import com.sodimac.batch.fiscal.download.repository.sap.AddendaRepository;
import com.sodimac.batch.fiscal.download.service.CfdiDesgloseService;
import com.sodimac.batch.fiscal.download.service.impl.CreditNoteDownloadBatchService;
import com.sodimac.batch.fiscal.download.service.impl.InvoiceDownloadBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba end-to-end del desglose de addenda (fiscal-download) contra SODIMAC_SAP_DEV local.
 * Cubre los 3 nodos: Addenda_Sodimac_Detecno (Tipo 1), Addenda_Transportistas_Sodimac_Detecno
 * (Tipo 2) y Addenda_NotaCredito_Sodimac_Detecno (Tipo 3). Se mockean los servicios batch para
 * NO disparar la descarga real (fiscal-api) al arrancar el contexto.
 */
@SpringBootTest(properties = {
        // El SQL Server local (docker) publica 1433 del container en el puerto 1434 del host.
        // El perfil local apunta a 1433 (válido dentro de la red docker); para el test en el host → 1434.
        "spring.datasource.sap.url=jdbc:sqlserver://localhost:1434;databaseName=SODIMAC_SAP_DEV;encrypt=false;trustServerCertificate=true",
        "spring.datasource.batch.url=jdbc:sqlserver://localhost:1434;databaseName=SODIMAC_BATCH_DEV;encrypt=false;trustServerCertificate=true"
})
@ActiveProfiles("local")
class AddendaDesgloseTest {

    private static final String UUID_MERC = "AAAA0001-0000-0000-0000-000000000001";
    private static final String UUID_TRAN = "AAAA0002-0000-0000-0000-000000000002";
    private static final String UUID_NC   = "AAAA0003-0000-0000-0000-000000000003";

    @Autowired private CfdiDesgloseService desglose;
    @Autowired private AddendaRepository addendaRepository;
    @Autowired @Qualifier("sapDataSource") private DataSource sapDataSource;

    // Evita que el CommandLineRunner ejecute la descarga real contra fiscal-api.
    @MockBean private InvoiceDownloadBatchService invoiceBatch;
    @MockBean private CreditNoteDownloadBatchService creditNoteBatch;

    private JdbcTemplate jdbc;

    @BeforeEach
    void limpiar() {
        jdbc = new JdbcTemplate(sapDataSource);
        // Esquema legado: todas las tablas se ligan por Uuid (no hay FKs de identidad).
        for (String uuid : new String[]{UUID_MERC, UUID_TRAN, UUID_NC}) {
            jdbc.update("DELETE FROM dbo.Addenda WHERE Uuid = ?", uuid);
            jdbc.update("DELETE FROM dbo.DetalleImpuesto WHERE Uuid = ?", uuid);
            jdbc.update("DELETE FROM dbo.Concepto WHERE Uuid = ?", uuid);
            jdbc.update("DELETE FROM dbo.Emisor WHERE Uuid = ?", uuid);
            jdbc.update("DELETE FROM dbo.Comprobante WHERE Uuid = ?", uuid);
        }
    }

    @Test
    void guardaAddendaMercanciaTipo1() throws Exception {
        desglose.desglosar(xmlMercancia(), UUID_MERC);

        Optional<AddendaEntity> opt = addendaRepository.findById(UUID_MERC);
        assertTrue(opt.isPresent(), "debe existir la addenda de mercancía");
        AddendaEntity a = opt.get();
        assertEquals(1, a.getTipo().intValue());
        assertEquals("700001", a.getExtra1());       // Proveedor
        assertEquals("900001", a.getExtra2());       // NoOC
        assertEquals("800001", a.getExtra3());       // NoRecepcion
        assertEquals("TST1001", a.getExtra4());      // Folio
        assertEquals(UUID_MERC, a.getExtra5());      // UUID
        assertEquals("AAA010101AAA", a.getExtra6()); // RFC
    }

    @Test
    void guardaAddendaTransporteTipo2() throws Exception {
        desglose.desglosar(xmlTransporte(), UUID_TRAN);

        Optional<AddendaEntity> opt = addendaRepository.findById(UUID_TRAN);
        assertTrue(opt.isPresent(), "debe existir la addenda de transporte");
        AddendaEntity a = opt.get();
        assertEquals(2, a.getTipo().intValue());
        assertEquals("700002", a.getExtra1());       // IdProveedor
        assertNull(a.getExtra2());
        assertNull(a.getExtra3());
        assertEquals("1.0", a.getExtra4());          // Version
        assertEquals("GUIA0002", a.getExtra5());     // IdGuiaEntrega
        assertEquals("V0002", a.getExtra6());        // IdViaje
    }

    @Test
    void guardaAddendaNcTipo3() throws Exception {
        desglose.desglosar(xmlNc(), UUID_NC);

        Optional<AddendaEntity> opt = addendaRepository.findById(UUID_NC);
        assertTrue(opt.isPresent(), "debe existir la addenda de NC");
        AddendaEntity a = opt.get();
        assertEquals(3, a.getTipo().intValue());
        assertEquals("700003", a.getExtra1());              // IdProveedor
        assertEquals("1.0", a.getExtra4());                 // Version
        assertEquals("Ajuste de recepcion", a.getExtra6()); // TipoNC
    }

    // ── XML de prueba (atributos con comilla simple para no escapar en Java) ──

    private String cabecera(String tipoComprobante) {
        // MetodoPago y LugarExpedicion son NOT NULL en dbo.Comprobante (esquema legado).
        return "<cfdi:Comprobante xmlns:cfdi='http://www.sat.gob.mx/cfd/4' "
                + "Version='4.0' Serie='TST' Folio='1001' Fecha='2026-07-07T10:00:00' "
                + "SubTotal='100.00' Total='116.00' Moneda='MXN' MetodoPago='PUE' "
                + "LugarExpedicion='06600' TipoDeComprobante='" + tipoComprobante + "'>"
                + "<cfdi:Emisor Rfc='AAA010101AAA' Nombre='TEST' RegimenFiscal='601'/>"
                + "<cfdi:Receptor Rfc='CSD161207R2A' Nombre='SDMHC' UsoCFDI='G01'/>";
    }

    private String timbre(String uuid) {
        return "<cfdi:Complemento><tfd:TimbreFiscalDigital "
                + "xmlns:tfd='http://www.sat.gob.mx/TimbreFiscalDigital' Version='1.1' "
                + "UUID='" + uuid + "' FechaTimbrado='2026-07-07T10:01:00'/></cfdi:Complemento>";
    }

    private String xmlMercancia() {
        return cabecera("I")
                + timbre(UUID_MERC)
                + "<cfdi:Addenda><Addenda_Sodimac_Detecno>"
                + "<RFC>AAA010101AAA</RFC><UUID>" + UUID_MERC + "</UUID><Folio>TST1001</Folio>"
                + "<NoOC>900001</NoOC><NoRecepcion>800001</NoRecepcion><Proveedor>700001</Proveedor>"
                + "</Addenda_Sodimac_Detecno></cfdi:Addenda></cfdi:Comprobante>";
    }

    private String xmlTransporte() {
        return cabecera("I")
                + timbre(UUID_TRAN)
                + "<cfdi:Addenda><Addenda_Transportistas_Sodimac_Detecno>"
                + "<Version>1.0</Version><IdProveedor>700002</IdProveedor>"
                + "<IdGuiaEntrega>GUIA0002</IdGuiaEntrega><IdViaje>V0002</IdViaje>"
                + "</Addenda_Transportistas_Sodimac_Detecno></cfdi:Addenda></cfdi:Comprobante>";
    }

    private String xmlNc() {
        return cabecera("E")
                + timbre(UUID_NC)
                + "<cfdi:Addenda><Addenda_NotaCredito_Sodimac_Detecno>"
                + "<Version>1.0</Version><IdProveedor>700003</IdProveedor>"
                + "<TipoNC>Ajuste de recepcion</TipoNC>"
                + "</Addenda_NotaCredito_Sodimac_Detecno></cfdi:Addenda></cfdi:Comprobante>";
    }
}
