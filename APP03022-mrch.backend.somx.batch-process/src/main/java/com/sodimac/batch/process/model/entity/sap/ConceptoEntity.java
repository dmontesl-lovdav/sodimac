package com.sodimac.batch.process.model.entity.sap;

import javax.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Concepto")
public class ConceptoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_concepto")
    private Integer idConcepto;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "clave_prod_serv", nullable = false, length = 10)
    private String claveProdServ;

    @Column(name = "no_identificacion", length = 100)
    private String noIdentificacion;

    @Column(name = "cantidad", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "clave_unidad", nullable = false, length = 5)
    private String claveUnidad;

    @Column(name = "unidad", length = 50)
    private String unidad;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "valor_unitario", nullable = false, precision = 18, scale = 6)
    private BigDecimal valorUnitario;

    @Column(name = "importe", nullable = false, precision = 18, scale = 6)
    private BigDecimal importe;

    @Column(name = "descuento", precision = 18, scale = 6)
    private BigDecimal descuento;

    @Column(name = "objeto_imp", length = 5)
    private String objetoImp;

    public ConceptoEntity() {}

    public Integer getIdConcepto() { return idConcepto; }
    public void setIdConcepto(Integer idConcepto) { this.idConcepto = idConcepto; }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public String getClaveProdServ() { return claveProdServ; }
    public void setClaveProdServ(String claveProdServ) { this.claveProdServ = claveProdServ; }

    public String getNoIdentificacion() { return noIdentificacion; }
    public void setNoIdentificacion(String noIdentificacion) { this.noIdentificacion = noIdentificacion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getClaveUnidad() { return claveUnidad; }
    public void setClaveUnidad(String claveUnidad) { this.claveUnidad = claveUnidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getObjetoImp() { return objetoImp; }
    public void setObjetoImp(String objetoImp) { this.objetoImp = objetoImp; }
}
