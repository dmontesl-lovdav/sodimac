package com.sodimac.oc.batch.entity;

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
@Table(name = "OrdenCompraProveedorTemp")
public class OrdenCompraProveedorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer idTemp;
	
	@Column(name = "IdOrdenCompra")
	private Integer id;

	@Column(name = "NumeroProveedor")
	private Integer numeroProveedor;

	@Column(name = "OrdenCompra")
	private Integer ordenCompra;

	@Column(name = "Recepcion")
	private Integer recepcion;

	@Column(name = "Sucursal")
	private String sucursal;

	@Column(name = "NoGuia")
	private String noGuia;

	@Column(name = "ImporteSinImpuesto")
	private Double importeSinImpuesto;

	@Column(name = "FechaRecepcion")
	private String fechaRecepcion;

	@Column(name = "Estatus")
	private String estatus;

	@Column(name = "Origen")
	private String origen;

	@Column(name = "FechaMovimiento")
	private String fechaMovimiento;

	@Column(name = "MotivoCancelacion")
	private String motivoCancelacion;

	@Column(name = "FechaRegistro")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRegistro;

	public OrdenCompraProveedorEntity() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(Integer numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public Integer getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(Integer ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public Integer getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(Integer recepcion) {
		this.recepcion = recepcion;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public String getNoGuia() {
		return noGuia;
	}

	public void setNoGuia(String noGuia) {
		this.noGuia = noGuia;
	}

	public Double getImporteSinImpuesto() {
		return importeSinImpuesto;
	}

	public void setImporteSinImpuesto(Double importeSinImpuesto) {
		this.importeSinImpuesto = importeSinImpuesto;
	}

	public String getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(String fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getFechaMovimiento() {
		return fechaMovimiento;
	}

	public void setFechaMovimiento(String fechaMovimiento) {
		this.fechaMovimiento = fechaMovimiento;
	}

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

}
