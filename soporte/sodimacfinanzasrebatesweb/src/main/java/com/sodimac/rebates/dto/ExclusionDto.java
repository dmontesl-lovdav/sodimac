package com.sodimac.rebates.dto;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ExclusionDto {

	private Integer idExclusion;
	private String folio;
	private String comentario;
	private String idCarga;
	private String numeroProveedor;
	private String exclusion;
	private UsuarioDto usuarioSolicitud;
	private UsuarioDto usuarioAutorizacion;
	private Date fechaHoraSolicitud;
	private Date fechaHoraAutorizacion;
	private boolean activo;
	private Integer contabilizado;
	private String strContabilizado;
	private String evidencia;
	private byte[] imagen;
	private boolean imagenCargada;
	private PeriodoDto periodo;
	private CatTipoRebateDto catTipoRebate;
	private CatEstatusExclusionDto catEstatusExclusion;
	private CatTipoExclusionDto catTipoExclusion;
	private List<ExclusionCargaDto> listExclusiones;
	private int soyHijo;

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public String getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(String idCarga) {
		this.idCarga = idCarga;
	}
	
	public String getExclusion() {
		return exclusion;
	}

	public void setExclusion(String exclusion) {
		this.exclusion = exclusion;
	}

	public UsuarioDto getUsuarioSolicitud() {
		return usuarioSolicitud;
	}

	public void setUsuarioSolicitud(UsuarioDto usuarioSolicitud) {
		this.usuarioSolicitud = usuarioSolicitud;
	}

	public UsuarioDto getUsuarioAutorizacion() {
		return usuarioAutorizacion;
	}

	public void setUsuarioAutorizacion(UsuarioDto usuarioAutorizacion) {
		this.usuarioAutorizacion = usuarioAutorizacion;
	}

	public Date getFechaHoraSolicitud() {
		return fechaHoraSolicitud;
	}

	public void setFechaHoraSolicitud(Date fechaHoraSolicitud) {
		this.fechaHoraSolicitud = fechaHoraSolicitud;
	}

	public Date getFechaHoraAutorizacion() {
		return fechaHoraAutorizacion;
	}

	public void setFechaHoraAutorizacion(Date fechaHoraAutorizacion) {
		this.fechaHoraAutorizacion = fechaHoraAutorizacion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	public Integer getContabilizado() {
		return contabilizado;
	}

	public void setContabilizado(Integer contabilizado) {
		this.contabilizado = contabilizado;
	}

	public String getStrContabilizado() {
		return strContabilizado;
	}

	public void setStrContabilizado(String strContabilizado) {
		this.strContabilizado = strContabilizado;
	}

	public String getEvidencia() {
		return evidencia;
	}

	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}

	public byte[] getImagen() {
		return imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}

	public boolean getImagenCargada() {
		return imagenCargada;
	}

	public void setImagenCargada(boolean imagenCargada) {
		this.imagenCargada = imagenCargada;
	}

	public PeriodoDto getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoDto periodo) {
		this.periodo = periodo;
	}

	public CatTipoRebateDto getCatTipoRebate() {
		return catTipoRebate;
	}

	public void setCatTipoRebate(CatTipoRebateDto catTipoRebate) {
		this.catTipoRebate = catTipoRebate;
	}

	public CatEstatusExclusionDto getCatEstatusExclusion() {
		return catEstatusExclusion;
	}

	public void setCatEstatusExclusion(CatEstatusExclusionDto catEstatusExclusion) {
		this.catEstatusExclusion = catEstatusExclusion;
	}

	public CatTipoExclusionDto getCatTipoExclusion() {
		return catTipoExclusion;
	}

	public void setCatTipoExclusion(CatTipoExclusionDto catTipoExclusion) {
		this.catTipoExclusion = catTipoExclusion;
	}

	public List<ExclusionCargaDto> getListExclusiones() {
		return listExclusiones;
	}

	public void setListExclusiones(List<ExclusionCargaDto> listExclusiones) {
		this.listExclusiones = listExclusiones;
	}

	public int getSoyHijo() {
		return soyHijo;
	}

	public void setSoyHijo(int soyHijo) {
		this.soyHijo = soyHijo;
	}

	@Override
	public String toString() {
		return "ExclusionDto [idExclusion=" + idExclusion + ", folio=" + folio + ", comentario=" + comentario
				+ ", idCarga=" + idCarga + ", numeroProveedor=" + numeroProveedor + ", exclusion=" + exclusion
				+ ", usuarioSolicitud=" + usuarioSolicitud + ", usuarioAutorizacion=" + usuarioAutorizacion
				+ ", fechaHoraSolicitud=" + fechaHoraSolicitud + ", fechaHoraAutorizacion=" + fechaHoraAutorizacion
				+ ", activo=" + activo + ", contabilizado=" + contabilizado + ", strContabilizado=" + strContabilizado
				+ ", evidencia=" + evidencia + ", imagen=" + Arrays.toString(imagen) + ", imagenCargada="
				+ imagenCargada + ", periodo=" + periodo + ", catTipoRebate=" + catTipoRebate + ", catEstatusExclusion="
				+ catEstatusExclusion + ", catTipoExclusion=" + catTipoExclusion + ", listExclusiones="
				+ listExclusiones + ", soyHijo=" + soyHijo + "]";
	}

}
