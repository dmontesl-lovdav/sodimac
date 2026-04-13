package com.sodimac.rebates.model.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author david.montes
 */
@Entity
@Table(name = "CatCompradorProveedor")
public class CatCompradorProveedorEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private Integer idcomprador;

	private String numeroProveedor;

	public Integer getIdcomprador() {
		return idcomprador;
	}

	public void setIdcomprador(Integer idcomprador) {
		this.idcomprador = idcomprador;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}


}
