package com.sodimac.rebates.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "CatTipoRebate")
public class TipoRebate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCatTipoRebate;
	private String tipoRebate;
	private boolean activo;
	private String nomenclatura;
	@JsonBackReference(value = "catTipoRebate")
	@OneToMany(mappedBy = "catTipoRebate")
	private List<RelPeriodoTipoRebate> relPeriodoTipoRebate;
	
	public Integer getIdCatTipoRebate() {
		return idCatTipoRebate;
	}

	public void setIdCatTipoRebate(Integer idCatTipoRebate) {
		this.idCatTipoRebate = idCatTipoRebate;
	}

	public String getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(String tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getNomenclatura() {
		return nomenclatura;
	}

	public void setNomenclatura(String nomenclatura) {
		this.nomenclatura = nomenclatura;
	}
	
	public List<RelPeriodoTipoRebate> getRelPeriodoTipoRebate() {
		return relPeriodoTipoRebate;
	}

	public void setRelPeriodoTipoRebate(List<RelPeriodoTipoRebate> relPeriodoTipoRebate) {
		this.relPeriodoTipoRebate = relPeriodoTipoRebate;
	}

	@Override
	public String toString() {
		return "TipoRebate [idCatTipoRebate=" + idCatTipoRebate + ", tipoRebate=" + tipoRebate + ", activo=" + activo
				+ ", nomenclatura=" + nomenclatura + "]";
	}

}
