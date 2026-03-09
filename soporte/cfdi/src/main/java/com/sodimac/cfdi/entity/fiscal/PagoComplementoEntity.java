package com.sodimac.cfdi.entity.fiscal;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "pagocomplemento")
public class PagoComplementoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "numeroCuenta")
	private String numeroCuenta;
	
	@Column(name = "fechaHoraMovimiento")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaHoraMovimiento;
	
	@Column(name = "signo")
	private String signo;
	
	@Column(name = "importe", precision=11, scale=2)
	private BigDecimal importe;

	@Column(name = "folioBanco")
	private String folioBanco;
	
	@Column(name = "concepto")
	private String concepto;
	
	@Column(name = "leyenda")
	private String leyenda;
	
	@Column(name = "refInterbancaria")
	private String refInterbancaria;
	
	@Column(name = "folioCliente")
	private String folioCliente;
	
	@Column(name = "tipoDivisa")
	private String tipoDivisa;
	
	@Column(name = "folioOperacion")
	private String folioOperacion;
	
	@Column(name = "formaPago")
	private String formaPago;
	
	@Column(name = "transaccion")
	private String transaccion;
	
	@Column(name = "totalTransaccion", precision=11, scale=2)
	private BigDecimal totalTransaccion;
	
	@Column(name = "saldoAnterior", precision=11, scale=2)
	private BigDecimal saldoAnterior;
	
	@Column(name = "granTotal", precision=11, scale=2)
	private BigDecimal granTotal;
	
	@Column(name = "saldoPendiente", precision=11, scale=2)
	private BigDecimal saldoPendiente;

	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "uuidRelacionado")
	private String uuidRelacionado;
	
	@Column(name = "rfc")
	private String rfc;
	
	@Column(name = "estatus")
	private String estatus;
	
	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "usuarioCreacion")
	private Integer usuarioCreacion;

	@Column(name = "fechaModificacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	@Column(name = "usuarioModificacion")
	private Integer usuarioModificacion;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public java.util.Date getFechaHoraMovimiento() {
		return fechaHoraMovimiento;
	}

	public void setFechaHoraMovimiento(java.util.Date fechaHoraMovimiento) {
		this.fechaHoraMovimiento = fechaHoraMovimiento;
	}

	public String getSigno() {
		return signo;
	}

	public void setSigno(String signo) {
		this.signo = signo;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public String getFolioBanco() {
		return folioBanco;
	}

	public void setFolioBanco(String folioBanco) {
		this.folioBanco = folioBanco;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getLeyenda() {
		return leyenda;
	}

	public void setLeyenda(String leyenda) {
		this.leyenda = leyenda;
	}

	public String getRefInterbancaria() {
		return refInterbancaria;
	}

	public void setRefInterbancaria(String refInterbancaria) {
		this.refInterbancaria = refInterbancaria;
	}

	public String getFolioCliente() {
		return folioCliente;
	}

	public void setFolioCliente(String folioCliente) {
		this.folioCliente = folioCliente;
	}

	public String getTipoDivisa() {
		return tipoDivisa;
	}

	public void setTipoDivisa(String tipoDivisa) {
		this.tipoDivisa = tipoDivisa;
	}

	public String getFolioOperacion() {
		return folioOperacion;
	}

	public void setFolioOperacion(String folioOperacion) {
		this.folioOperacion = folioOperacion;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

	public BigDecimal getTotalTransaccion() {
		return totalTransaccion;
	}

	public void setTotalTransaccion(BigDecimal totalTransaccion) {
		this.totalTransaccion = totalTransaccion;
	}

	public BigDecimal getSaldoAnterior() {
		return saldoAnterior;
	}

	public void setSaldoAnterior(BigDecimal saldoAnterior) {
		this.saldoAnterior = saldoAnterior;
	}

	public BigDecimal getGranTotal() {
		return granTotal;
	}

	public void setGranTotal(BigDecimal granTotal) {
		this.granTotal = granTotal;
	}

	public BigDecimal getSaldoPendiente() {
		return saldoPendiente;
	}

	public void setSaldoPendiente(BigDecimal saldoPendiente) {
		this.saldoPendiente = saldoPendiente;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
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

	public Integer getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(Integer usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public Integer getUsuarioModificacion() {
		return usuarioModificacion;
	}

	public void setUsuarioModificacion(Integer usuarioModificacion) {
		this.usuarioModificacion = usuarioModificacion;
	}

	@Override
	public String toString() {
		return "PagoComplementoEntity [id=" + id + ", numeroCuenta=" + numeroCuenta + ", fechaHoraMovimiento="
				+ fechaHoraMovimiento + ", signo=" + signo + ", importe=" + importe + ", folioBanco=" + folioBanco
				+ ", concepto=" + concepto + ", leyenda=" + leyenda + ", refInterbancaria=" + refInterbancaria
				+ ", folioCliente=" + folioCliente + ", tipoDivisa=" + tipoDivisa + ", folioOperacion=" + folioOperacion
				+ ", formaPago=" + formaPago + ", transaccion=" + transaccion + ", totalTransaccion=" + totalTransaccion
				+ ", saldoAnterior=" + saldoAnterior + ", granTotal=" + granTotal + ", saldoPendiente=" + saldoPendiente
				+ ", uuid=" + uuid + ", uuidRelacionado=" + uuidRelacionado + ", rfc=" + rfc + ", estatus=" + estatus
				+ ", activo=" + activo + ", fechaCreacion=" + fechaCreacion + ", usuarioCreacion=" + usuarioCreacion
				+ ", fechaModificacion=" + fechaModificacion + ", usuarioModificacion=" + usuarioModificacion + "]";
	}
	
}
