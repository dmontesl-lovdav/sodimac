package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.AdminCatalogo;
import com.sodimac.rebates.model.CatalogoId;

public interface ICatalogoService {

	List<AdminCatalogo> getAll();

	AdminCatalogo getCatalogo(CatalogoId catalogoId);

	List<AdminCatalogo> getActive();

	List<AdminCatalogo> getCatalogoTipoMensaje();

	List<AdminCatalogo> getCatalogoCompletado();

	List<AdminCatalogo> getCatalogoEstatusContabilidad();

	List<AdminCatalogo> getCatalogoEstatusDocumento();

}
