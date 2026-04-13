package com.sodimac.rebates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.RebateAcuerdos;
import com.sodimac.rebates.repository.RebateAcuerdosRepository;

@Service
public class RebateAcuerdosService implements IRebateAcuerdosService {

	@Autowired
	private RebateAcuerdosRepository repository;
	
	@Override
	public List<RebateAcuerdos> findAcuerdosConsultParams(String proveedor, String razonSocial, String tipoAcuerdo, String programaPago) {
		return repository.findAcuerdosByParamsConsult(proveedor.trim().equals("") ? null : proveedor, razonSocial.trim().equals("") ? null : razonSocial, tipoAcuerdo != null && tipoAcuerdo.equals("null") ? null : tipoAcuerdo, programaPago != null && programaPago.equals("null") ? null : programaPago);
	}

	@Override
	public List<RebateAcuerdos> findAll() {
		// TODO Auto-generated method stub
		return repository.findAll();
	}

	@Override
	public List<String> findTiposAcuerdos() {
		return repository.findTiposAcuerdos();
	}
	
}
