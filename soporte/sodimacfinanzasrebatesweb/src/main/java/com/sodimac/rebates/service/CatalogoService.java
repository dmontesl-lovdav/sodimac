package com.sodimac.rebates.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sodimac.rebates.repository.CatalogoRepository;
import com.sodimac.rebates.model.AdminCatalogo;
import com.sodimac.rebates.model.Catalogo;
import com.sodimac.rebates.model.CatalogoId;

@Service
public class CatalogoService implements ICatalogoService {

	@Autowired
	private CatalogoRepository catalogoRepo = null;

	private Integer idCatalogo;
	private Catalogo catalogo = new Catalogo();
	private CatalogoId catalogoId = new CatalogoId();

	@Override
	public List<AdminCatalogo> getAll() {

		return catalogoRepo.findAll();
	}

	@Override
	public AdminCatalogo getCatalogo(CatalogoId catalogoId) {

		Optional<AdminCatalogo> optional = catalogoRepo.findById(catalogoId);

		if (optional.isPresent()) {

			return optional.get();
		}

		return null;
	}

	@Override
	public List<AdminCatalogo> getActive() {

		return catalogoRepo.findByActivo(true);
	}

	@Override
	public List<AdminCatalogo> getCatalogoTipoMensaje() {

		idCatalogo = 1;
		catalogo.setIdCatalogo(idCatalogo);
		catalogoId.setCatalogo(catalogo);

		return catalogoRepo.findByCatalogoSpecific(catalogoId);
	}

	@Override
	public List<AdminCatalogo> getCatalogoCompletado() {

		idCatalogo = 2;
		catalogo.setIdCatalogo(idCatalogo);
		catalogoId.setCatalogo(catalogo);

		return catalogoRepo.findByCatalogoSpecific(catalogoId);
	}

	@Override
	public List<AdminCatalogo> getCatalogoEstatusContabilidad() {

		idCatalogo = 3;
		catalogo.setIdCatalogo(idCatalogo);
		catalogoId.setCatalogo(catalogo);

		return catalogoRepo.findByCatalogoSpecific(catalogoId);
	}

	@Override
	public List<AdminCatalogo> getCatalogoEstatusDocumento() {

		idCatalogo = 4;
		catalogo.setIdCatalogo(idCatalogo);
		catalogoId.setCatalogo(catalogo);

		return catalogoRepo.findByCatalogoSpecific(catalogoId);
	}

}
