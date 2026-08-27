package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Impuesto a nivel concepto en el destino SAP legado (SODIMAC_SAP_DEV.dbo.DetalleImpuesto).
 * PK compuesta (Uuid, IdPadre, ClaveProdServ, TipoImpuesto, Impuesto); IdPadre referencia
 * el IdPadre identity del Concepto. TipoImpuesto: 1 = traslado, 2 = retención.
 */
@Entity
@Table(name = "DetalleImpuesto")
@IdClass(DetalleImpuestoId.class)
public class DetalleImpuestoEntity {

    public static final String TIPO_TRASLADO = "1";
    public static final String TIPO_RETENCION = "2";

    @Id
    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Id
    @Column(name = "IdPadre")
    private long idPadre;

    @Id
    @Column(name = "ClaveProdServ", length = 8)
    private String claveProdServ;

    @Id
    @Column(name = "TipoImpuesto", length = 1)
    private String tipoImpuesto;

    @Id
    @Column(name = "Impuesto", length = 3)
    private String impuesto;

    @Column(name = "Base", precision = 18, scale = 6)
    private BigDecimal base;

    @Column(name = "TipoFactor", length = 10)
    private String tipoFactor;

    @Column(name = "TasaOCuota", precision = 18, scale = 6)
    private BigDecimal tasaOCuota;

    @Column(name = "Importe", precision = 18, scale = 6)
    private BigDecimal importe;

    public DetalleImpuestoEntity() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public long getIdPadre() { return idPadre; }
    public void setIdPadre(long idPadre) { this.idPadre = idPadre; }

    public String getClaveProdServ() { return claveProdServ; }
    public void setClaveProdServ(String claveProdServ) { this.claveProdServ = claveProdServ; }

    public String getTipoImpuesto() { return tipoImpuesto; }
    public void setTipoImpuesto(String tipoImpuesto) { this.tipoImpuesto = tipoImpuesto; }

    public String getImpuesto() { return impuesto; }
    public void setImpuesto(String impuesto) { this.impuesto = impuesto; }

    public BigDecimal getBase() { return base; }
    public void setBase(BigDecimal base) { this.base = base; }

    public String getTipoFactor() { return tipoFactor; }
    public void setTipoFactor(String tipoFactor) { this.tipoFactor = tipoFactor; }

    public BigDecimal getTasaOCuota() { return tasaOCuota; }
    public void setTasaOCuota(BigDecimal tasaOCuota) { this.tasaOCuota = tasaOCuota; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
