package com.sodimac.wsconfiguracion.entity.config;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "catregimenfiscal")
public class CatRegimenFiscalEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column
	private String regimenfiscal;
	
	@Column
	private String descripcion;
	
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="idcattipopersona")
    private CatTipoPersonaEntity catTipoPersonaEntity;
	
	@Column
	private Boolean activo;
	
	@Column(name = "updateDate")
	private LocalDateTime updateDate;
	
	@PreUpdate
	private void antesDeActualizar() {
		this.updateDate = LocalDateTime.now();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRegimenfiscal() {
		return regimenfiscal;
	}

	public void setRegimenfiscal(String regimenfiscal) {
		this.regimenfiscal = regimenfiscal;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public CatTipoPersonaEntity getCatTipoPersonaEntity() {
		return catTipoPersonaEntity;
	}

	public void setCatTipoPersonaEntity(CatTipoPersonaEntity catTipoPersonaEntity) {
		this.catTipoPersonaEntity = catTipoPersonaEntity;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "CatRegimenFiscalEntity [id=" + id + ", regimenfiscal=" + regimenfiscal + ", descripcion=" + descripcion
				+ ", catTipoPersonaEntity=" + catTipoPersonaEntity + ", activo=" + activo + ", updateDate=" + updateDate
				+ "]";
	}
	
	

}
