package com.sodimac.rebates.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.ComentarioComparativoAprobacion;
import com.sodimac.rebates.repository.ComentarioComparativoAprobacionRepository;

@Service
public class ComentarioComparativoAprobacionService implements IComentarioComparativoAprobacionService {

	@Autowired
	private ComentarioComparativoAprobacionRepository comentarioComparativoAprobacionRepo;

	@Override
	public ComentarioComparativoAprobacion getById(Integer id) {

		Optional<ComentarioComparativoAprobacion> optional = comentarioComparativoAprobacionRepo.findById(id);

		if (optional.isPresent()) {

			return optional.get();
		}

		return null;
	}

	@Override
	public boolean save(ComentarioComparativoAprobacion comentarioComparativoAprobacion) {

		try {

			comentarioComparativoAprobacionRepo.save(comentarioComparativoAprobacion);

		} catch (Exception ex) {

			// TODO: añadir a bitacora
			ex.printStackTrace();
			System.out.println(ex.getMessage());

			return false;
		}

		return false;
	}

}
