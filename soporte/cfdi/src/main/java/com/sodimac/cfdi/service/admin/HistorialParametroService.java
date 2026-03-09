package com.sodimac.cfdi.service.admin;

import java.util.Date;

public interface HistorialParametroService {

	public enum TipoAccion {

		CREATE("c"), UPDATE("u");

		private String value;

		private TipoAccion(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public void registrarAccion(String usuario, String parametro, Date fecha, TipoAccion tipoAccion);

}
