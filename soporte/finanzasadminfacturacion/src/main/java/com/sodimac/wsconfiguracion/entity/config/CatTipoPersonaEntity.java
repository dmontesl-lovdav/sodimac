package com.sodimac.wsconfiguracion.entity.config;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "cattipopersona")
public class CatTipoPersonaEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column
	private String tipo;
	
	@Column
	private String descripcion;
	
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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
		return "CatTipoPersona [id=" + id + ", tipo=" + tipo + ", descripcion=" + descripcion + ", activo=" + activo
				+ ", updateDate=" + updateDate + "]";
	}

	
	
}
