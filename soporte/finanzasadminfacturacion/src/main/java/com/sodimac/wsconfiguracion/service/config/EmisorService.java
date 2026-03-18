package com.sodimac.wsconfiguracion.service.config;

import java.util.List;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ComprobanteDto;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorDto;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDto;
import com.sodimac.wsconfiguracion.entity.config.CatConfiguracionEntity;
import com.sodimac.wsconfiguracion.entity.config.PacEntity;
import com.sodimac.wsconfiguracion.models.config.EmisorReq;

public interface EmisorService {

	public ConfDatosEmisorDto obtenerEmisor (String rfc) throws Exception;
	public ConfDatosEmisorTiendaDto obtenerLugarExpedicion (Integer idTienda);
	public ClientResponseTYPE<ComprobanteDto> obtenerEmisorYLugarExpedicion (EmisorReq request) throws Exception;
	public List<CatConfiguracionEntity> obtieneConfiguraciones();
	public List<PacEntity> obtienePacs();
	
//	public ConfDatosEmisorDto obtenerEmisorReb (String rfc);
//	public ConfDatosEmisorTiendaDto obtenerLugarExpedicionReb (Integer idTienda);
//	public EmisorYLugarExpedicionDto obtenerEmisorYLugarExpedicionReb (String rfc, Integer idTienda);
}
