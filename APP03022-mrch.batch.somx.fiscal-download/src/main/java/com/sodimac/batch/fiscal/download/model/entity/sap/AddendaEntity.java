package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;

/**
 * Addenda Sodimac en el destino SAP (SODIMAC_SAP_DEV.dbo.Addenda).
 *
 * Tabla genérica ligada al comprobante por {@code Uuid} (= folio fiscal SAT), NO por id_comprobante.
 * Estructura de columnas Extra1..6 (varchar 50). Mapeo del nodo CFDI
 * {@code <cfdi:Addenda><Addenda_Sodimac_Detecno>} para Tipo=1 (mercancía):
 * Extra1=Proveedor, Extra2=NoOC, Extra3=NoRecepcion, Extra4=Folio (serie+folio),
 * Extra5=UUID (folio fiscal), Extra6=RFC emisor.
 * Tipo=2 (transporte/CartaPorte) usa otra semántica y no se cubre aquí.
 */
@Entity
@Table(name = "Addenda")
public class AddendaEntity {

    @Id
    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Column(name = "Extra1", length = 50)
    private String extra1;

    @Column(name = "Extra2", length = 50)
    private String extra2;

    @Column(name = "Extra3", length = 50)
    private String extra3;

    @Column(name = "Extra4", length = 50)
    private String extra4;

    @Column(name = "Extra5", length = 50)
    private String extra5;

    @Column(name = "Extra6", length = 50)
    private String extra6;

    @Column(name = "Tipo")
    private Integer tipo;

    public AddendaEntity() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getExtra1() { return extra1; }
    public void setExtra1(String extra1) { this.extra1 = extra1; }

    public String getExtra2() { return extra2; }
    public void setExtra2(String extra2) { this.extra2 = extra2; }

    public String getExtra3() { return extra3; }
    public void setExtra3(String extra3) { this.extra3 = extra3; }

    public String getExtra4() { return extra4; }
    public void setExtra4(String extra4) { this.extra4 = extra4; }

    public String getExtra5() { return extra5; }
    public void setExtra5(String extra5) { this.extra5 = extra5; }

    public String getExtra6() { return extra6; }
    public void setExtra6(String extra6) { this.extra6 = extra6; }

    public Integer getTipo() { return tipo; }
    public void setTipo(Integer tipo) { this.tipo = tipo; }
}
