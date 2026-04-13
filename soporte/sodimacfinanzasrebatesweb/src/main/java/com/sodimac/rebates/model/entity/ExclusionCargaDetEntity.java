package com.sodimac.rebates.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "ExclusionCargaDet")
public class ExclusionCargaDetEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdExclusionCargaDet")
	private Long idExclusionCargaDet;

	@Column(name = "IdExclusionCarga")
	private Integer idExclusionCarga;
	
	@JoinColumn(name = "IdExclusionCarga", referencedColumnName = "idExclusionCarga", insertable = false, updatable = false)
	@ManyToOne(optional = false,  fetch = FetchType.LAZY)
	private ExclusionCargaEntity exclusionCarga;

	@Column(name = "NumProveedor")
	private String numProveedor;
	
	@Column(name = "NomProveedor")
	private String nomProveedor;

	@Column(name = "OrdenCompra")
	private String ordenCompra;

	@Column(name = "Clacom")
	private String clacom;

	@Column(name = "Sku")
	private String sku;

	@Column(name = "SkuDescripcion")
	private String skuDescripcion;

	@Column(name = "Activo")
	private boolean activo;
	
	@Column(name = "CantidadOrdenada")
	private Float cantidadOrdenada;
	
	@Column(name = "CantidadRecibida")
	private Float cantidadRecibida;

	@Column(name = "PeriodoVigente")
	private Integer periodoVigente;

	@Column(name = "TieneAcuerdo")
	private boolean tieneAcuerdo;

	public Long getIdExclusionCargaDet() {
		return idExclusionCargaDet;
	}

	public void setIdExclusionCargaDet(Long idExclusionCargaDet) {
		this.idExclusionCargaDet = idExclusionCargaDet;
	}

	public Integer getIdExclusionCarga() {
		return idExclusionCarga;
	}

	public void setIdExclusionCarga(Integer idExclusionCarga) {
		this.idExclusionCarga = idExclusionCarga;
	}

	public ExclusionCargaEntity getExclusionCarga() {
		return exclusionCarga;
	}

	public void setExclusionCarga(ExclusionCargaEntity exclusionCarga) {
		this.exclusionCarga = exclusionCarga;
	}

	public String getNumProveedor() {
		return numProveedor;
	}

	public void setNumProveedor(String numProveedor) {
		this.numProveedor = numProveedor;
	}
	
	public String getNomProveedor() {
		return nomProveedor;
	}

	public void setNomProveedor(String nomProveedor) {
		this.nomProveedor = nomProveedor;
	}

	public String getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(String ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public String getClacom() {
		return clacom;
	}

	public void setClacom(String clacom) {
		this.clacom = clacom;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSkuDescripcion() {
		return skuDescripcion;
	}

	public void setSkuDescripcion(String skuDescripcion) {
		this.skuDescripcion = skuDescripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Float getCantidadOrdenada() {
		return cantidadOrdenada;
	}

	public void setCantidadOrdenada(Float cantidadOrdenada) {
		this.cantidadOrdenada = cantidadOrdenada;
	}

	public Float getCantidadRecibida() {
		return cantidadRecibida;
	}

	public void setCantidadRecibida(Float cantidadRecibida) {
		this.cantidadRecibida = cantidadRecibida;
	}

	public Integer getPeriodoVigente() {
		return periodoVigente;
	}

	public void setPeriodoVigente(Integer periodoVigente) {
		this.periodoVigente = periodoVigente;
	}

	public boolean isTieneAcuerdo() {
		return tieneAcuerdo;
	}

	public void setTieneAcuerdo(boolean tieneAcuerdo) {
		this.tieneAcuerdo = tieneAcuerdo;
	}

}
