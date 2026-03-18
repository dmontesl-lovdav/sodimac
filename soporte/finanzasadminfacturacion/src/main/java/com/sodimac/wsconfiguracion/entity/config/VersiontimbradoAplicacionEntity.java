package com.sodimac.wsconfiguracion.entity.config;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

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
@Table(name = "versiontimbradoaplicacion")
public class VersiontimbradoAplicacionEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1783363305528110621L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="idcatversiontimbrado")
    private CatVersionTimbradoEntity catVersionTimbrado;
	
	@Column(name = "idcataplicaciones")
	private int idcataplicaciones;
	
	@Column
	private boolean activo;
	
	@Column
	private LocalDate fechaInicio;
	
	@Column
	private Date fechaFin;
	
	@Column
	private int idUsuario;
	
	@Column
	private LocalDate updateDate;
	
	
	
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}


	public CatVersionTimbradoEntity getCatVersionTimbrado() {
		return catVersionTimbrado;
	}



	public void setCatVersionTimbrado(CatVersionTimbradoEntity catVersionTimbrado) {
		this.catVersionTimbrado = catVersionTimbrado;
	}



	public int getIdcataplicaciones() {
		return idcataplicaciones;
	}



	public void setIdcataplicaciones(int idcataplicaciones) {
		this.idcataplicaciones = idcataplicaciones;
	}



	public boolean isActivo() {
		return activo;
	}



	public void setActivo(boolean activo) {
		this.activo = activo;
	}



	public LocalDate getFechaInicio() {
		return fechaInicio;
	}



	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}



	public Date getFechaFin() {
		return fechaFin;
	}



	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}



	public int getIdUsuario() {
		return idUsuario;
	}



	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}



	public LocalDate getUpdateDate() {
		return updateDate;
	}



	public void setUpdateDate(LocalDate updateDate) {
		this.updateDate = updateDate;
	}



	@PreUpdate
	private void antesDeActualizar() {
		this.updateDate = LocalDate.now();
	}



	@Override
	public String toString() {
		return "VersiontimbradoAplicacionEntity [id=" + id + ", catVersionTimbrado=" + catVersionTimbrado
				+ ", idcataplicaciones=" + idcataplicaciones + ", activo=" + activo + ", fechaInicio=" + fechaInicio
				+ ", fechaFin=" + fechaFin + ", idUsuario=" + idUsuario + ", updateDate=" + updateDate + "]";
	}




	
	
}
