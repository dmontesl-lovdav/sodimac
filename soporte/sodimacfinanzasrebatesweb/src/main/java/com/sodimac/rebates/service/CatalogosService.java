package com.sodimac.rebates.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.Catalogo;
import com.sodimac.rebates.repository.CatCatalogoRepository;

@Service
public class CatalogosService implements ICatalogosService {

	@Autowired
	CatCatalogoRepository catCatalogoRepository;

	@Override
	public List<Catalogo> findAll() {
		return catCatalogoRepository.findByLogicActive(true);

	}

	@Override
	public List<Catalogo> findByParams(Catalogo catalogo) {
		if (catalogo.getIdCatalogo() != null) {
			return existeCatalogo(catalogo.getIdCatalogo())
					? Arrays.asList(catCatalogoRepository.findByIdCatalogoAndLogicActive(catalogo.getIdCatalogo(), true).get())
					: Collections.emptyList();
		} else if (Strings.isNotBlank(catalogo.getNombre())) {
			return catCatalogoRepository.findByNombreContainingIgnoreCaseAndLogicActive(catalogo.getNombre(), true);
		} else if (Strings.isNotBlank(catalogo.getDescripcion())) {
			return catCatalogoRepository.findByDescripcionContainingIgnoreCaseAndLogicActive(catalogo.getDescripcion(),
					true);
		}

		return Collections.emptyList();
	}

	@Override
	public void addCatalogo(Catalogo newCatalogo) {
		newCatalogo.setFechaCreacion(new Date());
		newCatalogo.setLogicActive(true);
		catCatalogoRepository.save(newCatalogo);
	}

	@Override
	public boolean existeCatalogo(String nombre) {
		return catCatalogoRepository.findByNombreAndLogicActive(nombre, true).isPresent();
	}

	@Override
	public boolean existeCatalogo(Integer id) {
		return catCatalogoRepository.findByIdCatalogoAndLogicActive(id, true).isPresent();
	}

	@Override
	public void editCatalogo(Catalogo editCatalogo) {
		Catalogo catalogo = catCatalogoRepository.findByIdCatalogoAndLogicActive(editCatalogo.getIdCatalogo(), true).get();
		catalogo.setNombre(editCatalogo.getNombre());
		catalogo.setDescripcion(editCatalogo.getDescripcion());
		catalogo.setActivo(editCatalogo.isActivo());
		catalogo.setFechaActualizacion(new Date());
		catalogo.setUsuarioActualizacion(editCatalogo.getUsuarioActualizacion());
		catCatalogoRepository.save(catalogo);
	}

	@Override
	public void logicDelete(Integer id) {
		Catalogo catalogo = catCatalogoRepository.findByIdCatalogoAndLogicActive(id, true).get();
		catalogo.setLogicActive(false);
		catCatalogoRepository.save(catalogo);
	}
}
