package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "regimensocietariovariante")
public class RegimenSocietarioVarianteEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "sociedadNombre")
	private String sociedadNombre;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSociedadNombre() {
		return sociedadNombre;
	}

	public void setSociedadNombre(String sociedadNombre) {
		this.sociedadNombre = sociedadNombre;
	}

	@Override
	public String toString() {
		return "RegimenSocietarioVarianteEntity [id=" + id + ", sociedadNombre=" + sociedadNombre + "]";
	}
	
	
	

}
