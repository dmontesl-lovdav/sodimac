package com.sodimac.batch.fiscal.download.mapper;

import com.sodimac.batch.fiscal.download.model.entity.sap.ComprobanteEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComprobanteMapper {

    /** Formato legado de FechaCadena: sin ceros a la izquierda en día/mes/hora (ej. 6/7/2026 9:05:01). */
    private static final DateTimeFormatter FECHA_CADENA = DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss");

    /** Estatus inicial en el esquema legado: pendiente de envío (FechaEnvio null). */
    private static final int ESTATUS_PENDIENTE_ENVIO = 0;

    public static ComprobanteEntity toEntity(String uuid, String invoiceUuid, String version,
            String serie, String folio, LocalDateTime fecha, BigDecimal subTotal, BigDecimal total,
            BigDecimal descuento, String moneda, String tipoCambio, String tipoDeComprobante,
            String metodoPago, String formaPago, String condicionDePago, String lugarExpedicion,
            String xmlCompleto) {
        ComprobanteEntity e = new ComprobanteEntity();
        e.setUuid(uuid);
        e.setFiscalUuid(uuid);
        e.setInvoiceUuid(invoiceUuid);
        e.setVersion(version);
        e.setSerie(serie);
        e.setFolio(folio);
        e.setFecha(fecha);
        e.setFechaCadena(fecha != null ? FECHA_CADENA.format(fecha) : null);
        e.setSubTotal(subTotal);
        e.setTotal(total);
        e.setDescuento(descuento);
        e.setMoneda(moneda);
        e.setTipoCambio(tipoCambio);
        e.setTipoDeComprobante(tipoDeComprobante);
        e.setMetodoPago(metodoPago);
        e.setFormaPago(formaPago);
        e.setCondicionDePago(condicionDePago);
        e.setLugarExpedicion(lugarExpedicion);
        e.setXmlCompleto(xmlCompleto);
        e.setEstatus(ESTATUS_PENDIENTE_ENVIO);
        e.setFechaEnvio(null);
        return e;
    }
}
