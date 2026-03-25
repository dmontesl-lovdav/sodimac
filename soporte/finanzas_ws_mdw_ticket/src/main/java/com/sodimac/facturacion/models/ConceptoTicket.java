package com.sodimac.facturacion.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ConceptoTicket {

	public String claveProdServ;
	public String noIdentificacion;
	public BigDecimal cantidad;
	public String claveUnidad;
	public String unidad;
	public String descripcion;
	public BigDecimal valorUnitario;
	public BigDecimal importe;
	public BigDecimal descuento;
	public Long ordenador;
	public Long padre;
	public Integer nivel;

	public List<TrasladoConcepto> traslados = new ArrayList<>();

	public String getClaveProdServ() {
		return claveProdServ;
	}

	public void setClaveProdServ(String claveProdServ) {
		this.claveProdServ = claveProdServ;
	}

	public String getNoIdentificacion() {
		return noIdentificacion;
	}

	public void setNoIdentificacion(String noIdentificacion) {
		this.noIdentificacion = noIdentificacion;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public String getClaveUnidad() {
		return claveUnidad;
	}

	public void setClaveUnidad(String claveUnidad) {
		this.claveUnidad = claveUnidad;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getDescuento() {
		return descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	public Long getOrdenador() {
		return ordenador;
	}

	public void setOrdenador(Long ordenador) {
		this.ordenador = ordenador;
	}

	public Long getPadre() {
		return padre;
	}

	public void setPadre(Long padre) {
		this.padre = padre;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	public List<TrasladoConcepto> getTraslados() {
		return traslados;
	}

	public void setTraslados(List<TrasladoConcepto> traslados) {
		this.traslados = traslados;
	}

}
