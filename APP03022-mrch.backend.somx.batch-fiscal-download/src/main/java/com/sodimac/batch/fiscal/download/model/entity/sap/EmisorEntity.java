package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

@Entity
@Table(name = "Emisor")
public class EmisorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emisor")
    private Integer idEmisor;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "rfc", nullable = false, length = 13)
    private String rfc;

    @Column(name = "nombre", length = 300)
    private String nombre;

    @Column(name = "regimen_fiscal", length = 5)
    private String regimenFiscal;

    public EmisorEntity() {}

    public Integer getIdEmisor() { return idEmisor; }
    public void setIdEmisor(Integer idEmisor) { this.idEmisor = idEmisor; }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegimenFiscal() { return regimenFiscal; }
    public void setRegimenFiscal(String regimenFiscal) { this.regimenFiscal = regimenFiscal; }
}
