package com.sodimac.cfdi.cliente.wsadministracion;

public class ConfDatosEmisorTiendaDtoVM {

	private Integer id;
	private Integer idConfDatosEmisor;
	private String emisor;
	private Integer idTienda;
	private String descripcion;
	private String calle;
	private String noExterior;
	private String noInterior;
	private String colonia;
	private String localidad;
	private String referencia;
	private String municipio;
	private String estado;
	private Integer idCatCodigoPostal;
	private Integer idCatTipoTienda;
	private String tipoTienda;
	private Boolean activo;
	private String fechaInicio;
	
	public ConfDatosEmisorTiendaDtoVM() {}
	
	public ConfDatosEmisorTiendaDtoVM(ConfDatosEmisorTiendaDtoVM t) {
		this.id = t.id; this.idConfDatosEmisor = t.idConfDatosEmisor;  this.emisor = t.emisor; this.idTienda = t.idTienda; this.descripcion = t.descripcion; this.calle = calle;
		this.noExterior = t.noExterior; this.noInterior = t.noInterior; this.colonia = t.colonia; this.localidad = t.localidad;
		this.referencia = t.referencia; this.municipio = t.municipio; this.estado = t.estado; this.idCatCodigoPostal = t.idCatCodigoPostal;
		this.idCatTipoTienda = t.idCatTipoTienda;  this.tipoTienda = t.tipoTienda; this.activo = t.activo;
	    this.fechaInicio = t.fechaInicio;
	}
	
	public ConfDatosEmisorTiendaDtoVM(Integer id, Integer idConfDatosEmisor,  String emisor, Integer idTienda, String descripcion, String calle
			, String noExterior, String noInterior, String colonia, String localidad
			, String referencia, String municipio, String estado, Integer idCatCodigoPostal
			, Integer idCatTipoTienda, String tipoTienda, Boolean activo
			, String fechaInicio
				) {
				this.id = id; this.idConfDatosEmisor = idConfDatosEmisor;  this.emisor = emisor; this.idTienda = idTienda; this.descripcion = descripcion; this.calle = calle;
				this.noExterior = noExterior; this.noInterior = noInterior; this.colonia = colonia; this.localidad = localidad;
				this.referencia = referencia; this.municipio = municipio; this.estado = estado; this.idCatCodigoPostal = idCatCodigoPostal;
				this.idCatTipoTienda = idCatTipoTienda;  this.tipoTienda = tipoTienda; this.activo = activo;
			    this.fechaInicio = fechaInicio;
				}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdConfDatosEmisor() {
		return idConfDatosEmisor;
	}

	public void setIdConfDatosEmisor(Integer idConfDatosEmisor) {
		this.idConfDatosEmisor = idConfDatosEmisor;
	}

	public String getEmisor() {
		return emisor;
	}

	public void setEmisor(String emisor) {
		this.emisor = emisor;
	}

	public Integer getIdTienda() {
		return idTienda;
	}

	public void setIdTienda(Integer idTienda) {
		this.idTienda = idTienda;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getNoExterior() {
		return noExterior;
	}

	public void setNoExterior(String noExterior) {
		this.noExterior = noExterior;
	}

	public String getNoInterior() {
		return noInterior;
	}

	public void setNoInterior(String noInterior) {
		this.noInterior = noInterior;
	}

	public String getColonia() {
		return colonia;
	}

	public void setColonia(String colonia) {
		this.colonia = colonia;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getIdCatCodigoPostal() {
		return idCatCodigoPostal;
	}

	public void setIdCatCodigoPostal(Integer idCatCodigoPostal) {
		this.idCatCodigoPostal = idCatCodigoPostal;
	}

	public Integer getIdCatTipoTienda() {
		return idCatTipoTienda;
	}

	public void setIdCatTipoTienda(Integer idCatTipoTienda) {
		this.idCatTipoTienda = idCatTipoTienda;
	}

	public String getTipoTienda() {
		return tipoTienda;
	}

	public void setTipoTienda(String tipoTienda) {
		this.tipoTienda = tipoTienda;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	@Override
	public String toString() {
		return "ConfDatosEmisorTiendaDtoVM [id=" + id + ", idConfDatosEmisor=" + idConfDatosEmisor + ", emisor="
				+ emisor + ", idTienda=" + idTienda + ", descripcion=" + descripcion + ", calle=" + calle
				+ ", noExterior=" + noExterior + ", noInterior=" + noInterior + ", colonia=" + colonia + ", localidad="
				+ localidad + ", referencia=" + referencia + ", municipio=" + municipio + ", estado=" + estado
				+ ", idCatCodigoPostal=" + idCatCodigoPostal + ", idCatTipoTienda=" + idCatTipoTienda + ", tipoTienda="
				+ tipoTienda + ", activo=" + activo + ", fechaInicio=" + fechaInicio + "]";
	}
	
	
	
}
