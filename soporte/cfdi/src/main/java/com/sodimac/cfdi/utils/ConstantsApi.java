/**
 * 
 */
package com.sodimac.cfdi.utils;

/**
 * @author jfalvarez
 *
 */
public final class ConstantsApi {

	private ConstantsApi() {
		// restrict instantiation
	}

	// private final String urlBase = connection.getUrl();

	/** Url services device */
	public static final String URL_GET_CATALOG_BRANCH_OFFICES = "/totem/Catalog/Sucursales";
	public static final String URL_GET_CATALOG_TYPE_DEVICES = "/totem/Catalog/TipoDispositivo";
	public static final String URL_GET_CATALOG_ZONES = "/totem/Catalog/Zonas";
	public static final String URL_POST_CREATE_DEVICE = "/totem/Catalog/saveDispositivo";
	public static final String URL_POST_CONSULT_DEVICE = "/totem/Catalog/AllDispositivos";
	public static final String URL_POST_CHANGE_STATUS_DEVICE = "/totem/Catalog/disabledDispositivo";
	public static final String URL_PUT_EDIT_DEVICE = "/totem/Catalog/updateDispositivo/";

	/** Url services users device */
	public static final String URL_POST_CONSULT_USERS_DEVICE = "/totem/api/getUsersPDA";
	public static final String URL_PUT_EDIT_USERS_DEVICE = "/totem/api/updateUser";
	public static final String URL_POST_CREATE_USERS_DEVICE = "/totem/api/saveUserPDA";
	public static final String URL_POST_UPDATE_USERS_DEVICE = "/totem/api/activateUser";
	public static final String URL_POST_RESET_PASSWORD = "/totem/api/resetPasswordPDA";

	/** Url services users web */
	public static final String URL_GET_CATALOG_TYPE_PROFILES = "/totem/Catalog/Perfiles";
	public static final String URL_POST_CONSULT_USERS_WEB = "/totem/api/getUsersWeb";
	public static final String URL_PUT_EDIT_USERS_WEB = "/totem/api/updateUserWeb";
	public static final String URL_POST_CREATE_USERS_WEB = "/totem/api/saveUserWeb/";

	/** Url services report */
	public static final String URL_POTS_GENERAL_REPORT = "/totem/api/getReporteDocumento";
	public static final String URL_POTS_DETAIL_REPORT = "/totem/api/getReporteDetDocumento";
	public static final String URL_POTS_INCIDENT_REPORT = "/totem/api/getReporteIncidencias";

	/** Url services login */
	public static final String URL_POST_DO_LOGIN = "/totem/api/doLoginWeb";
	public static final String URL_POST_UPDATE_PASSTEMPORAL = "/totem/api/changePasswordWeb";
	public static final String URL_POST_RECOVER_PASSWORD = "/totem/api/resetPasswordWeb";
	public static final String URL_GET_TIMEOUT_WEB = "/totem/Catalog/Configuracion/timeOut";
	public static final String SET_CHARACTERS_RANDOM = "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx%$#&/()0123456789";

}