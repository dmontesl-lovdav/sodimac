package com.sodimac.cfdi.service.admin;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.admin.HistorialParametroEntity;
import com.sodimac.cfdi.repository.admin.HistorialParametroRepository;

@Service
@Transactional
public class HistorialParametroServiceImpl implements HistorialParametroService{

	@Autowired
	private HistorialParametroRepository historialParametroRepository;
	
	@Override
	public void registrarAccion(String usuario, String parametro, Date fecha, TipoAccion tipoAccion) {
		HistorialParametroEntity e = new HistorialParametroEntity();
		e.setUsuario(usuario);
		e.setParametro(parametro);
		e.setFecha(fecha);
		e.setAccion(tipoAccion.getValue());		
		historialParametroRepository.save(e);
		
	}

}
