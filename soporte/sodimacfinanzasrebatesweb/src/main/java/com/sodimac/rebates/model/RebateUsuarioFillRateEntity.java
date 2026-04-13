package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_reporte_usuario_fillrate")
public class RebateUsuarioFillRateEntity {

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
	@Column(name = "NumDepto")
	private String numDepto;
	@Column(name = "NombreDepto")
	private String nombreDepto;
	@Column(name = "CostoUnitario")
	private Double costoUnitario;
	@Column(name = "CantidadRecibida")
	private Double cantidadRecibida;
	@Column(name = "MontoRecibido")
	private Double montoRecibido;
	@Column(name = "OrdenCompra")
	private Integer ordenCompra;
	@Column(name = "FechaEmision")
	private String fechaEmision;
	@Column(name = "FechaRecepcion")
	private Date fechaRecepcion;
	@Column(name = "TipoAcuerdo")
	private String tipoAcuerdo;
	@Column(name = "MonedaS")
	private String monedaS;
	@Column(name = "Valor")
	private Double valor;
	@Column(name = "TipoValor")
	private String tipoValor;
	@Column(name = "MontoDescuento")
	private Double montoDescuento;
	@Column(name = "ProgramaPago")
	private String programaPago;
	@Column(name = "Periodo")
	private String periodo;
	@Column(name = "IdCatPeriodo")
	private Integer idCatPeriodo;
	@Column(name = "Tienda")
	private Integer tienda;
	@Column(name = "TipoOrdeCompra")
	private String tipoOrdenCompra;
	@Column(name = "SKU")
	private String sku;
	@Column(name = "DescripcionProducto")
	private String descripcionProducto;
	@Column(name = "IVA")
	private Double iva;
	@Column(name = "IEPS")
	private Double ieps;
	@Column(name = "MontoIva")
	private Double montoIva;
	@Column(name = "MontoIeps")
	private Double montoIeps;
	@Column(name = "LtEnvio")
	private Integer ltEnvio;
	@Column(name = "LtProceso")
	private Integer ltProceso;
	@Column(name = "DiasTotales")
	private Integer diasTotales;
	@Column(name = "DiasDesfase")
	private Integer diasDesfase;
	@Column(name = "Faltante")
	private Double faltante;
	@Column(name = "FaltanteGlobal")
	private Double faltanteGlobal;
	@Column(name = "CantidadOrdenada")
	private Double cantidadOrdenada;
	@Column(name = "MontoOrdenado")
	private Double montoOrdenado;
	@Column(name = "MontoOrdenadoTotal")
	private Double montoOrdenadoTotal;
	@Column(name = "PorcentajeFRPiezas")
	private Double porcentajeFRPiezas;
	@Column(name = "PorcentajeFRMonto")
	private Double porcentajeFRMonto;
	@Column(name = "SemanaAnio")
	private Integer semanaAnio;
	@Column(name = "EstatusContrato")
	private String estatusContrato;
	@Column(name = "LeadTime")
	private String leadTime;
	@Column(name = "FillRate")
	private String fillRate;
	@Column(name = "MontoDescuentoFillRate")
	private Double montoDescuentoFillRate;
	@Column(name = "MontoDescuentoFillRateSinImpuestos")
	private Double montoDescuentoFillRateSinImpuestos;
	@Column(name = "MontoDescuentoFillRateInf")
	private Double montoDescuentoFillRateInf;
	@Column(name = "FechaUltimaRecepcion")
	private String fechaUltimaRecepcion;
	@Column(name = "FecRecepcionInicial")
	private String fecRecepcionInicial;
	@Column(name = "TiendaCD")
	private Integer tiendaCD;
	@Column(name = "TipoCambio")
	private Double tipoCambio;
	@Column(name = "CuentaGlobal")
	private Integer cuentaGlobal;
	@Column(name = "Exclusion")
	private String exclusion;
	@Column(name = "FechaExclusion")
	private String fechaExclusion;
	@Column(name = "IdExclusion")
	private String idExclusion;
	@Column(name = "DiasLeadTime")
	private int diasLeadTime;
	@Column(name = "NumeroSemana")
	private String numeroSemana;
	
	@Column(name = "DetallePeriodo")
	private String detallePeriodo;
	@Column(name = "FechaIni")
	private String fechaIni;
	@Column(name = "FechaFin")
	private String fechaFin;
	@Column(name = "CalculoLogistica")
	private int calculoLogistica;
	@Column(name = "CalculoFinanzas")
	private int calculoFinanzas;

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

	public String getNumDepto() {
		return numDepto;
	}

	public void setNumDepto(String numDepto) {
		this.numDepto = numDepto;
	}

	public String getNombreDepto() {
		return nombreDepto;
	}

	public void setNombreDepto(String nombreDepto) {
		this.nombreDepto = nombreDepto;
	}

	public Double getCostoUnitario() {
		return costoUnitario;
	}

	public void setCostoUnitario(Double costoUnitario) {
		this.costoUnitario = costoUnitario;
	}

	public Double getCantidadRecibida() {
		return cantidadRecibida;
	}

	public void setCantidadRecibida(Double cantidadRecibida) {
		this.cantidadRecibida = cantidadRecibida;
	}

	public Double getMontoRecibido() {
		return montoRecibido;
	}

	public void setMontoRecibido(Double montoRecibido) {
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

	public String getMonedaS() {
		return monedaS;
	}

	public void setMonedaS(String monedaS) {
		this.monedaS = monedaS;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public String getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(String tipoValor) {
		this.tipoValor = tipoValor;
	}

	public Double getMontoDescuento() {
		return montoDescuento;
	}

	public void setMontoDescuento(Double montoDescuento) {
		this.montoDescuento = montoDescuento;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public Integer getIdCatPeriodo() {
		return idCatPeriodo;
	}

	public void setIdCatPeriodo(Integer idCatPeriodo) {
		this.idCatPeriodo = idCatPeriodo;
	}

	public Integer getTienda() {
		return tienda;
	}

	public void setTienda(Integer tienda) {
		this.tienda = tienda;
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

	public Double getIva() {
		return iva;
	}

	public void setIva(Double iva) {
		this.iva = iva;
	}

	public Double getIeps() {
		return ieps;
	}

	public void setIeps(Double ieps) {
		this.ieps = ieps;
	}

	public Double getMontoIva() {
		return montoIva;
	}

	public void setMontoIva(Double montoIva) {
		this.montoIva = montoIva;
	}

	public Double getMontoIeps() {
		return montoIeps;
	}

	public void setMontoIeps(Double montoIeps) {
		this.montoIeps = montoIeps;
	}

	public Integer getLtEnvio() {
		return ltEnvio;
	}

	public void setLtEnvio(Integer ltEnvio) {
		this.ltEnvio = ltEnvio;
	}

	public Integer getLtProceso() {
		return ltProceso;
	}

	public void setLtProceso(Integer ltProceso) {
		this.ltProceso = ltProceso;
	}

	public Integer getDiasTotales() {
		return diasTotales;
	}

	public void setDiasTotales(Integer diasTotales) {
		this.diasTotales = diasTotales;
	}

	public Integer getDiasDesfase() {
		return diasDesfase;
	}

	public void setDiasDesfase(Integer diasDesfase) {
		this.diasDesfase = diasDesfase;
	}

	public Double getFaltante() {
		return faltante;
	}

	public void setFaltante(Double faltante) {
		this.faltante = faltante;
	}

	public Double getFaltanteGlobal() {
		return faltanteGlobal;
	}

	public void setFaltanteGlobal(Double faltanteGlobal) {
		this.faltanteGlobal = faltanteGlobal;
	}

	public Double getCantidadOrdenada() {
		return cantidadOrdenada;
	}

	public void setCantidadOrdenada(Double cantidadOrdenada) {
		this.cantidadOrdenada = cantidadOrdenada;
	}

	public Double getMontoOrdenado() {
		return montoOrdenado;
	}

	public void setMontoOrdenado(Double montoOrdenado) {
		this.montoOrdenado = montoOrdenado;
	}

	public Double getMontoOrdenadoTotal() {
		return montoOrdenadoTotal;
	}

	public void setMontoOrdenadoTotal(Double montoOrdenadoTotal) {
		this.montoOrdenadoTotal = montoOrdenadoTotal;
	}

	public Double getPorcentajeFRPiezas() {
		return porcentajeFRPiezas;
	}

	public void setPorcentajeFRPiezas(Double porcentajeFRPiezas) {
		this.porcentajeFRPiezas = porcentajeFRPiezas;
	}

	public Double getPorcentajeFRMonto() {
		return porcentajeFRMonto;
	}

	public void setPorcentajeFRMonto(Double porcentajeFRMonto) {
		this.porcentajeFRMonto = porcentajeFRMonto;
	}

	public Integer getSemanaAnio() {
		return semanaAnio;
	}

	public void setSemanaAnio(Integer semanaAnio) {
		this.semanaAnio = semanaAnio;
	}

	public String getEstatusContrato() {
		return estatusContrato;
	}

	public void setEstatusContrato(String estatusContrato) {
		this.estatusContrato = estatusContrato;
	}

	public String getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(String leadTime) {
		this.leadTime = leadTime;
	}

	public String getFillRate() {
		return fillRate;
	}

	public void setFillRate(String fillRate) {
		this.fillRate = fillRate;
	}

	public Double getMontoDescuentoFillRate() {
		return montoDescuentoFillRate;
	}

	public void setMontoDescuentoFillRate(Double montoDescuentoFillRate) {
		this.montoDescuentoFillRate = montoDescuentoFillRate;
	}

	public Double getMontoDescuentoFillRateSinImpuestos() {
		return montoDescuentoFillRateSinImpuestos;
	}

	public void setMontoDescuentoFillRateSinImpuestos(Double montoDescuentoFillRateSinImpuestos) {
		this.montoDescuentoFillRateSinImpuestos = montoDescuentoFillRateSinImpuestos;
	}

	public Double getMontoDescuentoFillRateInf() {
		return montoDescuentoFillRateInf;
	}

	public void setMontoDescuentoFillRateInf(Double montoDescuentoFillRateInf) {
		this.montoDescuentoFillRateInf = montoDescuentoFillRateInf;
	}

	public String getFechaUltimaRecepcion() {
		return fechaUltimaRecepcion;
	}

	public void setFechaUltimaRecepcion(String fechaUltimaRecepcion) {
		this.fechaUltimaRecepcion = fechaUltimaRecepcion;
	}

	public String getFecRecepcionInicial() {
		return fecRecepcionInicial;
	}

	public void setFecRecepcionInicial(String fecRecepcionInicial) {
		this.fecRecepcionInicial = fecRecepcionInicial;
	}

	public Integer getTiendaCD() {
		return tiendaCD;
	}

	public void setTiendaCD(Integer tiendaCD) {
		this.tiendaCD = tiendaCD;
	}

	public Double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(Double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Integer getCuentaGlobal() {
		return cuentaGlobal;
	}

	public void setCuentaGlobal(Integer cuentaGlobal) {
		this.cuentaGlobal = cuentaGlobal;
	}

	public String getExclusion() {
		return exclusion;
	}

	public void setExclusion(String exclusion) {
		this.exclusion = exclusion;
	}

	public String getFechaExclusion() {
		return fechaExclusion;
	}

	public void setFechaExclusion(String fechaExclusion) {
		this.fechaExclusion = fechaExclusion;
	}

	public String getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(String idExclusion) {
		this.idExclusion = idExclusion;
	}

	public int getDiasLeadTime() {
		return diasLeadTime;
	}

	public void setDiasLeadTime(int diasLeadTime) {
		this.diasLeadTime = diasLeadTime;
	}

	public String getNumeroSemana() {
		return numeroSemana;
	}

	public void setNumeroSemana(String numeroSemana) {
		this.numeroSemana = numeroSemana;
	}

	public String getDetallePeriodo() {
		return detallePeriodo;
	}

	public void setDetallePeriodo(String detallePeriodo) {
		this.detallePeriodo = detallePeriodo;
	}

	public String getFechaIni() {
		return fechaIni;
	}

	public void setFechaIni(String fechaIni) {
		this.fechaIni = fechaIni;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	public int getCalculoLogistica() {
		return calculoLogistica;
	}

	public void setCalculoLogistica(int calculoLogistica) {
		this.calculoLogistica = calculoLogistica;
	}

	public int getCalculoFinanzas() {
		return calculoFinanzas;
	}

	public void setCalculoFinanzas(int calculoFinanzas) {
		this.calculoFinanzas = calculoFinanzas;
	}

}
