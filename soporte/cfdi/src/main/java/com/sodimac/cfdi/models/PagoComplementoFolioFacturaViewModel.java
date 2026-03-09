package com.sodimac.cfdi.models;

public class PagoComplementoFolioFacturaViewModel {

	private Integer idPagoComplementoFolioFactura;
	private Integer registro;
	private Integer idFolioFactura;
	private Integer idFactura;
	private Integer idPagoComplemento;
	private String folioFactura;
	private String rfc;
	private String razonSocial;
	private String uuid;
	private String serie;
	private Integer folio;
	private Double montoRealFactura;
	private String montoRealFacturaStr;
	private Double montoTotalNC;
	private String montoTotalNCStr;
	private Double montoPorPagar;
	private String montoPorPagarStr;
	private Double pagoComplemento;
	private String pagoComplementoStr;
	private String monedaPago;
	private Double equivalencia;
	private Integer parcialidad;
	private Double importeSaldoAnterior;
	private String importeSaldoAnteriorStr;
	private Double importePagado;
	private String importePagadoStr;
	private Double importePosterior;
	private String importePosteriorStr;
	private Double saldoControl;
	private String saldoControlStr;
	private String fechaRegistro;
	private Integer estatus;
	private String estatusDescripcion;
	private boolean checked;
	private String disabled;
	private boolean persistente;
	private boolean neteado;
	private boolean notNeteado;
	private boolean colorPagoComplemento;
	private Integer orden;

	public Integer getIdPagoComplementoFolioFactura() {
		return idPagoComplementoFolioFactura;
	}

	public void setIdPagoComplementoFolioFactura(Integer idPagoComplementoFolioFactura) {
		this.idPagoComplementoFolioFactura = idPagoComplementoFolioFactura;
	}

	public Integer getRegistro() {
		return registro;
	}

	public void setRegistro(Integer registro) {
		this.registro = registro;
	}

	public Integer getIdFolioFactura() {
		return idFolioFactura;
	}

	public void setIdFolioFactura(Integer idFolioFactura) {
		this.idFolioFactura = idFolioFactura;
	}

	public Integer getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(Integer idFactura) {
		this.idFactura = idFactura;
	}

	public Integer getIdPagoComplemento() {
		return idPagoComplemento;
	}

	public void setIdPagoComplemento(Integer idPagoComplemento) {
		this.idPagoComplemento = idPagoComplemento;
	}

	public String getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(String folioFactura) {
		this.folioFactura = folioFactura;
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

	public Integer getFolio() {
		return folio;
	}

	public void setFolio(Integer folio) {
		this.folio = folio;
	}
	
	public Double getMontoRealFactura() {
		return montoRealFactura;
	}

	public void setMontoRealFactura(Double montoRealFactura) {
		this.montoRealFactura = montoRealFactura;
	}

	public String getMontoRealFacturaStr() {
		return montoRealFacturaStr;
	}

	public void setMontoRealFacturaStr(String montoRealFacturaStr) {
		this.montoRealFacturaStr = montoRealFacturaStr;
	}

	public Double getMontoTotalNC() {
		return montoTotalNC;
	}

	public void setMontoTotalNC(Double montoTotalNC) {
		this.montoTotalNC = montoTotalNC;
	}

	public String getMontoTotalNCStr() {
		return montoTotalNCStr;
	}

	public Double getMontoPorPagar() {
		return montoPorPagar;
	}

	public void setMontoPorPagar(Double montoPorPagar) {
		this.montoPorPagar = montoPorPagar;
	}

	public String getMontoPorPagarStr() {
		return montoPorPagarStr;
	}

	public void setMontoPorPagarStr(String montoPorPagarStr) {
		this.montoPorPagarStr = montoPorPagarStr;
	}

	public void setMontoTotalNCStr(String montoTotalNCStr) {
		this.montoTotalNCStr = montoTotalNCStr;
	}

	public Double getPagoComplemento() {
		return pagoComplemento;
	}

	public void setPagoComplemento(Double pagoComplemento) {
		this.pagoComplemento = pagoComplemento;
	}

	public String getPagoComplementoStr() {
		return pagoComplementoStr;
	}

	public void setPagoComplementoStr(String pagoComplementoStr) {
		this.pagoComplementoStr = pagoComplementoStr;
	}

	public String getMonedaPago() {
		return monedaPago;
	}

	public void setMonedaPago(String monedaPago) {
		this.monedaPago = monedaPago;
	}

	public Double getEquivalencia() {
		return equivalencia;
	}

	public void setEquivalencia(Double equivalencia) {
		this.equivalencia = equivalencia;
	}

	public Integer getParcialidad() {
		return parcialidad;
	}

	public void setParcialidad(Integer parcialidad) {
		this.parcialidad = parcialidad;
	}

	public Double getImporteSaldoAnterior() {
		return importeSaldoAnterior;
	}

	public void setImporteSaldoAnterior(Double importeSaldoAnterior) {
		this.importeSaldoAnterior = importeSaldoAnterior;
	}

	public String getImporteSaldoAnteriorStr() {
		return importeSaldoAnteriorStr;
	}

	public void setImporteSaldoAnteriorStr(String importeSaldoAnteriorStr) {
		this.importeSaldoAnteriorStr = importeSaldoAnteriorStr;
	}

	public Double getImportePagado() {
		return importePagado;
	}

	public void setImportePagado(Double importePagado) {
		this.importePagado = importePagado;
	}

	public String getImportePagadoStr() {
		return importePagadoStr;
	}

	public void setImportePagadoStr(String importePagadoStr) {
		this.importePagadoStr = importePagadoStr;
	}

	public Double getImportePosterior() {
		return importePosterior;
	}

	public void setImportePosterior(Double importePosterior) {
		this.importePosterior = importePosterior;
	}

	public String getImportePosteriorStr() {
		return importePosteriorStr;
	}

	public void setImportePosteriorStr(String importePosteriorStr) {
		this.importePosteriorStr = importePosteriorStr;
	}

	public Double getSaldoControl() {
		return saldoControl;
	}

	public void setSaldoControl(Double saldoControl) {
		this.saldoControl = saldoControl;
	}

	public String getSaldoControlStr() {
		return saldoControlStr;
	}

	public void setSaldoControlStr(String saldoControlStr) {
		this.saldoControlStr = saldoControlStr;
	}

	public String getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(String fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public String getEstatusDescripcion() {
		return estatusDescripcion;
	}

	public void setEstatusDescripcion(String estatusDescripcion) {
		this.estatusDescripcion = estatusDescripcion;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public boolean isPersistente() {
		return persistente;
	}

	public void setPersistente(boolean persistente) {
		this.persistente = persistente;
	}

	public boolean isNeteado() {
		return neteado;
	}

	public void setNeteado(boolean neteado) {
		this.neteado = neteado;
	}

	public boolean isNotNeteado() {
		return notNeteado;
	}

	public void setNotNeteado(boolean notNeteado) {
		this.notNeteado = notNeteado;
	}

	public boolean isColorPagoComplemento() {
		return colorPagoComplemento;
	}

	public void setColorPagoComplemento(boolean colorPagoComplemento) {
		this.colorPagoComplemento = colorPagoComplemento;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	@Override
	public String toString() {
		return "PagoComplementoFolioFacturaModel [idPagoComplementoFolioFactura=" + idPagoComplementoFolioFactura
				+ ", registro=" + registro + ", idFolioFactura=" + idFolioFactura + ", idFactura=" + idFactura
				+ ", idPagoComplemento=" + idPagoComplemento + ", folioFactura=" + folioFactura + ", rfc=" + rfc
				+ ", razonSocial=" + razonSocial + ", uuid=" + uuid + ", serie=" + serie + ", folio=" + folio
				+ ", montoPorPagar=" + montoPorPagar + ", montoPorPagarStr=" + montoPorPagarStr + ", pagoComplemento="
				+ pagoComplemento + ", pagoComplementoStr=" + pagoComplementoStr + ", monedaPago=" + monedaPago
				+ ", equivalencia=" + equivalencia + ", parcialidad=" + parcialidad + ", importeSaldoAnterior="
				+ importeSaldoAnterior + ", importeSaldoAnteriorStr=" + importeSaldoAnteriorStr + ", importePagado="
				+ importePagado + ", importePagadoStr=" + importePagadoStr + ", importePosterior=" + importePosterior
				+ ", importePosteriorStr=" + importePosteriorStr + ", saldoControl=" + saldoControl
				+ ", saldoControlStr=" + saldoControlStr + ", fechaRegistro=" + fechaRegistro + ", estatus=" + estatus
				+ ", estatusDescripcion=" + estatusDescripcion + ", checked=" + checked + ", disabled=" + disabled
				+ ", persistente=" + persistente + ", neteado=" + neteado + ", notNeteado=" + notNeteado
				+ ", getIdPagoComplementoFolioFactura()=" + getIdPagoComplementoFolioFactura() + ", getRegistro()="
				+ getRegistro() + ", getIdFolioFactura()=" + getIdFolioFactura() + ", getIdFactura()=" + getIdFactura()
				+ ", getIdPagoComplemento()=" + getIdPagoComplemento() + ", getFolioFactura()=" + getFolioFactura()
				+ ", getRfc()=" + getRfc() + ", getRazonSocial()=" + getRazonSocial() + ", getUuid()=" + getUuid()
				+ ", getSerie()=" + getSerie() + ", getFolio()=" + getFolio() + ", getMontoPorPagar()="
				+ getMontoPorPagar() + ", getMontoPorPagarStr()=" + getMontoPorPagarStr() + ", getPagoComplemento()="
				+ getPagoComplemento() + ", getPagoComplementoStr()=" + getPagoComplementoStr() + ", getMonedaPago()="
				+ getMonedaPago() + ", getEquivalencia()=" + getEquivalencia() + ", getParcialidad()="
				+ getParcialidad() + ", getImporteSaldoAnterior()=" + getImporteSaldoAnterior()
				+ ", getImporteSaldoAnteriorStr()=" + getImporteSaldoAnteriorStr() + ", getImportePagado()="
				+ getImportePagado() + ", getImportePagadoStr()=" + getImportePagadoStr() + ", getImportePosterior()="
				+ getImportePosterior() + ", getImportePosteriorStr()=" + getImportePosteriorStr()
				+ ", getSaldoControl()=" + getSaldoControl() + ", getSaldoControlStr()=" + getSaldoControlStr()
				+ ", getFechaRegistro()=" + getFechaRegistro() + ", getEstatus()=" + getEstatus()
				+ ", getEstatusDescripcion()=" + getEstatusDescripcion() + ", isChecked()=" + isChecked()
				+ ", getDisabled()=" + getDisabled() + ", isPersistente()=" + isPersistente() + ", isNeteado()="
				+ isNeteado() + ", isNotNeteado()=" + isNotNeteado() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}
}
