/**
 * 
 */
package com.sodimac.cfdi.dto;

/**
 * @author jfalvarez
 *
 */

public class DtoActivity {

	private Integer idActivity;
	private String macAddress;
	private String explorer;
	private String ip;
	private Integer idUser;
	private String page;
	private String operatingSystem;
	private String latitude;
	private String longitude;

	public Integer getIdActivity() {
		return idActivity;
	}

	public void setIdActivity(Integer idActivity) {
		this.idActivity = idActivity;
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

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
