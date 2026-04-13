package com.sodimac.rebates.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.ComparativoAprobacion;
import com.sodimac.rebates.repository.ComparativoAprobacionRepository;

@Service
public class ComparativoAprobacionService implements IComparativoAprobacionService {

	@Autowired
	private ComparativoAprobacionRepository comparativoAprobacionRepo;

	@Override
	public ComparativoAprobacion getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(String iDderegistro,
			Integer idperiodo, Integer tipodeRebate, String cuenta) {

		ComparativoAprobacion comparativoAprobacion = comparativoAprobacionRepo
				.findByiDderegistroAndIdperiodoAndTipodeRebateAndCuenta(iDderegistro, idperiodo, tipodeRebate, cuenta);

		if (comparativoAprobacion != null) {

			return comparativoAprobacion;
		}

		return null;
	}

	@Override
	public ComparativoAprobacion getById(Integer idComparativoAprobacion) {

		Optional<ComparativoAprobacion> optional = comparativoAprobacionRepo.findById(idComparativoAprobacion);

		if (optional.isPresent()) {

			return optional.get();
		}

		return null;
	}

	@Override
	public boolean save(ComparativoAprobacion comparativoAprobacion) {

		try {

			comparativoAprobacionRepo.save(comparativoAprobacion);

		} catch (Exception ex) {

			// TODO: añadir a bitacora
			ex.printStackTrace();
			System.out.println(ex.getMessage());

			return false;
		}

		return true;
	}

}
