package com.sodimac.rebates.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Embeddable
public class CatalogoId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@OneToOne
	@JoinColumn(name = "idCatalogo")
	private Catalogo catalogo;
	private Integer idElemento;

	// default constructor
	public CatalogoId() {

	}

	public CatalogoId(Catalogo catalogo, Integer idElemento) {
		this.catalogo = catalogo;
		this.idElemento = idElemento;
	}

	public Catalogo getCatalogo() {
		return catalogo;
	}

	public void setCatalogo(Catalogo catalogo) {
		this.catalogo = catalogo;
	}

	public Integer getIdElemento() {
		return idElemento;
	}

	public void setIdElemento(Integer idElemento) {
		this.idElemento = idElemento;
	}

	public int hashCode() {
		return (int) this.catalogo.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof CatalogoId))
			return false;
		CatalogoId pk = (CatalogoId) obj;
		return pk.idElemento.equals(this.idElemento) && pk.catalogo.equals(this.catalogo);
	}

	@Override
	public String toString() {
		return "CatalogoId [catalogo=" + catalogo + ", idElemento=" + idElemento + "]";
	}

}
