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
@Table(name = "catversiontimbrado")
public class CatVersionTimbradoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "version")
	private String version;
	
	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "activo")
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		return "CatVersionTimbradoEntity [id=" + id + ", version=" + version + ", descripcion=" + descripcion
				+ ", activo=" + activo + ", updateDate=" + updateDate + "]";
	}
	
	

}
