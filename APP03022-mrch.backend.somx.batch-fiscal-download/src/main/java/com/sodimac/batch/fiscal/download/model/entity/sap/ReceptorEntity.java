package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

@Entity
@Table(name = "Receptor")
public class ReceptorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receptor")
    private Integer idReceptor;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "rfc", nullable = false, length = 13)
    private String rfc;

    @Column(name = "nombre", length = 300)
    private String nombre;

    @Column(name = "uso_cfdi", length = 5)
    private String usoCfdi;

    @Column(name = "regimen_fiscal", length = 5)
    private String regimenFiscal;

    @Column(name = "domicilio_fiscal", length = 5)
    private String domicilioFiscal;

    public ReceptorEntity() {}

    public Integer getIdReceptor() { return idReceptor; }
    public void setIdReceptor(Integer idReceptor) { this.idReceptor = idReceptor; }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsoCfdi() { return usoCfdi; }
    public void setUsoCfdi(String usoCfdi) { this.usoCfdi = usoCfdi; }

    public String getRegimenFiscal() { return regimenFiscal; }
    public void setRegimenFiscal(String regimenFiscal) { this.regimenFiscal = regimenFiscal; }

    public String getDomicilioFiscal() { return domicilioFiscal; }
    public void setDomicilioFiscal(String domicilioFiscal) { this.domicilioFiscal = domicilioFiscal; }
}
