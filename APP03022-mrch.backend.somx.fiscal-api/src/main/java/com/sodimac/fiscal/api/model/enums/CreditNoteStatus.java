package com.sodimac.fiscal.api.model.enums;

import lombok.Getter;

/**
 * Enum de estatus de Nota de Crédito (Tipo E).
 *
 * Basado en: https://confluence.falabella.tech/x/qjyzLQ
 *
 * Orden de estatus y transiciones permitidas según flujo de negocio.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Getter
public enum CreditNoteStatus {

    RECHAZO_COMERCIAL(0, "Rechazo Comercial",
            "Notas de crédito rechazadas o con error en los datos",
            false, new Integer[]{}),

    PENDIENTE_ADDENDA(1, "Pendiente Addenda",
            "Nota de crédito sin captura de Addenda",
            false, new Integer[]{2, 3}),

    RECIBIDO_PARCIAL(2, "Recibido Parcial",
            "El monto de la NC es mayor al valor permitido",
            false, new Integer[]{3}),

    PENDIENTE_CONTABILIZAR(3, "Pendiente de Contabilizar",
            "Nota de crédito registrada pendiente de contabilizar en SAP",
            false, new Integer[]{4}),

    PROCESO_DESCARGA(4, "Proceso de descarga",
            "NC descargada para ser enviada a SODIMAC SAP PROD",
            true, new Integer[]{5, 11}),

    DESGLOSE_NC(5, "Desglose de NC",
            "NC desglosada para ser enviada a SAPITO",
            true, new Integer[]{6, 11}),

    PENDIENTE_ENVIO_CONTABILIZAR(6, "Pendiente de envío a contabilizar",
            "NC pendiente de enviar de SAPITO a i213",
            true, new Integer[]{7, 11}),

    APLICADO(7, "Aplicado",
            "Nota de crédito aplicada",
            true, new Integer[]{8}),

    COMPLETADO(8, "Completado",
            "Nota de crédito completada",
            false, new Integer[]{}),

    RECHAZO_CONTABLE(11, "Rechazo Contable",
            "NC pendiente por contabilizar, rechazado por temas contables",
            true, new Integer[]{7}),

    NO_VALIDO_FISCAL(12, "No valido fiscal",
            "Nota de crédito invalida",
            false, new Integer[]{});

    private final Integer codigo;
    private final String nombre;
    private final String descripcion;
    private final boolean automatico;
    private final Integer[] estatusSiguientesPermitidos;

    CreditNoteStatus(Integer codigo, String nombre, String descripcion, boolean automatico, Integer[] estatusSiguientesPermitidos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.automatico = automatico;
        this.estatusSiguientesPermitidos = estatusSiguientesPermitidos;
    }

    /**
     * Obtiene el enum por código.
     */
    public static CreditNoteStatus fromCodigo(Integer codigo) {
        for (CreditNoteStatus status : values()) {
            if (status.getCodigo().equals(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estatus de nota de crédito no válido: " + codigo);
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
