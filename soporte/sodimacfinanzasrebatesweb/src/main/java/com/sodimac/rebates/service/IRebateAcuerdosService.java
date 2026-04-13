package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.model.RebateAcuerdos;

public interface IRebateAcuerdosService {
	
	List<RebateAcuerdos> findAcuerdosConsultParams(String proveedor, String razonSocial, String tipoAcuerdo, String programaPago);
	List<RebateAcuerdos> findAll();
	List<String> findTiposAcuerdos();
}
