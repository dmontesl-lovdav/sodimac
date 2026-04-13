package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.Catalogo;

public interface ICatalogosService {
	List<Catalogo> findAll();
	List<Catalogo> findByParams(Catalogo catalogo);
	boolean existeCatalogo(Integer id);
	boolean existeCatalogo(String nombre);
	void addCatalogo(Catalogo newCatalogo);
	void editCatalogo(Catalogo editCatalogo);
	void logicDelete(Integer id);
}
