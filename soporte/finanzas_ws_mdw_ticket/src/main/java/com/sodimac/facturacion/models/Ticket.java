package com.sodimac.facturacion.models;

import java.util.List;

public class Ticket {

	private Cabecera cabecera;
	private Emisor emisor;
	private List<ConceptoTicket> conceptos;
	private List<TrasladoConcepto> impuestos;
	private TotalesTicket totales;
	private DatosControl control;
	private DatosExtra datosExtra;
	private ErrorBase error;

	public Cabecera getCabecera() {
		return cabecera;
	}

	public void setCabecera(Cabecera cabecera) {
		this.cabecera = cabecera;
	}

	public Emisor getEmisor() {
		return emisor;
	}

	public void setEmisor(Emisor emisor) {
		this.emisor = emisor;
	}

	public List<ConceptoTicket> getConceptos() {
		return conceptos;
	}

	public void setConceptos(List<ConceptoTicket> conceptos) {
		this.conceptos = conceptos;
	}

	public List<TrasladoConcepto> getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(List<TrasladoConcepto> impuestos) {
		this.impuestos = impuestos;
	}

	public TotalesTicket getTotales() {
		return totales;
	}

	public void setTotales(TotalesTicket totales) {
		this.totales = totales;
	}

	public DatosControl getControl() {
		return control;
	}

	public void setControl(DatosControl control) {
		this.control = control;
	}

	public DatosExtra getDatosExtra() {
		return datosExtra;
	}

	public void setDatosExtra(DatosExtra datosExtra) {
		this.datosExtra = datosExtra;
	}

	public ErrorBase getError() {
		return error;
	}

	public void setError(ErrorBase error) {
		this.error = error;
	}

}
