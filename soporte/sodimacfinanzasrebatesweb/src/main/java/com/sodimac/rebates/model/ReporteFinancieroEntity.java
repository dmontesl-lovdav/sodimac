package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_reporte_financiero")
public class ReporteFinancieroEntity {

	@Id
	@Column(name = "RowNumber")
	private String rowNumber;
	
	@Column(name = "IdRegistro")
	private String idRegistro;
	
	@Column(name = "Sociedad")
	private String sociedad;
	
	@Column(name = "FechaDocumento")
	private String fechaDocumento;
	
	@Column(name = "FechaContabilizacion")
	private Date fechaContabilizacion;
	
	@Column(name = "TipoDocumento")
	private String tipoDocumento;
	
	@Column(name = "ReferenciaFact")
	private String referenciaFact;
	
	@Column(name = "ReferenciaEjercicio")
	private String referenciaEjercicio;
	
	@Column(name = "ReferenciaPosicion")
	private String referenciaPosicion;
	
	@Column(name = "NoContrato")
	private String noContrato;
	
	@Column(name = "Periodo")
	private String periodo;
	
	@Column(name = "Referencia")
	private String referencia;
	
	@Column(name = "TextoCabecera")
	private String textoCabecera;
	
	@Column(name = "Moneda")
	private String moneda;
	
	@Column(name = "FechaConversion")
	private String fechaConversion;
	
	@Column(name = "ClaveContabilizacion")
	private String claveContabilizacion;
	
	@Column(name = "Cuenta")
	private String cuenta;
	
	@Column(name = "IndicadorCME")
	private String indicadorCME;
	
	@Column(name = "ClaseMovimiento")
	private String claseMovimiento;
	
	@Column(name = "Importe")
	private Double importe;
	
	@Column(name = "ImporteImpuestos")
	private String importeImpuestos;
	
	@Column(name = "CalcularImpuestos")
	private String calcularImpuestos;
	
	@Column(name = "IndicadorImpuestos")
	private String indicadorImpuestos;
	
	@Column(name = "CentroBeneficios")
	private String centroBeneficios;
	
	@Column(name = "CentroCoste")
	private String centroCoste;
	
	@Column(name = "Orden")
	private String orden;
	
	@Column(name = "ElementoPEP")
	private String elementoPEP;
	
	@Column(name = "Segmento")
	private String segmento;
	
	@Column(name = "CondicionPago")
	private String condicionPago;
	
	@Column(name = "FechaBase")
	private String fechaBase;
	
	@Column(name = "MetodoPago")
	private String metodoPago;
	
	@Column(name = "BloqueoPago")
	private String bloqueoPago;
	
	@Column(name = "Articulo")
	private String articulo;
	
	@Column(name = "Cantidad")
	private String cantidad;
	
	@Column(name = "UnidadMedida")
	private String unidadMedida;
	
	@Column(name = "Asignacion")
	private String asignacion;
	
	@Column(name = "Texto")
	private String texto;
	
	@Column(name = "Referencia1")
	private String referencia1;
	
	@Column(name = "Referencia2")
	private String referencia2;
	
	@Column(name = "Referencia3")
	private Integer referencia3;
	
	@Column(name = "FechaValor")
	private String fechaValor;
	
	@Column(name = "TipoCambio")
	private Double tipoCambio;
	
	@Column(name = "IdCatPeriodo")
	private Integer idCatPeriodo;
	
	@Column(name = "ProgramaPago")
	private String programaPago;
	
	@Column(name = "TipoRebate")
	private String rebate;
	
	@Column(name = "NumeroProveedor")
	private Integer numeroProveedor;
	
	@Column(name = "NombreProveedor")
	private String proveedor;
	
	public String getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getIdRegistro() {
		return idRegistro;
	}

	public void setIdRegistro(String idRegistro) {
		this.idRegistro = idRegistro;
	}

	public String getSociedad() {
		return sociedad;
	}

	public void setSociedad(String sociedad) {
		this.sociedad = sociedad;
	}

	public String getFechaDocumento() {
		return fechaDocumento;
	}

	public void setFechaDocumento(String fechaDocumento) {
		this.fechaDocumento = fechaDocumento;
	}

	public Date getFechaContabilizacion() {
		return fechaContabilizacion;
	}

	public void setFechaContabilizacion(Date fechaContabilizacion) {
		this.fechaContabilizacion = fechaContabilizacion;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getReferenciaFact() {
		return referenciaFact;
	}

	public void setReferenciaFact(String referenciaFact) {
		this.referenciaFact = referenciaFact;
	}

	public String getReferenciaEjercicio() {
		return referenciaEjercicio;
	}

	public void setReferenciaEjercicio(String referenciaEjercicio) {
		this.referenciaEjercicio = referenciaEjercicio;
	}

	public String getReferenciaPosicion() {
		return referenciaPosicion;
	}

	public void setReferenciaPosicion(String referenciaPosicion) {
		this.referenciaPosicion = referenciaPosicion;
	}

	public String getNoContrato() {
		return noContrato;
	}

	public void setNoContrato(String noContrato) {
		this.noContrato = noContrato;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getTextoCabecera() {
		return textoCabecera;
	}

	public void setTextoCabecera(String textoCabecera) {
		this.textoCabecera = textoCabecera;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getFechaConversion() {
		return fechaConversion;
	}

	public void setFechaConversion(String fechaConversion) {
		this.fechaConversion = fechaConversion;
	}

	public String getClaveContabilizacion() {
		return claveContabilizacion;
	}

	public void setClaveContabilizacion(String claveContabilizacion) {
		this.claveContabilizacion = claveContabilizacion;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getIndicadorCME() {
		return indicadorCME;
	}

	public void setIndicadorCME(String indicadorCME) {
		this.indicadorCME = indicadorCME;
	}

	public String getClaseMovimiento() {
		return claseMovimiento;
	}

	public void setClaseMovimiento(String claseMovimiento) {
		this.claseMovimiento = claseMovimiento;
	}

	public Double getImporte() {
		return importe;
	}

	public void setImporte(Double importe) {
		this.importe = importe;
	}

	public String getImporteImpuestos() {
		return importeImpuestos;
	}

	public void setImporteImpuestos(String importeImpuestos) {
		this.importeImpuestos = importeImpuestos;
	}

	public String getCalcularImpuestos() {
		return calcularImpuestos;
	}

	public void setCalcularImpuestos(String calcularImpuestos) {
		this.calcularImpuestos = calcularImpuestos;
	}

	public String getIndicadorImpuestos() {
		return indicadorImpuestos;
	}

	public void setIndicadorImpuestos(String indicadorImpuestos) {
		this.indicadorImpuestos = indicadorImpuestos;
	}

	public String getCentroBeneficios() {
		return centroBeneficios;
	}

	public void setCentroBeneficios(String centroBeneficios) {
		this.centroBeneficios = centroBeneficios;
	}

	public String getCentroCoste() {
		return centroCoste;
	}

	public void setCentroCoste(String centroCoste) {
		this.centroCoste = centroCoste;
	}

	public String getOrden() {
		return orden;
	}

	public void setOrden(String orden) {
		this.orden = orden;
	}

	public String getElementoPEP() {
		return elementoPEP;
	}

	public void setElementoPEP(String elementoPEP) {
		this.elementoPEP = elementoPEP;
	}

	public String getSegmento() {
		return segmento;
	}

	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}

	public String getCondicionPago() {
		return condicionPago;
	}

	public void setCondicionPago(String condicionPago) {
		this.condicionPago = condicionPago;
	}

	public String getFechaBase() {
		return fechaBase;
	}

	public void setFechaBase(String fechaBase) {
		this.fechaBase = fechaBase;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public String getBloqueoPago() {
		return bloqueoPago;
	}

	public void setBloqueoPago(String bloqueoPago) {
		this.bloqueoPago = bloqueoPago;
	}

	public String getArticulo() {
		return articulo;
	}

	public void setArticulo(String articulo) {
		this.articulo = articulo;
	}

	public String getCantidad() {
		return cantidad;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public String getAsignacion() {
		return asignacion;
	}

	public void setAsignacion(String asignacion) {
		this.asignacion = asignacion;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getReferencia1() {
		return referencia1;
	}

	public void setReferencia1(String referencia1) {
		this.referencia1 = referencia1;
	}

	public String getReferencia2() {
		return referencia2;
	}

	public void setReferencia2(String referencia2) {
		this.referencia2 = referencia2;
	}

	public Integer getReferencia3() {
		return referencia3;
	}

	public void setReferencia3(Integer referencia3) {
		this.referencia3 = referencia3;
	}

	public String getFechaValor() {
		return fechaValor;
	}

	public void setFechaValor(String fechaValor) {
		this.fechaValor = fechaValor;
	}

	public Double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(Double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Integer getIdCatPeriodo() {
		return idCatPeriodo;
	}

	public void setIdCatPeriodo(Integer idCatPeriodo) {
		this.idCatPeriodo = idCatPeriodo;
	}

	public String getRebate() {
		return rebate;
	}

	public void setRebate(String rebate) {
		this.rebate = rebate;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public Integer getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(Integer numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

}
