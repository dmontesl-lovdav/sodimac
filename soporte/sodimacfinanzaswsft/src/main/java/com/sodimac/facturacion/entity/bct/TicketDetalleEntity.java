package com.sodimac.facturacion.entity.bct;

public class TicketDetalleEntity {

	private String ticket;
    private Integer id;
    private Integer sku;
    private String dvSku;
	private float cantidad;
	private String um;
	private String descripcion;
	private float precioTotal;
	private float precioUnitario;
	private Long numeroDocumento;
	private String portable;

    public TicketDetalleEntity() {

	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSku() {
		return sku;
	}

	public void setSku(Integer sku) {
		this.sku = sku;
	}

	public String getDvSku() {
		return dvSku;
	}

	public void setDvSku(String dvSku) {
		this.dvSku = dvSku;
	}

	public float getCantidad() {
		return cantidad;
	}

	public void setCantidad(float cantidad) {
		this.cantidad = cantidad;
	}

	public String getUm() {
		return um;
	}

	public void setUm(String um) {
		this.um = um;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public float getPrecioTotal() {
		return precioTotal;
	}

	public void setPrecioTotal(float precioTotal) {
		this.precioTotal = precioTotal;
	}

	public float getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(float precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public Long getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(Long numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getPortable() {
		return portable;
	}

	public void setPortable(String portable) {
		this.portable = portable;
	}

	@Override
	public String toString() {
		return "TicketDetalleEntity [ticket=" + ticket + ", id=" + id + ", sku=" + sku + ", dvSku=" + dvSku
				+ ", cantidad=" + cantidad + ", um=" + um + ", descripcion=" + descripcion + ", precioTotal="
				+ precioTotal + ", precioUnitario=" + precioUnitario + ", numeroDocumento=" + numeroDocumento
				+ ", portable=" + portable + "]";
	}
	
}
