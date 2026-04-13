package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "RebateOrdenCompra")
public class RebateOrdenCompraEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdRebateOrdenCompra")
	private Integer idRebateOrdenCompra;
	
	@Column(name = "SKU")
	private String sku;
	
	@Column(name = "UsuarioRecepcion")
	private String usuarioRecepcion;
	
	@Column(name = "NumeroTienda")
	private Integer numeroTienda;
	
	@Column(name = "NumeroProveedor")
	private String numeroProveedor;
	
	@Column(name = "NumeroOrdenCompra")
	private Integer numeroOrdenCompra;
	
	@Column(name = "MonedaOrdenCompra")
	private String monedaOrdenCompra;
	
	@Column(name = "EstadoOrdenCompra")
	private String estadoOrdenCompra;
	
	@Column(name = "TipoOrdenCompra")
	private String tipoOrdenCompra;
	 
	@Column(name = "FechaEmision")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaEmision;
	
	@Column(name = "FechaReciboEsperada")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaReciboEsperada;
		    		  
    @Column(name = "FechaCancelacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCancelacion;
    
    @Column(name = "CantidadOrdenada")
	private Integer cantidadOrdenada;
    
    @Column(name = "CostoTotalOrdenado")
	private Double costoTotalOrdenado;
    
    @Column(name = "NumeroRecepcion")
	private Integer numeroRecepcion;
    
    @Column(name = "FechaRecepcion")
    @CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRecepcion;
    
    @Column(name = "CantidadRecibida")
	private Integer cantidadRecibida;
    
    @Column(name = "TotalRecibido")
	private Double totalRecibido;
    
    @Column(name = "Estado")
	private String estado;

	public Integer getIdRebateOrdenCompra() {
		return idRebateOrdenCompra;
	}

	public void setIdRebateOrdenCompra(Integer idRebateOrdenCompra) {
		this.idRebateOrdenCompra = idRebateOrdenCompra;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getUsuarioRecepcion() {
		return usuarioRecepcion;
	}

	public void setUsuarioRecepcion(String usuarioRecepcion) {
		this.usuarioRecepcion = usuarioRecepcion;
	}

	public Integer getNumeroTienda() {
		return numeroTienda;
	}

	public void setNumeroTienda(Integer numeroTienda) {
		this.numeroTienda = numeroTienda;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public Integer getNumeroOrdenCompra() {
		return numeroOrdenCompra;
	}

	public void setNumeroOrdenCompra(Integer numeroOrdenCompra) {
		this.numeroOrdenCompra = numeroOrdenCompra;
	}

	public String getMonedaOrdenCompra() {
		return monedaOrdenCompra;
	}

	public void setMonedaOrdenCompra(String monedaOrdenCompra) {
		this.monedaOrdenCompra = monedaOrdenCompra;
	}

	public String getEstadoOrdenCompra() {
		return estadoOrdenCompra;
	}

	public void setEstadoOrdenCompra(String estadoOrdenCompra) {
		this.estadoOrdenCompra = estadoOrdenCompra;
	}

	public String getTipoOrdenCompra() {
		return tipoOrdenCompra;
	}

	public void setTipoOrdenCompra(String tipoOrdenCompra) {
		this.tipoOrdenCompra = tipoOrdenCompra;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaReciboEsperada() {
		return fechaReciboEsperada;
	}

	public void setFechaReciboEsperada(Date fechaReciboEsperada) {
		this.fechaReciboEsperada = fechaReciboEsperada;
	}

	public Date getFechaCancelacion() {
		return fechaCancelacion;
	}

	public void setFechaCancelacion(Date fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}

	public Integer getCantidadOrdenada() {
		return cantidadOrdenada;
	}

	public void setCantidadOrdenada(Integer cantidadOrdenada) {
		this.cantidadOrdenada = cantidadOrdenada;
	}

	public Double getCostoTotalOrdenado() {
		return costoTotalOrdenado;
	}

	public void setCostoTotalOrdenado(Double costoTotalOrdenado) {
		this.costoTotalOrdenado = costoTotalOrdenado;
	}

	public Integer getNumeroRecepcion() {
		return numeroRecepcion;
	}

	public void setNumeroRecepcion(Integer numeroRecepcion) {
		this.numeroRecepcion = numeroRecepcion;
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public Integer getCantidadRecibida() {
		return cantidadRecibida;
	}

	public void setCantidadRecibida(Integer cantidadRecibida) {
		this.cantidadRecibida = cantidadRecibida;
	}

	public Double getTotalRecibido() {
		return totalRecibido;
	}

	public void setTotalRecibido(Double totalRecibido) {
		this.totalRecibido = totalRecibido;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
}
