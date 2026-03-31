package com.sodimac.bctfacturacion.entity.bct;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FAC_HDR")
public class FacHdrEntity {

	@Id
	@Column(name = "ID_PROCESO")
	private Long idProceso;
	
	@Column(name = "UUID")
	private Integer uuid;
	
	@Column(name = "FECHA")
	private Date fecha;
	
	@Column(name = "F_INI_PROCESO")
	private Date fechaIniProceso;
	
	@Column(name = "F_FIN_PROCESO")
	private Date fechaFinProceso;
	
	@Column(name = "NUM_TIENDA")
	private Integer numTienda;
	
	@Column(name = "TIPO_FACTURA")
	private String tipoFactura;
	
	@Column(name = "ESTADO")
	private String estado;
	
	@Column(name = "SERIE")
	private String serie;
	
	@Column(name = "FOLIO")
	private String folio;
	
	@Column(name = "RFC_RECEPTOR")
	private String rfcReceptor;
	
	@Column(name = "TIPO_OPERACION")
	private String tipoOperacion;
	
	@Column(name = "F_VENTA")
	private Date fechaVenta;
	
	@Column(name = "VERSION")
	private String version;
	
	@Column(name = "F_TIMBRADO")
	private Date fechaTimbrado;

	@Column(name = "PAC_ID")
	private String pacId;

	public Long getIdProceso() {
		return idProceso;
	}

	public void setIdProceso(Long idProceso) {
		this.idProceso = idProceso;
	}

	public Integer getUuid() {
		return uuid;
	}

	public void setUuid(Integer uuid) {
		this.uuid = uuid;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaIniProceso() {
		return fechaIniProceso;
	}

	public void setFechaIniProceso(Date fechaIniProceso) {
		this.fechaIniProceso = fechaIniProceso;
	}

	public Date getFechaFinProceso() {
		return fechaFinProceso;
	}

	public void setFechaFinProceso(Date fechaFinProceso) {
		this.fechaFinProceso = fechaFinProceso;
	}

	public Integer getNumTienda() {
		return numTienda;
	}

	public void setNumTienda(Integer numTienda) {
		this.numTienda = numTienda;
	}

	public String getTipoFactura() {
		return tipoFactura;
	}

	public void setTipoFactura(String tipoFactura) {
		this.tipoFactura = tipoFactura;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getRfcReceptor() {
		return rfcReceptor;
	}

	public void setRfcReceptor(String rfcReceptor) {
		this.rfcReceptor = rfcReceptor;
	}

	public String getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	public Date getFechaVenta() {
		return fechaVenta;
	}

	public void setFechaVenta(Date fechaVenta) {
		this.fechaVenta = fechaVenta;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getFechaTimbrado() {
		return fechaTimbrado;
	}

	public void setFechaTimbrado(Date fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}

	public String getPacId() {
		return pacId;
	}

	public void setPacId(String pacId) {
		this.pacId = pacId;
	}
	
}
