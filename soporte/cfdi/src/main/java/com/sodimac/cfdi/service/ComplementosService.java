package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.entity.fiscal.ComplementosEntity;
import com.sodimac.cfdi.entity.fiscal.PagoComplementoEntity;
import com.sodimac.cfdi.entity.fiscal.PagosEntity;
import com.sodimac.cfdi.models.ComplementosModel;

public interface ComplementosService {
	
	public List<PagoComplementoEntity> getAllComplementos();

	
	public List<ComplementosModel> getComplementosByParams(String fechaInicial, String fechaFinal, String rfcEncriptado, int start, int rowsPerPage,String estatusComplemento, Double pmonto);

	
	public String altaComplemento(PagosEntity pago);
	
	
	public boolean getComplementosExcelFechas(String fechaInicial, String fechaFinal, String rfcEncriptado, String nombreArchivo, String estatusComplemento, Double pmonto);

	
	public String timbrarComplementoPago(Integer idPagoComplemento);
	
	
	public ComplementosEntity getComplemento(String idComplemento);


	
}
