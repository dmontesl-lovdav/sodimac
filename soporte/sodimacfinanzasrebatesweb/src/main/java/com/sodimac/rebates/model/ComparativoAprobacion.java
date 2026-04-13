package com.sodimac.rebates.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ComparativoAprobacion")
public class ComparativoAprobacion extends Generic{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idComparativoAprobacion;
	private String iDderegistro;
	private String sociedad;
	private String fechadocumento;
	private String fechacontabilizacion;
	private String tipodedocumento;
	private String referenciaFact;
	private String referenciaEjercicio;
	private String referenciaPosicion;
	// private String no.ContratoRE;
	private String periodo;
	private String referencia;
	private String textodecabecera;
	private String moneda;
	private String tipodecambio;
	private String fechadeconversion;
	private String clavecontabilizacion;
	private String cuenta;
	private String indicadorCME;
	private String clasedemovimiento;
	private float importe;
	private String importeimpuestos;
	private String calcularimpuestos;
	private String indicadorimpuestos;
	private String centrodebeneficios;
	private String centrodecoste;
	private String orden;
	private String elementoPEP;
	private String segmento;
	private String condiciondepago;
	private String fechabase;
	private String metododepago;
	private String bloqueodepago;
	private String articulo;
	private String cantidad;
	private String unidaddemedida;
	private String asignacion;
	private String texto;
	private String referencia1;
	private String referencia2;
	private String referencia3;
	private String fechavalor;
	private String tipodebancointerlocutor;
	private float tipoCambio;
	private Date fechaRegistro;
	private Date fechaActualizacion;
	private Integer usuarioAutorizacion;
	private String estatus;
	private Integer idperiodo;
	private Integer usuario;
	private boolean activo;
	private Integer tipodeRebate;

	public Integer getIdComparativoAprobacion() {
		return idComparativoAprobacion;
	}

	public void setIdComparativoAprobacion(Integer idComparativoAprobacion) {
		this.idComparativoAprobacion = idComparativoAprobacion;
	}

	public String getiDderegistro() {
		return iDderegistro;
	}

	public void setiDderegistro(String iDderegistro) {
		this.iDderegistro = iDderegistro;
	}

	public String getSociedad() {
		return sociedad;
	}

	public void setSociedad(String sociedad) {
		this.sociedad = sociedad;
	}

	public String getFechadocumento() {
		return fechadocumento;
	}

	public void setFechadocumento(String fechadocumento) {
		this.fechadocumento = fechadocumento;
	}

	public String getFechacontabilizacion() {
		return fechacontabilizacion;
	}

	public void setFechacontabilizacion(String fechacontabilizacion) {
		this.fechacontabilizacion = fechacontabilizacion;
	}

	public String getTipodedocumento() {
		return tipodedocumento;
	}

	public void setTipodedocumento(String tipodedocumento) {
		this.tipodedocumento = tipodedocumento;
	}

	public String getReferenciaFact() {
		return referenciaFact;
	}

	public void setReferenciaFact(String referenciaFact) {
		this.referenciaFact = referenciaFact;
	}

	public String getReferenciaEjercicio() {
		return referenciaEjercicio;
	}

	public void setReferenciaEjercicio(String referenciaEjercicio) {
		this.referenciaEjercicio = referenciaEjercicio;
	}

	public String getReferenciaPosicion() {
		return referenciaPosicion;
	}

	public void setReferenciaPosicion(String referenciaPosicion) {
		this.referenciaPosicion = referenciaPosicion;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getTextodecabecera() {
		return textodecabecera;
	}

	public void setTextodecabecera(String textodecabecera) {
		this.textodecabecera = textodecabecera;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getTipodecambio() {
		return tipodecambio;
	}

	public void setTipodecambio(String tipodecambio) {
		this.tipodecambio = tipodecambio;
	}

	public String getFechadeconversion() {
		return fechadeconversion;
	}

	public void setFechadeconversion(String fechadeconversion) {
		this.fechadeconversion = fechadeconversion;
	}

	public String getClavecontabilizacion() {
		return clavecontabilizacion;
	}

	public void setClavecontabilizacion(String clavecontabilizacion) {
		this.clavecontabilizacion = clavecontabilizacion;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getIndicadorCME() {
		return indicadorCME;
	}

	public void setIndicadorCME(String indicadorCME) {
		this.indicadorCME = indicadorCME;
	}

	public String getClasedemovimiento() {
		return clasedemovimiento;
	}

	public void setClasedemovimiento(String clasedemovimiento) {
		this.clasedemovimiento = clasedemovimiento;
	}

	public float getImporte() {
		return importe;
	}

	public void setImporte(float importe) {
		this.importe = importe;
	}

	public String getImporteimpuestos() {
		return importeimpuestos;
	}

	public void setImporteimpuestos(String importeimpuestos) {
		this.importeimpuestos = importeimpuestos;
	}

	public String getCalcularimpuestos() {
		return calcularimpuestos;
	}

	public void setCalcularimpuestos(String calcularimpuestos) {
		this.calcularimpuestos = calcularimpuestos;
	}

	public String getIndicadorimpuestos() {
		return indicadorimpuestos;
	}

	public void setIndicadorimpuestos(String indicadorimpuestos) {
		this.indicadorimpuestos = indicadorimpuestos;
	}

	public String getCentrodebeneficios() {
		return centrodebeneficios;
	}

	public void setCentrodebeneficios(String centrodebeneficios) {
		this.centrodebeneficios = centrodebeneficios;
	}

	public String getCentrodecoste() {
		return centrodecoste;
	}

	public void setCentrodecoste(String centrodecoste) {
		this.centrodecoste = centrodecoste;
	}

	public String getOrden() {
		return orden;
	}

	public void setOrden(String orden) {
		this.orden = orden;
	}

	public String getElementoPEP() {
		return elementoPEP;
	}

	public void setElementoPEP(String elementoPEP) {
		this.elementoPEP = elementoPEP;
	}

	public String getSegmento() {
		return segmento;
	}

	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}

	public String getCondiciondepago() {
		return condiciondepago;
	}

	public void setCondiciondepago(String condiciondepago) {
		this.condiciondepago = condiciondepago;
	}

	public String getFechabase() {
		return fechabase;
	}

	public void setFechabase(String fechabase) {
		this.fechabase = fechabase;
	}

	public String getMetododepago() {
		return metododepago;
	}

	public void setMetododepago(String metododepago) {
		this.metododepago = metododepago;
	}

	public String getBloqueodepago() {
		return bloqueodepago;
	}

	public void setBloqueodepago(String bloqueodepago) {
		this.bloqueodepago = bloqueodepago;
	}

	public String getArticulo() {
		return articulo;
	}

	public void setArticulo(String articulo) {
		this.articulo = articulo;
	}

	public String getCantidad() {
		return cantidad;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getUnidaddemedida() {
		return unidaddemedida;
	}

	public void setUnidaddemedida(String unidaddemedida) {
		this.unidaddemedida = unidaddemedida;
	}

	public String getAsignacion() {
		return asignacion;
	}

	public void setAsignacion(String asignacion) {
		this.asignacion = asignacion;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getReferencia1() {
		return referencia1;
	}

	public void setReferencia1(String referencia1) {
		this.referencia1 = referencia1;
	}

	public String getReferencia2() {
		return referencia2;
	}

	public void setReferencia2(String referencia2) {
		this.referencia2 = referencia2;
	}

	public String getReferencia3() {
		return referencia3;
	}

	public void setReferencia3(String referencia3) {
		this.referencia3 = referencia3;
	}

	public String getFechavalor() {
		return fechavalor;
	}

	public void setFechavalor(String fechavalor) {
		this.fechavalor = fechavalor;
	}

	public String getTipodebancointerlocutor() {
		return tipodebancointerlocutor;
	}

	public void setTipodebancointerlocutor(String tipodebancointerlocutor) {
		this.tipodebancointerlocutor = tipodebancointerlocutor;
	}

	public float getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(float tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public Integer getUsuarioAutorizacion() {
		return usuarioAutorizacion;
	}

	public void setUsuarioAutorizacion(Integer usuarioAutorizacion) {
		this.usuarioAutorizacion = usuarioAutorizacion;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public Integer getIdperiodo() {
		return idperiodo;
	}

	public void setIdperiodo(Integer idperiodo) {
		this.idperiodo = idperiodo;
	}

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Integer getTipodeRebate() {
		return tipodeRebate;
	}

	public void setTipodeRebate(Integer tipodeRebate) {
		this.tipodeRebate = tipodeRebate;
	}

	@Override
	public String toString() {
		return "ComparativoAprobacion [idComparativoAprobacion=" + idComparativoAprobacion + ", iDderegistro="
				+ iDderegistro + ", sociedad=" + sociedad + ", fechadocumento=" + fechadocumento
				+ ", fechacontabilizacion=" + fechacontabilizacion + ", tipodedocumento=" + tipodedocumento
				+ ", referenciaFact=" + referenciaFact + ", referenciaEjercicio=" + referenciaEjercicio
				+ ", referenciaPosicion=" + referenciaPosicion + ", periodo=" + periodo + ", referencia=" + referencia
				+ ", textodecabecera=" + textodecabecera + ", moneda=" + moneda + ", tipodecambio=" + tipodecambio
				+ ", fechadeconversion=" + fechadeconversion + ", clavecontabilizacion=" + clavecontabilizacion
				+ ", cuenta=" + cuenta + ", indicadorCME=" + indicadorCME + ", clasedemovimiento=" + clasedemovimiento
				+ ", importe=" + importe + ", importeimpuestos=" + importeimpuestos + ", calcularimpuestos="
				+ calcularimpuestos + ", indicadorimpuestos=" + indicadorimpuestos + ", centrodebeneficios="
				+ centrodebeneficios + ", centrodecoste=" + centrodecoste + ", orden=" + orden + ", elementoPEP="
				+ elementoPEP + ", segmento=" + segmento + ", condiciondepago=" + condiciondepago + ", fechabase="
				+ fechabase + ", metododepago=" + metododepago + ", bloqueodepago=" + bloqueodepago + ", articulo="
				+ articulo + ", cantidad=" + cantidad + ", unidaddemedida=" + unidaddemedida + ", asignacion="
				+ asignacion + ", texto=" + texto + ", referencia1=" + referencia1 + ", referencia2=" + referencia2
				+ ", referencia3=" + referencia3 + ", fechavalor=" + fechavalor + ", tipodebancointerlocutor="
				+ tipodebancointerlocutor + ", tipoCambio=" + tipoCambio + ", fechaRegistro=" + fechaRegistro
				+ ", fechaActualizacion=" + fechaActualizacion + ", usuarioAutorizacion=" + usuarioAutorizacion
				+ ", estatus=" + estatus + ", idperiodo=" + idperiodo + ", usuario=" + usuario + ", activo=" + activo
				+ ", tipodeRebate=" + tipodeRebate + "]";
	}

}
