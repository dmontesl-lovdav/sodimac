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

@Entity
@Table(name = "cattipocomprobantesodimac")
public class CatTipoComprobanteSodimacEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "tipocomprobante")
	private String tipocomprobante;
	
	@Column(name = "descripcion")
	private String descripcion;
	
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="idcattipocomprobantesat")
    private CatTipoComprobanteSatEntity catTipoComprobanteSatEntity;
    
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "formaMetodoPago")
	private ConfFormaMetodoPagoEntity confFormaMetodoPagoEntity;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTipocomprobante() {
		return tipocomprobante;
	}

	public void setTipocomprobante(String tipocomprobante) {
		this.tipocomprobante = tipocomprobante;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public CatTipoComprobanteSatEntity getCatTipoComprobanteSatEntity() {
		return catTipoComprobanteSatEntity;
	}

	public void setCatTipoComprobanteSatEntity(CatTipoComprobanteSatEntity catTipoComprobanteSatEntity) {
		this.catTipoComprobanteSatEntity = catTipoComprobanteSatEntity;
	}

	public ConfFormaMetodoPagoEntity getConfFormaMetodoPagoEntity() {
		return confFormaMetodoPagoEntity;
	}

	public void setConfFormaMetodoPagoEntity(ConfFormaMetodoPagoEntity confFormaMetodoPagoEntity) {
		this.confFormaMetodoPagoEntity = confFormaMetodoPagoEntity;
	}

	@Override
	public String toString() {
		return "CatTipoComprobanteSodimacEntity [id=" + id + ", tipocomprobante=" + tipocomprobante + ", descripcion="
				+ descripcion + ", catTipoComprobanteSatEntity=" + catTipoComprobanteSatEntity
				+ ", confFormaMetodoPagoEntity=" + confFormaMetodoPagoEntity + "]";
	}

    
}
