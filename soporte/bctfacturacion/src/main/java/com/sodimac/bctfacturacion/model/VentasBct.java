package com.sodimac.bctfacturacion.model;

import java.util.Date;

public class VentasBct {

	private Date fechaTrx;
	private Integer tienda;
	private Integer total;

	public Date getFechaTrx() {
		return fechaTrx;
	}

	public void setFechaTrx(Date fechaTrx) {
		this.fechaTrx = fechaTrx;
	}

	public Integer getTienda() {
		return tienda;
	}

	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
