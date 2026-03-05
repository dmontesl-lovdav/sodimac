package com.sodimac.fiscal.api.model.enums;

import lombok.Getter;

/**
 * Enum de estatus de Factura (Tipo I).
 *
 * Basado en: https://confluence.falabella.tech/x/pTyzLQ
 *
 * Orden de estatus y transiciones permitidas según flujo de negocio.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Getter
public enum InvoiceStatus {

    RECHAZO_COMERCIAL(0, "Rechazo Comercial",
            "Facturas rechazadas o con error en los datos para iniciar el proceso de pago",
            false, new Integer[]{}),

    PENDIENTE_ADDENDA(1, "Pendiente Addenda",
            "Factura sin captura de Addenda",
            false, new Integer[]{2, 3, 13}),

    RECIBIDO_PARCIAL(2, "Recibido Parcial",
            "El monto de la factura es mayor al valor de la OC",
            false, new Integer[]{3, 13}),

    PENDIENTE_CONTABILIZAR(3, "Pendiente de Contabilizar",
            "Factura registrada pendiente de contabilizar en SAP",
            false, new Integer[]{4}),

    PROCESO_DESCARGA(4, "Proceso de descarga",
            "Factura descargada para ser enviada a SODIMAC SAP PROD",
            true, new Integer[]{5, 11, 14}),

    DESGLOSE_FACTURA(5, "Desglose de factura",
            "Factura desglosada para ser enviada a SAPITO",
            true, new Integer[]{6, 11}),

    PENDIENTE_ENVIO_CONTABILIZAR(6, "Pendiente de envío a contabilizar",
            "Factura pendiente de enviar de SAPITO a i213",
            true, new Integer[]{7, 11}),

    PENDIENTE_PAGO(7, "Pendiente de Pago",
            "Factura contabilizada, lista para iniciar con el proceso de pago",
            true, new Integer[]{8}),

    PAGADO(8, "Pagado",
            "Factura pagada al proveedor",
            true, new Integer[]{9}),

    PENDIENTE_COMPLEMENTO(9, "Pendiente de complemento",
            "Factura pendiente de relacionar un complemento de pago",
            false, new Integer[]{10}),

    COMPLETADO(10, "Completado",
            "Factura relacionada con un complemento de pago",
            false, new Integer[]{}),

    RECHAZO_CONTABLE(11, "Rechazo Contable",
            "Factura pendiente por contabilizar, rechazado por temas contables",
            true, new Integer[]{7}),

    NO_VALIDO_FISCAL(12, "No valido fiscal",
            "Factura invalida",
            false, new Integer[]{}),

    PAGO_MANUAL(13, "Pago Manual",
            "Orden de compra pagada manualmente",
            false, new Integer[]{}),

    ERROR_DESGLOSE_FACTURA(14, "Error en el desglose de la factura",
            "Error al intentar desglosar la factura descargada (STM-719)",
            true, new Integer[]{3});

    private final Integer codigo;
    private final String nombre;
    private final String descripcion;
    private final boolean automatico;
    private final Integer[] estatusSiguientesPermitidos;

    InvoiceStatus(Integer codigo, String nombre, String descripcion, boolean automatico, Integer[] estatusSiguientesPermitidos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.automatico = automatico;
        this.estatusSiguientesPermitidos = estatusSiguientesPermitidos;
    }

    /**
     * Obtiene el enum por código.
     */
    public static InvoiceStatus fromCodigo(Integer codigo) {
        for (InvoiceStatus status : values()) {
            if (status.getCodigo().equals(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estatus de factura no válido: " + codigo);
    }

    /**
     * Valida si la transición al nuevo estatus es permitida.
     */
    public boolean puedeTransicionarA(Integer nuevoEstatus) {
        for (Integer estatusPermitido : estatusSiguientesPermitidos) {
            if (estatusPermitido.equals(nuevoEstatus)) {
                return true;
            }
        }
        return false;
    }
}
