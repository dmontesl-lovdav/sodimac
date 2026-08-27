package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Concepto en el destino SAP legado (SODIMAC_SAP_DEV.dbo.Concepto).
 * Ligado al comprobante por Uuid. IdPadre es identity y es la referencia que usa
 * DetalleImpuesto (junto con Uuid y ClaveProdServ) para ligar los impuestos del concepto.
 * Cantidad es varchar en el esquema legado: se guarda la cadena cruda del atributo CFDI.
 */
@Entity
@Table(name = "Concepto")
public class ConceptoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPadre")
    private long idPadre;

    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Column(name = "ClaveProdServ", length = 8)
    private String claveProdServ;

    @Column(name = "Cantidad", length = 20)
    private String cantidad;

    @Column(name = "ClaveUnidad", length = 3)
    private String claveUnidad;

    @Column(name = "Unidad", length = 20)
    private String unidad;

    @Column(name = "Descripcion", length = 1000)
    private String descripcion;

    @Column(name = "ValorUnitario", precision = 18, scale = 6)
    private BigDecimal valorUnitario;

    @Column(name = "Importe", precision = 18, scale = 6)
    private BigDecimal importe;

    public ConceptoEntity() {}

    public long getIdPadre() { return idPadre; }
    public void setIdPadre(long idPadre) { this.idPadre = idPadre; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getClaveProdServ() { return claveProdServ; }
    public void setClaveProdServ(String claveProdServ) { this.claveProdServ = claveProdServ; }

    public String getCantidad() { return cantidad; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

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
}
