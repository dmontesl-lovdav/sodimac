package com.sodimac.cfdi.models;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.googlecode.jmapper.annotations.JGlobalMap;
import com.googlecode.jmapper.annotations.JMapConversion;

@JGlobalMap
public class RelacionVentaFacturacionModel {
	private String ticket;
	private String fechaTicket;
	private String tienda;
	private String caja;
	private String transaccion;
	private String tipo;
	private String total;
	private String subTotal;
	private String redondeo;
	private String ticketOrigen;
	private String fechaEnlace;
	private String numDocCanal;
	private String canalLinio;
	private String uuidGlobal;
	private String fechaTimbradoGlobal;
	private String totalGlobal;
	private String subTotalGlobal;
	private String repeticionGlobal;
	private String UUID_CLIENTE;
	private String fechaTimbradoCliente;
	private String totalCliente;
	private String subTotalCliente;
	private String repeticionCliente;
	private String uuidFacNcGlobal;
	private String fechaTimbradoFacNcGlobal;
	private String totalFacNcGlobal;
	private String subTotalFacNcGlobal;
	private String repeticionFacNcGlobal;
	private String uuidNcGlobal;
	private String fechaTimbradoNcGlobal;
	private String totalNcGlobal;
	private String subTotalNcGlobal;
	private String repeticionNcGlobal;
	private String uuidNcCliente;
	private String fechaTimbradoNcCliente;
	private String totalNcCliente;
	private String subTotalNcCliente;
	private String repeticionNcCliente;
	private String facturaClienteId;
	private String facturaClienteNcId;
	private String pac;
	private String facturaInhouseId;
	private String uuidInHouse;
	private String uuidLinio;
	private String uuidRelacionadoGlobal;
	private String uuidRelacionadoCliente;
	private String facturaIdRelacionada;
	
	@JMapConversion(from={"totalGlobal"}, to={"totalGlobal"})
    public String conversionTotalGlobal(BigDecimal totalGlobal){
        return totalGlobal.toString();
    }
	
	@JMapConversion(from={"subTotalGlobal"}, to={"subTotalGlobal"})
    public String conversionSubTotalGlobal(BigDecimal subTotalGlobal){
        return subTotalGlobal.toString();
    }
	
	@JMapConversion(from={"repeticionGlobal"}, to={"repeticionGlobal"})
    public String conversionRepeticionGlobal(BigInteger repeticionGlobal){
        return repeticionGlobal.toString();
    }
	
	@JMapConversion(from={"totalCliente"}, to={"totalCliente"})
    public String conversionTotalCliente(BigDecimal totalCliente){
        return totalCliente.toString();
    }
	
	@JMapConversion(from={"subTotalCliente"}, to={"subTotalCliente"})
    public String conversionSubTotalCliente(BigDecimal subTotalCliente){
        return subTotalCliente.toString();
    }
	
	@JMapConversion(from={"repeticionCliente"}, to={"repeticionCliente"})
    public String conversionRepeticionCliente(BigInteger repeticionCliente){
        return repeticionCliente.toString();
    }
	
	@JMapConversion(from={"totalFacNcGlobal"}, to={"totalFacNcGlobal"})
    public String conversionTotalFacNcGlobal(BigDecimal totalFacNcGlobal){
        return totalFacNcGlobal.toString();
    }
	
	@JMapConversion(from={"subTotalFacNcGlobal"}, to={"subTotalFacNcGlobal"})
    public String conversionSubTotalFacNcGlobal(BigDecimal subTotalFacNcGlobal){
        return subTotalFacNcGlobal.toString();
    }
	
	@JMapConversion(from={"repeticionFacNcGlobal"}, to={"repeticionFacNcGlobal"})
    public String conversionRepeticionFacNcGlobal(BigInteger repeticionFacNcGlobal){
        return repeticionFacNcGlobal.toString();
    }
	
	@JMapConversion(from={"totalNcGlobal"}, to={"totalNcGlobal"})
    public String conversionTotalNcGlobal(BigDecimal totalNcGlobal){
        return totalNcGlobal.toString();
    }
	
	@JMapConversion(from={"subTotalNcGlobal"}, to={"subTotalNcGlobal"})
    public String conversionSubTotalNcGlobal(BigDecimal subTotalNcGlobal){
        return subTotalNcGlobal.toString();
    }
	
	@JMapConversion(from={"repeticionNcGlobal"}, to={"repeticionNcGlobal"})
    public String conversionRepeticionNcGlobal(BigInteger repeticionNcGlobal){
        return repeticionNcGlobal.toString();
    }
	
	@JMapConversion(from={"totalNcCliente"}, to={"totalNcCliente"})
    public String conversionTotalNcCliente(BigDecimal totalNcCliente){
        return totalNcCliente.toString();
    }
	
	@JMapConversion(from={"subTotalNcCliente"}, to={"subTotalNcCliente"})
    public String conversionSubTotalNcCliente(BigDecimal subTotalNcCliente){
        return subTotalNcCliente.toString();
    }
	
	@JMapConversion(from={"repeticionNcCliente"}, to={"repeticionNcCliente"})
    public String conversionRepeticionNcCliente(BigInteger repeticionNcCliente){
        return repeticionNcCliente.toString();
    }
	
	@JMapConversion(from={"facturaClienteId"}, to={"facturaClienteId"})
    public String conversionFacturaClienteId(BigInteger facturaClienteId){
        return facturaClienteId.toString();
    }
	
	@JMapConversion(from={"facturaClienteNcId"}, to={"facturaClienteNcId"})
    public String conversionFacturaClienteNcId(BigInteger facturaClienteNcId){
        return facturaClienteNcId.toString();
    }
	
	@JMapConversion(from={"facturaInhouseId"}, to={"facturaInhouseId"})
    public String conversionFacturaInhouseId(BigInteger facturaInhouseId){
        return facturaInhouseId.toString();
    }
	
	@JMapConversion(from={"facturaIdRelacionada"}, to={"facturaIdRelacionada"})
    public String conversionFacturaIdRelacionada(BigInteger facturaIdRelacionada){
        return facturaIdRelacionada.toString();
    }
	
	@JMapConversion(from={"pac"}, to={"pac"})
    public String conversionPac(BigInteger pac){
        return pac.toString();
    }
	
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public String getFechaTicket() {
		return fechaTicket;
	}
	public void setFechaTicket(String fechaTicket) {
		this.fechaTicket = fechaTicket;
	}
	public String getTienda() {
		return tienda;
	}
	public void setTienda(String tienda) {
		this.tienda = tienda;
	}
	public String getCaja() {
		return caja;
	}
	public void setCaja(String caja) {
		this.caja = caja;
	}
	public String getTransaccion() {
		return transaccion;
	}
	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}
	public String getRedondeo() {
		return redondeo;
	}
	public void setRedondeo(String redondeo) {
		this.redondeo = redondeo;
	}
	public String getTicketOrigen() {
		return ticketOrigen;
	}
	public void setTicketOrigen(String ticketOrigen) {
		this.ticketOrigen = ticketOrigen;
	}
	public String getFechaEnlace() {
		return fechaEnlace;
	}
	public void setFechaEnlace(String fechaEnlace) {
		this.fechaEnlace = fechaEnlace;
	}
	public String getNumDocCanal() {
		return numDocCanal;
	}
	public void setNumDocCanal(String numDocCanal) {
		this.numDocCanal = numDocCanal;
	}
	public String getCanalLinio() {
		return canalLinio;
	}
	public void setCanalLinio(String canalLinio) {
		this.canalLinio = canalLinio;
	}
	public String getUuidGlobal() {
		return uuidGlobal;
	}
	public void setUuidGlobal(String uuidGlobal) {
		this.uuidGlobal = uuidGlobal;
	}
	public String getFechaTimbradoGlobal() {
		return fechaTimbradoGlobal;
	}
	public void setFechaTimbradoGlobal(String fechaTimbradoGlobal) {
		this.fechaTimbradoGlobal = fechaTimbradoGlobal;
	}
	public String getTotalGlobal() {
		return totalGlobal;
	}
	public void setTotalGlobal(String totalGlobal) {
		this.totalGlobal = totalGlobal;
	}
	public String getSubTotalGlobal() {
		return subTotalGlobal;
	}
	public void setSubTotalGlobal(String subTotalGlobal) {
		this.subTotalGlobal = subTotalGlobal;
	}
	public String getRepeticionGlobal() {
		return repeticionGlobal;
	}
	public void setRepeticionGlobal(String repeticionGlobal) {
		this.repeticionGlobal = repeticionGlobal;
	}
	public String getUUID_CLIENTE() {
		return UUID_CLIENTE;
	}
	public void setUUID_CLIENTE(String uUID_CLIENTE) {
		UUID_CLIENTE = uUID_CLIENTE;
	}
	public String getFechaTimbradoCliente() {
		return fechaTimbradoCliente;
	}
	public void setFechaTimbradoCliente(String fechaTimbradoCliente) {
		this.fechaTimbradoCliente = fechaTimbradoCliente;
	}
	public String getTotalCliente() {
		return totalCliente;
	}
	public void setTotalCliente(String totalCliente) {
		this.totalCliente = totalCliente;
	}
	public String getSubTotalCliente() {
		return subTotalCliente;
	}
	public void setSubTotalCliente(String subTotalCliente) {
		this.subTotalCliente = subTotalCliente;
	}
	public String getRepeticionCliente() {
		return repeticionCliente;
	}
	public void setRepeticionCliente(String repeticionCliente) {
		this.repeticionCliente = repeticionCliente;
	}
	public String getUuidFacNcGlobal() {
		return uuidFacNcGlobal;
	}
	public void setUuidFacNcGlobal(String uuidFacNcGlobal) {
		this.uuidFacNcGlobal = uuidFacNcGlobal;
	}
	public String getFechaTimbradoFacNcGlobal() {
		return fechaTimbradoFacNcGlobal;
	}
	public void setFechaTimbradoFacNcGlobal(String fechaTimbradoFacNcGlobal) {
		this.fechaTimbradoFacNcGlobal = fechaTimbradoFacNcGlobal;
	}
	public String getTotalFacNcGlobal() {
		return totalFacNcGlobal;
	}
	public void setTotalFacNcGlobal(String totalFacNcGlobal) {
		this.totalFacNcGlobal = totalFacNcGlobal;
	}
	public String getSubTotalFacNcGlobal() {
		return subTotalFacNcGlobal;
	}
	public void setSubTotalFacNcGlobal(String subTotalFacNcGlobal) {
		this.subTotalFacNcGlobal = subTotalFacNcGlobal;
	}
	public String getRepeticionFacNcGlobal() {
		return repeticionFacNcGlobal;
	}
	public void setRepeticionFacNcGlobal(String repeticionFacNcGlobal) {
		this.repeticionFacNcGlobal = repeticionFacNcGlobal;
	}
	public String getUuidNcGlobal() {
		return uuidNcGlobal;
	}
	public void setUuidNcGlobal(String uuidNcGlobal) {
		this.uuidNcGlobal = uuidNcGlobal;
	}
	public String getFechaTimbradoNcGlobal() {
		return fechaTimbradoNcGlobal;
	}
	public void setFechaTimbradoNcGlobal(String fechaTimbradoNcGlobal) {
		this.fechaTimbradoNcGlobal = fechaTimbradoNcGlobal;
	}
	public String getTotalNcGlobal() {
		return totalNcGlobal;
	}
	public void setTotalNcGlobal(String totalNcGlobal) {
		this.totalNcGlobal = totalNcGlobal;
	}
	public String getSubTotalNcGlobal() {
		return subTotalNcGlobal;
	}
	public void setSubTotalNcGlobal(String subTotalNcGlobal) {
		this.subTotalNcGlobal = subTotalNcGlobal;
	}
	public String getRepeticionNcGlobal() {
		return repeticionNcGlobal;
	}
	public void setRepeticionNcGlobal(String repeticionNcGlobal) {
		this.repeticionNcGlobal = repeticionNcGlobal;
	}
	public String getUuidNcCliente() {
		return uuidNcCliente;
	}
	public void setUuidNcCliente(String uuidNcCliente) {
		this.uuidNcCliente = uuidNcCliente;
	}
	public String getFechaTimbradoNcCliente() {
		return fechaTimbradoNcCliente;
	}
	public void setFechaTimbradoNcCliente(String fechaTimbradoNcCliente) {
		this.fechaTimbradoNcCliente = fechaTimbradoNcCliente;
	}
	public String getTotalNcCliente() {
		return totalNcCliente;
	}
	public void setTotalNcCliente(String totalNcCliente) {
		this.totalNcCliente = totalNcCliente;
	}
	public String getSubTotalNcCliente() {
		return subTotalNcCliente;
	}
	public void setSubTotalNcCliente(String subTotalNcCliente) {
		this.subTotalNcCliente = subTotalNcCliente;
	}
	public String getRepeticionNcCliente() {
		return repeticionNcCliente;
	}
	public void setRepeticionNcCliente(String repeticionNcCliente) {
		this.repeticionNcCliente = repeticionNcCliente;
	}
	public String getFacturaClienteId() {
		return facturaClienteId;
	}
	public void setFacturaClienteId(String facturaClienteId) {
		this.facturaClienteId = facturaClienteId;
	}
	public String getFacturaClienteNcId() {
		return facturaClienteNcId;
	}
	public void setFacturaClienteNcId(String facturaClienteNcId) {
		this.facturaClienteNcId = facturaClienteNcId;
	}
	public String getPac() {
		return pac;
	}
	public void setPac(String pac) {
		this.pac = pac;
	}
	public String getFacturaInhouseId() {
		return facturaInhouseId;
	}
	public void setFacturaInhouseId(String facturaInhouseId) {
		this.facturaInhouseId = facturaInhouseId;
	}
	public String getUuidInHouse() {
		return uuidInHouse;
	}
	public void setUuidInHouse(String uuidInHouse) {
		this.uuidInHouse = uuidInHouse;
	}
	public String getUuidLinio() {
		return uuidLinio;
	}
	public void setUuidLinio(String uuidLinio) {
		this.uuidLinio = uuidLinio;
	}
	public String getUuidRelacionadoGlobal() {
		return uuidRelacionadoGlobal;
	}
	public void setUuidRelacionadoGlobal(String uuidRelacionadoGlobal) {
		this.uuidRelacionadoGlobal = uuidRelacionadoGlobal;
	}
	public String getUuidRelacionadoCliente() {
		return uuidRelacionadoCliente;
	}
	public void setUuidRelacionadoCliente(String uuidRelacionadoCliente) {
		this.uuidRelacionadoCliente = uuidRelacionadoCliente;
	}
	public String getFacturaIdRelacionada() {
		return facturaIdRelacionada;
	}
	public void setFacturaIdRelacionada(String facturaIdRelacionada) {
		this.facturaIdRelacionada = facturaIdRelacionada;
	}
	@Override
	public String toString() {
		return "RelacionVentaFacturacionModel [ticket=" + ticket + ", fechaTicket=" + fechaTicket + ", tienda=" + tienda
				+ ", caja=" + caja + ", transaccion=" + transaccion + ", tipo=" + tipo + ", total=" + total
				+ ", subTotal=" + subTotal + ", redondeo=" + redondeo + ", ticketOrigen=" + ticketOrigen
				+ ", fechaEnlace=" + fechaEnlace + ", numDocCanal=" + numDocCanal + ", canalLinio=" + canalLinio
				+ ", uuidGlobal=" + uuidGlobal + ", fechaTimbradoGlobal=" + fechaTimbradoGlobal + ", totalGlobal="
				+ totalGlobal + ", subTotalGlobal=" + subTotalGlobal + ", repeticionGlobal=" + repeticionGlobal
				+ ", UUID_CLIENTE=" + UUID_CLIENTE + ", fechaTimbradoCliente=" + fechaTimbradoCliente
				+ ", totalCliente=" + totalCliente + ", subTotalCliente=" + subTotalCliente + ", repeticionCliente="
				+ repeticionCliente + ", uuidFacNcGlobal=" + uuidFacNcGlobal + ", fechaTimbradoFacNcGlobal="
				+ fechaTimbradoFacNcGlobal + ", totalFacNcGlobal=" + totalFacNcGlobal + ", subTotalFacNcGlobal="
				+ subTotalFacNcGlobal + ", repeticionFacNcGlobal=" + repeticionFacNcGlobal + ", uuidNcGlobal="
				+ uuidNcGlobal + ", fechaTimbradoNcGlobal=" + fechaTimbradoNcGlobal + ", totalNcGlobal=" + totalNcGlobal
				+ ", subTotalNcGlobal=" + subTotalNcGlobal + ", repeticionNcGlobal=" + repeticionNcGlobal
				+ ", uuidNcCliente=" + uuidNcCliente + ", fechaTimbradoNcCliente=" + fechaTimbradoNcCliente
				+ ", totalNcCliente=" + totalNcCliente + ", subTotalNcCliente=" + subTotalNcCliente
				+ ", repeticionNcCliente=" + repeticionNcCliente + ", facturaClienteId=" + facturaClienteId
				+ ", facturaClienteNcId=" + facturaClienteNcId + ", pac=" + pac + ", facturaInhouseId="
				+ facturaInhouseId + ", uuidInHouse=" + uuidInHouse + ", uuidLinio=" + uuidLinio
				+ ", uuidRelacionadoGlobal=" + uuidRelacionadoGlobal + ", uuidRelacionadoCliente="
				+ uuidRelacionadoCliente + ", facturaIdRelacionada=" + facturaIdRelacionada + "]";
	}
}
