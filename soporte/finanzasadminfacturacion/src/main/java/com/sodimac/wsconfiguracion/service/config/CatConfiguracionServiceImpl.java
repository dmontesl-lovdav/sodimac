package com.sodimac.wsconfiguracion.service.config;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.InformacionGlobal;
import com.sodimac.wsconfiguracion.dto.InformacionGlobalDto;
import com.sodimac.wsconfiguracion.repository.config.CatConfiguracionRepository;

@Service("catConfiguracionServiceImplConfig")
public class CatConfiguracionServiceImpl implements CatConfiguracionService {

	@Autowired
	@Qualifier("catConfiguracionRepositoryConfig")
	private CatConfiguracionRepository catConfiguracionRepository;
	
	@Override
	public String findParameterByKey(String NombreCampo) {
		return catConfiguracionRepository.findParameterByKey(NombreCampo);
	}
	
	@Override
	public ClientResponseTYPE<InformacionGlobal> obtieneParamsFG() {
		ClientResponseTYPE<InformacionGlobal> informacionGlobal = new ClientResponseTYPE<InformacionGlobal>(new InformacionGlobal());
		String year = String.valueOf((Calendar.getInstance().get(Calendar.YEAR)));
		String month = String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
		String periodicidad = findParameterByKey("PERIODICIDAD_FACTURACION_GLOBAL");
		
		informacionGlobal.getData().setAño(year);
		informacionGlobal.getData().setMes(month);
		informacionGlobal.getData().setPeriodicidad(periodicidad);

		return informacionGlobal;
	}

}
