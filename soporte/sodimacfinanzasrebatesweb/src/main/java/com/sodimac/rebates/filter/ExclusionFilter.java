package com.sodimac.rebates.filter;

public class ExclusionFilter {

	private int start;
	private int rowsPerPage;
	private Integer idPeriodo;
	private Integer idTipoExclusion;
	private String folio;
	private String comentario;
	private String numProveedor;
	private String ordenCompra;
	private Integer idUsuario;

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

	public Integer getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(Integer idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public Integer getIdTipoExclusion() {
		return idTipoExclusion;
	}

	public void setIdTipoExclusion(Integer idTipoExclusion) {
		this.idTipoExclusion = idTipoExclusion;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getNumProveedor() {
		return numProveedor;
	}

	public void setNumProveedor(String numProveedor) {
		this.numProveedor = numProveedor;
	}

	public String getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(String ordenCompra) {
		this.ordenCompra = ordenCompra;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

}
