package com.sodimac.rebates.model;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sodimac.rebates.dto.CatPerfilDto;
import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.filter.ExclusionFilter;
import com.sodimac.rebates.filter.PolizaContableFilter;
import com.sodimac.rebates.filter.RebateUsuarioFilter;
import com.sodimac.rebates.filter.ReporteFinancieroFilter;
import com.sodimac.rebates.filter.UsuarioFillRateFilter;

@Component
public class Sesion implements Principal {

	private Integer idUser;
	private String email;
	private String nombre;
	private String password;
	private List<String> perfiles;
	private List<String> permisos;
	private List<CatEventoDto> eventos;
	private List<CatPerfilDto> perfilesDto;
	private List<CatRolDto> roles;
	private String pathSftp;
	private ReporteOrdenCompra reporteOrdenCompra;
	private ReporteOrdenCompraFill reporteOrdenCompraFill;
	private CalculoRebateMSI calculoRebateMSI;
	private ExclusionFilter exclusionFilter;
	private ReporteFinancieroFilter reporteFinancieroFilter;
	private PolizaContableFilter polizaContableFilter;
	private RebateUsuarioFilter rebateUsuarioFilter;
	private UsuarioFillRateFilter usuarioFillRateFilter;
	
	public Sesion() {

	}

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getPerfiles() {
		return perfiles;
	}

	public void setPerfiles(List<String> perfiles) {
		this.perfiles = perfiles;
	}

	public String getPathSftp() {
		return pathSftp;
	}

	public void setPathSftp(String pathSftp) {
		this.pathSftp = pathSftp;
	}

	public ReporteOrdenCompra getReporteOrdenCompra() {
		return reporteOrdenCompra;
	}

	public void setReporteOrdenCompra(ReporteOrdenCompra reporteOrdenCompra) {
		this.reporteOrdenCompra = reporteOrdenCompra;
	}

	public ReporteOrdenCompraFill getReporteOrdenCompraFill() {
		return reporteOrdenCompraFill;
	}

	public void setReporteOrdenCompraFill(ReporteOrdenCompraFill reporteOrdenCompraFill) {
		this.reporteOrdenCompraFill = reporteOrdenCompraFill;
	}

	public CalculoRebateMSI getCalculoRebateMSI() {
		return calculoRebateMSI;
	}

	public void setCalculoRebateMSI(CalculoRebateMSI calculoRebateMSI) {
		this.calculoRebateMSI = calculoRebateMSI;
	}

	public List<String> getPermisos() {
		return permisos;
	}

	public void setPermisos(List<String> permisos) {
		this.permisos = permisos;
	}

	public List<CatEventoDto> getEventos() {
		return eventos;
	}

	public void setEventos(List<CatEventoDto> eventos) {
		this.eventos = eventos;
	}
	
	public List<CatPerfilDto> getPerfilesDto() {
		return perfilesDto;
	}

	public void setPerfilesDto(List<CatPerfilDto> perfilesDto) {
		this.perfilesDto = perfilesDto;
	}

	public List<CatRolDto> getRoles() {
		return roles;
	}

	public void setRoles(List<CatRolDto> roles) {
		this.roles = roles;
	}

	public ExclusionFilter getExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(ExclusionFilter exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}
	
	public ReporteFinancieroFilter getReporteFinancieroFilter() {
		return reporteFinancieroFilter;
	}

	public void setReporteFinancieroFilter(ReporteFinancieroFilter reporteFinancieroFilter) {
		this.reporteFinancieroFilter = reporteFinancieroFilter;
	}

	public PolizaContableFilter getPolizaContableFilter() {
		return polizaContableFilter;
	}

	public void setPolizaContableFilter(PolizaContableFilter polizaContableFilter) {
		this.polizaContableFilter = polizaContableFilter;
	}

	public RebateUsuarioFilter getRebateUsuarioFilter() {
		return rebateUsuarioFilter;
	}

	public void setRebateUsuarioFilter(RebateUsuarioFilter rebateUsuarioFilter) {
		this.rebateUsuarioFilter = rebateUsuarioFilter;
	}

	public UsuarioFillRateFilter getUsuarioFillRateFilter() {
		return usuarioFillRateFilter;
	}

	public void setUsuarioFillRateFilter(UsuarioFillRateFilter usuarioFillRateFilter) {
		this.usuarioFillRateFilter = usuarioFillRateFilter;
	}

	@Override
	public String toString() {
		return "Sesion [idUser=" + idUser + ", email=" + email + ", nombre=" + nombre + ", password=" + password
				+ ", perfiles=" + perfiles + ", pathSftp=" + pathSftp + "]";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
