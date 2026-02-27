package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para el complemento Pagos v2.0 CFDI - Recepción de pagos.
 *
 * El complemento Pagos se utiliza para documentar la recepción de pagos
 * realizados por el contribuyente. Incluye información sobre los totales
 * de impuestos, detalles de cada pago y documentos relacionados.
 *
 * Componentes principales:
 * - Totales de impuestos retenidos y trasladados
 * - Lista de pagos individuales con formas de pago
 * - Documentos relacionados con cada pago
 * - Impuestos calculados por cada pago
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlRootElement(name = "Pagos", namespace = "http://www.sat.gob.mx/Pagos20")
@XmlAccessorType(XmlAccessType.FIELD)
public class PagosDto {

    @XmlElement(name = "Totales")
    private TotalesPagosDto totales;

    @XmlElement(name = "Pago")
    private List<PagoDto> pagos;
}