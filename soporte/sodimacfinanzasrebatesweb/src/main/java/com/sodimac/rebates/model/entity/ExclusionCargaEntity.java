package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "ExclusionCarga")
public class ExclusionCargaEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdExclusionCarga")
	private Long idExclusionCarga;

	@Column(name = "IdExclusion")
	private Integer idExclusion;
	
	@JoinColumn(name = "IdExclusion", referencedColumnName = "idExclusion", insertable = false, updatable = false)
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private ExclusionEntity exclusion;
	
	@Column(name = "Carga")
	private String carga;

	@Column(name = "Motivo")
	private String motivo;

	@Column(name = "FechaRegistro")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaRegistro;

	@Column(name = "Activo")
	private boolean activo;
	
	@JsonBackReference(value = "exclusionCarga")
	@OneToMany(mappedBy = "exclusionCarga", fetch = FetchType.LAZY)
	Set<ExclusionCargaDetEntity> detalles;

	public Long getIdExclusionCarga() {
		return idExclusionCarga;
	}

	public void setIdExclusionCarga(Long idExclusionCarga) {
		this.idExclusionCarga = idExclusionCarga;
	}

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}

	public ExclusionEntity getExclusion() {
		return exclusion;
	}

	public void setExclusion(ExclusionEntity exclusion) {
		this.exclusion = exclusion;
	}

	public String getCarga() {
		return carga;
	}

	public void setCarga(String carga) {
		this.carga = carga;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Set<ExclusionCargaDetEntity> getDetalles() {
		return detalles;
	}

	public void setDetalles(Set<ExclusionCargaDetEntity> detalles) {
		this.detalles = detalles;
	}

}