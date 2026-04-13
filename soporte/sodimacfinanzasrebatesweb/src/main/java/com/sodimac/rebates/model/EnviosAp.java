package com.sodimac.rebates.model;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Envios_Ap_Temp")
public class EnviosAp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GenericGenerator(name = "generator", strategy = "guid", parameters = {})
	//@GeneratedValue(generator = "generator")
	//@Column(columnDefinition="uniqueidentifier")
	private Integer ID;
	private String EMPRESA;
	private Date FECHA_DOCUMENTO;
	private String REFERENCIA_DOCUMENTO;
	private String NUMERO_DOCUMENTO;
	private String MONEDA;
	private BigDecimal TIPO_CAMBIO;
	private String DEBITO_CREDITO;
	private String CUENTA_CONTABLE;
	private String CODIGO_PROVEEDOR;
	private BigDecimal IMPORTE;
	private String SUCURSAL;
	private String CONDICION_PAGO;
	private Date FECHA_VENCIMIENTO;
	private String BLOQUEO_PAGO;
	private String SISTEMA_ORIGEN;
	private Date FECHA_ENVIO;
	private Date FECHA_CONTABLE;
	private String CLASE_DOCUMENTO;
	private String NUMERO_REFERENCIA;
	private String CENTRO_COSTO;
	private String CENTRO_BENEFICIO;
	private String NUMERO_UUID;
	private String FLAG_ENVIADO;
	private Date FECHA_RECEPCION;
	private String TIPO_DOCUMENTO;
	private String ORIGEN_ETL;
	private Integer IdPeriodo;
	private Integer Timbrado;
	private Integer TipoRebate;
	
	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
	}

	public String getEMPRESA() {
		return EMPRESA;
	}

	public void setEMPRESA(String eMPRESA) {
		EMPRESA = eMPRESA;
	}

	public Date getFECHA_DOCUMENTO() {
		return FECHA_DOCUMENTO;
	}

	public void setFECHA_DOCUMENTO(Date fECHA_DOCUMENTO) {
		FECHA_DOCUMENTO = fECHA_DOCUMENTO;
	}

	public String getREFERENCIA_DOCUMENTO() {
		return REFERENCIA_DOCUMENTO;
	}

	public void setREFERENCIA_DOCUMENTO(String rEFERENCIA_DOCUMENTO) {
		REFERENCIA_DOCUMENTO = rEFERENCIA_DOCUMENTO;
	}

	public String getNUMERO_DOCUMENTO() {
		return NUMERO_DOCUMENTO;
	}

	public void setNUMERO_DOCUMENTO(String nUMERO_DOCUMENTO) {
		NUMERO_DOCUMENTO = nUMERO_DOCUMENTO;
	}

	public String getMONEDA() {
		return MONEDA;
	}

	public void setMONEDA(String mONEDA) {
		MONEDA = mONEDA;
	}

	public BigDecimal getTIPO_CAMBIO() {
		return TIPO_CAMBIO;
	}

	public void setTIPO_CAMBIO(BigDecimal tIPO_CAMBIO) {
		TIPO_CAMBIO = tIPO_CAMBIO;
	}

	public String getDEBITO_CREDITO() {
		return DEBITO_CREDITO;
	}

	public void setDEBITO_CREDITO(String dEBITO_CREDITO) {
		DEBITO_CREDITO = dEBITO_CREDITO;
	}

	public String getCUENTA_CONTABLE() {
		return CUENTA_CONTABLE;
	}

	public void setCUENTA_CONTABLE(String cUENTA_CONTABLE) {
		CUENTA_CONTABLE = cUENTA_CONTABLE;
	}

	public String getCODIGO_PROVEEDOR() {
		return CODIGO_PROVEEDOR;
	}

	public void setCODIGO_PROVEEDOR(String cODIGO_PROVEEDOR) {
		CODIGO_PROVEEDOR = cODIGO_PROVEEDOR;
	}

	public BigDecimal getIMPORTE() {
		return IMPORTE;
	}

	public void setIMPORTE(BigDecimal iMPORTE) {
		IMPORTE = iMPORTE;
	}

	public String getSUCURSAL() {
		return SUCURSAL;
	}

	public void setSUCURSAL(String sUCURSAL) {
		SUCURSAL = sUCURSAL;
	}

	public String getCONDICION_PAGO() {
		return CONDICION_PAGO;
	}

	public void setCONDICION_PAGO(String cONDICION_PAGO) {
		CONDICION_PAGO = cONDICION_PAGO;
	}

	public Date getFECHA_VENCIMIENTO() {
		return FECHA_VENCIMIENTO;
	}

	public void setFECHA_VENCIMIENTO(Date fECHA_VENCIMIENTO) {
		FECHA_VENCIMIENTO = fECHA_VENCIMIENTO;
	}

	public String getBLOQUEO_PAGO() {
		return BLOQUEO_PAGO;
	}

	public void setBLOQUEO_PAGO(String bLOQUEO_PAGO) {
		BLOQUEO_PAGO = bLOQUEO_PAGO;
	}

	public String getSISTEMA_ORIGEN() {
		return SISTEMA_ORIGEN;
	}

	public void setSISTEMA_ORIGEN(String sISTEMA_ORIGEN) {
		SISTEMA_ORIGEN = sISTEMA_ORIGEN;
	}

	public Date getFECHA_ENVIO() {
		return FECHA_ENVIO;
	}

	public void setFECHA_ENVIO(Date fECHA_ENVIO) {
		FECHA_ENVIO = fECHA_ENVIO;
	}

	public Date getFECHA_CONTABLE() {
		return FECHA_CONTABLE;
	}

	public void setFECHA_CONTABLE(Date fECHA_CONTABLE) {
		FECHA_CONTABLE = fECHA_CONTABLE;
	}

	public String getCLASE_DOCUMENTO() {
		return CLASE_DOCUMENTO;
	}

	public void setCLASE_DOCUMENTO(String cLASE_DOCUMENTO) {
		CLASE_DOCUMENTO = cLASE_DOCUMENTO;
	}

	public String getNUMERO_REFERENCIA() {
		return NUMERO_REFERENCIA;
	}

	public void setNUMERO_REFERENCIA(String nUMERO_REFERENCIA) {
		NUMERO_REFERENCIA = nUMERO_REFERENCIA;
	}

	public String getCENTRO_COSTO() {
		return CENTRO_COSTO;
	}

	public void setCENTRO_COSTO(String cENTRO_COSTO) {
		CENTRO_COSTO = cENTRO_COSTO;
	}

	public String getCENTRO_BENEFICIO() {
		return CENTRO_BENEFICIO;
	}

	public void setCENTRO_BENEFICIO(String cENTRO_BENEFICIO) {
		CENTRO_BENEFICIO = cENTRO_BENEFICIO;
	}

	public String getNUMERO_UUID() {
		return NUMERO_UUID;
	}

	public void setNUMERO_UUID(String nUMERO_UUID) {
		NUMERO_UUID = nUMERO_UUID;
	}

	public String getFLAG_ENVIADO() {
		return FLAG_ENVIADO;
	}

	public void setFLAG_ENVIADO(String fLAG_ENVIADO) {
		FLAG_ENVIADO = fLAG_ENVIADO;
	}

	public Date getFECHA_RECEPCION() {
		return FECHA_RECEPCION;
	}

	public void setFECHA_RECEPCION(Date fECHA_RECEPCION) {
		FECHA_RECEPCION = fECHA_RECEPCION;
	}

	public String getTIPO_DOCUMENTO() {
		return TIPO_DOCUMENTO;
	}

	public void setTIPO_DOCUMENTO(String tIPO_DOCUMENTO) {
		TIPO_DOCUMENTO = tIPO_DOCUMENTO;
	}

	public String getORIGEN_ETL() {
		return ORIGEN_ETL;
	}

	public void setORIGEN_ETL(String oRIGEN_ETL) {
		ORIGEN_ETL = oRIGEN_ETL;
	}

	public Integer getIdPeriodo() {
		return IdPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		IdPeriodo = idPeriodo;
	}

	public Integer getTimbrado() {
		return Timbrado;
	}

	public void setTimbrado(Integer timbrado) {
		Timbrado = timbrado;
	}

	public Integer getTipoRebate() {
		return TipoRebate;
	}

	public void setTipoRebate(Integer tipoRebate) {
		TipoRebate = tipoRebate;
	}

	@Override
	public String toString() {
		return "EnviosAp [ID=" + ID + ", EMPRESA=" + EMPRESA + ", FECHA_DOCUMENTO=" + FECHA_DOCUMENTO
				+ ", REFERENCIA_DOCUMENTO=" + REFERENCIA_DOCUMENTO + ", NUMERO_DOCUMENTO=" + NUMERO_DOCUMENTO
				+ ", MONEDA=" + MONEDA + ", TIPO_CAMBIO=" + TIPO_CAMBIO + ", DEBITO_CREDITO=" + DEBITO_CREDITO
				+ ", CUENTA_CONTABLE=" + CUENTA_CONTABLE + ", CODIGO_PROVEEDOR=" + CODIGO_PROVEEDOR + ", IMPORTE="
				+ IMPORTE + ", SUCURSAL=" + SUCURSAL + ", CONDICION_PAGO=" + CONDICION_PAGO + ", FECHA_VENCIMIENTO="
				+ FECHA_VENCIMIENTO + ", BLOQUEO_PAGO=" + BLOQUEO_PAGO + ", SISTEMA_ORIGEN=" + SISTEMA_ORIGEN
				+ ", FECHA_ENVIO=" + FECHA_ENVIO + ", FECHA_CONTABLE=" + FECHA_CONTABLE + ", CLASE_DOCUMENTO="
				+ CLASE_DOCUMENTO + ", NUMERO_REFERENCIA=" + NUMERO_REFERENCIA + ", CENTRO_COSTO=" + CENTRO_COSTO
				+ ", CENTRO_BENEFICIO=" + CENTRO_BENEFICIO + ", NUMERO_UUID=" + NUMERO_UUID + ", FLAG_ENVIADO="
				+ FLAG_ENVIADO + ", FECHA_RECEPCION=" + FECHA_RECEPCION + ", TIPO_DOCUMENTO=" + TIPO_DOCUMENTO
				+ ", ORIGEN_ETL=" + ORIGEN_ETL + ", IdPeriodo=" + IdPeriodo + ", Timbrado=" + Timbrado + ", TipoRebate="
				+ TipoRebate + "]";
	}
}
