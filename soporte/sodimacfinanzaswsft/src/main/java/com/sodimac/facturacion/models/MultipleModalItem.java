package com.sodimac.facturacion.models;

public class MultipleModalItem {

	private String ticket;
	private String status;
	private String msg;
	
	public MultipleModalItem() {
		super();
	}
	public MultipleModalItem(String ticket, String status, String msg) {
		super();
		this.ticket = ticket;
		this.status = status;
		this.msg = msg;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
