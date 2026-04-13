package com.sodimac.rebates.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_poliza_contable_reporte")
public class PolizaContableReporteEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", columnDefinition = "uniqueidentifier")
	private String id;
	
	@Column(name = "EMPRESA")
	private String empresa;
	
	@Column(name = "FECHA_DOCUMENTO")
	private Date fechaDocumento;
	
	@Column(name = "REFERENCIA_DOCUMENTO")
	private String referenciaDocumento;
	
	@Column(name = "NUMERO_DOCUMENTO")
	private String numeroDocumento;
	
	@Column(name = "MONEDA")
	private String moneda;
	
	@Column(name = "TIPO_CAMBIO")
	private Integer tipoCambio;

	@Column(name = "DEBITO_CREDITO")
	private String debitoCredito;
	
	@Column(name = "CUENTA_CONTABLE")
	private String cuentaContable;
	
	@Column(name = "CODIGO_PROVEEDOR")
	private String codigoProveedor;
	
	@Column(name = "IMPORTE")
	private Double importe;
	
	@Column(name = "SUCURSAL")
	private String sucursal;
	
	@Column(name = "CONDICION_PAGO")
	private String condicionPago;
	
	@Column(name = "FECHA_VENCIMIENTO")
	private Date fechaVencimiento;
	
	@Column(name = "BLOQUEO_PAGO")
	private String bloqueoPago;
	
	@Column(name = "SISTEMA_ORIGEN")
	private String sistemaOrigen;
	
	@Column(name = "FECHA_ENVIO")
	private Date fechaEnvio;
	
	@Column(name = "FECHA_CONTABLE")
	private Date fechaContable;

	@Column(name = "CLASE_DOCUMENTO")
	private String claseDocumento;

	@Column(name = "NUMERO_REFERENCIA")
	private String numeroReferencia;
	
	@Column(name = "CENTRO_COSTO")
	private String centroCosto;
	
	@Column(name = "CENTRO_BENEFICIO")
	private String centroBeneficio;
	
	@Column(name = "NUMERO_UUID")
	private String numeroUuid;
	
	@Column(name = "FLAG_ENVIADO")
	private String flagEnviado;
	
	@Column(name = "FECHA_RECEPCION")
	private Date fechaRecepcion;
	
	@Column(name = "TIPO_DOCUMENTO")
	private String tipoDocumento;
	
	@Column(name = "ORIGEN_ETL")
	private String origenEtl;
	
	@Column(name = "IdPeriodo")
	private Integer idPeriodo;
	
	@Column(name = "FECHA_INICIO_PERIODO")
	private Date fechaInicioPeriodo;
	
	@Column(name = "FECHA_FINAL_PERIODO")
	private Date fechaFinPeriodo;
	
	@Column(name = "idCatTipoRebate")
	private Integer idTipoRebate;
	
	@Column(name = "TipoRebateName")
	private String tipoRebate;
	
	@Column(name = "Timbrado")
	private Integer timbrado;

	@Column(name = "MONTO_CALCULADO")
	private Double montoCalculado;
	
	@Column(name = "MONTO_CONTABILIZADO")
	private Double montoContabilizado;

	public String getId() {
		return id;
	}

	public String getEmpresa() {
		return empresa;
	}

	public Date getFechaDocumento() {
		return fechaDocumento;
	}

	public String getReferenciaDocumento() {
		return referenciaDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public String getMoneda() {
		return moneda;
	}

	public Integer getTipoCambio() {
		return tipoCambio;
	}

	public String getDebitoCredito() {
		return debitoCredito;
	}

	public String getCuentaContable() {
		return cuentaContable;
	}

	public String getCodigoProveedor() {
		return codigoProveedor;
	}

	public Double getImporte() {
		return importe;
	}

	public String getSucursal() {
		return sucursal;
	}

	public String getCondicionPago() {
		return condicionPago;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public String getBloqueoPago() {
		return bloqueoPago;
	}

	public String getSistemaOrigen() {
		return sistemaOrigen;
	}

	public Date getFechaEnvio() {
		return fechaEnvio;
	}

	public Date getFechaContable() {
		return fechaContable;
	}

	public String getClaseDocumento() {
		return claseDocumento;
	}

	public String getNumeroReferencia() {
		return numeroReferencia;
	}

	public String getCentroCosto() {
		return centroCosto;
	}

	public String getCentroBeneficio() {
		return centroBeneficio;
	}

	public String getNumeroUuid() {
		return numeroUuid;
	}

	public String getFlagEnviado() {
		return flagEnviado;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public String getOrigenEtl() {
		return origenEtl;
	}

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public Date getFechaInicioPeriodo() {
		return fechaInicioPeriodo;
	}

	public Date getFechaFinPeriodo() {
		return fechaFinPeriodo;
	}

	public Integer getIdTipoRebate() {
		return idTipoRebate;
	}

	public String getTipoRebate() {
		return tipoRebate;
	}

	public Integer getTimbrado() {
		return timbrado;
	}

	public Double getMontoCalculado() {
		return montoCalculado;
	}

	public Double getMontoContabilizado() {
		return montoContabilizado;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public void setFechaDocumento(Date fechaDocumento) {
		this.fechaDocumento = fechaDocumento;
	}

	public void setReferenciaDocumento(String referenciaDocumento) {
		this.referenciaDocumento = referenciaDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public void setTipoCambio(Integer tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public void setDebitoCredito(String debitoCredito) {
		this.debitoCredito = debitoCredito;
	}

	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}

	public void setCodigoProveedor(String codigoProveedor) {
		this.codigoProveedor = codigoProveedor;
	}

	public void setImporte(Double importe) {
		this.importe = importe;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public void setCondicionPago(String condicionPago) {
		this.condicionPago = condicionPago;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public void setBloqueoPago(String bloqueoPago) {
		this.bloqueoPago = bloqueoPago;
	}

	public void setSistemaOrigen(String sistemaOrigen) {
		this.sistemaOrigen = sistemaOrigen;
	}

	public void setFechaEnvio(Date fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}

	public void setFechaContable(Date fechaContable) {
		this.fechaContable = fechaContable;
	}

	public void setClaseDocumento(String claseDocumento) {
		this.claseDocumento = claseDocumento;
	}

	public void setNumeroReferencia(String numeroReferencia) {
		this.numeroReferencia = numeroReferencia;
	}

	public void setCentroCosto(String centroCosto) {
		this.centroCosto = centroCosto;
	}

	public void setCentroBeneficio(String centroBeneficio) {
		this.centroBeneficio = centroBeneficio;
	}

	public void setNumeroUuid(String numeroUuid) {
		this.numeroUuid = numeroUuid;
	}

	public void setFlagEnviado(String flagEnviado) {
		this.flagEnviado = flagEnviado;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public void setOrigenEtl(String origenEtl) {
		this.origenEtl = origenEtl;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public void setFechaInicioPeriodo(Date fechaInicioPeriodo) {
		this.fechaInicioPeriodo = fechaInicioPeriodo;
	}

	public void setFechaFinPeriodo(Date fechaFinPeriodo) {
		this.fechaFinPeriodo = fechaFinPeriodo;
	}

	public void setIdTipoRebate(Integer idTipoRebate) {
		this.idTipoRebate = idTipoRebate;
	}

	public void setTipoRebate(String tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public void setTimbrado(Integer timbrado) {
		this.timbrado = timbrado;
	}

	public void setMontoCalculado(Double montoCalculado) {
		this.montoCalculado = montoCalculado;
	}

	public void setMontoContabilizado(Double montoContabilizado) {
		this.montoContabilizado = montoContabilizado;
	}

}
