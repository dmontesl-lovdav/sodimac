package com.sodimac.facturacion.entity.reb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "CatTipoRebate", schema="dbo")
public class CatTipoRebateEntity {

	@Id
	@Column(name = "IdCatTipoRebate")
	private int id;

	@Column(name = "TipoRebate", columnDefinition = "varchar(30)")
	private String tipoRebate;

	@Column(name = "activo", nullable = true, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo;

	@Column(name = "Nomenclatura", columnDefinition = "varchar(50)")
	private String nomenclatura;

	public CatTipoRebateEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	@Override
	public String toString() {
		return "CatTipoRebateEntity [id=" + id + ", tipoRebate=" + tipoRebate + ", activo=" + activo + ", nomenclatura="
				+ nomenclatura + "]";
	}
	
}
