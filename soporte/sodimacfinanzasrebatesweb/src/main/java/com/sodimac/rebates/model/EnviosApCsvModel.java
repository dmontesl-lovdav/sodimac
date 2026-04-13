package com.sodimac.rebates.model;

import com.opencsv.bean.CsvBindByPosition;

public class EnviosApCsvModel {

    @CsvBindByPosition(position = 0)
    private String empresa;

    @CsvBindByPosition(position = 1)
    private String fechaDocumento;

    @CsvBindByPosition(position = 2)
    private String referenciaDocumento;

    @CsvBindByPosition(position = 3)
    private String numeroDocumento;

    @CsvBindByPosition(position = 4)
    private String moneda;

    @CsvBindByPosition(position = 5)
    private String tipoCambio;

    @CsvBindByPosition(position = 6)
    private String debitoCredito;

    @CsvBindByPosition(position = 7)
    private String cuentaContable;

    @CsvBindByPosition(position = 8)
    private String codigoProveedor;

    @CsvBindByPosition(position = 9)
    private String importe;

    @CsvBindByPosition(position = 10)
    private String sucursal;

    @CsvBindByPosition(position = 11)
    private String condicionPago;

    @CsvBindByPosition(position = 12)
    private String fechaVencimiento;

    @CsvBindByPosition(position = 13)
    private String bloqueoPago;

    @CsvBindByPosition(position = 14)
    private String sistemaOrigen;

    @CsvBindByPosition(position = 15)
    private String fechaEnvio;

    @CsvBindByPosition(position = 16)
    private String fechaContable;

    @CsvBindByPosition(position = 17)
    private String claseDocumento;

    @CsvBindByPosition(position = 18)
    private String numeroReferencia;

    @CsvBindByPosition(position = 19)
    private String centroCosto;
    
    @CsvBindByPosition(position = 20)
    private String centroBeneficio;

    @CsvBindByPosition(position = 21)
    private String numeroUuid;

    @CsvBindByPosition(position = 22)
    private String flagEnviado;

    @CsvBindByPosition(position = 23)
    private String fechaRecepcion;

    @CsvBindByPosition(position = 24)
    private String tipoDocumento;

    @CsvBindByPosition(position = 25)
    private String origenEtl;

    @CsvBindByPosition(position = 26)
    private String idPeriodo;

    @CsvBindByPosition(position = 27)
    private String timbrado;

    @CsvBindByPosition(position = 28)
    private String tipoRebate;

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFechaDocumento() {
        return fechaDocumento;
    }

    public void setFechaDocumento(String fechaDocumento) {
        this.fechaDocumento = fechaDocumento;
    }

    public String getReferenciaDocumento() {
        return referenciaDocumento;
    }

    public void setReferenciaDocumento(String referenciaDocumento) {
        this.referenciaDocumento = referenciaDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getDebitoCredito() {
        return debitoCredito;
    }

    public void setDebitoCredito(String debitoCredito) {
        this.debitoCredito = debitoCredito;
    }

    public String getCuentaContable() {
        return cuentaContable;
    }

    public void setCuentaContable(String cuentaContable) {
        this.cuentaContable = cuentaContable;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }

    public void setCodigoProveedor(String codigoProveedor) {
        this.codigoProveedor = codigoProveedor;
    }

    public String getImporte() {
        return importe;
    }

    public void setImporte(String importe) {
        this.importe = importe;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getCondicionPago() {
        return condicionPago;
    }

    public void setCondicionPago(String condicionPago) {
        this.condicionPago = condicionPago;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getBloqueoPago() {
        return bloqueoPago;
    }

    public void setBloqueoPago(String bloqueoPago) {
        this.bloqueoPago = bloqueoPago;
    }

    public String getSistemaOrigen() {
        return sistemaOrigen;
    }

    public void setSistemaOrigen(String sistemaOrigen) {
        this.sistemaOrigen = sistemaOrigen;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getFechaContable() {
        return fechaContable;
    }

    public void setFechaContable(String fechaContable) {
        this.fechaContable = fechaContable;
    }

    public String getClaseDocumento() {
        return claseDocumento;
    }

    public void setClaseDocumento(String claseDocumento) {
        this.claseDocumento = claseDocumento;
    }

    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }

    public String getCentroCosto() {
        return centroCosto;
    }

    public void setCentroCosto(String centroCosto) {
        this.centroCosto = centroCosto;
    }
    

    public String getCentroBeneficio() {
		return centroBeneficio;
	}

	public void setCentroBeneficio(String centroBeneficio) {
		this.centroBeneficio = centroBeneficio;
	}

	public String getNumeroUuid() {
        return numeroUuid;
    }

    public void setNumeroUuid(String numeroUuid) {
        this.numeroUuid = numeroUuid;
    }

    public String getFlagEnviado() {
        return flagEnviado;
    }

    public void setFlagEnviado(String flagEnviado) {
        this.flagEnviado = flagEnviado;
    }

    public String getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(String fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getOrigenEtl() {
        return origenEtl;
    }

    public void setOrigenEtl(String origenEtl) {
        this.origenEtl = origenEtl;
    }

    public String getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(String idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(String timbrado) {
        this.timbrado = timbrado;
    }

    public String getTipoRebate() {
        return tipoRebate;
    }

    public void setTipoRebate(String tipoRebate) {
        this.tipoRebate = tipoRebate;
    }

    @Override
	public String toString() {
		return "EnviosApCsvModel [empresa=" + empresa + ", fechaDocumento=" + fechaDocumento + ", referenciaDocumento="
				+ referenciaDocumento + ", numeroDocumento=" + numeroDocumento + ", moneda=" + moneda + ", tipoCambio="
				+ tipoCambio + ", debitoCredito=" + debitoCredito + ", cuentaContable=" + cuentaContable
				+ ", codigoProveedor=" + codigoProveedor + ", importe=" + importe + ", sucursal=" + sucursal
				+ ", condicionPago=" + condicionPago + ", fechaVencimiento=" + fechaVencimiento + ", bloqueoPago="
				+ bloqueoPago + ", sistemaOrigen=" + sistemaOrigen + ", fechaEnvio=" + fechaEnvio + ", fechaContable="
				+ fechaContable + ", claseDocumento=" + claseDocumento + ", numeroReferencia=" + numeroReferencia
				+ ", centroCosto=" + centroCosto + ", centroBeneficio=" + centroBeneficio + ", numeroUuid=" + numeroUuid
				+ ", flagEnviado=" + flagEnviado + ", fechaRecepcion=" + fechaRecepcion + ", tipoDocumento="
				+ tipoDocumento + ", origenEtl=" + origenEtl + ", idPeriodo=" + idPeriodo + ", timbrado=" + timbrado
				+ ", tipoRebate=" + tipoRebate + "]";
	}
}
