package com.sodimac.fiscal.api.model.enums;

/**
 * Enumeración para los tipos de documentos fiscales mexicanos CFDI.
 *
 * Define los diferentes tipos de comprobantes fiscales digitales
 * según las reglas del SAT (Sistema de Administración Tributaria).
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
public enum TipoDocumentoFiscal {

    /**
     * Factura - Comprobante de ingresos
     */
    FACTURA("I", "Factura", "CFDI v4.0"),

    /**
     * Nota de Crédito - Comprobante de egresos
     */
    NOTA_CREDITO("E", "Nota de Crédito", "CFDI v4.0"),

    /**
     * Factura con Complemento CartaPorte - Comprobante de traslado
     */
    FACTURA_CARTA_PORTE("T", "Factura con CartaPorte", "CartaPorte v3.1"),

    /**
     * Complemento de Pago - Comprobante de pago
     */
    COMPLEMENTO_PAGO("P", "Complemento de Pago", "Pagos v2.0");

    private final String codigo;
    private final String descripcion;
    private final String version;

    TipoDocumentoFiscal(String codigo, String descripcion, String version) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.version = version;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Obtiene el tipo de documento fiscal por su código.
     *
     * @param codigo Código del tipo de comprobante
     * @return TipoDocumentoFiscal correspondiente
     * @throws IllegalArgumentException si el código no es válido
     */
    public static TipoDocumentoFiscal fromCodigo(String codigo) {
        for (TipoDocumentoFiscal tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de documento fiscal no válido: " + codigo);
    }
}