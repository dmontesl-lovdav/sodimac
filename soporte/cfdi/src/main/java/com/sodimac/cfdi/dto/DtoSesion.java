/**
 * 
 */
package com.sodimac.cfdi.dto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sodimac.cfdi.component.ActividadesComponent;
import com.sodimac.cfdi.component.ErrorComponent;
import com.sodimac.cfdi.utils.RandomString;

@Component
public class DtoSesion {

	private Integer idUser;
	private String email;
	private String password;
	private String macAddress;
	private String explorer;
	private String ip;
	private String operatingSystem;
	private RandomString token;
	private String rol;
	private Integer idRol;
	private String idSesion;
	private List<DtoMenu> menus;
	public String strMenu;
	
	@Autowired
	private ActividadesComponent actividadesComponent;
	@Autowired
	private ErrorComponent errorComponent;


	public DtoSesion(Integer idUser, String email, String password, String macAddress, String explorer, String ip,
			String operatingSystem, RandomString token, String rol, Integer idRol, String idSesion, List<DtoMenu> menus,
			String strMenu) {
		super();
		this.idUser = idUser;
		this.email = email;
		this.password = password;
		this.macAddress = macAddress;
		this.explorer = explorer;
		this.ip = ip;
		this.operatingSystem = operatingSystem;
		this.token = token;
		this.rol = rol;
		this.idRol = idRol;
		this.idSesion = idSesion;
		this.menus = menus;
		this.strMenu = strMenu;
	}

	public DtoSesion() {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getExplorer() {
		return explorer;
	}

	public void setExplorer(String explorer) {
		this.explorer = explorer;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public RandomString getToken() {
		return token;
	}

	public void setToken(RandomString token) {
		this.token = token;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public Integer getIdRol() {
		return idRol;
	}

	public void setIdRol(Integer idRol) {
		this.idRol = idRol;
	}

	public String getIdSesion() {
		return idSesion;
	}

	public void setIdSesion(String idSesion) {
		this.idSesion = idSesion;
	}

	public List<DtoMenu> getMenus() {
		return menus;
	}

	public void setMenus(List<DtoMenu> menus) {
		this.menus = menus;
	}

	public String getStrMenu() {
		return strMenu;
	}

	public void setStrMenu(String strMenu) {
		this.strMenu = strMenu;
	}

	public ActividadesComponent getActividadesComponent() {
		return actividadesComponent;
	}

	public void setActividadesComponent(ActividadesComponent actividadesComponent) {
		this.actividadesComponent = actividadesComponent;
	}

	public ErrorComponent getErrorComponent() {
		return errorComponent;
	}

	public void setErrorComponent(ErrorComponent errorComponent) {
		this.errorComponent = errorComponent;
	}

}