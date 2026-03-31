package com.sodimac.bctfacturacion.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VENTA_DET")
public class VentaMSIDetEntity {

	@Id
	@Column(name = "TICKET")
	private String ticket;

	@Column(name = "FECHA_TICKET")
	private Date fechaTicket;

	@Column(name = "TIENDA")
	private Integer tienda;

	@Column(name = "CAJA")
	private Integer caja;

	@Column(name = "NUM_DOC_CANAL")
	private String numDocCanal;

	@Column(name = "CANAL_LINIO")
	private String canalLinio;

	@Column(name = "TOTAL_ARTICULO")
	private Integer totalArticulo;

	@Column(name = "FECHA_CARGA")
	private Date fechaCarga;

	@Column(name = "ESTATUS_PROCESO")
	private Integer cajaEstatusProceso;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Date getFechaTicket() {
		return fechaTicket;
	}

	public void setFechaTicket(Date fechaTicket) {
		this.fechaTicket = fechaTicket;
	}

	public Integer getTienda() {
		return tienda;
	}

	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}

	public Integer getCaja() {
		return caja;
	}

	public void setCaja(Integer caja) {
		this.caja = caja;
	}

	public String getNumDocCanal() {
		return numDocCanal;
	}

	public void setNumDocCanal(String numDocCanal) {
		this.numDocCanal = numDocCanal;
	}

	public String getCanalLinio() {
		return canalLinio;
	}

	public void setCanalLinio(String canalLinio) {
		this.canalLinio = canalLinio;
	}

	public Integer getTotalArticulo() {
		return totalArticulo;
	}

	public void setTotalArticulo(Integer totalArticulo) {
		this.totalArticulo = totalArticulo;
	}

	public Date getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(Date fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public Integer getCajaEstatusProceso() {
		return cajaEstatusProceso;
	}

	public void setCajaEstatusProceso(Integer cajaEstatusProceso) {
		this.cajaEstatusProceso = cajaEstatusProceso;
	}

}
