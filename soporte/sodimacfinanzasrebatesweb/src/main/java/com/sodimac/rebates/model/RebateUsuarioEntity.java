package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_reporte_usuario")
public class RebateUsuarioEntity {

	@Id
	@Column(name = "RowNumber")
	private Integer rowNumber;
	@Column(name = "Origen")
	private String origen;
	@Column(name = "Moneda")
	private String moneda;
	@Column(name = "RFC")
	private String rfc;
	@Column(name = "CodigoProveedor")
	private String codigoProveedor;
	@Column(name = "Proveedor")
	private String proveedor;
	@Column(name = "GerenteNegocio")
	private String gerenteNegocio;
	@Column(name = "NombreGerente")
	private String nombreGerente;
	@Column(name = "NumeroJefeLinea")
	private String numeroJefeLinea;
	@Column(name = "NombreJefeLinea")
	private String nombreJefeLinea;
	@Column(name = "Familia")
	private String familia;
	@Column(name = "NombreFamilia")
	private String nombreFamilia;
	@Column(name = "MontoRecibido")
	private String montoRecibido;
	@Column(name = "OrdenCompra")
	private Integer ordenCompra;
	@Column(name = "FechaEmision")
	private String fechaEmision;
	@Column(name = "FechaRecepcion")
	private Date fechaRecepcion;
	@Column(name = "TipoAcuerdo")
	private String tipoAcuerdo;
	@Column(name = "Monedas")
	private String monedas;
	@Column(name = "Valor")
	private String valor;
	@Column(name = "TipoValor")
	private String tipoValor;
	@Column(name = "MontoRebate")
	private String montoRebate;
	@Column(name = "IdCatProgramaPago")
	private String idCatProgramaPago;
	@Column(name = "ProgramaPago")
	private String programaPago;
	@Column(name = "TipoOrdenCompra")
	private String tipoOrdenCompra;
	@Column(name = "SKU")
	private String sku;
	@Column(name = "DescripcionProducto")
	private String descripcionProducto;
	@Column(name = "IVA")
	private String iva;
	@Column(name = "IEPS")
	private String ieps;
	@Column(name = "MontoIva")
	private String montoIva;
	@Column(name = "MontoIeps")
	private String montoIeps;
	@Column(name = "MontoDescuento")
	private String montoDescuento;
	@Column(name = "TipoCambio")
	private String tipoCambio;
	@Column(name = "IdCatPeriodo")
	private String periodo;
	@Column(name = "IdTipoRebate")
	private String idTipoRebate;
	@Column(name = "Exclusion")
	private Integer exclusion;
	@Column(name = "FechaExclusion")
	private Date fechaExclusion;
	@Column(name = "IdExclusion")
	private Integer idExclusion;
	@Column(name = "Marca")
	private String marca;

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getCodigoProveedor() {
		return codigoProveedor;
	}

	public void setCodigoProveedor(String codigoProveedor) {
		this.codigoProveedor = codigoProveedor;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getGerenteNegocio() {
		return gerenteNegocio;
	}

	public void setGerenteNegocio(String gerenteNegocio) {
		this.gerenteNegocio = gerenteNegocio;
	}

	public String getNombreGerente() {
		return nombreGerente;
	}

	public void setNombreGerente(String nombreGerente) {
		this.nombreGerente = nombreGerente;
	}

	public String getNumeroJefeLinea() {
		return numeroJefeLinea;
	}

	public void setNumeroJefeLinea(String numeroJefeLinea) {
		this.numeroJefeLinea = numeroJefeLinea;
	}

	public String getNombreJefeLinea() {
		return nombreJefeLinea;
	}

	public void setNombreJefeLinea(String nombreJefeLinea) {
		this.nombreJefeLinea = nombreJefeLinea;
	}

	public String getFamilia() {
		return familia;
	}

	public void setFamilia(String familia) {
		this.familia = familia;
	}

	public String getNombreFamilia() {
		return nombreFamilia;
	}

	public void setNombreFamilia(String nombreFamilia) {
		this.nombreFamilia = nombreFamilia;
	}

	public String getMontoRecibido() {
		return montoRecibido;
	}

	public void setMontoRecibido(String montoRecibido) {
		this.montoRecibido = montoRecibido;
	}

	public Integer getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(Integer ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public String getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(String fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public String getTipoAcuerdo() {
		return tipoAcuerdo;
	}

	public void setTipoAcuerdo(String tipoAcuerdo) {
		this.tipoAcuerdo = tipoAcuerdo;
	}

	public String getMonedas() {
		return monedas;
	}

	public void setMonedas(String monedas) {
		this.monedas = monedas;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(String tipoValor) {
		this.tipoValor = tipoValor;
	}

	public String getMontoRebate() {
		return montoRebate;
	}

	public void setMontoRebate(String montoRebate) {
		this.montoRebate = montoRebate;
	}

	public String getIdCatProgramaPago() {
		return idCatProgramaPago;
	}

	public void setIdCatProgramaPago(String idCatProgramaPago) {
		this.idCatProgramaPago = idCatProgramaPago;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public String getTipoOrdenCompra() {
		return tipoOrdenCompra;
	}

	public void setTipoOrdenCompra(String tipoOrdenCompra) {
		this.tipoOrdenCompra = tipoOrdenCompra;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getDescripcionProducto() {
		return descripcionProducto;
	}

	public void setDescripcionProducto(String descripcionProducto) {
		this.descripcionProducto = descripcionProducto;
	}

	public String getIva() {
		return iva;
	}

	public void setIva(String iva) {
		this.iva = iva;
	}

	public String getIeps() {
		return ieps;
	}

	public void setIeps(String ieps) {
		this.ieps = ieps;
	}

	public String getMontoIva() {
		return montoIva;
	}

	public void setMontoIva(String montoIva) {
		this.montoIva = montoIva;
	}

	public String getMontoIeps() {
		return montoIeps;
	}

	public void setMontoIeps(String montoIeps) {
		this.montoIeps = montoIeps;
	}

	public String getMontoDescuento() {
		return montoDescuento;
	}

	public void setMontoDescuento(String montoDescuento) {
		this.montoDescuento = montoDescuento;
	}

	public String getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(String tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getIdTipoRebate() {
		return idTipoRebate;
	}

	public void setIdTipoRebate(String idTipoRebate) {
		this.idTipoRebate = idTipoRebate;
	}

	public Integer getExclusion() {
		return exclusion;
	}

	public void setExclusion(Integer exclusion) {
		this.exclusion = exclusion;
	}

	public Date getFechaExclusion() {
		return fechaExclusion;
	}

	public void setFechaExclusion(Date fechaExclusion) {
		this.fechaExclusion = fechaExclusion;
	}

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

}
