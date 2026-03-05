package com.sodimac.batch.process.model.entity.batch;

import java.io.Serializable;
import java.util.Objects;

public class AdminCatalogoId implements Serializable {

    private Integer idCatalogo;
    private Integer idElemento;

    public AdminCatalogoId() {}

    public AdminCatalogoId(Integer idCatalogo, Integer idElemento) {
        this.idCatalogo = idCatalogo;
        this.idElemento = idElemento;
    }

    public Integer getIdCatalogo() { return idCatalogo; }
    public void setIdCatalogo(Integer idCatalogo) { this.idCatalogo = idCatalogo; }

    public Integer getIdElemento() { return idElemento; }
    public void setIdElemento(Integer idElemento) { this.idElemento = idElemento; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminCatalogoId that = (AdminCatalogoId) o;
        return Objects.equals(idCatalogo, that.idCatalogo) && Objects.equals(idElemento, that.idElemento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCatalogo, idElemento);
    }
}
