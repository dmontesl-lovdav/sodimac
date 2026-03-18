package com.sodimac.facturacion.models;

import java.math.BigDecimal;
import java.util.List;

public class CompTemporalModel extends CodigoError {

	private int id;

	private String rfc;

	private String ticket;

	private String razonSocial;

	private String idUsoCfdi;
	
	private String metodoPago;

	private String email;

	private boolean activo = true; //DEFAULT

	private java.util.Date fechaCreacion;

	private int autorizoGuardado;
	
	private int pac;
	
	private int idFacturaPac;
	
	private String uuid;
	
	private String fechaTimbrado;
	
	private String versionFacturacionSat;
	
	private String xml;
	
	private String fechaCompra;
	
	private int idEstatusFactura;
	
	private String nombreEstatus;
	
	private String nombreArchivo;
	
	private String ticketBct;
	
	private String versionFactura;
	
	private String transaccion;
	
	private int idActividad;
	
	private String actividadDescripcion;
	
	private int idUsuario;
	
	private String longitud;
	
	private String latitud;
	
	private String pagina;
	
	private String explorador;
	
	private String sistemaOperativo;
	
	private String ip;
	
	private String checked;
	
	private BigDecimal total;
	
	private String nombreObra;
	
	private String responsableObra;
	
	private String idComprobante;
	
	private String uuidRelacionado;
	
	private String emailCC;
	
	private BigDecimal subTotal;
	
	private String serie;
	
	private int folio;
	
	private int idOrigen;
	
	private String regimenFiscal;
	
	private String codigoPostal;
	
	private String pais;
	private String comercio;
	private String canal;
	
	private int idAplicacion;
	
	List<FacturasCompModel> facturas;
	
	public CompTemporalModel() {
		
		id = 0;
		rfc = "";
		ticket = "";
		razonSocial = "";
		idUsoCfdi = "";
		metodoPago = "";
		email = "";
		autorizoGuardado = 0;
		pac = 0;
		idFacturaPac = 0;
		uuid = "";
		versionFacturacionSat = "";
		xml = "";
		idEstatusFactura = 0;
		nombreEstatus = "";
		nombreArchivo = "";
		ticketBct = "";
		versionFactura = "";
		transaccion = "";
		
		actividadDescripcion = "";
		longitud = "";
		latitud = "";
		pagina = "";
		explorador = "";
		sistemaOperativo = "";
		ip = "";
		checked = "";
		nombreObra = "";
		responsableObra = "";
		idComprobante = "";
		uuidRelacionado = "";
		emailCC = "";
		subTotal = BigDecimal.ZERO;
		total = BigDecimal.ZERO;
		serie = "";
		folio = 0;
		idOrigen = 0;
		regimenFiscal = "";
		codigoPostal = "";
		
		pais = "";
		comercio = "";
		canal = "";
		
		idAplicacion = 0;
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

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getIdUsoCfdi() {
		return idUsoCfdi;
	}

	public void setIdUsoCfdi(String idUsoCfdi) {
		this.idUsoCfdi = idUsoCfdi;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	
	public int getAutorizoGuardado() {
		return autorizoGuardado;
	}

	public void setAutorizoGuardado(int autorizoGuardado) {
		this.autorizoGuardado = autorizoGuardado;
	}

	public int getPac() {
		return pac;
	}

	public void setPac(int pac) {
		this.pac = pac;
	}

	public int getIdFacturaPac() {
		return idFacturaPac;
	}

	public void setIdFacturaPac(int idFacturaPac) {
		this.idFacturaPac = idFacturaPac;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public int getIdEstatusFactura() {
		return idEstatusFactura;
	}

	public void setIdEstatusFactura(int idEstatusFactura) {
		this.idEstatusFactura = idEstatusFactura;
	}

	public String getNombreEstatus() {
		return nombreEstatus;
	}

	public void setNombreEstatus(String nombreEstatus) {
		this.nombreEstatus = nombreEstatus;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getTicketBct() {
		return ticketBct;
	}

	public void setTicketBct(String ticketBct) {
		this.ticketBct = ticketBct;
	}

	public String getVersionFactura() {
		return versionFactura;
	}

	public void setVersionFactura(String versionFactura) {
		this.versionFactura = versionFactura;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

	public int getIdActividad() {
		return idActividad;
	}

	public void setIdActividad(int idActividad) {
		this.idActividad = idActividad;
	}

	public String getActividadDescripcion() {
		return actividadDescripcion;
	}

	public void setActividadDescripcion(String actividadDescripcion) {
		this.actividadDescripcion = actividadDescripcion;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
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

	public String getSistemaOperativo() {
		return sistemaOperativo;
	}

	public void setSistemaOperativo(String sistemaOperativo) {
		this.sistemaOperativo = sistemaOperativo;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getChecked() {
		return checked;
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getNombreObra() {
		return nombreObra;
	}

	public void setNombreObra(String nombreObra) {
		this.nombreObra = nombreObra;
	}

	public String getResponsableObra() {
		return responsableObra;
	}

	public void setResponsableObra(String responsableObra) {
		this.responsableObra = responsableObra;
	}

	public String getIdComprobante() {
		return idComprobante;
	}

	public void setIdComprobante(String idComprobante) {
		this.idComprobante = idComprobante;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
	}

	public String getEmailCC() {
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}
	
	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
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

	public int getIdOrigen() {
		return idOrigen;
	}

	public void setIdOrigen(int idOrigen) {
		this.idOrigen = idOrigen;
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
	
	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getComercio() {
		return comercio;
	}

	public void setComercio(String comercio) {
		this.comercio = comercio;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public int getIdAplicacion() {
		return idAplicacion;
	}

	public void setIdAplicacion(int idAplicacion) {
		this.idAplicacion = idAplicacion;
	}

	public List<FacturasCompModel> getFacturas() {
		return facturas;
	}

	public void setFacturas(List<FacturasCompModel> facturas) {
		this.facturas = facturas;
	}
	
}
