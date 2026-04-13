package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "CalculoRebateMSI3")
public class CalculoRebateMSI3Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdCalculoRebate")
	private Integer idCalculoRebate;

	@Column(name = "Origen")
	private String origen;

	@Column(name = "MonedaVenta")
	private String monedaVenta;

	@Column(name = "RFC")
	private String rfc;

	@Column(name = "NumeroProveedor")
	private String numeroProveedor;

	@Column(name = "Familia")
	private String familia;

	@Column(name = "NombreFamilia")
	private String nombreFamilia;

	@Column(name = "TicketVenta")
	private String ticketVenta;

	@Column(name = "SucursalVenta")
	private String sucursalVenta;

	@Column(name = "FechaVenta")
	private Date fechaVenta;

	@Column(name = "Banco")
	private String banco;

	@Column(name = "NumCuota")
	private Integer numCuota;

	@Column(name = "SKU")
	private String sku;

	@Column(name = "DescripcionProducto")
	private String descripcionProducto;

	@Column(name = "SubtotalSKU")
	private Double subtotalSku;

	@Column(name = "MontoVentaSku")
	private Double montoVentaSku;

	@Column(name = "TipoAcuerdo")
	private String tipoAcuerdo;

	@Column(name = "MonedaAcuerdo")
	private String MonedaAcuerdo;

	@Column(name = "ValorDescuento")
	private Double valorDescuento;

	@Column(name = "TipoDescuento")
	private String tipoDescuento;

	@Column(name = "MontoRebate")
	private Double montoRebate;

	@Column(name = "IvaRebate")
	private Double ivaRebate;

	@Column(name = "MontoTotalRebate")
	private Double montoTotalRebate;

	@Column(name = "ProgramaPago")
	private String programaPago;

	@Column(name = "IdPeriodo")
	private Integer idPeriodo;

	@Column(name = "SubtotalCuenta")
	private String subtotalCuenta;

	@Column(name = "IVACuenta")
	private String ivaCuenta;

	@Column(name = "ProveedorMercancia")
	private String proveedorMercancia;

	@Column(name = "TipoDocumentoPoliza")
	private String tipoDocumentoPoliza;

	@Column(name = "CentroCostos")
	private String centroCostos;

	@Column(name = "CentroBeneficios")
	private String centroBeneficios;

	@Column(name = "Sucursal")
	private Integer sucursal;

	@Column(name = "CondicionesPago")
	private String condicionesPago;

	public Integer getIdCalculoRebate() {
		return idCalculoRebate;
	}

	public void setIdCalculoRebate(Integer idCalculoRebate) {
		this.idCalculoRebate = idCalculoRebate;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getMonedaVenta() {
		return monedaVenta;
	}

	public void setMonedaVenta(String monedaVenta) {
		this.monedaVenta = monedaVenta;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
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

	public String getTicketVenta() {
		return ticketVenta;
	}

	public void setTicketVenta(String ticketVenta) {
		this.ticketVenta = ticketVenta;
	}

	public String getSucursalVenta() {
		return sucursalVenta;
	}

	public void setSucursalVenta(String sucursalVenta) {
		this.sucursalVenta = sucursalVenta;
	}

	public Date getFechaVenta() {
		return fechaVenta;
	}

	public void setFechaVenta(Date fechaVenta) {
		this.fechaVenta = fechaVenta;
	}

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public Integer getNumCuota() {
		return numCuota;
	}

	public void setNumCuota(Integer numCuota) {
		this.numCuota = numCuota;
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

	public Double getSubtotalSku() {
		return subtotalSku;
	}

	public void setSubtotalSku(Double subtotalSku) {
		this.subtotalSku = subtotalSku;
	}

	public Double getMontoVentaSku() {
		return montoVentaSku;
	}

	public void setMontoVentaSku(Double montoVentaSku) {
		this.montoVentaSku = montoVentaSku;
	}

	public String getTipoAcuerdo() {
		return tipoAcuerdo;
	}

	public void setTipoAcuerdo(String tipoAcuerdo) {
		this.tipoAcuerdo = tipoAcuerdo;
	}

	public String getMonedaAcuerdo() {
		return MonedaAcuerdo;
	}

	public void setMonedaAcuerdo(String monedaAcuerdo) {
		MonedaAcuerdo = monedaAcuerdo;
	}

	public Double getValorDescuento() {
		return valorDescuento;
	}

	public void setValorDescuento(Double valorDescuento) {
		this.valorDescuento = valorDescuento;
	}

	public String getTipoDescuento() {
		return tipoDescuento;
	}

	public void setTipoDescuento(String tipoDescuento) {
		this.tipoDescuento = tipoDescuento;
	}

	public Double getMontoRebate() {
		return montoRebate;
	}

	public void setMontoRebate(Double montoRebate) {
		this.montoRebate = montoRebate;
	}

	public Double getIvaRebate() {
		return ivaRebate;
	}

	public void setIvaRebate(Double ivaRebate) {
		this.ivaRebate = ivaRebate;
	}

	public Double getMontoTotalRebate() {
		return montoTotalRebate;
	}

	public void setMontoTotalRebate(Double montoTotalRebate) {
		this.montoTotalRebate = montoTotalRebate;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public String getSubtotalCuenta() {
		return subtotalCuenta;
	}

	public void setSubtotalCuenta(String subtotalCuenta) {
		this.subtotalCuenta = subtotalCuenta;
	}

	public String getIvaCuenta() {
		return ivaCuenta;
	}

	public void setIvaCuenta(String ivaCuenta) {
		this.ivaCuenta = ivaCuenta;
	}

	public String getProveedorMercancia() {
		return proveedorMercancia;
	}

	public void setProveedorMercancia(String proveedorMercancia) {
		this.proveedorMercancia = proveedorMercancia;
	}

	public String getTipoDocumentoPoliza() {
		return tipoDocumentoPoliza;
	}

	public void setTipoDocumentoPoliza(String tipoDocumentoPoliza) {
		this.tipoDocumentoPoliza = tipoDocumentoPoliza;
	}

	public String getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(String centroCostos) {
		this.centroCostos = centroCostos;
	}

	public String getCentroBeneficios() {
		return centroBeneficios;
	}

	public void setCentroBeneficios(String centroBeneficios) {
		this.centroBeneficios = centroBeneficios;
	}

	public Integer getSucursal() {
		return sucursal;
	}

	public void setSucursal(Integer sucursal) {
		this.sucursal = sucursal;
	}

	public String getCondicionesPago() {
		return condicionesPago;
	}

	public void setCondicionesPago(String condicionesPago) {
		this.condicionesPago = condicionesPago;
	}

}
