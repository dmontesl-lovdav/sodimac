package com.sodimac.batch.process.model.entity.sap;

import javax.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Traslado")
public class TrasladoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_traslado")
    private Integer idTraslado;

    @Column(name = "id_impuesto", nullable = false)
    private Integer idImpuesto;

    @Column(name = "base", precision = 18, scale = 6)
    private BigDecimal base;

    @Column(name = "impuesto", nullable = false, length = 5)
    private String impuesto;

    @Column(name = "tipo_factor", nullable = false, length = 10)
    private String tipoFactor;

    @Column(name = "tasa_o_cuota", precision = 18, scale = 6)
    private BigDecimal tasaOCuota;

    @Column(name = "importe", precision = 18, scale = 6)
    private BigDecimal importe;

    public TrasladoEntity() {}

    public Integer getIdTraslado() { return idTraslado; }
    public void setIdTraslado(Integer idTraslado) { this.idTraslado = idTraslado; }

    public Integer getIdImpuesto() { return idImpuesto; }
    public void setIdImpuesto(Integer idImpuesto) { this.idImpuesto = idImpuesto; }

    public BigDecimal getBase() { return base; }
    public void setBase(BigDecimal base) { this.base = base; }

    public String getImpuesto() { return impuesto; }
    public void setImpuesto(String impuesto) { this.impuesto = impuesto; }

    public String getTipoFactor() { return tipoFactor; }
    public void setTipoFactor(String tipoFactor) { this.tipoFactor = tipoFactor; }

    public BigDecimal getTasaOCuota() { return tasaOCuota; }
    public void setTasaOCuota(BigDecimal tasaOCuota) { this.tasaOCuota = tasaOCuota; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
