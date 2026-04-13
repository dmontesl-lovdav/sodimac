package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.repository.ProgramaPagoRepository;

@Service
public class ProgramaPagoService implements IProgramaPagoService {

	@Autowired
	private ProgramaPagoRepository programaPagoRepo = null;

	@Override
	public List<ProgramaPago> getAll() {

		return programaPagoRepo.findAll();
	}

	@Override
	public List<ProgramaPago> getActive() {

		return programaPagoRepo.findByActivo(true);
	}

}
