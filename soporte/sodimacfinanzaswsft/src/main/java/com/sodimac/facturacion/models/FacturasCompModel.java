package com.sodimac.facturacion.models;

public class FacturasCompModel extends CodigoError {

	private int id;

	private String rfc;
	
	private String razonSocial;
	
	private String email;
	
	private String uuid;

	private String serie;
	
	private int folio;
	
	private java.util.Date fechaCreacion;
	
	private String fechaTimbrado;
	
	private String versionFacturacionSat;
	
	private String xml;
	
	private String fechaCompra;
			
	private double total;
		
	private double subTotal;

	private String regimenFiscal;
	
	private String codigoPostal;
	
	private double montoFactura;

	private double pagoComplemento;

	private String monedaPago;

	private double equivalencia;

	private int parcialidad;

	private double importeSaldoAnterior;

	private double importePagado;

	private double importePosterior;

	private double saldoControl;
	
		
	public FacturasCompModel() {
		
		id = 0;
		rfc = "";
		razonSocial = "";
		email = "";
		uuid = "";
		versionFacturacionSat = "";
		xml = "";
		subTotal = 0d;
		total = 0d;
		serie = "";
		folio = 0;
		montoFactura = 0d;
		pagoComplemento = 0d;
		monedaPago = "";
		equivalencia = 0d;
		importeSaldoAnterior = 0d; 
		importePagado = 0d;
		importePosterior = 0d;
		saldoControl = 0d;
		
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public String getUuid() {
		return uuid;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	public String getSerie() {
		return serie;
	}


	public void setSerie(String serie) {
		this.serie = serie;
	}


	public int getFolio() {
		return folio;
	}


	public void setFolio(int folio) {
		this.folio = folio;
	}


	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}


	public String getFechaTimbrado() {
		return fechaTimbrado;
	}


	public void setFechaTimbrado(String fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}


	public String getVersionFacturacionSat() {
		return versionFacturacionSat;
	}


	public void setVersionFacturacionSat(String versionFacturacionSat) {
		this.versionFacturacionSat = versionFacturacionSat;
	}


	public String getXml() {
		return xml;
	}


	public void setXml(String xml) {
		this.xml = xml;
	}


	public String getFechaCompra() {
		return fechaCompra;
	}


	public void setFechaCompra(String fechaCompra) {
		this.fechaCompra = fechaCompra;
	}


	public double getTotal() {
		return total;
	}


	public void setTotal(double total) {
		this.total = total;
	}


	public double getSubTotal() {
		return subTotal;
	}


	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}


	public String getRegimenFiscal() {
		return regimenFiscal;
	}


	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}


	public String getCodigoPostal() {
		return codigoPostal;
	}


	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}


	public double getMontoFactura() {
		return montoFactura;
	}


	public void setMontoFactura(double montoFactura) {
		this.montoFactura = montoFactura;
	}


	public double getPagoComplemento() {
		return pagoComplemento;
	}


	public void setPagoComplemento(double pagoComplemento) {
		this.pagoComplemento = pagoComplemento;
	}


	public String getMonedaPago() {
		return monedaPago;
	}


	public void setMonedaPago(String monedaPago) {
		this.monedaPago = monedaPago;
	}


	public double getEquivalencia() {
		return equivalencia;
	}


	public void setEquivalencia(double equivalencia) {
		this.equivalencia = equivalencia;
	}


	public int getParcialidad() {
		return parcialidad;
	}


	public void setParcialidad(int parcialidad) {
		this.parcialidad = parcialidad;
	}


	public double getImporteSaldoAnterior() {
		return importeSaldoAnterior;
	}


	public void setImporteSaldoAnterior(double importeSaldoAnterior) {
		this.importeSaldoAnterior = importeSaldoAnterior;
	}


	public double getImportePagado() {
		return importePagado;
	}


	public void setImportePagado(double importePagado) {
		this.importePagado = importePagado;
	}


	public double getImportePosterior() {
		return importePosterior;
	}


	public void setImportePosterior(double importePosterior) {
		this.importePosterior = importePosterior;
	}


	public double getSaldoControl() {
		return saldoControl;
	}


	public void setSaldoControl(double saldoControl) {
		this.saldoControl = saldoControl;
	}

		
}
