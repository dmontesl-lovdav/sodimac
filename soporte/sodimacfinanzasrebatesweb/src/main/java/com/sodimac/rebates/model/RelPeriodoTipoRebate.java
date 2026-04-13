package com.sodimac.rebates.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "RelPeriodoTipoRebate")
public class RelPeriodoTipoRebate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id")
	private Integer id;

	@JoinColumn(name = "IdCatPeriodo", referencedColumnName = "IdCatPeriodo")
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private Periodo periodo;
	
	@JoinColumn(name = "IdCatTipoRebate", referencedColumnName = "idCatTipoRebate")
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private TipoRebate catTipoRebate;
	
	@Column(name = "Activo")
	private boolean activo;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public TipoRebate getCatTipoRebate() {
		return catTipoRebate;
	}

	public void setCatTipoRebate(TipoRebate catTipoRebate) {
		this.catTipoRebate = catTipoRebate;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	
}
