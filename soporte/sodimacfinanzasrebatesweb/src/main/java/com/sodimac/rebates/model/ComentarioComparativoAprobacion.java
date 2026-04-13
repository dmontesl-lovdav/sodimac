package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ComentarioComparativoAprobacion")
public class ComentarioComparativoAprobacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer idPeriodo;
	private Integer idProveedor;
	private Integer tipoRebate;
	private String comentario;
	private Date fechaCreacion;
	private Integer idUsuario;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public Integer getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(Integer idProveedor) {
		this.idProveedor = idProveedor;
	}

	public Integer getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(Integer tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	@Override
	public String toString() {
		return "ComentarioComparativoAprobacion [id=" + id + ", idPeriodo=" + idPeriodo + ", idProveedor=" + idProveedor
				+ ", tipoRebate=" + tipoRebate + ", comentario=" + comentario + ", fechaCreacion=" + fechaCreacion
				+ ", idUsuario=" + idUsuario + "]";
	}

}
