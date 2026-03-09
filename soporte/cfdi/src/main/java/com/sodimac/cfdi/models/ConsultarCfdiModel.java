package com.sodimac.cfdi.models;

public class ConsultarCfdiModel extends CodigoError {

	private String dateDesde;
	private String dateHasta;
	private String rfc;
	private String uuid;
	private String ticket;
	private String pageNumber;
	private int start;
	private int rowsPerPage;

	public ConsultarCfdiModel() {
		
		dateDesde = "";
		dateHasta = "";
		rfc = "";
		uuid = "";
		ticket = "";
		pageNumber = "0";
		start = 0;
		rowsPerPage = 0;
	}

	public String getDateDesde() {
		return dateDesde;
	}

	public void setDateDesde(String dateDesde) {
		this.dateDesde = dateDesde;
	}

	public String getDateHasta() {
		return dateHasta;
	}

	public void setDateHasta(String dateHasta) {
		this.dateHasta = dateHasta;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(String pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	@Override
	public String toString() {
		return "ConsultarCfdiModel [dateDesde=" + dateDesde + ", dateHasta=" + dateHasta + ", rfc=" + rfc + ", uuid="
				+ uuid + ", ticket=" + ticket + ", pageNumber=" + pageNumber + ", start=" + start + ", rowsPerPage="
				+ rowsPerPage + "]";
	}

}
