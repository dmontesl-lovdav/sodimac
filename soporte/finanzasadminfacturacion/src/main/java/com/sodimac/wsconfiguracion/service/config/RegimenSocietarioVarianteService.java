package com.sodimac.wsconfiguracion.service.config;

import java.util.List;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.RegimenDeCapitalDto;

public interface RegimenSocietarioVarianteService {

	ClientResponseTYPE<RegimenDeCapitalDto> validaRazonSocial(String razonSocial);

}
