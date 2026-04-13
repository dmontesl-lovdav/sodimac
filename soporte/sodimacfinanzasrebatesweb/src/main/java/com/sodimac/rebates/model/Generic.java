package com.sodimac.rebates.model;

import java.util.Arrays;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

public class Generic {

	private String message;
	private Integer typeMessage;
	private String title;
	private boolean code;
	private MultipartFile[] archivo;
	private Integer idResponse;
	private Date fechaCargaIni;
	private Date fechaCargaFin;
	private String newPassword;
	private boolean cambiarPassword;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getTypeMessage() {
		return typeMessage;
	}

	public void setTypeMessage(Integer typeMessage) {
		this.typeMessage = typeMessage;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCode() {
		return code;
	}

	public void setCode(boolean code) {
		this.code = code;
	}

	public MultipartFile[] getArchivo() {
		return archivo;
	}

	public void setArchivo(MultipartFile[] archivo) {
		this.archivo = archivo;
	}

	public Integer getIdResponse() {
		return idResponse;
	}

	public void setIdResponse(Integer idResponse) {
		this.idResponse = idResponse;
	}

	public Date getFechaCargaIni() {
		return fechaCargaIni;
	}

	public void setFechaCargaIni(Date fechaCargaIni) {
		this.fechaCargaIni = fechaCargaIni;
	}

	public Date getFechaCargaFin() {
		return fechaCargaFin;
	}

	public void setFechaCargaFin(Date fechaCargaFin) {
		this.fechaCargaFin = fechaCargaFin;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public boolean isCambiarPassword() {
		return cambiarPassword;
	}

	public void setCambiarPassword(boolean cambiarPassword) {
		this.cambiarPassword = cambiarPassword;
	}

	@Override
	public String toString() {
		return "Generic [message=" + message + ", typeMessage=" + typeMessage + ", title=" + title + ", code=" + code
				+ ", archivo=" + Arrays.toString(archivo) + ", idResponse=" + idResponse + ", fechaCargaIni="
				+ fechaCargaIni + ", fechaCargaFin=" + fechaCargaFin + ", newPassword=" + newPassword
				+ ", cambiarPassword=" + cambiarPassword + "]";
	}

}
