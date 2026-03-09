/**
 * 
 */
package com.sodimac.cfdi.model.users;

import java.util.List;

import com.sodimac.cfdi.dto.*;
import com.sodimac.cfdi.model.ModelGeneric;

/**
 * @author jfalvarez
 *
 */

public class VMUsersDevice extends ModelGeneric {

	public List<DtoUserDevice> usersDevice;
	public DtoActivity dtoActivity;
	public String filter;
	public DtoUserDevice dtoUserDevice;
	public Integer idProfile;

	public List<DtoUserDevice> getUsersDevice() {
		return usersDevice;
	}

	public void setUsersDevice(List<DtoUserDevice> usersDevice) {
		this.usersDevice = usersDevice;
	}

	public DtoActivity getDtoActivity() {
		return dtoActivity;
	}

	public void setDtoActivity(DtoActivity dtoActivity) {
		this.dtoActivity = dtoActivity;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public DtoUserDevice getDtoUserDevice() {
		return dtoUserDevice;
	}

	public void setDtoUserDevice(DtoUserDevice dtoUserDevice) {
		this.dtoUserDevice = dtoUserDevice;
	}

	public Integer getIdProfile() {
		return idProfile;
	}

	public void setIdProfile(Integer idProfile) {
		this.idProfile = idProfile;
	}

}
