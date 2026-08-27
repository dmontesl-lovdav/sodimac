package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

/**
 * Receptor en el destino SAP legado (SODIMAC_SAP_DEV.dbo.Receptor).
 * Ligado al comprobante por Uuid. Ver docs/receptor-ddl.sql.
 */
@Entity
@Table(name = "Receptor")
public class ReceptorEntity {

    @Id
    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Column(name = "Nombre", nullable = false, length = 254)
    private String nombre;

    @Column(name = "Rfc", nullable = false, length = 13)
    private String rfc;

    @Column(name = "Regimen", nullable = false, length = 3)
    private String regimen;

    @Column(name = "UsoCFDI", nullable = false, length = 4)
    private String usoCfdi;

    @Column(name = "DomicilioFiscal", length = 5)
    private String domicilioFiscal;

    public ReceptorEntity() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }

    public String getUsoCfdi() { return usoCfdi; }
    public void setUsoCfdi(String usoCfdi) { this.usoCfdi = usoCfdi; }

    public String getDomicilioFiscal() { return domicilioFiscal; }
    public void setDomicilioFiscal(String domicilioFiscal) { this.domicilioFiscal = domicilioFiscal; }
}
