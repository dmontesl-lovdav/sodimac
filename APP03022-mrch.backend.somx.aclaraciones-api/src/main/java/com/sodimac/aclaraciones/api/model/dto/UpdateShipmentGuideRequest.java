// src/main/java/com/sodimac/aclaraciones/api/model/dto/UpdateShipmentGuideRequest.java
package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.*;

/**
 * Campos opcionales: solo se envían los que quieras modificar.
 */
public class UpdateShipmentGuideRequest {

    @Min(value = 0, message = "ERR_FIELD_STATUS|Estatus inválido")
    private Integer status;

    @Pattern(regexp = "^[A-Z0-9-]{1,15}$", message = "ERR_FIELD_PLATE|Placa inválida")
    private String plate;

    @Pattern(regexp = "^[A-Z0-9-]{0,15}$", message = "ERR_FIELD_TRAILER_PLATE|Placa remolque inválida")
    private String trailerPlate;

    private String cartaPorteXml;
    private String cartaPorteCsv;

    /* ---- getters / setters ---- */
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getTrailerPlate() {
        return trailerPlate;
    }

    public void setTrailerPlate(String trailerPlate) {
        this.trailerPlate = trailerPlate;
    }

    public String getCartaPorteXml() {
        return cartaPorteXml;
    }

    public void setCartaPorteXml(String cartaPorteXml) {
        this.cartaPorteXml = cartaPorteXml;
    }

    public String getCartaPorteCsv() {
        return cartaPorteCsv;
    }

    public void setCartaPorteCsv(String cartaPorteCsv) {
        this.cartaPorteCsv = cartaPorteCsv;
    }
}
