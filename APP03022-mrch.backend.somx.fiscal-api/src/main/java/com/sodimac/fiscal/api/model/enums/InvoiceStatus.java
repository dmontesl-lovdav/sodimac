package com.sodimac.fiscal.api.model.enums;

import lombok.Getter;

/**
 * Enum de estatus de Factura (Tipo I) — Tren de Estatus v1.0 (2).
 *
 * Renumeración completa según Tren_Estatus_Portal_FBC_v1.0.xlsx (Ivan, 2026-06-02),
 * actualizado a v1.0 (2) (Ivan, 2026-07-30): el estatus 16 pasa de "Estructura inválida"
 * a "Error envío DMS" y se agregan 19 (Error envío SAPITO), 20 (Error contabilización),
 * 21 (Pendiente movimiento contable) y 22 (Error desglose contable).
 *
 * NOTA: el tren autoritativo vive en shared_catalogs.status_train (lo consulta util-api).
 * Estas transiciones son solo el FALLBACK cuando status-train.api.enabled=false.
 */
@Getter
public enum InvoiceStatus {

    RECHAZO_COMERCIAL(1, "Rechazo Comercial",
            "Factura rechazada o con error en los datos para iniciar el proceso de pago",
            false, new Integer[]{}),

    RECIBIDO_PARCIAL(2, "Recibido Parcial",
            "El monto de la factura es mayor al valor de la OC",
            false, new Integer[]{1, 3, 18}),

    RECIBIDA(3, "Recibida",
            "Factura registrada y validada, pendiente de contabilizar en SAP",
            false, new Integer[]{4, 5, 21, 22}),

    EN_PROCESO_DESCARGA(4, "En proceso de descarga",
            "Factura descargada para ser enviada a Sodimac SAP PROD",
            true, new Integer[]{5, 6, 21, 22}),

    DESGLOSE_FACTURA(5, "Desglose de factura",
            "Factura desglosada para ser enviada a SAPITO",
            true, new Integer[]{7, 19}),

    ERROR_DESGLOSE(6, "Error en el desglose de la factura",
            "Error al intentar desglosar la factura descargada",
            true, new Integer[]{3}),

    PENDIENTE_REGISTRO_SAPITO(7, "Pendiente registro en SAPITO",
            "Factura pendiente de registrar en SAPITO",
            true, new Integer[]{8, 16}),

    PENDIENTE_ENVIO_I213(8, "Pendiente de envío a i213",
            "Factura pendiente de enviar de SAPITO a i213",
            true, new Integer[]{9, 19}),

    FACTURA_ENVIADA_I213(9, "Factura enviada a i213",
            "Factura enviada al sistema i213 en espera de resultado",
            true, new Integer[]{10, 11, 14, 20}),

    PENDIENTE_CONTABILIZAR(10, "Pendiente de contabilizar",
            "Factura pendiente por contabilizar en SAP",
            true, new Integer[]{11, 14, 20}),

    PENDIENTE_PAGO(11, "Pendiente de Pago",
            "Factura contabilizada, lista para iniciar con el proceso de pago",
            true, new Integer[]{12}),

    PENDIENTE_COMPLEMENTO(12, "Pendiente de complemento",
            "Factura pendiente de relacionar un complemento de pago",
            false, new Integer[]{13}),

    COMPLETADO(13, "Completado",
            "Factura relacionada con un complemento de pago",
            false, new Integer[]{}),

    RECHAZO_CONTABLE(14, "Rechazo Contable",
            "Factura rechazada por temas contables, regresa a proceso en i213",
            true, new Integer[]{8}),

    NO_VALIDO_FISCAL(15, "No válido fiscal",
            "Factura con validación fiscal inválida",
            false, new Integer[]{}),

    ERROR_ENVIO_DMS(16, "Error envío DMS",
            "Error al intentar enviar la factura de SAPITO a i213 (DMS)",
            true, new Integer[]{3}),

    ERROR_ENVIO_I213(17, "Error envío i213",
            "Error al enviar la factura a i213, regresa a proceso de envío",
            true, new Integer[]{3}),

    PAGO_MANUAL(18, "Pago Manual",
            "Orden de compra pagada manualmente",
            false, new Integer[]{}),

    ERROR_ENVIO_SAPITO(19, "Error envío SAPITO",
            "Error al intentar enviar el movimiento contable a la BD SAPITO",
            false, new Integer[]{3}),

    ERROR_CONTABILIZACION(20, "Error en la contabilización",
            "Error al intentar contabilizar la factura en SAP",
            false, new Integer[]{3}),

    PENDIENTE_MOV_CONTABLE(21, "Pendiente movimiento contable",
            "Factura pendiente de generar el envío de AP desde la BD Sodimac SAP",
            true, new Integer[]{5, 22}),

    ERROR_DESGLOSE_CONTABLE(22, "Error en el desglose contable",
            "Error al intentar contabilizar la factura por un error en la interfase",
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

    public static InvoiceStatus fromCodigo(Integer codigo) {
        for (InvoiceStatus status : values()) {
            if (status.getCodigo().equals(codigo)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estatus de factura no válido: " + codigo);
    }

    public boolean puedeTransicionarA(Integer nuevoEstatus) {
        for (Integer estatusPermitido : estatusSiguientesPermitidos) {
            if (estatusPermitido.equals(nuevoEstatus)) {
                return true;
            }
        }
        return false;
    }
}
