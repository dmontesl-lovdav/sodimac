package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

/**
 * Emisor en el destino SAP legado (SODIMAC_SAP_DEV.dbo.Emisor).
 * Ligado al comprobante por Uuid (un emisor por comprobante); la tabla no tiene
 * columna identity ni PK declarada, se usa Uuid como @Id.
 */
@Entity
@Table(name = "Emisor")
public class EmisorEntity {

    @Id
    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Column(name = "Rfc", length = 13)
    private String rfc;

    @Column(name = "Nombre", length = 254)
    private String nombre;

    @Column(name = "Regimen", length = 3)
    private String regimen;

    public EmisorEntity() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }
}
