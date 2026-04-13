package com.sodimac.rebates.dto;

import java.util.Date;

public class ExclusionViewDetDto {

	private Integer idCatPeriodo;
	private String detallePeriodo;
	private Date periodoFechaIni;
	private Date periodoFechaFin;
	private Integer idExclusion;
	private Integer idCatTipoRebate;
	private String descripcionRebate;
	private Integer idCatTipoExclusion;
	private String descripcionExclusion;
	private Integer idCatEstatusExclusion;
	private String folio;
	private Integer contabilizado;
	private String comentario;
	private Integer IdExclusionCarga;
	private Integer idExclusionCargaDet;
	private String motivo;
	private String numProveedor;
	private String nomProveedor;
	private String ordenCompra;
	private String clacom;
	private String sku;
	private String skuDescripcion;
	private Integer periodoVigente;
	private boolean tieneAcuerdo;

	public Integer getIdCatPeriodo() {
		return idCatPeriodo;
	}

	public void setIdCatPeriodo(Integer idCatPeriodo) {
		this.idCatPeriodo = idCatPeriodo;
	}

	public String getDetallePeriodo() {
		return detallePeriodo;
	}

	public void setDetallePeriodo(String detallePeriodo) {
		this.detallePeriodo = detallePeriodo;
	}

	public Date getPeriodoFechaIni() {
		return periodoFechaIni;
	}

	public void setPeriodoFechaIni(Date periodoFechaIni) {
		this.periodoFechaIni = periodoFechaIni;
	}

	public Date getPeriodoFechaFin() {
		return periodoFechaFin;
	}

	public void setPeriodoFechaFin(Date periodoFechaFin) {
		this.periodoFechaFin = periodoFechaFin;
	}

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}

	public Integer getIdCatTipoRebate() {
		return idCatTipoRebate;
	}

	public void setIdCatTipoRebate(Integer idCatTipoRebate) {
		this.idCatTipoRebate = idCatTipoRebate;
	}

	public String getDescripcionRebate() {
		return descripcionRebate;
	}

	public void setDescripcionRebate(String descripcionRebate) {
		this.descripcionRebate = descripcionRebate;
	}

	public Integer getIdCatTipoExclusion() {
		return idCatTipoExclusion;
	}

	public void setIdCatTipoExclusion(Integer idCatTipoExclusion) {
		this.idCatTipoExclusion = idCatTipoExclusion;
	}

	public String getDescripcionExclusion() {
		return descripcionExclusion;
	}

	public void setDescripcionExclusion(String descripcionExclusion) {
		this.descripcionExclusion = descripcionExclusion;
	}

	public Integer getIdCatEstatusExclusion() {
		return idCatEstatusExclusion;
	}

	public void setIdCatEstatusExclusion(Integer idCatEstatusExclusion) {
		this.idCatEstatusExclusion = idCatEstatusExclusion;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public Integer getContabilizado() {
		return contabilizado;
	}

	public void setContabilizado(Integer contabilizado) {
		this.contabilizado = contabilizado;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Integer getIdExclusionCarga() {
		return IdExclusionCarga;
	}

	public void setIdExclusionCarga(Integer idExclusionCarga) {
		IdExclusionCarga = idExclusionCarga;
	}

	public Integer getIdExclusionCargaDet() {
		return idExclusionCargaDet;
	}

	public void setIdExclusionCargaDet(Integer idExclusionCargaDet) {
		this.idExclusionCargaDet = idExclusionCargaDet;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
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
