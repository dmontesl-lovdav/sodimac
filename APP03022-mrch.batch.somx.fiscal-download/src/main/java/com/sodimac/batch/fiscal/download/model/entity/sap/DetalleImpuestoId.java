package com.sodimac.batch.fiscal.download.model.entity.sap;

import java.io.Serializable;
import java.util.Objects;

/** PK compuesta de DetalleImpuesto (Uuid, IdPadre, ClaveProdServ, TipoImpuesto, Impuesto). */
public class DetalleImpuestoId implements Serializable {

    private String uuid;
    private long idPadre;
    private String claveProdServ;
    private String tipoImpuesto;
    private String impuesto;

    public DetalleImpuestoId() {}

    public DetalleImpuestoId(String uuid, long idPadre, String claveProdServ,
                              String tipoImpuesto, String impuesto) {
        this.uuid = uuid;
        this.idPadre = idPadre;
        this.claveProdServ = claveProdServ;
        this.tipoImpuesto = tipoImpuesto;
        this.impuesto = impuesto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleImpuestoId that = (DetalleImpuestoId) o;
        return idPadre == that.idPadre
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(claveProdServ, that.claveProdServ)
                && Objects.equals(tipoImpuesto, that.tipoImpuesto)
                && Objects.equals(impuesto, that.impuesto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, idPadre, claveProdServ, tipoImpuesto, impuesto);
    }
}
