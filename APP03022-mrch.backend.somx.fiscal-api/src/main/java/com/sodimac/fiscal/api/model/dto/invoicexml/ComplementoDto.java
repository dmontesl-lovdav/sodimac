package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para el elemento Complemento del CFDI v4.0.
 *
 * Soporta multiples versiones de CartaPorte:
 * - CartaPorte 3.0: namespace http://www.sat.gob.mx/CartaPorte30
 * - CartaPorte 3.1: namespace http://www.sat.gob.mx/CartaPorte31
 *
 * @author Sodimac Tech Team
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ComplementoDto {

    @XmlElement(name = "TimbreFiscalDigital", namespace = "http://www.sat.gob.mx/TimbreFiscalDigital")
    private TimbreFiscalDigitalDto timbreFiscalDigital;

    // CartaPorte version 3.0
    @XmlElement(name = "CartaPorte", namespace = "http://www.sat.gob.mx/CartaPorte30")
    private CartaPorteDto cartaPorte30;

    // CartaPorte version 3.1
    @XmlElement(name = "CartaPorte", namespace = "http://www.sat.gob.mx/CartaPorte31")
    private CartaPorteDto cartaPorte31;

    /**
     * Obtiene el CartaPorte disponible (3.1 tiene prioridad sobre 3.0).
     *
     * @return CartaPorteDto si existe alguna version, null si no hay CartaPorte
     */
    public CartaPorteDto getCartaPorte() {
        return cartaPorte31 != null ? cartaPorte31 : cartaPorte30;
    }

    /**
     * Indica si el documento tiene complemento CartaPorte.
     *
     * @return true si tiene CartaPorte 3.0 o 3.1
     */
    public boolean hasCartaPorte() {
        return cartaPorte30 != null || cartaPorte31 != null;
    }

    /**
     * Obtiene la version del CartaPorte presente.
     *
     * @return "3.1", "3.0" o null si no hay CartaPorte
     */
    public String getCartaPorteVersion() {
        if (cartaPorte31 != null) {
            return "3.1";
        } else if (cartaPorte30 != null) {
            return "3.0";
        }
        return null;
    }

    /**
     * Establece el CartaPorte (por defecto usa version 3.1).
     * Metodo de compatibilidad para codigo existente.
     *
     * @param cartaPorte el CartaPorteDto a establecer
     */
    public void setCartaPorte(CartaPorteDto cartaPorte) {
        this.cartaPorte31 = cartaPorte;
    }
}
