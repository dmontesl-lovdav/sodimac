package com.sodimac.rebates.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.repository.DocumentoRepository;

@Service
public class DocumentoService implements IDocumentoService {

	@Autowired
	private DocumentoRepository documentoRepo = null;

	@Override
	public List<Documento> getAll() {

		return documentoRepo.findAll();
	}

	@Override
	public List<Documento> getActive() {

		return documentoRepo.findByActivo(true);
	}

	@Override
	public Documento getById(Integer idDocumento) {

		Optional<Documento> optional = documentoRepo.findById(idDocumento);

		if (optional.isPresent()) {

			return optional.get();
		}

		return null;
	}

	@Override
	public boolean aceptaTodosPeriodos(Documento documento) {
		boolean aceptaTodos = false;
		if (documento.getPeriodoComun() != null) {
			return (documento.getPeriodoComun().intValue() > 0);
		}
		return aceptaTodos;
	}

}
