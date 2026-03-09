/**
 * 
 */
package com.sodimac.cfdi.model.login;

import com.sodimac.cfdi.dto.DtoActivity;
import com.sodimac.cfdi.dto.DtoConfiguration;
import com.sodimac.cfdi.dto.DtoUser;
import com.sodimac.cfdi.model.ModelGeneric;

/**
 * @author jfalvarez
 *
 */
public class VMLogin extends ModelGeneric {

	private DtoUser user;
	private DtoActivity dtoActivity;
	private DtoConfiguration dtoConfiguration;
	private String password;
	private Integer idUser;
	private String oldPassword;
	private Integer idRol;
	private String email;
	private boolean flag;
	private String strMenu;

	public DtoUser getUser() {
		return user;
	}

	public void setUser(DtoUser user) {
		this.user = user;
	}

	public DtoActivity getDtoActivity() {
		return dtoActivity;
	}

	public void setDtoActivity(DtoActivity dtoActivity) {
		this.dtoActivity = dtoActivity;
	}

	public DtoConfiguration getDtoConfiguration() {
		return dtoConfiguration;
	}

	public void setDtoConfiguration(DtoConfiguration dtoConfiguration) {
		this.dtoConfiguration = dtoConfiguration;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public Integer getIdRol() {
		return idRol;
	}

	public void setIdRol(Integer idRol) {
		this.idRol = idRol;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getStrMenu() {
		return strMenu;
	}

	public void setStrMenu(String strMenu) {
		this.strMenu = strMenu;
	}

}
