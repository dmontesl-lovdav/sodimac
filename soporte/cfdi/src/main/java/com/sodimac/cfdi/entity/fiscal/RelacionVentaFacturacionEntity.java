package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "relacionventafacturacion")
public class RelacionVentaFacturacionEntity {

	@Id
	@Column(name = "TICKET")
	private String ticket;
	@Column(name = "FECHA_TICKET")
	private String fechaTicket;
	@Column(name = "TIENDA")
	private String tienda;
	@Column(name = "CAJA")
	private String caja;
	@Column(name = "TRANSACCION")
	private String transaccion;
	@Column(name = "TIPO")
	private String tipo;
	@Column(name = "TOTAL")
	private String total;
	@Column(name = "SUBTOTAL")
	private String subtotal;
	@Column(name = "REDONDEO")
	private String redondeo;
	@Column(name = "TICKET_ORIGEN")
	private String ticketOrigen;
	@Column(name = "FECHA_ENLACE")
	private String fechaEnlace;
	@Column(name = "NUM_DOC_CANAL")
	private String numDocCanal;
	@Column(name = "CANAL_LINIO")
	private String canalLinio;
	@Column(name = "UUID_GLOBAL")
	private String uuidGlobal;
	@Column(name = "FECHA_TIMBRADO_GLOBAL")
	private String fechaTimbradoGlobal;
	@Column(name = "TOTAL_GLOBAL")
	private String totalGlobal;
	@Column(name = "SUBTOTAL_GLOBAL")
	private String subTotalGlobal;
	@Column(name = "REPETICION_GLOBAL")
	private String repeticionGlobal;
	@Column(name = "UUID_CLIENTE")
	private String UUID_CLIENTE;
	@Column(name = "FECHA_TIMBRADO_CLIENTE")
	private String fechaTimbradoCliente;
	@Column(name = "TOTAL_CLIENTE")
	private String totalCliente;
	@Column(name = "SUBTOTAL_CLIENTE")
	private String subtotalCliente;
	@Column(name = "REPETICION_CLIENTE")
	private String repeticionCliente;
	@Column(name = "UUID_FAC_NC_GLOBAL")
	private String uuidFacNcGlobal;
	@Column(name = "FECHA_TIMBRADO_FAC_NC_GLOBAL")
	private String fechaTimbradoFacNcGlobal;
	@Column(name = "TOTAL_FAC_NC_GLOBAL")
	private String totalFacNcGlobal;
	@Column(name = "SUBTOTAL_FAC_NC_GLOBAL")
	private String subtotalFacNcGlobal;
	@Column(name = "REPETICION_FAC_NC_GLOBAL")
	private String repeticionFacNcGlobal;
	@Column(name = "UUID_NC_GLOBAL")
	private String uuidNcGlobal;
	@Column(name = "FECHA_TIMBRADO_NC_GLOBAL")
	private String fechaTimbradoNcGlobal;
	@Column(name = "TOTAL_NC_GLOBAL")
	private String totalNcGlobal;
	@Column(name = "SUBTOTAL_NC_GLOBAL")
	private String subtotalNcGlobal;
	@Column(name = "REPETICION_NC_GLOBAL")
	private String repeticionNcGlobal;
	@Column(name = "UUID_NC_CLIENTE")
	private String uuidNcCliente;
	@Column(name = "FECHA_TIMBRADO_NC_CLIENTE")
	private String fechaTimbradoNcCliente;
	@Column(name = "TOTAL_NC_CLIENTE")
	private String totalNcCliente;
	@Column(name = "SUBTOTAL_NC_CLIENTE")
	private String subtotalNcCliente;
	@Column(name = "REPETICION_NC_CLIENTE")
	private String repeticionNcCliente;
	@Column(name = "FACTURA_CLIENTE_ID")
	private String facturaClienteId;
	@Column(name = "FACTURA_CLIENTE_NC_ID")
	private String facturaClienteNcId;
	@Column(name = "PAC")
	private String pac;
	@Column(name = "FACTURA_INHOUSE_ID")
	private String facturaInhouseId;
	@Column(name = "UUID_IN_HOUSE")
	private String uuidInHouse;
	@Column(name = "UUID_LINIO")
	private String uuidLinio;
	@Column(name = "UUID_RELACIONADO_GLOBAL")
	private String uuidRelacionadoGlobal;
	@Column(name = "UUID_RELACIONADO_CLIENTE")
	private String uuidRelacionadoCliente;
	@Column(name = "FACTURA_ID_RELACIONADA")
	private String facturaIdRelacionada;
	
	
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
	public String getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(String subtotal) {
		this.subtotal = subtotal;
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
	public String getSubtotalCliente() {
		return subtotalCliente;
	}
	public void setSubtotalCliente(String subtotalCliente) {
		this.subtotalCliente = subtotalCliente;
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
	public String getSubtotalFacNcGlobal() {
		return subtotalFacNcGlobal;
	}
	public void setSubtotalFacNcGlobal(String subtotalFacNcGlobal) {
		this.subtotalFacNcGlobal = subtotalFacNcGlobal;
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
	public String getSubtotalNcGlobal() {
		return subtotalNcGlobal;
	}
	public void setSubtotalNcGlobal(String subtotalNcGlobal) {
		this.subtotalNcGlobal = subtotalNcGlobal;
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
	public String getSubtotalNcCliente() {
		return subtotalNcCliente;
	}
	public void setSubtotalNcCliente(String subtotalNcCliente) {
		this.subtotalNcCliente = subtotalNcCliente;
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
		return "RelacionVentaFacturacionEntity [ticket=" + ticket + ", fechaTicket=" + fechaTicket + ", tienda="
				+ tienda + ", caja=" + caja + ", transaccion=" + transaccion + ", tipo=" + tipo + ", total=" + total
				+ ", subtotal=" + subtotal + ", redondeo=" + redondeo + ", ticketOrigen=" + ticketOrigen
				+ ", fechaEnlace=" + fechaEnlace + ", numDocCanal=" + numDocCanal + ", canalLinio=" + canalLinio
				+ ", uuidGlobal=" + uuidGlobal + ", fechaTimbradoGlobal=" + fechaTimbradoGlobal + ", totalGlobal="
				+ totalGlobal + ", subTotalGlobal=" + subTotalGlobal + ", repeticionGlobal=" + repeticionGlobal
				+ ", UUID_CLIENTE=" + UUID_CLIENTE + ", fechaTimbradoCliente=" + fechaTimbradoCliente
				+ ", totalCliente=" + totalCliente + ", subtotalCliente=" + subtotalCliente + ", repeticionCliente="
				+ repeticionCliente + ", uuidFacNcGlobal=" + uuidFacNcGlobal + ", fechaTimbradoFacNcGlobal="
				+ fechaTimbradoFacNcGlobal + ", totalFacNcGlobal=" + totalFacNcGlobal + ", subtotalFacNcGlobal="
				+ subtotalFacNcGlobal + ", repeticionFacNcGlobal=" + repeticionFacNcGlobal + ", uuidNcGlobal="
				+ uuidNcGlobal + ", fechaTimbradoNcGlobal=" + fechaTimbradoNcGlobal + ", totalNcGlobal=" + totalNcGlobal
				+ ", subtotalNcGlobal=" + subtotalNcGlobal + ", repeticionNcGlobal=" + repeticionNcGlobal
				+ ", uuidNcCliente=" + uuidNcCliente + ", fechaTimbradoNcCliente=" + fechaTimbradoNcCliente
				+ ", totalNcCliente=" + totalNcCliente + ", subtotalNcCliente=" + subtotalNcCliente
				+ ", repeticionNcCliente=" + repeticionNcCliente + ", facturaClienteId=" + facturaClienteId
				+ ", facturaClienteNcId=" + facturaClienteNcId + ", pac=" + pac + ", facturaInhouseId="
				+ facturaInhouseId + ", uuidInHouse=" + uuidInHouse + ", uuidLinio=" + uuidLinio
				+ ", uuidRelacionadoGlobal=" + uuidRelacionadoGlobal + ", uuidRelacionadoCliente="
				+ uuidRelacionadoCliente + ", facturaIdRelacionada=" + facturaIdRelacionada + "]";
	}
	
}
