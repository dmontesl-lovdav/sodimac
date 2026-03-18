package com.sodimac.wsconfiguracion.service;

//import com.sodimac.wsconfiguracion.models.ClientesTemporalModel;

public interface LoggerService {

	public void setLogErroresProperties(
			  String longitud
			, String latitud
			, String explorador
			, String sistemaOper
			, String ip
			, String ticket
			, String rfc
			);
	public void write (String error, String objeto, String params);
//	public void guardarLog (Object obj, ClientesTemporalModel model, String longitud, 
//			String latitud, String explorador, String sistemaOper, String ip
//			,String ticket, String rfc,String pagina, String xml, int idFacturaPac, String sessionId, String parametrosLlamado);
//	
	public void guardarLog (Object obj, String longitud, 
			String latitud, String explorador, String sistemaOper, String ip
			,String ticket, String rfc,String pagina, String xml, int idFacturaPac, String sessionId, String parametrosLlamado);
}
