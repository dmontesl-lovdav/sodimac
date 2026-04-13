package com.sodimac.rebates.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "RebateOrdenCompraFill")
public class RebateOrdenCompraFillEntity implements Serializable {
	
	private static final long serialVersionUID = -6491323930178871875L;

	@EmbeddedId
	private RebateOrdenCompraFillId id;
	
	@Column(name = "UsuarioRecepcion")
	private String usuarioRecepcion;

	private Integer numeroTienda;
	private Integer numeroProveedor;
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
    
    @Column(name = "FechaRecepcion")
    @CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRecepcion;
    
    @Column(name = "CantidadRecibida")
	private Integer cantidadRecibida;
    
    @Column(name = "TotalRecibido")
	private Double totalRecibido;
    
    @Column(name = "NumeroTiendaCD")
    private Integer numeroTiendaCd;
    
    @Column(name = "Fec_Recepcion_Inicial")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRecepcionInicial;
	
	@Column(name = "Fecha_Ultima_Recepcion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaUltimaRecepcion;
	
	@Column(name = "FechaRegistro")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRegistro;

	public RebateOrdenCompraFillId getId() {
		return id;
	}

	public void setId(RebateOrdenCompraFillId id) {
		this.id = id;
	}

	public String getUsuarioRecepcion() {
		return usuarioRecepcion;
	}

	public void setUsuarioRecepcion(String usuarioRecepcion) {
		this.usuarioRecepcion = usuarioRecepcion;
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

	public Integer getNumeroTiendaCd() {
		return numeroTiendaCd;
	}

	public void setNumeroTiendaCd(Integer numeroTiendaCd) {
		this.numeroTiendaCd = numeroTiendaCd;
	}

	public Date getFechaRecepcionInicial() {
		return fechaRecepcionInicial;
	}

	public void setFechaRecepcionInicial(Date fechaRecepcionInicial) {
		this.fechaRecepcionInicial = fechaRecepcionInicial;
	}

	public Date getFechaUltimaRecepcion() {
		return fechaUltimaRecepcion;
	}

	public void setFechaUltimaRecepcion(Date fechaUltimaRecepcion) {
		this.fechaUltimaRecepcion = fechaUltimaRecepcion;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Integer getNumeroTienda() {
		return numeroTienda;
	}

	public void setNumeroTienda(Integer numeroTienda) {
		this.numeroTienda = numeroTienda;
	}

	public Integer getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(Integer numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public Integer getNumeroOrdenCompra() {
		return numeroOrdenCompra;
	}

	public void setNumeroOrdenCompra(Integer numeroOrdenCompra) {
		this.numeroOrdenCompra = numeroOrdenCompra;
	}
	
}
