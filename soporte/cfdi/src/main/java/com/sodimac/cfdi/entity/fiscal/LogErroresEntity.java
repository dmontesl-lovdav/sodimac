package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "logerrores")
public class LogErroresEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idLogError")
	private int idLogError;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Lob
	@Column(name = "errorLog")
	private String errorLog;

	@Column(name = "idUsuario", nullable=true)
	private Integer idUsuario;

	@Column(name = "objeto")
	private String objeto;

	@Column(name = "longitud")
	private String longitud;

	@Column(name = "latitud")
	private String latitud;

	@Column(name = "pagina")
	private String pagina;

	@Column(name = "explorador")
	private String explorador;

	@Column(name = "so")
	private String so;

	@Column(name = "ip")
	private String ip;

	@Column(name = "params")
	private String params;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	

	public LogErroresEntity() {

	}


	public int getIdLogError() {
		return idLogError;
	}


	public void setIdLogError(int idLogError) {
		this.idLogError = idLogError;
	}


	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}


	public String getErrorLog() {
		return errorLog;
	}


	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}


	public Integer getIdUsuario() {
		return idUsuario;
	}


	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}


	public String getObjeto() {
		return objeto;
	}


	public void setObjeto(String objeto) {
		this.objeto = objeto;
	}


	public String getLongitud() {
		return longitud;
	}


	public void setLongitud(String longitud) {
		this.longitud = longitud;
	}


	public String getLatitud() {
		return latitud;
	}


	public void setLatitud(String latitud) {
		this.latitud = latitud;
	}


	public String getPagina() {
		return pagina;
	}


	public void setPagina(String pagina) {
		this.pagina = pagina;
	}


	public String getExplorador() {
		return explorador;
	}


	public void setExplorador(String explorador) {
		this.explorador = explorador;
	}


	public String getSo() {
		return so;
	}


	public void setSo(String so) {
		this.so = so;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public String getParams() {
		return params;
	}


	public void setParams(String params) {
		this.params = params;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}


	@Override
	public String toString() {
		return "LogErroresEntity [idLogError=" + idLogError + ", fechaCreacion=" + fechaCreacion + ", errorLog="
				+ errorLog + ", idUsuario=" + idUsuario + ", objeto=" + objeto + ", longitud=" + longitud + ", latitud="
				+ latitud + ", pagina=" + pagina + ", explorador=" + explorador + ", so=" + so + ", ip=" + ip
				+ ", params=" + params + ", activo=" + activo + "]";
	}

}
