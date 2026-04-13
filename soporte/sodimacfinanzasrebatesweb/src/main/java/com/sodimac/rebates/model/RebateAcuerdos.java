package com.sodimac.rebates.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vw_acuerdos_comerciales")
public class RebateAcuerdos {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdRebateAcuerdos")
	private Integer id;
	
	@Column(name = "NumeroProveedor")
	private String numeroProveedor;
	
	@Column(name = "RFC")
	private String rfc;
	
	@Column(name = "Correo")
	private String correo;
	
	@Column(name = "RazonSocial")
	private String razonSocial;
	
	@Column(name = "Estado")
	private String estado;
	
	@Column(name = "Familia")
	private String familia;
	
	@Column(name = "ClasificacionComercial")
	private String clasificacionComercial;
	
	@Column(name = "NumeroAcuerdo")
	private Integer numeroAcuerdo;
	
	@Column(name = "TipoAcuerdo")
	private String tipoAcuerdo;
	
	@Column(name = "Moneda")
	private String moneda;
	
	@Column(name = "Valor")
	private Double valor;
	
	@Column(name = "TipoValor")
	private String tipoValor;
	
	@Column(name = "FillRate")
	private String fillRate;
	
	@Column(name = "ProgramaPago")
	private String programaPago;
	
	@Column(name = "Marca")
	private String marca;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumeroProveedor() {
		return numeroProveedor;
	}

	public void setNumeroProveedor(String numeroProveedor) {
		this.numeroProveedor = numeroProveedor;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getFamilia() {
		return familia;
	}

	public void setFamilia(String familia) {
		this.familia = familia;
	}

	public String getClasificacionComercial() {
		return clasificacionComercial;
	}

	public void setClasificacionComercial(String clasificacionComercial) {
		this.clasificacionComercial = clasificacionComercial;
	}

	public Integer getNumeroAcuerdo() {
		return numeroAcuerdo;
	}

	public void setNumeroAcuerdo(Integer numeroAcuerdo) {
		this.numeroAcuerdo = numeroAcuerdo;
	}

	public String getTipoAcuerdo() {
		return tipoAcuerdo;
	}

	public void setTipoAcuerdo(String tipoAcuerdo) {
		this.tipoAcuerdo = tipoAcuerdo;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public String getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(String tipoValor) {
		this.tipoValor = tipoValor;
	}

	public String getFillRate() {
		return fillRate;
	}

	public void setFillRate(String fillRate) {
		this.fillRate = fillRate;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

}
