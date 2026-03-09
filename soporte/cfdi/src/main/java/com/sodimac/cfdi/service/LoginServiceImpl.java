package com.sodimac.cfdi.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.entity.fiscal.CatOpcionesEntity;
import com.sodimac.cfdi.entity.fiscal.UsuariosEntity;
import com.sodimac.cfdi.repository.fiscal.UsuariosRepository;
import com.sodimac.cfdi.util.UtilsFechas;

@Service
@EnableTransactionManagement
public class LoginServiceImpl implements LoginService {

	@Autowired
	private UsuariosRepository usuariosRepository;
	@Autowired
	private SeguridadService seguridadService;

	public UsuariosEntity validarLogin(String usuario, String password) {

		password = seguridadService.encriptar(password);
		return usuariosRepository.findByUsuarioAndPassword(usuario, password);
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<CatOpcionesEntity> obtenerOpcionesRol(int idRol) {
		List<CatOpcionesEntity> list = new ArrayList<CatOpcionesEntity>();
		usuariosRepository.obtenerOpcionesRol(idRol).forEach(item -> {
			CatOpcionesEntity itemList = new CatOpcionesEntity();
			itemList.setIdOpcion(Integer.parseInt(item[0].toString()));
			itemList.setCodOpcion(item[1].toString().trim());
			itemList.setNomOpcion(item[2].toString().trim());
			itemList.setLink(item[3].toString().trim());
			itemList.setIdPadre(Integer.parseInt(item[4].toString()));
			itemList.setActivo(Boolean.parseBoolean(item[5].toString()));
			try {
				itemList.setFechaCreacion(UtilsFechas.convertirDate(item[6].toString(), "yyyy-MM-dd HH:mm:ss"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			list.add(itemList);
		});
		return list;
	}
		
}
