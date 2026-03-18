package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

//@Entity
//@Table(name = "confdatosemisortiendareb")
public class ConfDatosEmisorTiendaRebEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "idConfDatosEmisor")
	private Integer idConfDatosEmisor;
	
	@Column(name = "idTienda")
	private Integer idTienda;
	
	@Column(name = "calle")
	private String calle;
	
	@Column(name = "noExterior")
	private String noExterior;
	
	@Column(name = "noInterior")
	private String noInterior;
	
	@Column(name = "colonia")
	private String colonia;
	
	@Column(name = "localidad")
	private String localidad;
	
	@Column(name = "referencia")
	private String referencia;
	
	@Column(name = "municipio")
	private String municipio;
	
	@Column(name = "estado")
	private String estado;
	
	@Column(name = "pais")
	private String pais;
	
	@Column(name = "codigoPostal")
	private String codigoPostal;
	
	@Column(name = "activo")
	private Boolean activo;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaModificacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdConfDatosEmisor() {
		return idConfDatosEmisor;
	}

	public void setIdConfDatosEmisor(Integer idConfDatosEmisor) {
		this.idConfDatosEmisor = idConfDatosEmisor;
	}

	public Integer getIdTienda() {
		return idTienda;
	}

	public void setIdTienda(Integer idTienda) {
		this.idTienda = idTienda;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getNoExterior() {
		return noExterior;
	}

	public void setNoExterior(String noExterior) {
		this.noExterior = noExterior;
	}

	public String getNoInterior() {
		return noInterior;
	}

	public void setNoInterior(String noInterior) {
		this.noInterior = noInterior;
	}

	public String getColonia() {
		return colonia;
	}

	public void setColonia(String colonia) {
		this.colonia = colonia;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	@Override
	public String toString() {
		return "ConfDatosEmisorTiendaRebEntity [id=" + id + ", idConfDatosEmisor=" + idConfDatosEmisor + ", idTienda="
				+ idTienda + ", calle=" + calle + ", noExterior=" + noExterior + ", noInterior=" + noInterior
				+ ", colonia=" + colonia + ", localidad=" + localidad + ", referencia=" + referencia + ", municipio="
				+ municipio + ", estado=" + estado + ", pais=" + pais + ", codigoPostal=" + codigoPostal + ", activo="
				+ activo + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion=" + fechaModificacion + "]";
	}
	
	

}
