package com.sodimac.batch.process.model.entity.sap;

import javax.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DetalleImpuesto")
public class DetalleImpuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "id_concepto", nullable = false)
    private Integer idConcepto;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // TRASLADO o RETENCION

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

    public DetalleImpuestoEntity() {}

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getIdConcepto() { return idConcepto; }
    public void setIdConcepto(Integer idConcepto) { this.idConcepto = idConcepto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

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
