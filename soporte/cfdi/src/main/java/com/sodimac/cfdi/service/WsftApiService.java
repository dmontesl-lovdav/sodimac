package com.sodimac.cfdi.service;

import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.TimbrarComplemento;

public interface WsftApiService {

	public String obtenerToken(String url, String usuario, String password);

	public ClientResponseTYPE<TimbrarComplemento> timbrarComplementoCFDI(Integer idPagoComplemento);

}
