package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ComprobanteEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ComprobanteMapper {

    public static ComprobanteEntity toEntity(String invoiceUuid, String version, String serie,
                                              String folio, LocalDateTime fecha, BigDecimal subtotal,
                                              BigDecimal total, BigDecimal descuento, String moneda,
                                              BigDecimal tipoCambio, String tipoComprobante,
                                              String metodoPago, String formaPago,
                                              String condicionesPago, String lugarExpedicion,
                                              String exportacion, String noCertificado,
                                              String sello, String certificado, String xmlCompleto) {
        ComprobanteEntity entity = new ComprobanteEntity();
        entity.setFiscalUuid("");
        entity.setInvoiceUuid(invoiceUuid);
        entity.setVersion(version);
        entity.setSerie(serie);
        entity.setFolio(folio);
        entity.setFecha(fecha);
        entity.setSubtotal(subtotal);
        entity.setTotal(total);
        entity.setDescuento(descuento);
        entity.setMoneda(moneda);
        entity.setTipoCambio(tipoCambio);
        entity.setTipoComprobante(tipoComprobante);
        entity.setMetodoPago(metodoPago);
        entity.setFormaPago(formaPago);
        entity.setCondicionesPago(condicionesPago);
        entity.setLugarExpedicion(lugarExpedicion);
        entity.setExportacion(exportacion);
        entity.setNoCertificado(noCertificado);
        entity.setSello(sello);
        entity.setCertificado(certificado);
        entity.setXmlCompleto(xmlCompleto);
        entity.setEstatusProceso("PROCESADO");
        entity.setFechaRegistro(LocalDateTime.now());
        return entity;
    }
}
