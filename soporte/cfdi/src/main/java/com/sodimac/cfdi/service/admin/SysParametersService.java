package com.sodimac.cfdi.service.admin;

import java.io.OutputStream;
import java.util.List;

import com.sodimac.cfdi.dto.DtoUser;
import com.sodimac.cfdi.model.admin.PrivilegiosUsuario;
import com.sodimac.cfdi.model.admin.SysParameter;

public interface SysParametersService {

	public List<SysParameter> findParameters(String nombre, Integer idusuario, Integer idrol);
	public void guardarParametro(SysParameter parametro, DtoUser user) throws Exception;
	public List<Object[]> getAllRoles();
	public List<Object[]> getAllAplicaciones();
	public OutputStream getExcel(String busqueda, int idusuario, int idrol);
	public PrivilegiosUsuario getPrivilegiosUsuario(int idrol);
	
}
