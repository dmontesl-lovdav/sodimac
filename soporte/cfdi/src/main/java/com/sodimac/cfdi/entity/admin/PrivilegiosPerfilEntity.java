package com.sodimac.cfdi.entity.admin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "privsperfil")
public class PrivilegiosPerfilEntity {

	@Id
	private int idPerfil;
	private int c;
	private int r;
	private int u;
	private int d;
	private int admin;

	public int getIdPerfil() {
		return idPerfil;
	}

	public void setIdPerfil(int idPerfil) {
		this.idPerfil = idPerfil;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getU() {
		return u;
	}

	public void setU(int u) {
		this.u = u;
	}

	public int getD() {
		return d;
	}

	public void setD(int d) {
		this.d = d;
	}

	public int getAdmin() {
		return admin;
	}

	public void setAdmin(int admin) {
		this.admin = admin;
	}

}
