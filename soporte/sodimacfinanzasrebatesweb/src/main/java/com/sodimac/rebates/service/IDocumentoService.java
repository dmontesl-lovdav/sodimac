package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.Documento;

public interface IDocumentoService {

	List<Documento> getAll();

	List<Documento> getActive();

	Documento getById(Integer idDocumento);
	
	public boolean aceptaTodosPeriodos(Documento documento);
}
