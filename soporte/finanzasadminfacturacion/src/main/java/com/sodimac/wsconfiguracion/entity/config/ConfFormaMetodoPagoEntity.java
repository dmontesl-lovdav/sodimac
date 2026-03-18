package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "confformametodopago")
public class ConfFormaMetodoPagoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="tipoComprobante")
    private CatTipoComprobanteSodimacEntity catTipoComprobanteSodimacEntity;
	
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="formaPago")
    private CatFormaPagoEntity catFormaPagoEntity;
    
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="metodoPago")
    private CatMetodoPagoEntity catMetodoPagoEntity;
    
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="medioPago")
    private CatMedioPagoEntity catMedioPagoEntity;
    
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="version")
    private VersionEntity versionEntity;
    
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;
	
	@Column(name = "fechaModificacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	@Column(name = "idUsuario")
	private Integer idUsuario;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CatTipoComprobanteSodimacEntity getCatTipoComprobanteSodimacEntity() {
		return catTipoComprobanteSodimacEntity;
	}

	public void setCatTipoComprobanteSodimacEntity(CatTipoComprobanteSodimacEntity catTipoComprobanteSodimacEntity) {
		this.catTipoComprobanteSodimacEntity = catTipoComprobanteSodimacEntity;
	}

	public CatFormaPagoEntity getCatFormaPagoEntity() {
		return catFormaPagoEntity;
	}

	public void setCatFormaPagoEntity(CatFormaPagoEntity catFormaPagoEntity) {
		this.catFormaPagoEntity = catFormaPagoEntity;
	}

	public CatMetodoPagoEntity getCatMetodoPagoEntity() {
		return catMetodoPagoEntity;
	}

	public void setCatMetodoPagoEntity(CatMetodoPagoEntity catMetodoPagoEntity) {
		this.catMetodoPagoEntity = catMetodoPagoEntity;
	}
	

	public CatMedioPagoEntity getCatMedioPagoEntity() {
		return catMedioPagoEntity;
	}

	public void setCatMedioPagoEntity(CatMedioPagoEntity catMedioPagoEntity) {
		this.catMedioPagoEntity = catMedioPagoEntity;
	}

	public VersionEntity getVersionEntity() {
		return versionEntity;
	}

	public void setVersionEntity(VersionEntity versionEntity) {
		this.versionEntity = versionEntity;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	@Override
	public String toString() {
		return "ConfFormaMetodoPagoEntity [id=" + id + ", catTipoComprobanteSodimacEntity="
				+ catTipoComprobanteSodimacEntity + ", catFormaPagoEntity=" + catFormaPagoEntity
				+ ", catMetodoPagoEntity=" + catMetodoPagoEntity + ", catMedioPagoEntity=" + catMedioPagoEntity
				+ ", versionEntity=" + versionEntity + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion="
				+ fechaModificacion + ", idUsuario=" + idUsuario + "]";
	}
	
}
