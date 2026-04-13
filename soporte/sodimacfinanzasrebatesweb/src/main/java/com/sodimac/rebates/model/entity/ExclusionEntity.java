package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.TipoRebate;
import com.sodimac.rebates.model.Usuario;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "Exclusion")
public class ExclusionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdExclusion")
	private Integer idExclusion;

	@Column(name = "Folio")
	private String folio;

	@Column(name = "Comentario")
	private String comentario;

	@Column(name = "FechaHoraSolicitud")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaHoraSolicitud;

	@Column(name = "FechaHoraAutorizacion")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaHoraAutorizacion;

	@Column(name = "Activo")
	private boolean activo;
		
	@Column(name = "Contabilizado")
	private Integer contabilizado;
	
	@Lob
	@Column(name = "Imagen")
	@Basic(fetch = FetchType.LAZY)
	private byte[] imagen;
	
	@Column(name = "Evidencia")
	private String evidencia;

	@JoinColumn(name = "IdCatPeriodo", referencedColumnName = "IdCatPeriodo")
	@ManyToOne(optional = false)
	private Periodo periodo;

	@JoinColumn(name = "IdCatTipoRebate", referencedColumnName = "idCatTipoRebate")
	@ManyToOne(optional = false)
	private TipoRebate catTipoRebate;
	
	@JoinColumn(name = "IdCatEstatusExclusion", referencedColumnName = "IdCatEstatusExclusion")
	@ManyToOne(optional = false)
	private CatEstatusExclusionEntity catEstatusExclusion;

	@JoinColumn(name = "IdCatTipoExclusion", referencedColumnName = "IdCatTipoExclusion")
	@ManyToOne(optional = false)
	private CatTipoExclusionEntity catTipoExclusion;

	@JoinColumn(name = "IdUsuarioSolicitud", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Usuario usuarioSolicitud;

	@JoinColumn(name = "IdUsuarioAutorizacion", referencedColumnName = "id", nullable = true)
	@ManyToOne(optional = true)
	private Usuario usuarioAutorizacion;

	@JsonBackReference(value = "exclusion")
	@OneToMany(mappedBy = "exclusion", fetch = FetchType.LAZY)
	private Set<ExclusionCargaEntity> cargas;
	
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

	public byte[] getImagen() {
		return imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}

	public String getEvidencia() {
		return evidencia;
	}

	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}
	
	public TipoRebate getCatTipoRebate() {
		return catTipoRebate;
	}

	public void setCatTipoRebate(TipoRebate catTipoRebate) {
		this.catTipoRebate = catTipoRebate;
	}

	public CatEstatusExclusionEntity getCatEstatusExclusion() {
		return catEstatusExclusion;
	}

	public void setCatEstatusExclusion(CatEstatusExclusionEntity catEstatusExclusion) {
		this.catEstatusExclusion = catEstatusExclusion;
	}

	public CatTipoExclusionEntity getCatTipoExclusion() {
		return catTipoExclusion;
	}

	public void setCatTipoExclusion(CatTipoExclusionEntity catTipoExclusion) {
		this.catTipoExclusion = catTipoExclusion;
	}

	public Usuario getUsuarioSolicitud() {
		return usuarioSolicitud;
	}

	public void setUsuarioSolicitud(Usuario usuarioSolicitud) {
		this.usuarioSolicitud = usuarioSolicitud;
	}

	public Usuario getUsuarioAutorizacion() {
		return usuarioAutorizacion;
	}

	public void setUsuarioAutorizacion(Usuario usuarioAutorizacion) {
		this.usuarioAutorizacion = usuarioAutorizacion;
	}

	public Set<ExclusionCargaEntity> getCargas() {
		return cargas;
	}

	public void setCargas(Set<ExclusionCargaEntity> cargas) {
		this.cargas = cargas;
	}

}