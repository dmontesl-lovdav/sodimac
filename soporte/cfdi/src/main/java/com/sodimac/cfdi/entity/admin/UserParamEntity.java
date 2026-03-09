package com.sodimac.cfdi.entity.admin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "userparams")
public class UserParamEntity {

	@Id
	String parametro;
	int idUsuario;

	public String getParametro() {
		return parametro;
	}

	public void setParametro(String parametro) {
		this.parametro = parametro;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

}
