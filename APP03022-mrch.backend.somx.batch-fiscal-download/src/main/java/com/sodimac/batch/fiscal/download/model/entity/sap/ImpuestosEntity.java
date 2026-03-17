package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Impuestos")
public class ImpuestosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_impuesto")
    private Integer idImpuesto;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "total_impuestos_trasladados", precision = 18, scale = 6)
    private BigDecimal totalImpuestosTrasladados;

    @Column(name = "total_impuestos_retenidos", precision = 18, scale = 6)
    private BigDecimal totalImpuestosRetenidos;

    public ImpuestosEntity() {}

    public Integer getIdImpuesto() { return idImpuesto; }
    public void setIdImpuesto(Integer idImpuesto) { this.idImpuesto = idImpuesto; }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public BigDecimal getTotalImpuestosTrasladados() { return totalImpuestosTrasladados; }
    public void setTotalImpuestosTrasladados(BigDecimal totalImpuestosTrasladados) { this.totalImpuestosTrasladados = totalImpuestosTrasladados; }

    public BigDecimal getTotalImpuestosRetenidos() { return totalImpuestosRetenidos; }
    public void setTotalImpuestosRetenidos(BigDecimal totalImpuestosRetenidos) { this.totalImpuestosRetenidos = totalImpuestosRetenidos; }
}
